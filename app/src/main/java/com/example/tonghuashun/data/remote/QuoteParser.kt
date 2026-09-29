package com.example.tonghuashun.data.remote

import com.example.tonghuashun.data.model.KLineEntry
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.OrderBookLevel
import com.example.tonghuashun.data.model.QuoteExtra
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.model.TimeShareEntry
import org.json.JSONObject

/**
 * 新浪/腾讯行情响应解析工具。
 * 新浪实时格式（逗号分隔）:
 *  0名称 1今开 2昨收 3现价 4最高 5最低 6竞买价 7竞卖价 8成交量(股) 9成交额(元)
 *  10买一量 11买一价 ... 18买五量 19买五价
 *  20卖一量 21卖一价 ... 28卖五量 29卖五价
 *  30日期 31时间
 */
object QuoteParser {

    /** 解析新浪实时行情整段文本，返回 fullCode(sh600519) -> Stock */
    fun parseSinaRealtime(text: String): Map<String, Stock> {
        val result = LinkedHashMap<String, Stock>()
        // 每行: var hq_str_sh600519="....";
        text.split("\n").forEach { line ->
            val l = line.trim()
            if (!l.startsWith("var hq_str_")) return@forEach
            val eq = l.indexOf('=')
            if (eq < 0) return@forEach
            val fullCode = l.substring("var hq_str_".length, eq)
            val q1 = l.indexOf('"')
            val q2 = l.lastIndexOf('"')
            if (q1 < 0 || q2 <= q1) return@forEach
            val body = l.substring(q1 + 1, q2)
            if (body.isBlank()) return@forEach
            val f = body.split(",")
            if (f.size < 32) return@forEach
            val market = fullCode.take(2)
            val code = fullCode.drop(2)
            val open = f[1].toDoubleOrNull() ?: 0.0
            val prevClose = f[2].toDoubleOrNull() ?: 0.0
            var price = f[3].toDoubleOrNull() ?: 0.0
            if (price == 0.0) price = prevClose // 停牌/未开盘用昨收
            val high = f[4].toDoubleOrNull() ?: 0.0
            val low = f[5].toDoubleOrNull() ?: 0.0
            val volumeShares = f[8].toDoubleOrNull() ?: 0.0
            val turnover = f[9].toDoubleOrNull() ?: 0.0
            result[fullCode] = Stock(
                code = code,
                name = f[0],
                price = price,
                prevClose = prevClose,
                open = open,
                high = high,
                low = low,
                volume = (volumeShares / 100).toLong(), // 股 -> 手
                turnover = turnover,
                market = market,
            )
        }
        return result
    }

    /** 从新浪实时文本解析某只股票的五档盘口 */
    fun parseSinaOrderBook(text: String): OrderBook {
        val line = text.split("\n").firstOrNull { it.contains("hq_str_") } ?: return OrderBook(emptyList(), emptyList())
        val q1 = line.indexOf('"'); val q2 = line.lastIndexOf('"')
        if (q1 < 0 || q2 <= q1) return OrderBook(emptyList(), emptyList())
        val f = line.substring(q1 + 1, q2).split(",")
        if (f.size < 30) return OrderBook(emptyList(), emptyList())
        val bids = ArrayList<OrderBookLevel>()
        for (i in 1..5) {
            val vol = f[8 + i * 2].toDoubleOrNull() ?: 0.0
            val prc = f[9 + i * 2].toDoubleOrNull() ?: 0.0
            bids.add(OrderBookLevel("买$i", prc, (vol / 100).toLong()))
        }
        val asks = ArrayList<OrderBookLevel>()
        for (i in 1..5) {
            val vol = f[18 + i * 2].toDoubleOrNull() ?: 0.0
            val prc = f[19 + i * 2].toDoubleOrNull() ?: 0.0
            asks.add(OrderBookLevel("卖$i", prc, (vol / 100).toLong()))
        }
        // 卖五~卖一（从高到低展示），买一~买五
        return OrderBook(asks = asks.reversed(), bids = bids)
    }

    /** 解析新浪指数简版 s_sh000001: 名称,当前点,涨跌额,涨跌幅,成交量,成交额 */
    fun parseSinaIndices(text: String): List<MarketIndex> {
        val list = ArrayList<MarketIndex>()
        text.split("\n").forEach { line ->
            val l = line.trim()
            if (!l.startsWith("var hq_str_s_")) return@forEach
            val eq = l.indexOf('=')
            val fullCode = l.substring("var hq_str_s_".length, eq)
            val q1 = l.indexOf('"'); val q2 = l.lastIndexOf('"')
            if (q1 < 0 || q2 <= q1) return@forEach
            val f = l.substring(q1 + 1, q2).split(",")
            if (f.size < 4) return@forEach
            val point = f[1].toDoubleOrNull() ?: 0.0
            val change = f[2].toDoubleOrNull() ?: 0.0
            list.add(MarketIndex(code = fullCode, name = f[0], point = point, prevClose = point - change))
        }
        return list
    }

    /** 解析腾讯日K JSON */
    fun parseTencentKLine(json: String, fullCode: String): List<KLineEntry> {
        return try {
            val root = JSONObject(json)
            val data = root.getJSONObject("data").getJSONObject(fullCode)
            val arr = when {
                data.has("qfqday") -> data.getJSONArray("qfqday")
                data.has("day") -> data.getJSONArray("day")
                else -> return emptyList()
            }
            val result = ArrayList<KLineEntry>(arr.length())
            for (i in 0 until arr.length()) {
                val e = arr.getJSONArray(i)
                val date = e.getString(0)            // 2026-09-11
                val open = e.getString(1).toDoubleOrNull() ?: 0.0
                val close = e.getString(2).toDoubleOrNull() ?: 0.0
                val high = e.getString(3).toDoubleOrNull() ?: 0.0
                val low = e.getString(4).toDoubleOrNull() ?: 0.0
                val vol = e.getString(5).toDoubleOrNull() ?: 0.0
                result.add(
                    KLineEntry(
                        label = date.substring(5),   // MM-dd
                        open = open, close = close, high = high, low = low,
                        volume = vol.toLong(),
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 解析腾讯分时 JSON。
     * data.<code>.data.data = ["HHmm price cumVol cumAmount", ...]
     *   字段: 时间, 价格, 累计成交量(手), 累计成交额(元)
     * data.<code>.qt.<code>[4] = 昨收
     */
    fun parseTencentMinute(json: String, fullCode: String): List<TimeShareEntry> {
        return try {
            val root = JSONObject(json)
            val codeObj = root.getJSONObject("data").getJSONObject(fullCode)
            val minuteArr = codeObj.getJSONObject("data").getJSONArray("data")
            val result = ArrayList<TimeShareEntry>(minuteArr.length())
            var prevCumVol = 0.0
            for (i in 0 until minuteArr.length()) {
                val parts = minuteArr.getString(i).split(" ")
                if (parts.size < 2) continue
                val t = parts[0]
                val time = if (t.length >= 4) "${t.substring(0, 2)}:${t.substring(2, 4)}" else t
                val price = parts[1].toDoubleOrNull() ?: 0.0
                val cumVol = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                val cumAmount = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                // 均价 = 累计成交额 / 累计成交股数；累计量单位是手，*100 得股
                val avg = if (cumVol > 0) cumAmount / (cumVol * 100) else price
                val minuteVol = (cumVol - prevCumVol).coerceAtLeast(0.0)
                prevCumVol = cumVol
                result.add(
                    TimeShareEntry(
                        time = time,
                        price = price,
                        avgPrice = if (avg > 0) avg else price,
                        volume = minuteVol.toLong(),
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 从腾讯分时JSON取昨收 */
    fun parseTencentMinutePrevClose(json: String, fullCode: String): Double {
        return try {
            val qt = JSONObject(json).getJSONObject("data").getJSONObject(fullCode)
                .getJSONObject("qt").getJSONArray(fullCode)
            qt.getString(4).toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * 解析腾讯 qt 简版行情（GBK 文本，`~` 分隔）。
     * 字段下标按公开的 v_ 行情字段表：
     *   38 换手率(%) | 39 市盈率TTM | 43 振幅(%) | 44 流通市值(亿) | 45 总市值(亿) | 46 市净率 | 49 量比
     * 返回 null 表示接口不可用，由 UI 侧显示 "—"。
     */
    fun parseTencentQuote(text: String): QuoteExtra? {
        val q1 = text.indexOf('"')
        val q2 = text.lastIndexOf('"')
        if (q1 < 0 || q2 <= q1) return null
        val f = text.substring(q1 + 1, q2).split("~")
        if (f.size < 50) return null
        fun d(i: Int): Double = f.getOrNull(i)?.toDoubleOrNull() ?: 0.0
        return QuoteExtra(
            turnoverRate = d(38),
            peTtm = d(39),
            amplitude = d(43),
            circulationCap = d(44) * 1e8,
            totalCap = d(45) * 1e8,
            pb = d(46),
            volumeRatio = d(49),
        )
    }
}
