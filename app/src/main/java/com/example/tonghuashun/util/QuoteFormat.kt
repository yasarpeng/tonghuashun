package com.example.tonghuashun.util

import java.util.Locale
import androidx.compose.ui.graphics.Color
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockFlat
import com.example.tonghuashun.ui.theme.StockUp

object QuoteFormat {

    /** 根据涨跌返回 A 股配色：涨红、跌绿、平灰 */
    fun colorOf(change: Double): Color = when {
        change > 0 -> StockUp
        change < 0 -> StockDown
        else -> StockFlat
    }

    /** 价格保留两位 */
    fun price(v: Double): String = "%.2f".format(v)

    /** ETF/LOF 报价三位小数，普通股票两位。接受带市场前缀或六位代码。 */
    fun priceDecimals(code: String): Int {
        val symbol = code.removePrefix("sh").removePrefix("sz")
        return if (symbol.startsWith("5") || symbol.startsWith("15") || symbol.startsWith("16") || symbol.startsWith("18")) 3 else 2
    }

    fun price(code: String, value: Double): String = String.format(Locale.CHINA, "%.${priceDecimals(code)}f", value)

    fun normalizedPrice(code: String, value: Double): Double = price(code, value).toDouble()

    /** 带正负号的涨跌额 */
    fun signed(v: Double): String = (if (v > 0) "+" else "") + "%.2f".format(v)

    /** 带正负号的百分比 */
    fun percent(v: Double): String = (if (v > 0) "+" else "") + "%.2f".format(v) + "%"

    /** 大数字格式化：万 / 亿 */
    fun bigNumber(v: Double): String = when {
        v >= 1_0000_0000 -> "%.2f亿".format(v / 1_0000_0000)
        v >= 1_0000 -> "%.2f万".format(v / 1_0000)
        else -> "%.0f".format(v)
    }

    /** 成交量（手 -> 万手/手） */
    fun volume(hands: Long): String = when {
        hands >= 1_0000 -> "%.2f万手".format(hands / 1_0000.0)
        else -> "${hands}手"
    }
}
