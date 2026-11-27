package com.example.tonghuashun.ui.screen.trade

import com.example.tonghuashun.data.model.Order
import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.OrderStatus
import com.example.tonghuashun.data.model.TradeRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TradeHistoryTest {
    private fun timestamp(value: String): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        }.parse(value)!!.time

    private fun stateAt(recordTime: String, now: String = "2026-11-27 18:00:00.000"): TradeUiState {
        val ts = timestamp(recordTime)
        return TradeUiState(
            orders = listOf(Order("W1", "sh600000", "600000", "浦发银行", OrderSide.BUY,
                10.0, 100, 100, OrderStatus.FILLED, "10:00:00", ts)),
            trades = listOf(TradeRecord("T1", "W1", "sh600000", "600000", "浦发银行",
                OrderSide.BUY, 10.0, 100, "10:00:00", ts)),
            now = timestamp(now),
        )
    }

    @Test
    fun `工作日两个时段及起止时刻直接进入历史`() {
        listOf("09:30:00.000", "10:30:00.000", "12:00:00.000", "13:00:00.000",
            "14:30:00.000", "15:00:00.000").forEach { time ->
            val state = stateAt("2026-11-27 $time")
            assertEquals(time, 0, state.todayOrders.size)
            assertEquals(time, 0, state.todayTrades.size)
            assertEquals(time, 1, state.historyOrders.size)
            assertEquals(time, 1, state.historyTrades.size)
        }
    }

    @Test
    fun `工作日时段外留在当日列表`() {
        listOf("00:00:00.000", "09:29:59.999", "12:00:00.001", "12:30:00.000",
            "12:59:59.999", "15:00:00.001", "23:59:59.999").forEach { time ->
            val state = stateAt("2026-11-27 $time")
            assertEquals(time, 1, state.todayOrders.size)
            assertEquals(time, 1, state.todayTrades.size)
            assertEquals(time, 0, state.historyOrders.size)
            assertEquals(time, 0, state.historyTrades.size)
        }
    }

    @Test
    fun `周末时段内也留在当日列表`() {
        listOf("2026-11-28", "2026-11-29").forEach { date ->
            val state = stateAt("$date 10:00:00.000", "$date 18:00:00.000")
            assertEquals(1, state.todayOrders.size)
            assertEquals(1, state.todayTrades.size)
            assertEquals(0, state.historyOrders.size)
            assertEquals(0, state.historyTrades.size)
        }
    }

    @Test
    fun `跨日后时段外记录进入历史且不受设备时区影响`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            val state = stateAt("2026-11-27 23:59:59.999", "2026-11-28 00:00:00.000")
            assertEquals(0, state.todayOrders.size)
            assertEquals(0, state.todayTrades.size)
            assertEquals(1, state.historyOrders.size)
            assertEquals(1, state.historyTrades.size)
            assertEquals(1, stateAt("2026-11-27 10:00:00.000").historyOrders.size)
        } finally {
            TimeZone.setDefault(original)
        }
    }
    @Test
    fun `周一至周五归档规则一致`() {
        listOf("2026-11-23", "2026-11-24", "2026-11-25", "2026-11-26", "2026-11-27").forEach { date ->
            val state = stateAt("$date 10:00:00.000", "$date 10:00:00.000")
            assertEquals(date, 1, state.historyOrders.size)
            assertEquals(date, 1, state.historyTrades.size)
        }
    }

    @Test
    fun `买卖记录互斥分流且时钟前进不会改变时段外记录归属`() {
        val beforeOpen = stateAt("2026-11-27 09:00:00.000", "2026-11-27 09:00:00.000")
        val inSession = stateAt("2026-11-27 10:00:00.000")
        val mixed = beforeOpen.copy(
            orders = beforeOpen.orders + inSession.orders.map { it.copy(id = "W2", side = OrderSide.SELL) },
            trades = beforeOpen.trades + inSession.trades.map { it.copy(id = "T2", side = OrderSide.SELL) },
            now = timestamp("2026-11-27 10:00:00.000"),
        )
        assertEquals(listOf("W1"), mixed.todayOrders.map { it.id })
        assertEquals(listOf("T1"), mixed.todayTrades.map { it.id })
        assertEquals(listOf("W2"), mixed.historyOrders.map { it.id })
        assertEquals(listOf("T2"), mixed.historyTrades.map { it.id })
        val nextDay = mixed.copy(now = timestamp("2026-11-28 00:00:00.000"))
        assertEquals(0, nextDay.todayOrders.size)
        assertEquals(0, nextDay.todayTrades.size)
        assertEquals(listOf("W1", "W2"), nextDay.historyOrders.map { it.id })
        assertEquals(listOf("T1", "T2"), nextDay.historyTrades.map { it.id })
    }

}
