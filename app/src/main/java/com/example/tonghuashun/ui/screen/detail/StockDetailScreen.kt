package com.example.tonghuashun.ui.screen.detail

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.OrderBookLevel
import com.example.tonghuashun.data.model.QuoteExtra
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.ui.component.THSTabRow
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.THSLayout
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.util.QuoteFormat

/** 详情页下方功能标签：看点 / 资讯 / 盘口 / 资金 / 简况(F10) / 诊股 */
private val infoTabs = listOf("看点", "资讯", "盘口", "资金", "简况(F10)", "诊股")

private const val NO_DATA = "—"

@Composable
fun StockDetailScreen(
    onBack: () -> Unit,
    onOrder: (String, String) -> Unit,
    onNavigateStock: (String) -> Unit,
    viewModel: StockDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val period by viewModel.period.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val detailScroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var infoTop by remember { mutableIntStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var bookTab by remember { mutableIntStateOf(0) }   // 0 = 五档, 1 = 成交
    var infoTab by remember { mutableIntStateOf(1) }   // 默认"资讯"

    fun openInfo(index: Int) {
        infoTab = index
        scope.launch {
            withFrameNanos { }
            detailScroll.animateScrollTo(infoTop)
        }
    }

    Scaffold(
        containerColor = CardWhite,
        snackbarHost = { SnackbarHost(snackbar) },
        // 外层 Scaffold 已处理过状态栏内边距，这里清零避免顶格多出一截空白
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            state.stock?.let { stock ->
                ReferenceDetailToolbar(stock, state.referenceIndex, isFavorite,
                    onOrder = { onOrder(stock.fullCode, it) },
                    onFavorite = { viewModel.toggleFavorite() },
                    onDiagnosis = { openInfo(5) },
                    onInfo = ::openInfo,
                    onReference = {
                        val index = state.referenceIndex
                        if (index != null) onNavigateStock(if (index.code.startsWith("sh")) index.code else "sh${index.code}")
                        else openInfo(3)
                    })
            }
        },
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).background(CardWhite)) {
            ReferenceDetailHeader(state.stock, onBack,
                onPrevious = { viewModel.adjacentCode(-1)?.let(onNavigateStock) },
                onNext = { viewModel.adjacentCode(1)?.let(onNavigateStock) },
                onSearch = { showSearch = true })

            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = THSRed)
                }

                state.stock == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message ?: "加载失败", color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text("点击重试", color = THSRed, modifier = Modifier.clickable { viewModel.load() })
                    }
                }

                else -> {
                    val stock = state.stock!!
                    Column(modifier = Modifier.weight(1f).verticalScroll(detailScroll)) {
                        ReferenceQuoteHeader(stock, state.extra)
                        ReferenceQuoteContext(state, onProfile = { openInfo(4) })
                        ReferenceChartSection(state, period, onSelect = { viewModel.setPeriod(it) })
                        Spacer(Modifier.height(8.dp))
                        // 分时视图已把五档/成交并进图表右侧（对齐同花顺分时+盘口分栏），
                        // 这里只在 K 线周期下再单独展示一次五档/成交区。
                        if (period != ChartPeriod.TIME_SHARE) {
                            OrderBookSection(state, bookTab, onTab = { bookTab = it })
                            Spacer(Modifier.height(8.dp))
                        }
                        Box(Modifier.onGloballyPositioned { infoTop = it.positionInParent().y.toInt() }) {
                            InfoSection(state, infoTab, onTab = { infoTab = it })
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
    if (showSearch) ReferenceStockSearch(
        onDismiss = { showSearch = false }, search = { viewModel.searchStocks(it) },
        onOpen = { code -> showSearch = false; onNavigateStock(code) })

}

/* ------------------------------------------------------------------ 顶部栏 */

/* ------------------------------------------------------- 现价 + 九宫格指标 */

/* ----------------------------------------------------------- 图表 + 周期切换 */

/** 最近一根K线的均线值，图例里展示 */

@Composable
private fun EmptyChart(text: String) {
    Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
        Text(text, color = TextSecondary)
    }
}

/* --------------------------------------------------------------- 五档 / 成交 */

@Composable
private fun OrderBookSection(state: DetailUiState, tab: Int, onTab: (Int) -> Unit) {
    val book = state.orderBook
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrderBookTab("五档", tab == 0) { onTab(0) }
            Spacer(Modifier.width(14.dp))
            OrderBookTab("成交", tab == 1) { onTab(1) }
            Spacer(Modifier.weight(1f))
            Text("明细", color = TextSecondary, fontSize = 12.sp)
            Icon(
                Icons.Filled.KeyboardArrowRight, contentDescription = null,
                tint = TextTertiary, modifier = Modifier.size(14.dp),
            )
        }
        Spacer(Modifier.height(THSLayout.ItemGap))

        if (tab == 0) {
            if (book.asks.isEmpty() && book.bids.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                    Text("暂无盘口数据", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                // 卖盘量条从右向左生长、买盘从左向右，量条长度按最大委托量归一化
                val maxVol = (book.asks.maxOfOrNull { it.volume } ?: 1L)
                    .coerceAtLeast(book.bids.maxOfOrNull { it.volume } ?: 1L)
                    .coerceAtLeast(1L)
                book.asks.forEach { OrderBookRow(it, maxVol, isAsk = true, code = state.stock?.code.orEmpty()) }
                HorizontalDivider(color = DividerGray, modifier = Modifier.padding(vertical = 4.dp))
                book.bids.forEach { OrderBookRow(it, maxVol, isAsk = false, code = state.stock?.code.orEmpty()) }
            }
        } else {
            val recent = state.timeShare.takeLast(12).reversed()
            if (recent.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                    Text("暂无成交明细", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                var prev = state.prevClose
                recent.forEach { e ->
                    val c = if (e.price >= prev) StockUp else StockDown
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(e.time, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(
                            detailPrice(state.stock?.code.orEmpty(), e.price), color = c, fontSize = 13.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                        )
                        Text(
                            if (e.volume >= 1000) "%.2f万".format(e.volume / 10000.0) else "${e.volume}",
                            color = TextSecondary, fontSize = 12.sp,
                            textAlign = TextAlign.End, modifier = Modifier.weight(1f),
                        )
                    }
                    prev = e.price
                }
            }
        }
    }
}

@Composable
private fun OrderBookTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) TextPrimary else TextSecondary,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 14.sp,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** 单档盘口：背景量条 + 标签/价格/委托量 */
@Composable
private fun OrderBookRow(level: OrderBookLevel, maxVolume: Long, isAsk: Boolean, code: String = "") {
    val color = if (isAsk) StockUp else StockDown
    val fraction = (level.volume.toFloat() / maxVolume.toFloat()).coerceIn(0.02f, 1f)
    Box(modifier = Modifier.fillMaxWidth().height(26.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                // 与同花顺一致：量条统一贴右，长度表示委托量占比，不遮挡左侧档位文字
                .align(Alignment.CenterEnd)
                .background(color.copy(alpha = 0.14f)),
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(level.label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(38.dp))
            Text(
                detailPrice(code, level.price), color = color, fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Text(formatHands(level.volume), color = TextSecondary, fontSize = 12.sp)
        }
    }
}

private fun formatHands(hands: Long): String =
    if (hands >= 1000) "%.2f万".format(hands / 10000.0) else hands.toString()
/* ---------------------- 分时右侧：五档盘口（卖5~卖1 / 买1~买5） ---------------------- */

/* ---------------------- 分时右侧：成交明细（时间 / 价格 / 量） ---------------------- */

/** 右侧窄栏用的量格式：优先「万」，否则原值 */

@Composable
private fun InfoSection(state: DetailUiState, tab: Int, onTab: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        THSTabRow(tabs = infoTabs, selected = tab, onSelect = onTab, scrollable = true)
        when (tab) {
            0, 1 -> NewsList(state.news)
            2 -> PanelBody { OrderBookPanel(state.orderBook, state.stock?.code.orEmpty()) }
            3 -> PanelBody { CapitalPanel(state.stock, state.extra) }
            4 -> PanelBody { ProfilePanel(state.stock, state.extra) }
            else -> PanelBody { DiagnosePanel(state.stock, state.extra) }
        }
    }
}

@Composable
private fun PanelBody(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter, vertical = 12.dp)) { content() }
}

@Composable
private fun NewsList(news: List<NewsItem>) {
    if (news.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
            Text("暂无资讯", color = TextSecondary, fontSize = 13.sp)
        }
        return
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        news.forEach { item ->
            Column(modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
                Text(
                    item.title, color = TextPrimary, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, maxLines = 3, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(THSLayout.ItemGap))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.tag?.let {
                        Text(
                            it, color = THSRed, fontSize = 10.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0x14E63946))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(item.source, color = TextTertiary, fontSize = 11.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(item.time, color = TextTertiary, fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = DividerGray)
        }
    }
}

@Composable
private fun OrderBookPanel(book: OrderBook, code: String) {
    if (book.asks.isEmpty() && book.bids.isEmpty()) {
        Text("暂无盘口数据", color = TextSecondary, fontSize = 13.sp)
        return
    }
    val askVol = book.asks.sumOf { it.volume }
    val bidVol = book.bids.sumOf { it.volume }
    val total = (askVol + bidVol).coerceAtLeast(1L)
    val ratio = (bidVol - askVol).toDouble() / total * 100.0
    KeyValueGrid(
        listOf(
            "委买" to "${bidVol}手",
            "委卖" to "${askVol}手",
            "委差" to "${bidVol - askVol}手",
            "委比" to QuoteFormat.percent(ratio),
            "卖一" to (book.asks.lastOrNull()?.let { detailPrice(code, it.price) } ?: NO_DATA),
            "买一" to (book.bids.firstOrNull()?.let { detailPrice(code, it.price) } ?: NO_DATA),
        ),
    )
}

@Composable
private fun CapitalPanel(stock: Stock?, extra: QuoteExtra?) {
    if (stock == null) {
        Text("暂无资金数据", color = TextSecondary, fontSize = 13.sp)
        return
    }
    KeyValueGrid(
        listOf(
            "成交额" to QuoteFormat.bigNumber(stock.turnover),
            "成交量" to QuoteFormat.volume(stock.volume),
            "换手率" to (extra?.turnoverRate?.takeIf { it > 0 }?.let { QuoteFormat.percent(it).removePrefix("+") } ?: NO_DATA),
            "量比" to (extra?.volumeRatio?.takeIf { it > 0 }?.let { "%.2f".format(it) } ?: NO_DATA),
            "振幅" to QuoteFormat.percent(stock.amplitude).removePrefix("+"),
            "市净率" to (extra?.pb?.takeIf { it > 0 }?.let { "%.2f".format(it) } ?: NO_DATA),
        ),
    )
}

@Composable
private fun ProfilePanel(stock: Stock?, extra: QuoteExtra?) {
    if (stock == null) {
        Text("暂无资料", color = TextSecondary, fontSize = 13.sp)
        return
    }
    KeyValueGrid(
        listOf(
            "代码" to stock.fullCode,
            "名称" to stock.name,
            "最高" to detailPrice(stock.code, stock.high),
            "最低" to detailPrice(stock.code, stock.low),
            "今开" to detailPrice(stock.code, stock.open),
            "昨收" to detailPrice(stock.code, stock.prevClose),
            "总市值" to (extra?.totalCap?.takeIf { it > 0 }?.let { QuoteFormat.bigNumber(it) } ?: NO_DATA),
            "流通市值" to (extra?.circulationCap?.takeIf { it > 0 }?.let { QuoteFormat.bigNumber(it) } ?: NO_DATA),
            "市盈(TTM)" to (extra?.peTtm?.takeIf { it > 0 }?.let { "%.2f".format(it) } ?: NO_DATA),
        ),
    )
}

/**
 * 简版"诊股"：只基于真实行情做四个维度的强弱判断，不编造预测结论。
 */
@Composable
private fun DiagnosePanel(stock: Stock?, extra: QuoteExtra?) {
    if (stock == null) {
        Text("暂无诊断数据", color = TextSecondary, fontSize = 13.sp)
        return
    }
    val changeScore = (stock.changePercent / 5.0).coerceIn(-1.0, 1.0)
    val ampScore = (stock.amplitude / 8.0).coerceIn(0.0, 1.0)
    val volScore = (extra?.volumeRatio?.takeIf { it > 0 }?.let { ((it - 1.0) / 2.0).coerceIn(-1.0, 1.0) } ?: 0.0)
    val turnScore = (extra?.turnoverRate?.takeIf { it > 0 }?.let { (it / 5.0).coerceIn(0.0, 1.0) } ?: 0.0)

    Column {
        ScoreBar("涨跌强度", changeScore.toFloat(), color = QuoteFormat.colorOf(stock.change))
        ScoreBar("波动幅度", ampScore.toFloat(), color = StockUp)
        ScoreBar("量比活跃", volScore.toFloat(), color = THSRed)
        ScoreBar("换手活跃", turnScore.toFloat(), color = StockUp)
        Spacer(Modifier.height(THSLayout.ItemGap))
        Text(
            "以上指标由当前行情实时计算，仅供参考，不构成投资建议。",
            color = TextTertiary, fontSize = 11.sp,
        )
    }
}

@Composable
private fun ScoreBar(label: String, value: Float, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(64.dp))
        Box(
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(BackgroundGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(((value + 1f) / 2f).coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .background(color),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("%.2f".format(value), color = TextPrimary, fontSize = 12.sp)
    }
}

@Composable
private fun KeyValueGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(THSLayout.ItemGap)) {
        items.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, color = TextTertiary, fontSize = 11.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/* ------------------------------------------------------------------ 底部操作 */
