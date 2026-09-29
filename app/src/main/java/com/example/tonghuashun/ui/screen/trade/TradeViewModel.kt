package com.example.tonghuashun.ui.screen.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonghuashun.data.model.Order
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.Position
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.model.TimeShareEntry
import com.example.tonghuashun.data.model.TradeRecord
import com.example.tonghuashun.data.repository.Holding
import com.example.tonghuashun.data.repository.StockRepository
import com.example.tonghuashun.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradeUiState(
    val cash: Double = 0.0,
    val frozenCash: Double = 0.0,
    val positions: List<Position> = emptyList(),
    val orders: List<Order> = emptyList(),
    val trades: List<TradeRecord> = emptyList(),
) {
    val marketValue: Double get() = positions.sumOf { it.marketValue }
    val totalAsset: Double get() = cash + frozenCash + marketValue
    val totalProfit: Double get() = positions.sumOf { it.profit }
    /** 当日参考盈亏（按昨收计算），同花顺交易首页"当日参考盈亏" */
    val dayProfit: Double get() = positions.sumOf { it.dayProfit }
    /** 昨日持仓市值，用于当日参考盈亏百分比 */
    val prevMarketValue: Double get() = positions.sumOf { it.prevClose * it.shares }
    /** 待成交（可撤）委托 */
    val pendingOrders: List<Order> get() = orders.filter { it.canCancel }
}

/** 买入/卖出下单页的表单状态 */
data class OrderFormState(
    val codeInput: String = "",       // 股票代码/简拼输入框内容
    val stock: Stock? = null,         // 已解析出的行情
    val priceText: String = "",       // 价格输入
    val sharesText: String = "",      // 数量输入
    val orderBook: OrderBook = OrderBook(emptyList(), emptyList()),
    val prevClose: Double = 0.0,      // 昨收（缺省时用 stock.prevClose）
    val timeShare: List<TimeShareEntry> = emptyList(), // 下单页"展开"后的分时图
) {
    val price: Double get() = priceText.toDoubleOrNull() ?: (stock?.price ?: 0.0)
    val shares: Long get() = sharesText.toLongOrNull() ?: 0L
    val amount: Double get() = price * shares
    /** 昨收（用于涨跌停价与分时基准） */
    val basePrice: Double get() = if (prevClose > 0) prevClose else (stock?.prevClose ?: 0.0)
    /** 当日均价 = 成交额 / 成交量(手*100)。无数据时回退为现价。 */
    val avgPrice: Double
        get() {
            val s = stock ?: return 0.0
            val vol = s.volume * 100.0
            return if (vol > 0) s.turnover / vol else s.price
        }
}

@HiltViewModel
class TradeViewModel @Inject constructor(
    private val repository: StockRepository,
    private val tradeRepository: TradeRepository,
) : ViewModel() {

    /** 持仓/委托相关股票的最新行情快照，由定时任务刷新，驱动持仓表实时跳动 */
    private val _priceMap = MutableStateFlow<Map<String, Stock>>(emptyMap())

    /** 买入/卖出下单页表单 */
    private val _orderForm = MutableStateFlow(OrderFormState())
    val orderForm: StateFlow<OrderFormState> = _orderForm.asStateFlow()

    /** 账户快照（不含行情），行情单独作为一路流 combine 进来 */
    private data class AccountSnapshot(
        val cash: Double,
        val frozenCash: Double,
        val holdings: List<Holding>,
        val orders: List<Order>,
        val trades: List<TradeRecord>,
    )

    private val accountSnapshot: Flow<AccountSnapshot> =
        combine(
            tradeRepository.cash,
            tradeRepository.frozenCash,
            tradeRepository.holdings,
            tradeRepository.orders,
            tradeRepository.trades,
        ) { cash, frozen, holdings, orders, trades ->
            AccountSnapshot(cash, frozen, holdings, orders, trades)
        }

    val uiState: StateFlow<TradeUiState> =
        combine(accountSnapshot, _priceMap) { snap, prices ->
            TradeUiState(
                cash = snap.cash,
                frozenCash = snap.frozenCash,
                positions = snap.holdings.map { toPosition(it, prices[it.fullCode]) },
                orders = snap.orders,
                trades = snap.trades,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradeUiState())

    init {
        // 定时刷新行情并撮合挂单
        viewModelScope.launch {
            while (true) {
                refreshAndMatch()
                refreshOrderForm()
                delay(4000)
            }
        }
    }

    private suspend fun refreshAndMatch() {
        val holdingCodes = tradeRepository.holdings.value.map { it.fullCode }
        val orderCodes = tradeRepository.orders.value.filter { it.canCancel }.map { it.fullCode }
        val codes = (holdingCodes + orderCodes).distinct()
        if (codes.isEmpty()) return
        val realtime = runCatching { repository.getRealtime(codes) }.getOrDefault(emptyList())
        if (realtime.isEmpty()) return
        _priceMap.value = realtime.associateBy { it.fullCode }
        tradeRepository.tryMatch(realtime.associate { it.fullCode to it.price })
    }

    fun cancelOrder(orderId: String): String {
        val r = tradeRepository.cancelOrder(orderId)
        return if (r.isSuccess) "已撤单" else (r.exceptionOrNull()?.message ?: "撤单失败")
    }

    /** 手动刷新持仓行情（交易页头部"刷新"按钮） */
    fun refreshNow() {
        viewModelScope.launch { refreshAndMatch() }
    }

    /** 全撤 / 撤买 / 撤卖 */
    fun cancelAll(side: OrderSide? = null): String {
        val pending = tradeRepository.orders.value.filter {
            it.canCancel && (side == null || it.side == side)
        }
        if (pending.isEmpty()) return "无可撤委托"
        var n = 0
        pending.forEach { if (tradeRepository.cancelOrder(it.id).isSuccess) n++ }
        return "已撤 $n 笔委托"
    }

    /* ----------------- 买入/卖出下单表单 ----------------- */

    /** 预置下单股票（从持仓表点“买入/卖出”或详情页跳入时调用） */
    fun prepareOrder(fullCode: String) {
        // 切股时清掉上一只的委托价/数量，等解析出新股后自动填
        _orderForm.value = OrderFormState(codeInput = fullCode)
        loadOrderStock(fullCode)
    }

    fun onCodeInput(text: String) {
        _orderForm.value = _orderForm.value.copy(codeInput = text)
        val t = text.trim()
        // 输入满 6 位数字或带市场前缀时尝试解析
        if (t.length >= 6) loadOrderStock(t)
    }

    /**
     * 解析下单股票：支持 sh600519 / 600519 两种输入。
     * 解析成功后自动填代码（6 位）与该股最新价，并拉五档、分时。
     */
    private fun loadOrderStock(input: String) {
        val query = input.trim()
        viewModelScope.launch {
            val stock = runCatching { repository.getStockByAnyCode(query) }.getOrNull()
                ?: runCatching { repository.searchStocks(query) }.getOrDefault(emptyList()).firstOrNull()
                ?: return@launch
            val ob = runCatching { repository.getOrderBook(stock.fullCode) }
                .getOrDefault(OrderBook(emptyList(), emptyList()))
            val (ts, prevClose) = runCatching { repository.getTimeShare(stock.fullCode) }
                .getOrDefault(emptyList<TimeShareEntry>() to 0.0)
            // 解析期间用户可能已经改了输入，避免把旧结果写回去
            if (_orderForm.value.codeInput.trim() != query) return@launch
            _orderForm.value = _orderForm.value.copy(
                codeInput = stock.code,                       // 统一回填 6 位代码
                stock = stock,
                orderBook = ob,
                prevClose = if (prevClose > 0) prevClose else stock.prevClose,
                timeShare = ts,
                priceText = "%.2f".format(stock.price),        // 自动填充该股最新价
            )
        }
    }

    /** 下单页行情轮询：现价 / 五档 / 分时跟着实时行情刷新（不动用户输入的委托价与数量） */
    private suspend fun refreshOrderForm() {
        val current = _orderForm.value
        val stock = current.stock ?: return
        val live = runCatching { repository.getStock(stock.fullCode) }.getOrNull() ?: return
        val ob = runCatching { repository.getOrderBook(stock.fullCode) }
            .getOrDefault(OrderBook(emptyList(), emptyList()))
        val (ts, prevClose) = runCatching { repository.getTimeShare(stock.fullCode) }
            .getOrDefault(emptyList<TimeShareEntry>() to 0.0)
        // 期间已切换到别的股票就丢弃本次结果
        if (_orderForm.value.stock?.fullCode != stock.fullCode) return
        _orderForm.value = _orderForm.value.copy(
            stock = live,
            orderBook = if (ob.asks.isEmpty() && ob.bids.isEmpty()) _orderForm.value.orderBook else ob,
            prevClose = if (prevClose > 0) prevClose else _orderForm.value.prevClose,
            timeShare = if (ts.isEmpty()) _orderForm.value.timeShare else ts,
        )
    }

    fun setPriceText(v: String) { _orderForm.value = _orderForm.value.copy(priceText = v.filter { it.isDigit() || it == '.' }) }
    fun setSharesText(v: String) { _orderForm.value = _orderForm.value.copy(sharesText = v.filter { it.isDigit() }) }

    fun stepPrice(delta: Double) {
        val cur = _orderForm.value.priceText.toDoubleOrNull() ?: (_orderForm.value.stock?.price ?: 0.0)
        val next = (cur + delta).coerceAtLeast(0.01)
        _orderForm.value = _orderForm.value.copy(priceText = "%.2f".format(next))
    }

    fun stepShares(delta: Long) {
        val cur = _orderForm.value.sharesText.toLongOrNull() ?: 0L
        _orderForm.value = _orderForm.value.copy(sharesText = (cur + delta).coerceAtLeast(0).toString())
    }

    /** 仓位比例：买入按可用资金，卖出按可卖股数 */
    fun setByRatio(side: OrderSide, ratio: Double) {
        val form = _orderForm.value
        val stock = form.stock ?: return
        val p = form.price
        val target = when (side) {
            OrderSide.BUY -> {
                if (p <= 0) return
                val max = (tradeRepository.cash.value / p).toLong() / 100 * 100
                (max * ratio).toLong() / 100 * 100
            }
            OrderSide.SELL -> {
                val avail = tradeRepository.availableShares(stock.fullCode)
                (avail * ratio).toLong() / 100 * 100
            }
        }
        _orderForm.value = form.copy(sharesText = target.coerceAtLeast(0).toString())
    }

    fun availableCashOrShares(side: OrderSide): String {
        val form = _orderForm.value
        return when (side) {
            OrderSide.BUY -> {
                val p = form.price
                if (p <= 0) "可买 --"
                else "可买 ${(tradeRepository.cash.value / p).toLong() / 100 * 100} 股"
            }
            OrderSide.SELL -> {
                val stock = form.stock ?: return "可卖 --"
                "可卖 ${tradeRepository.availableShares(stock.fullCode)} 股"
            }
        }
    }

    fun submitOrder(side: OrderSide): String {
        val form = _orderForm.value
        val stock = form.stock ?: return "请输入股票代码"
        val p = form.price
        val qty = form.shares
        if (p <= 0) return "请输入价格"
        if (qty <= 0) return "请输入数量"
        // 点了就成交：不受涨跌停限制，按用户填的价格直接成交
        val r = tradeRepository.executeImmediately(
            fullCode = stock.fullCode, code = stock.code, name = stock.name,
            side = side, price = p, shares = qty,
        )
        return if (r.isSuccess) {
            _orderForm.value = form.copy(sharesText = "")
            "%s成交 %d 股 @ %.2f".format(side.label, qty, p)
        } else r.exceptionOrNull()?.message ?: "${side.label}失败"
    }

    /** 自定义总资产（保留持仓，调整可用资金） */
    fun setTotalCapital(total: Double): String {
        if (total < 0) return "金额无效"
        val positionValue = uiState.value.marketValue
        if (positionValue > total + 0.01) {
            return "持仓市值 %.2f 已超过该值，请先卖出或调高金额".format(positionValue)
        }
        tradeRepository.setTotalCapitalKeepingHoldings(total, positionValue)
        return "总资产已设为 %.2f".format(total)
    }

    /** 自定义总资产并清空持仓/委托/成交（全新账户） */
    fun resetAccount(capital: Double): String {
        if (capital < 0) return "金额无效"
        tradeRepository.resetAccount(capital)
        return "账户已重置，可用资金 %.2f".format(capital)
    }

    /** 用最新行情构造持仓（纯函数，不再发请求） */
    private fun toPosition(h: Holding, stock: Stock?): Position = Position(
        code = stock?.code ?: h.fullCode.filter { it.isDigit() },
        name = h.name,
        market = stock?.market ?: h.fullCode.take(2),
        shares = h.shares,
        available = h.available,
        costPrice = h.costPrice,
        currentPrice = stock?.price ?: h.costPrice,
        prevClose = stock?.prevClose ?: 0.0,
    )
}
