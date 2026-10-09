package com.example.tonghuashun.data.repository

import com.example.tonghuashun.data.local.LocalStore
import com.example.tonghuashun.data.local.NoOpLocalStore
import com.example.tonghuashun.data.model.Order
import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.OrderStatus
import com.example.tonghuashun.data.model.TradeRecord
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/** 一笔持仓的原始记账（现价由真实行情实时填充） */
data class Holding(
    val fullCode: String,
    val name: String,
    val shares: Long,
    val available: Long,      // 可用（可卖）数量。当日买入 T+1，次日才可卖，这里简化为买入即冻结当日
    val costPrice: Double,
)

/**
 * 模拟交易账户（同花顺模拟盘思路）：本地记账，成交价使用真实行情价。
 * 支持委托挂单、撮合成交、撤单、成交记录。初始资金 100 万。
 *
 * 撮合规则（模拟）：
 * - 买入委托价 >= 现价，或卖出委托价 <= 现价 -> 立即全部成交。
 * - 否则挂单等待，由 [tryMatch] 在行情刷新时撮合。
 * - 冻结资金/冻结持仓，撤单后释放。
 * - **成交价一律取用户填报的委托价**（不用市价），这样"填多少就按多少成交"。
 *
 * 持久化：资金、冻结资金、持仓、委托、成交以及自增 ID 序号会在每次变更后
 * 写入 [LocalStore]（App 运行时为 SharedPreferences），下次启动自动恢复，
 * 因此退出 App 后再打开，账户数据不会重置。
 */
@Singleton
class TradeRepository @Inject constructor(
    private val store: LocalStore,
) {

    /** 单元测试 / 无 Context 场景：纯内存运行，不落盘。 */
    constructor() : this(NoOpLocalStore())

    private val initialCash = 1_000_000.0

    private val gson = Gson()

    private val _cash = MutableStateFlow(initialCash)
    val cash: StateFlow<Double> = _cash.asStateFlow()

    /** 买入委托冻结的资金 */
    private val _frozenCash = MutableStateFlow(0.0)
    val frozenCash: StateFlow<Double> = _frozenCash.asStateFlow()

    private val _holdings = MutableStateFlow<List<Holding>>(
        listOf(
            Holding("sh600519", "贵州茅台", 100, 100, 1600.00),
            Holding("sz000858", "五粮液", 500, 500, 150.00),
        )
    )
    val holdings: StateFlow<List<Holding>> = _holdings.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _trades = MutableStateFlow<List<TradeRecord>>(emptyList())
    val trades: StateFlow<List<TradeRecord>> = _trades.asStateFlow()

    private val idGen = AtomicLong(1000)
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.CHINA)

    init {
        restore()
    }

    /* ------------------------- 持久化 ------------------------- */

    /** 账户快照：需要跨进程存活的最小数据集。字段都给默认值，方便旧数据平滑升级。 */
    private data class TradeSnapshot(
        val cash: Double = 0.0,
        val frozenCash: Double = 0.0,
        val holdings: List<Holding>? = null,
        val orders: List<Order>? = null,
        val trades: List<TradeRecord>? = null,
        val idSeq: Long = 1000L,
    )

    /** 从本地快照恢复；首次启动（无快照）时按默认持仓播种，并立即写盘。 */
    private fun restore() {
        val snapshot = store.read(KEY_ACCOUNT)
            ?.let { raw -> runCatching { gson.fromJson(raw, TradeSnapshot::class.java) }.getOrNull() }
        if (snapshot != null) {
            _cash.value = snapshot.cash
            _frozenCash.value = snapshot.frozenCash
            _holdings.value = snapshot.holdings ?: emptyList()
            _orders.value = snapshot.orders ?: emptyList()
            _trades.value = snapshot.trades ?: emptyList()
            idGen.set(snapshot.idSeq)
            return
        }
        // 首次启动：扣除初始持仓成本，得到初始可用资金
        val cost = _holdings.value.sumOf { it.shares * it.costPrice }
        _cash.value = initialCash - cost
        persist()
    }

    /** 把当前账户快照写入本地存储。 */
    private fun persist() {
        val snapshot = TradeSnapshot(
            cash = _cash.value,
            frozenCash = _frozenCash.value,
            holdings = _holdings.value,
            orders = _orders.value,
            trades = _trades.value,
            idSeq = idGen.get(),
        )
        store.write(KEY_ACCOUNT, gson.toJson(snapshot))
    }

    private fun nowTime(): String = timeFmt.format(Date())
    private fun nextId(prefix: String): String = "$prefix${idGen.incrementAndGet()}"

    /**
     * 自定义总资产：清空持仓/委托/成交，将全部资金设为 [capital]（作为可用现金）。
     * 用于"自定义总资产"功能，从一个干净的空仓账户重新开始。
     */
    fun resetAccount(capital: Double) {
        val c = capital.coerceAtLeast(0.0)
        _cash.value = c
        _frozenCash.value = 0.0
        _holdings.value = emptyList()
        _orders.value = emptyList()
        _trades.value = emptyList()
        persist()
    }

    /**
     * 仅设置总资产但保留当前持仓：调整可用现金 = 目标总资产 - 当前持仓成本市值。
     * 若目标小于持仓成本则可用现金为 0。
     */
    /**
     * 自定义总资产（保留持仓，调整可用资金）。
     *
     * 关键：总资产 = 可用资金 + 持仓**市值**，所以要用市值去扣减，
     * 否则"设为 100 万"的结果会随浮盈浮亏偏离 100 万。
     * [positionValue] 传 0（行情还没到）时退回按持仓成本估算。
     */
    fun setTotalCapitalKeepingHoldings(totalCapital: Double, positionValue: Double = 0.0) {
        val base = if (positionValue > 0) positionValue
        else _holdings.value.sumOf { it.shares * it.costPrice }
        _cash.value = (totalCapital - base).coerceAtLeast(0.0)
        _frozenCash.value = 0.0
        persist()
    }

    /** 某只股票的可卖数量 */
    fun availableShares(fullCode: String): Long =
        _holdings.value.firstOrNull { it.fullCode == fullCode }?.available ?: 0L

    /**
     * 提交委托单。
     * @param limitPrice 委托价（限价）
     * @param marketPrice 当前行情价，仅用于判断该委托能否立即成交
     */
    fun placeOrder(
        fullCode: String,
        code: String,
        name: String,
        side: OrderSide,
        limitPrice: Double,
        shares: Long,
        marketPrice: Double,
    ): Result<Order> {
        if (limitPrice <= 0 || shares <= 0) {
            return Result.failure(IllegalArgumentException("价格或数量无效"))
        }
        if (shares % 100 != 0L) {
            return Result.failure(IllegalArgumentException("数量必须为 100 股整数倍"))
        }

        when (side) {
            OrderSide.BUY -> {
                val needCash = limitPrice * shares
                if (needCash > _cash.value) {
                    return Result.failure(IllegalStateException("可用资金不足"))
                }
                // 冻结资金
                _cash.value -= needCash
                _frozenCash.value += needCash
            }
            OrderSide.SELL -> {
                val avail = availableShares(fullCode)
                if (shares > avail) {
                    return Result.failure(IllegalStateException("可用持仓不足（可卖 $avail 股）"))
                }
                // 冻结持仓（减少 available）
                freezeHoldingForSell(fullCode, shares)
            }
        }

        val ts = System.currentTimeMillis()
        var order = Order(
            id = nextId("W"),
            fullCode = fullCode,
            code = code,
            name = name,
            side = side,
            price = limitPrice,
            shares = shares,
            filledShares = 0,
            status = OrderStatus.PENDING,
            time = nowTime(),
            timestamp = ts,
        )
        _orders.value = listOf(order) + _orders.value

        // 立即尝试撮合
        order = matchOrder(order, marketPrice)
        persist()
        return Result.success(order)
    }

    /**
     * 撮合单个委托。若可成交则**按用户的委托价 [Order.price] 成交**，更新持仓/资金/成交记录。
     * @return 更新后的 order
     */
    private fun matchOrder(order: Order, marketPrice: Double): Order {
        if (marketPrice <= 0) return order
        if (!order.canCancel) return order

        val canFill = when (order.side) {
            OrderSide.BUY -> order.price >= marketPrice   // 委托价不低于市价 -> 能买到
            OrderSide.SELL -> order.price <= marketPrice  // 委托价不高于市价 -> 能卖出
        }
        if (!canFill) return order

        val fillShares = order.remainShares
        if (fillShares <= 0) return order
        val fillPrice = order.price  // 按用户填写的价格成交，不使用市价

        when (order.side) {
            OrderSide.BUY -> {
                // 委托时已按委托价冻结资金；成交价与委托价一致，直接解冻即可
                _frozenCash.value -= order.price * fillShares
                addHoldingOnBuy(order.fullCode, order.name, fillShares, fillPrice)
            }
            OrderSide.SELL -> {
                // 卖出成交：增加现金，扣减持仓（持仓的冻结部分已在下单时减 available）
                _cash.value += fillPrice * fillShares
                reduceHoldingOnSell(order.fullCode, fillShares)
            }
        }

        val filled = order.filledShares + fillShares
        val newStatus = if (filled >= order.shares) OrderStatus.FILLED else OrderStatus.PARTIAL
        val updated = order.copy(filledShares = filled, status = newStatus)
        replaceOrder(updated)

        // 记录成交
        val record = TradeRecord(
            id = nextId("T"),
            orderId = order.id,
            fullCode = order.fullCode,
            code = order.code,
            name = order.name,
            side = order.side,
            price = fillPrice,
            shares = fillShares,
            time = nowTime(),
            timestamp = System.currentTimeMillis(),
        )
        _trades.value = listOf(record) + _trades.value
        return updated
    }

    /** 行情刷新时，撮合所有挂单（未成交/部成的委托） */
    fun tryMatch(priceMap: Map<String, Double>) {
        val pending = _orders.value.filter { it.canCancel }
        val tradesBefore = _trades.value.size
        pending.forEach { order ->
            val price = priceMap[order.fullCode] ?: return@forEach
            matchOrder(order, price)
        }
        // 只有真的产生成交才值得写盘，避免行情轮询带来的无谓 IO
        if (_trades.value.size != tradesBefore) persist()
    }

    /**
     * 立即成交：点了就成交，不挂单、不等待撮合。
     *
     * 与 [placeOrder] 的区别：不做"委托价 vs 市价"的撮合判断，
     * 直接以用户输入的价格成交，因此价格完全自由（不受涨跌停限制）。
     */
    fun executeImmediately(
        fullCode: String,
        code: String,
        name: String,
        side: OrderSide,
        price: Double,
        shares: Long,
    ): Result<Order> {
        if (price <= 0 || shares <= 0) return Result.failure(IllegalArgumentException("价格或数量无效"))
        if (shares % 100 != 0L) return Result.failure(IllegalArgumentException("数量必须为 100 股整数倍"))

        when (side) {
            OrderSide.BUY -> {
                val needCash = price * shares
                if (needCash > _cash.value + 0.001) {
                    return Result.failure(IllegalStateException("可用资金不足"))
                }
                _cash.value -= needCash
                addHoldingOnBuy(fullCode, name, shares, price)
            }
            OrderSide.SELL -> {
                val avail = availableShares(fullCode)
                if (shares > avail) {
                    return Result.failure(IllegalStateException("可用持仓不足（可卖 $avail 股）"))
                }
                freezeHoldingForSell(fullCode, shares)
                reduceHoldingOnSell(fullCode, shares)
                _cash.value += price * shares
            }
        }

        val ts = System.currentTimeMillis()
        val order = Order(
            id = nextId("C"),
            fullCode = fullCode,
            code = code,
            name = name,
            side = side,
            price = price,
            shares = shares,
            filledShares = shares,
            status = OrderStatus.FILLED,
            time = nowTime(),
            timestamp = ts,
        )
        _orders.value = listOf(order) + _orders.value
        _trades.value = listOf(
            TradeRecord(
                id = nextId("T"),
                orderId = order.id,
                fullCode = fullCode,
                code = code,
                name = name,
                side = side,
                price = price,
                shares = shares,
                time = order.time,
                timestamp = ts,
            ),
        ) + _trades.value
        persist()
        return Result.success(order)
    }

    /** 撤单：释放冻结资金/持仓 */
    fun cancelOrder(orderId: String): Result<Unit> {
        val order = _orders.value.firstOrNull { it.id == orderId }
            ?: return Result.failure(IllegalStateException("委托不存在"))
        if (!order.canCancel) return Result.failure(IllegalStateException("该委托无法撤销"))

        val remain = order.remainShares
        when (order.side) {
            OrderSide.BUY -> {
                // 释放冻结资金
                val frozen = order.price * remain
                _frozenCash.value -= frozen
                _cash.value += frozen
            }
            OrderSide.SELL -> {
                // 释放冻结持仓（恢复 available）
                unfreezeHoldingForSell(order.fullCode, remain)
            }
        }
        replaceOrder(order.copy(status = OrderStatus.CANCELLED))
        persist()
        return Result.success(Unit)
    }

    private fun replaceOrder(order: Order) {
        _orders.value = _orders.value.map { if (it.id == order.id) order else it }
    }

    /* ------------------------- 持仓变更 ------------------------- */

    private fun addHoldingOnBuy(fullCode: String, name: String, shares: Long, price: Double) {
        val list = _holdings.value.toMutableList()
        val idx = list.indexOfFirst { it.fullCode == fullCode }
        if (idx >= 0) {
            val h = list[idx]
            val newShares = h.shares + shares
            val newCost = (h.costPrice * h.shares + price * shares) / newShares
            // 当日买入 T+1，不增加 available
            list[idx] = h.copy(shares = newShares, costPrice = newCost)
        } else {
            list.add(Holding(fullCode, name, shares, 0, price))
        }
        _holdings.value = list
    }

    private fun reduceHoldingOnSell(fullCode: String, shares: Long) {
        val list = _holdings.value.toMutableList()
        val idx = list.indexOfFirst { it.fullCode == fullCode }
        if (idx < 0) return
        val h = list[idx]
        val newShares = h.shares - shares
        if (newShares <= 0) list.removeAt(idx)
        else list[idx] = h.copy(shares = newShares)
        _holdings.value = list
    }

    private fun freezeHoldingForSell(fullCode: String, shares: Long) {
        val list = _holdings.value.toMutableList()
        val idx = list.indexOfFirst { it.fullCode == fullCode }
        if (idx < 0) return
        val h = list[idx]
        list[idx] = h.copy(available = (h.available - shares).coerceAtLeast(0))
        _holdings.value = list
    }

    private fun unfreezeHoldingForSell(fullCode: String, shares: Long) {
        val list = _holdings.value.toMutableList()
        val idx = list.indexOfFirst { it.fullCode == fullCode }
        if (idx < 0) return
        val h = list[idx]
        list[idx] = h.copy(available = (h.available + shares).coerceAtMost(h.shares))
        _holdings.value = list
    }

    private companion object {
        const val KEY_ACCOUNT = "trade_account_snapshot"
    }
}
