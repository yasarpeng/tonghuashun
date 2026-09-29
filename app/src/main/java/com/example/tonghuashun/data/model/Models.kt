package com.example.tonghuashun.data.model

/** 股票基本行情 */
data class Stock(
    val code: String,          // 股票代码，如 600519
    val name: String,          // 股票名称
    val price: Double,         // 当前价
    val prevClose: Double,     // 昨收
    val open: Double,          // 今开
    val high: Double,          // 最高
    val low: Double,           // 最低
    val volume: Long,          // 成交量（手）
    val turnover: Double,      // 成交额（元）
    val marketCap: Double = 0.0, // 总市值（元）
    val peRatio: Double = 0.0,   // 市盈率
    val market: String = "",     // sh / sz，用于拼接接口代码
    val trend: List<Double> = emptyList(), // 分时价格点，用于自选页迷你走势图
) {
    /** 涨跌额 */
    val change: Double get() = price - prevClose
    /** 涨跌幅（%） */
    val changePercent: Double get() = if (prevClose == 0.0) 0.0 else change / prevClose * 100.0
    /** 振幅（%） */
    val amplitude: Double get() = if (prevClose == 0.0) 0.0 else (high - low) / prevClose * 100.0
    /** 带市场前缀的完整代码，如 sh600519 */
    val fullCode: String get() = if (market.isNotEmpty()) "$market$code" else code
}

/** 大盘指数 */
data class MarketIndex(
    val code: String,
    val name: String,
    val point: Double,      // 当前点位
    val prevClose: Double,  // 昨收点位
) {
    val change: Double get() = point - prevClose
    val changePercent: Double get() = if (prevClose == 0.0) 0.0 else change / prevClose * 100.0
}

/**
 * 个股详情页扩展行情（来自腾讯 qt 接口）：
 * 市值 / 流通市值 / 市盈(TTM) / 市净率 / 量比 / 换手率 / 振幅。
 */
data class QuoteExtra(
    val turnoverRate: Double = 0.0,   // 换手率 %
    val peTtm: Double = 0.0,          // 市盈率(TTM)
    val pb: Double = 0.0,             // 市净率
    val amplitude: Double = 0.0,      // 振幅 %
    val volumeRatio: Double = 0.0,    // 量比
    val circulationCap: Double = 0.0, // 流通市值（元）
    val totalCap: Double = 0.0,       // 总市值（元）
)

/** 行业/概念板块 */
data class Sector(
    val name: String,
    val changePercent: Double,
    val leadingStock: String,   // 领涨股
)

/** K线数据（日K/分时通用的一根蜡烛） */
data class KLineEntry(
    val label: String,   // 日期或时间标签
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Long,
)

/** 分时点 */
data class TimeShareEntry(
    val time: String,       // HH:mm
    val price: Double,      // 价格
    val avgPrice: Double,   // 均价
    val volume: Long,       // 分钟成交量
)

/** 五档盘口一档 */
data class OrderBookLevel(
    val label: String,   // 买一/卖一 ...
    val price: Double,
    val volume: Long,    // 委托量（手）
)

/** 五档盘口 */
data class OrderBook(
    val asks: List<OrderBookLevel>,  // 卖五~卖一
    val bids: List<OrderBookLevel>,  // 买一~买五
)

/** 资讯 */
data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val time: String,
    val tag: String? = null,
)

/** 模拟交易 - 持仓 */
data class Position(
    val code: String,
    val name: String,
    val market: String,
    val shares: Long,          // 持股数
    val available: Long,       // 可卖数量
    val costPrice: Double,     // 成本价
    val currentPrice: Double,  // 现价（真实行情）
    val prevClose: Double = 0.0, // 昨收（算当日参考盈亏用）
) {
    val marketValue: Double get() = shares * currentPrice
    val profit: Double get() = (currentPrice - costPrice) * shares
    val profitPercent: Double get() = if (costPrice == 0.0) 0.0 else (currentPrice - costPrice) / costPrice * 100.0
    /** 当日参考盈亏 */
    val dayProfit: Double get() = if (prevClose == 0.0) 0.0 else (currentPrice - prevClose) * shares
    val dayProfitPercent: Double get() = if (prevClose == 0.0) 0.0 else (currentPrice - prevClose) / prevClose * 100.0
}

/** 模拟交易 - 账户 */
data class TradeAccount(
    val cash: Double,                 // 可用资金
    val positions: List<Position>,    // 持仓
) {
    val marketValue: Double get() = positions.sumOf { it.marketValue }
    val totalAsset: Double get() = cash + marketValue
    val totalProfit: Double get() = positions.sumOf { it.profit }
}

/** 交易方向：买入 / 卖出 */
enum class OrderSide(val label: String) {
    BUY("买入"),
    SELL("卖出"),
}

/** 委托状态 */
enum class OrderStatus(val label: String) {
    PENDING("已报"),      // 已提交，等待成交
    PARTIAL("部成"),      // 部分成交
    FILLED("已成"),       // 全部成交
    CANCELLED("已撤"),    // 已撤单
}

/**
 * 委托单（同花顺"当日委托"）。
 * price 为委托价，filledShares 为已成交数量。
 */
data class Order(
    val id: String,
    val fullCode: String,
    val code: String,
    val name: String,
    val side: OrderSide,
    val price: Double,          // 委托价
    val shares: Long,           // 委托数量（股）
    val filledShares: Long,     // 已成交数量（股）
    val status: OrderStatus,
    val time: String,           // 委托时间 HH:mm:ss
    val timestamp: Long,        // 排序用
) {
    val remainShares: Long get() = shares - filledShares
    val canCancel: Boolean get() = status == OrderStatus.PENDING || status == OrderStatus.PARTIAL
}

/**
 * 成交记录（同花顺"当日成交"）。
 */
data class TradeRecord(
    val id: String,
    val orderId: String,
    val fullCode: String,
    val code: String,
    val name: String,
    val side: OrderSide,
    val price: Double,          // 成交价
    val shares: Long,           // 成交数量（股）
    val time: String,           // 成交时间 HH:mm:ss
    val timestamp: Long,
) {
    val amount: Double get() = price * shares
}

/** 理财产品 */
data class FinanceProduct(
    val name: String,
    val desc: String,
    val yield7: Double,      // 7日年化 %
    val tag: String? = null,
)
