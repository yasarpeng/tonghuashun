package com.example.tonghuashun.data.remote

import com.example.tonghuashun.data.model.KLineEntry
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.QuoteExtra
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.model.TimeShareEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 真实行情数据源。封装新浪(实时/指数,GBK) + 腾讯(K线/分时,UTF-8) 网络调用与解析。
 * 所有方法在 IO 线程执行，失败返回空/默认值（由上层决定回退）。
 */
@Singleton
class QuoteDataSource @Inject constructor(
    private val sinaApi: SinaApi,
    private val tencentApi: TencentApi,
    private val tencentQtApi: TencentQtApi,
) {
    private val gbk: Charset = Charset.forName("GBK")

    private fun decodeGbk(body: ResponseBody): String = body.bytes().toString(gbk)
    private fun decodeUtf8(body: ResponseBody): String = body.bytes().toString(Charsets.UTF_8)

    /** 批量实时行情，输入 fullCode 列表(sh600519)。 */
    suspend fun getRealtime(fullCodes: List<String>): Map<String, Stock> = withContext(Dispatchers.IO) {
        if (fullCodes.isEmpty()) return@withContext emptyMap()
        try {
            val url = "https://hq.sinajs.cn/list=" + fullCodes.joinToString(",")
            val text = decodeGbk(sinaApi.getRealtime(url))
            QuoteParser.parseSinaRealtime(text)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** 单只股票实时行情 */
    suspend fun getStock(fullCode: String): Stock? =
        getRealtime(listOf(fullCode))[fullCode]

    /** 五档盘口 */
    suspend fun getOrderBook(fullCode: String): OrderBook = withContext(Dispatchers.IO) {
        try {
            val url = "https://hq.sinajs.cn/list=$fullCode"
            val text = decodeGbk(sinaApi.getRealtime(url))
            QuoteParser.parseSinaOrderBook(text)
        } catch (e: Exception) {
            OrderBook(emptyList(), emptyList())
        }
    }

    /** 个股扩展行情：市值 / 流通 / 市盈TTM / 市净率 / 量比 / 换手 / 振幅 */
    suspend fun getQuoteExtra(fullCode: String): QuoteExtra? = withContext(Dispatchers.IO) {
        try {
            val text = decodeGbk(tencentQtApi.getQuote("https://qt.gtimg.cn/q=$fullCode"))
            QuoteParser.parseTencentQuote(text)
        } catch (e: Exception) {
            null
        }
    }

    /** 大盘指数（新浪简版 s_ 前缀） */
    suspend fun getIndices(fullCodes: List<String>): List<MarketIndex> = withContext(Dispatchers.IO) {
        try {
            val url = "https://hq.sinajs.cn/list=" + fullCodes.joinToString(",") { "s_$it" }
            val text = decodeGbk(sinaApi.getRealtime(url))
            QuoteParser.parseSinaIndices(text)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 日K线 */
    suspend fun getDailyKLine(fullCode: String, count: Int = 120): List<KLineEntry> = withContext(Dispatchers.IO) {
        try {
            val json = decodeUtf8(tencentApi.getKLine("$fullCode,day,,,$count,qfq"))
            QuoteParser.parseTencentKLine(json, fullCode)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 分时 */
    suspend fun getTimeShare(fullCode: String): List<TimeShareEntry> = withContext(Dispatchers.IO) {
        try {
            val json = decodeUtf8(tencentApi.getMinute(fullCode))
            QuoteParser.parseTencentMinute(json, fullCode)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 分时(含昨收) */
    suspend fun getTimeSharePair(fullCode: String): Pair<List<TimeShareEntry>, Double> = withContext(Dispatchers.IO) {
        try {
            val json = decodeUtf8(tencentApi.getMinute(fullCode))
            QuoteParser.parseTencentMinute(json, fullCode) to QuoteParser.parseTencentMinutePrevClose(json, fullCode)
        } catch (e: Exception) {
            emptyList<TimeShareEntry>() to 0.0
        }
    }
}
