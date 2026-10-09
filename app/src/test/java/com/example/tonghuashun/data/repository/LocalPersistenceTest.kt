package com.example.tonghuashun.data.repository

import com.example.tonghuashun.data.local.LocalStore
import com.example.tonghuashun.data.model.OrderSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 内存版 LocalStore：模拟"落盘"的键值存储，进程内可反复读写同一份数据。 */
private class FakeLocalStore : LocalStore {
    private val map = mutableMapOf<String, String>()
    override fun read(key: String): String? = map[key]
    override fun write(key: String, value: String) { map[key] = value }
}

/**
 * 持久化测试：同一个 [FakeLocalStore] 上重新 new 仓库，等价于"杀进程后重启 App"。
 * 验证资金 / 持仓 / 委托 / 成交 / 自选股 / 自增 ID 都能跨"重启"保留。
 */
class LocalPersistenceTest {

    @Test
    fun `首次启动没有快照时播种默认账户并写盘`() {
        val store = FakeLocalStore()
        val trade = TradeRepository(store)
        // 默认持仓（茅台 / 五粮液）仍在
        assertTrue(trade.holdings.value.isNotEmpty())
        // 首次启动即写盘，重启后不会又退回"初始资金 - 默认持仓成本"以外的状态
        val restarted = TradeRepository(store)
        assertEquals(trade.holdings.value.size, restarted.holdings.value.size)
        assertEquals(trade.cash.value, restarted.cash.value, 0.001)

        val watch = WatchlistRepository(store)
        assertTrue(watch.codes.value.isNotEmpty())
    }

    @Test
    fun `交易账户重启后保留资金持仓与成交`() {
        val store = FakeLocalStore()
        val repo = TradeRepository(store)
        repo.resetAccount(500_000.0)
        val buy = repo.executeImmediately(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, price = 10.5, shares = 1000,
        )
        assertTrue(buy.isSuccess)
        val cashAfterBuy = repo.cash.value

        // 杀进程后重启
        val restarted = TradeRepository(store)
        assertEquals(cashAfterBuy, restarted.cash.value, 0.001)
        assertEquals(0.0, restarted.frozenCash.value, 0.001)
        val holding = restarted.holdings.value.first { it.fullCode == "sh600000" }
        assertEquals(1000L, holding.shares)
        assertEquals(10.5, holding.costPrice, 0.001)
        assertEquals(1, restarted.trades.value.size)
        assertEquals(1, restarted.orders.value.size)
    }

    @Test
    fun `挂单与冻结资金重启后仍然保留`() {
        val store = FakeLocalStore()
        val repo = TradeRepository(store)
        repo.resetAccount(1_000_000.0)
        // 委托价 9 < 市价 10 -> 挂单，冻结 900
        repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 9.0, shares = 100, marketPrice = 10.0,
        )

        val restarted = TradeRepository(store)
        assertEquals(900.0, restarted.frozenCash.value, 0.001)
        assertEquals(999_100.0, restarted.cash.value, 0.001)
        assertEquals(1, restarted.orders.value.size)
        assertTrue(restarted.orders.value.first().canCancel)
    }

    @Test
    fun `撤单与自定义总资产的结果重启后保留`() {
        val store = FakeLocalStore()
        val repo = TradeRepository(store)
        repo.resetAccount(1_000_000.0)
        val order = repo.placeOrder(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, limitPrice = 9.0, shares = 100, marketPrice = 10.0,
        ).getOrThrow()
        repo.cancelOrder(order.id)
        repo.setTotalCapitalKeepingHoldings(2_000_000.0, 0.0)
        val cash = repo.cash.value

        val restarted = TradeRepository(store)
        assertEquals(cash, restarted.cash.value, 0.001)
        assertEquals(0.0, restarted.frozenCash.value, 0.001)
        assertEquals(1, restarted.orders.value.size)
        assertEquals(
            com.example.tonghuashun.data.model.OrderStatus.CANCELLED,
            restarted.orders.value.first().status,
        )
    }

    @Test
    fun `自增ID序号重启后继续递增不重复`() {
        val store = FakeLocalStore()
        val repo = TradeRepository(store)
        repo.resetAccount(1_000_000.0)
        val firstId = repo.executeImmediately(
            fullCode = "sh600000", code = "600000", name = "浦发银行",
            side = OrderSide.BUY, price = 10.0, shares = 100,
        ).getOrThrow().id

        val restarted = TradeRepository(store)
        val secondId = restarted.executeImmediately(
            fullCode = "sh600001", code = "600001", name = "浦发银行2",
            side = OrderSide.BUY, price = 10.0, shares = 100,
        ).getOrThrow().id
        assertNotEquals(firstId, secondId)
    }

    @Test
    fun `自选股增删重启后保留`() {
        val store = FakeLocalStore()
        val repo = WatchlistRepository(store)
        repo.add("sh601318")
        repo.remove("sz002594")

        val restarted = WatchlistRepository(store)
        assertTrue(restarted.codes.value.contains("sh601318"))
        assertFalse(restarted.codes.value.contains("sz002594"))
        // 默认自选里的其它股票不受影响
        assertTrue(restarted.codes.value.contains("sh600519"))
    }

    @Test
    fun `清空自选后重启保持为空而不是回退默认`() {
        val store = FakeLocalStore()
        val repo = WatchlistRepository(store)
        repo.codes.value.toList().forEach { repo.remove(it) }
        assertTrue(repo.codes.value.isEmpty())

        val restarted = WatchlistRepository(store)
        assertTrue(restarted.codes.value.isEmpty())
    }

    @Test
    fun `没有持久化时仓库退化为纯内存不报错`() {
        // 默认构造（单元测试里大量使用）不应依赖任何存储
        val repo = TradeRepository()
        repo.resetAccount(123_000.0)
        assertEquals(123_000.0, repo.cash.value, 0.001)
        val watch = WatchlistRepository()
        watch.toggle("sh600000")
        assertTrue(watch.isFavorite("sh600000"))
    }
}
