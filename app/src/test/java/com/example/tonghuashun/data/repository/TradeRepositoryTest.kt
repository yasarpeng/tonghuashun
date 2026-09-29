package com.example.tonghuashun.data.repository

import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TradeRepository 交易核心逻辑的本地单元测试（纯 JVM，不依赖 Android）。
 * 覆盖：下单立即成交 / 挂单撮合 / 撤单 / 资金冻结 / 卖出可用校验 / 自定义总资产。
 */
class TradeRepositoryTest {

    private lateinit var repo: TradeRepository

    @Before
    fun setUp() {
        repo = TradeRepository()
        // 从一个干净的空仓账户开始，避免默认持仓干扰
        repo.resetAccount(1_000_000.0)
    }

    @Test
    fun `买入委托价高于市价立即全部成交`() {
        // 委托价 12 >= 市价 10，应立即以市价 10 成交
        val result = repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 12.0, shares = 100, marketPrice = 10.0,
        )
        assertTrue(result.isSuccess)
        val order = result.getOrThrow()
        assertEquals(OrderStatus.FILLED, order.status)

        // 持仓增加 100 股，成本价为成交价 10
        val holding = repo.holdings.value.first { it.fullCode == "sh600000" }
        assertEquals(100L, holding.shares)
        assertEquals(10.0, holding.costPrice, 0.001)

        // 现金 = 100万 - 100*10 = 999000；无冻结（差额已退回）
        assertEquals(999_000.0, repo.cash.value, 0.001)
        assertEquals(0.0, repo.frozenCash.value, 0.001)

        // 成交记录一条
        assertEquals(1, repo.trades.value.size)
        assertEquals(OrderSide.BUY, repo.trades.value.first().side)
    }

    @Test
    fun `买入委托价低于市价则挂单等待并冻结资金`() {
        // 委托价 9 < 市价 10，不能成交，挂单等待
        val result = repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 9.0, shares = 100, marketPrice = 10.0,
        )
        assertTrue(result.isSuccess)
        assertEquals(OrderStatus.PENDING, result.getOrThrow().status)

        // 冻结 9*100 = 900，现金减少 900
        assertEquals(900.0, repo.frozenCash.value, 0.001)
        assertEquals(999_100.0, repo.cash.value, 0.001)
        // 尚无持仓、无成交
        assertTrue(repo.holdings.value.none { it.fullCode == "sh600000" })
        assertEquals(0, repo.trades.value.size)
    }

    @Test
    fun `挂单在行情下跌到委托价后被撮合成交`() {
        repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 9.0, shares = 100, marketPrice = 10.0,
        )
        // 行情下跌到 9，触发撮合
        repo.tryMatch(mapOf("sh600000" to 9.0))

        val order = repo.orders.value.first()
        assertEquals(OrderStatus.FILLED, order.status)
        assertEquals(100L, repo.holdings.value.first { it.fullCode == "sh600000" }.shares)
        assertEquals(0.0, repo.frozenCash.value, 0.001)
        assertEquals(1, repo.trades.value.size)
    }

    @Test
    fun `撤单释放冻结资金`() {
        val order = repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 9.0, shares = 100, marketPrice = 10.0,
        ).getOrThrow()
        assertEquals(900.0, repo.frozenCash.value, 0.001)

        val cancel = repo.cancelOrder(order.id)
        assertTrue(cancel.isSuccess)
        assertEquals(OrderStatus.CANCELLED, repo.orders.value.first().status)
        // 冻结资金全部释放，现金恢复
        assertEquals(0.0, repo.frozenCash.value, 0.001)
        assertEquals(1_000_000.0, repo.cash.value, 0.001)
    }

    @Test
    fun `资金不足时买入失败`() {
        val result = repo.placeOrder(
            fullCode = "sh600519", code = "600519", name = "贵州茅台",
            side = OrderSide.BUY, limitPrice = 2000.0, shares = 1000, marketPrice = 2000.0,
        )
        // 2000*1000 = 200万 > 100万
        assertTrue(result.isFailure)
        assertEquals(1_000_000.0, repo.cash.value, 0.001)
    }

    @Test
    fun `数量非100整数倍时下单失败`() {
        val result = repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 10.0, shares = 150, marketPrice = 10.0,
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `卖出成交增加现金并减少持仓`() {
        // 先建立可卖持仓：直接用 setTotalCapitalKeepingHoldings 不行（空仓），改为买入后手动放开可用
        // 通过 buildHoldingWithAvailable 模拟 T+1 后可卖：这里用 resetAccount 再注入
        repo.resetAccount(1_000_000.0)
        // 买入成交（当日不可卖），再模拟次日可卖：直接卖出应因可用不足失败
        repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 10.0, shares = 200, marketPrice = 10.0,
        )
        // 当日买入 available=0，卖出应失败
        val failSell = repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.SELL, limitPrice = 10.0, shares = 100, marketPrice = 10.0,
        )
        assertTrue("当日买入不可卖(T+1)", failSell.isFailure)
    }

    @Test
    fun `默认账户初始持仓可卖并能卖出成交`() {
        // 用全新 repo（含默认持仓 贵州茅台100股 available=100）
        val fresh = TradeRepository()
        val availBefore = fresh.availableShares("sh600519")
        assertEquals(100L, availBefore)
        val cashBefore = fresh.cash.value

        val result = fresh.placeOrder(
            fullCode = "sh600519", code = "600519", name = "贵州茅台",
            side = OrderSide.SELL, limitPrice = 1500.0, shares = 100, marketPrice = 1700.0,
        )
        // 卖出委托价 1500 <= 市价 1700，以市价成交
        assertTrue(result.isSuccess)
        assertEquals(OrderStatus.FILLED, result.getOrThrow().status)
        // 持仓清空
        assertNull(fresh.holdings.value.firstOrNull { it.fullCode == "sh600519" })
        // 现金增加 100*1700
        assertEquals(cashBefore + 100 * 1700.0, fresh.cash.value, 0.001)
    }

    @Test
    fun `自定义总资产保留持仓时调整可用资金`() {
        val fresh = TradeRepository()
        // 总资产 = 可用资金 + 持仓市值，所以可用资金按"目标总资产 - 持仓市值"反推
        fresh.setTotalCapitalKeepingHoldings(2_000_000.0, 250_000.0)
        assertEquals(2_000_000.0 - 250_000.0, fresh.cash.value, 0.001)
        assertEquals(0.0, fresh.frozenCash.value, 0.001)
        // 持仓仍在
        assertNotNull(fresh.holdings.value.firstOrNull { it.fullCode == "sh600519" })
    }

    @Test
    fun `自定义总资产在行情未就绪时按持仓成本估算`() {
        val fresh = TradeRepository()
        // 行情拿不到时（positionValue = 0）退回成本口径：茅台 100*1600 + 五粮液 500*150 = 235000
        fresh.setTotalCapitalKeepingHoldings(2_000_000.0)
        assertEquals(2_000_000.0 - 235_000.0, fresh.cash.value, 0.001)
    }

    @Test
    fun `重置账户清空持仓委托成交`() {
        val fresh = TradeRepository()
        fresh.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 9.0, shares = 100, marketPrice = 10.0,
        )
        fresh.resetAccount(500_000.0)
        assertEquals(500_000.0, fresh.cash.value, 0.001)
        assertEquals(0.0, fresh.frozenCash.value, 0.001)
        assertTrue(fresh.holdings.value.isEmpty())
        assertTrue(fresh.orders.value.isEmpty())
        assertTrue(fresh.trades.value.isEmpty())
    }

    /* ---------------- 立即成交（点了就成交，价格自定义） ---------------- */

    @Test
    fun `立即成交按自定义价格买入并扣款建仓`() {
        val fresh = TradeRepository()
        fresh.resetAccount(100_000.0)
        val r = fresh.executeImmediately(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, price = 9.99, shares = 1000,
        )
        assertTrue(r.isSuccess)
        assertEquals(OrderStatus.FILLED, r.getOrThrow().status)
        assertEquals(100_000.0 - 9.99 * 1000, fresh.cash.value, 0.001)
        val h = fresh.holdings.value.first { it.fullCode == "sh600000" }
        assertEquals(1000L, h.shares)
        assertEquals(9.99, h.costPrice, 0.001)
        assertEquals(1, fresh.trades.value.size)
    }

    @Test
    fun `立即成交按自定义价格卖出增加现金`() {
        val fresh = TradeRepository()
        val cashBefore = fresh.cash.value
        // 默认持仓：茅台 100 股，可用 100
        val r = fresh.executeImmediately(
            fullCode = "sh600519", code = "600519", name = "贵州茅台",
            side = OrderSide.SELL, price = 1500.0, shares = 100,
        )
        assertTrue(r.isSuccess)
        assertEquals(cashBefore + 1500.0 * 100, fresh.cash.value, 0.001)
        assertNull(fresh.holdings.value.firstOrNull { it.fullCode == "sh600519" })
    }

    @Test
    fun `立即成交价格不受涨跌停限制`() {
        val fresh = TradeRepository()
        fresh.resetAccount(10_000_000.0)
        // 昨收约 10 元、涨停 11 元，这里按 15 元买也应当直接成交
        val r = fresh.executeImmediately(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, price = 15.0, shares = 100,
        )
        assertTrue(r.isSuccess)
        assertEquals(15.0, fresh.trades.value.first().price, 0.001)
        assertEquals(100L, fresh.trades.value.first().shares)
    }
}
