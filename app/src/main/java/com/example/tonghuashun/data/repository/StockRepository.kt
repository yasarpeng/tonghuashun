package com.example.tonghuashun.data.repository

import com.example.tonghuashun.data.model.KLineEntry
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.QuoteExtra
import com.example.tonghuashun.data.model.Sector
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.data.model.TimeShareEntry
import com.example.tonghuashun.data.remote.QuoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 真实行情仓库。行情/K线/分时/盘口/指数全部来自新浪+腾讯免费公开接口。
 * 股票池是一份固定的热门股列表（用于行情榜、首页热门等），其实时数据实时拉取。
 * 板块、资讯为静态展示内容（免费实时接口不提供这些聚合数据）。
 */
@Singleton
class StockRepository @Inject constructor(
    private val dataSource: QuoteDataSource,
) {
    /** 热门股票池（fullCode），用于行情列表/热门。 */
    val stockPool: List<String> = listOf(
        "sh600519", "sz000858", "sh601318", "sh600036", "sz000001",
        "sz002594", "sz300750", "sh601899", "sh600030", "sz300059",
        "sh688981", "sz002230", "sh601012", "sh600276", "sz000333",
        "sh600887", "sh601166", "sz002415", "sh600900", "sh601988",
        "sz000725", "sh601857", "sh600028", "sh601390", "sh600585",
    )

    /** 常用指数（fullCode） */
    val indexCodes: List<String> = listOf(
        "sh000001", "sz399001", "sz399006", "sh000688", "bj899050",
    )

    suspend fun getRealtime(fullCodes: List<String>): List<Stock> {
        val map = dataSource.getRealtime(fullCodes)
        // 保持传入顺序
        return fullCodes.mapNotNull { map[it] }
    }

    suspend fun getStockPool(): List<Stock> = getRealtime(stockPool)

    suspend fun getStock(fullCode: String): Stock? = dataSource.getStock(fullCode)

    /**
     * 宽容解析股票：同时接受 `sh600519` / `SH600519` / `600519` 三种写法，
     * 用于下单页选择股票后自动填充代码与价格。
     */
    suspend fun getStockByAnyCode(input: String): Stock? {
        val t = input.trim().lowercase()
        if (t.isEmpty()) return null
        // 带市场前缀：sh600519 / sz000858 / bj899050
        if (t.length == 8 && (t.startsWith("sh") || t.startsWith("sz") || t.startsWith("bj"))) {
            return getStock(t)
        }
        // 6 位纯数字：推断市场后拉取
        if (t.length == 6 && t.all { it.isDigit() }) {
            return getRealtime(guessFullCodes(t)).firstOrNull()
        }
        return null
    }

    /**
     * 搜索股票（用于添加自选）。
     * 支持：
     * - 6 位纯数字代码：自动补 sh/sz 前缀后拉取实时行情验证。
     * - 带市场前缀的代码（sh600519）。
     * - 关键字：在热门股票池的实时数据中按名称/代码模糊匹配。
     */
    suspend fun searchStocks(keyword: String): List<Stock> {
        val kw = keyword.trim()
        if (kw.isEmpty()) return emptyList()

        // 纯数字 / 带前缀代码：直接按代码查询（找得到就不再走关键字）
        getStockByAnyCode(kw)?.let { return listOf(it) }

        // 关键字：在股票池实时数据中匹配名称或代码
        val pool = runCatching { getStockPool() }.getOrDefault(emptyList())
        return pool.filter {
            it.name.contains(kw, ignoreCase = true) || it.code.contains(kw)
        }
    }

    /** 6 位代码推断市场前缀：6/9 开头为沪市，0/2/3 开头为深市。 */
    private fun guessFullCodes(code6: String): List<String> = when (code6.firstOrNull()) {
        '6', '9' -> listOf("sh$code6")
        '0', '2', '3' -> listOf("sz$code6")
        '4', '8' -> listOf("bj$code6")
        else -> listOf("sh$code6", "sz$code6")
    }

    suspend fun getIndices(): List<MarketIndex> = dataSource.getIndices(indexCodes)

    suspend fun getDailyKLine(fullCode: String, count: Int = 120): List<KLineEntry> =
        dataSource.getDailyKLine(fullCode, count)

    suspend fun getTimeShare(fullCode: String): Pair<List<TimeShareEntry>, Double> =
        dataSource.getTimeSharePair(fullCode)

    suspend fun getOrderBook(fullCode: String): OrderBook = dataSource.getOrderBook(fullCode)

    /** 个股扩展行情（市值/市盈/量比/换手等），失败返回 null */
    suspend fun getQuoteExtra(fullCode: String): QuoteExtra? = dataSource.getQuoteExtra(fullCode)

    suspend fun getTopGainers(limit: Int = 30): List<Stock> =
        getStockPool().sortedByDescending { it.changePercent }.take(limit)

    suspend fun getTopLosers(limit: Int = 30): List<Stock> =
        getStockPool().sortedBy { it.changePercent }.take(limit)

    /** 板块热点（静态展示，免费接口无聚合数据） */
    fun getSectors(): List<Sector> = listOf(
        Sector("半导体", 4.32, "中芯国际"),
        Sector("龙虎榜", 0.0, ""),
        Sector("新能源车", 2.88, "比亚迪"),
        Sector("人工智能", 5.67, "科大讯飞"),
        Sector("医药", -0.76, "恒瑞医药"),
        Sector("银行", 0.45, "招商银行"),
        Sector("光伏", 3.21, "隆基绿能"),
        Sector("券商", 1.98, "东方财富"),
    )

    /** 资讯（静态展示） */
    fun getNews(): List<NewsItem> = listOf(
        NewsItem("1", "商务部公布中美贸易理事会和\"300亿对300亿\"对等降税框架有关情况", "同花顺7x24快讯", "2小时前", "全民关注"),
        NewsItem("2", "A股三大股指齐跌，算力硬件下挫，光模块龙头跌超8%", "中国证券报", "1小时前", null),
        NewsItem("3", "美股期货、黄金、白银集体跳水！伊朗称已为与美战事重开做好准备", "同花顺7x24快讯", "1小时前", null),
        NewsItem("4", "知名投资人段永平：买了点贵州茅台", "同花顺7x24快讯", "25分钟前", null),
        NewsItem("5", "中国工业企业利润前8月同比增长15.7%，电子行业利润同比增长1.1倍", "华尔街见闻", "3小时前", null),
        NewsItem("6", "金价破4200美元，黄金ETF资金转向，机构建议逢跌分批建仓", "21世纪经济报道", "1小时前", null),
    )
}
