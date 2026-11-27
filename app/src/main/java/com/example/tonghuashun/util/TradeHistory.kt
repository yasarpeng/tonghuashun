package com.example.tonghuashun.util

import java.util.Calendar
import java.util.TimeZone

/** 模拟盘归档规则：北京时间周一至周五的两个时段直接归历史，其余跨日归档。 */
class TradeHistory(now: Long) {
    private val zone = TimeZone.getTimeZone("Asia/Shanghai")
    private fun calendar(timestamp: Long) = Calendar.getInstance(zone).apply { timeInMillis = timestamp }
    private val today = calendar(now)

    fun contains(timestamp: Long): Boolean {
        val record = calendar(timestamp)
        if (record.get(Calendar.YEAR) != today.get(Calendar.YEAR) ||
            record.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR)) return true
        val day = record.get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) return false
        val millis = ((record.get(Calendar.HOUR_OF_DAY) * 60L + record.get(Calendar.MINUTE)) * 60 +
            record.get(Calendar.SECOND)) * 1000 + record.get(Calendar.MILLISECOND)
        return millis in (570 * 60_000L)..(720 * 60_000L) ||
            millis in (780 * 60_000L)..(900 * 60_000L)
    }
}
