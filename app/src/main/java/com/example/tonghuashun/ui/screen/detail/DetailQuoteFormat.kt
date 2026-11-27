package com.example.tonghuashun.ui.screen.detail

import com.example.tonghuashun.util.QuoteFormat

internal fun isFundQuote(code: String): Boolean = QuoteFormat.priceDecimals(code) == 3

internal fun detailPrice(code: String, value: Double): String = QuoteFormat.price(code, value)

/** 分时轴以实际交易分钟定位，午休前后的 11:30 和 13:00 共用中点。 */
internal fun quoteMinuteIndex(time: String): Int? {
    val parts = time.split(':')
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull()?.takeIf { it in 0..23 } ?: return null
    val minute = parts[1].toIntOrNull()?.takeIf { it in 0..59 } ?: return null
    val clockMinute = hour * 60 + minute
    return when {
        clockMinute <= 690 -> clockMinute - 570
        clockMinute < 780 -> 120
        else -> clockMinute - 780 + 120
    }.coerceIn(0, 240)
}

internal fun adjacentQuoteCode(codes: List<String>, current: String, direction: Int): String? {
    if (codes.isEmpty()) return null
    val index = codes.indexOf(current)
    if (index < 0) return if (direction >= 0) codes.first() else codes.last()
    return codes[Math.floorMod(index + direction, codes.size)]
}
