package com.example.tonghuashun.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.IndeterminateCheckBox
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tonghuashun.data.model.*
import com.example.tonghuashun.ui.screen.detail.chart.CandleChart
import com.example.tonghuashun.ui.screen.detail.chart.ReferenceTimeShareChart
import com.example.tonghuashun.ui.theme.*
import com.example.tonghuashun.util.QuoteFormat
import java.util.Locale

internal val QuoteRed = Color(0xFFCE3942)
internal val QuoteGreen = Color(0xFF098E0F)
private val QuoteMuted = Color(0xFF999999)
private val QuotePurple = Color(0xFF86108C)
private fun quoteColor(value: Double) = if (value > 0) QuoteRed else if (value < 0) QuoteGreen else TextPrimary
internal fun quoteVolume(value: Long): String = if (value >= 10_000) String.format(Locale.CHINA, "%.2f万", value / 10000.0) else value.toString()

@Composable
internal fun ReferenceDetailHeader(stock: Stock?, onBack: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit, onSearch: () -> Unit) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    BoxWithConstraints(Modifier.fillMaxWidth().background(TradeHeaderRed).statusBarsPadding().heightIn(min = 58.dp)) {
        val maxTitleWidth = (maxWidth - 208.dp).coerceAtLeast(100.dp)
        Box(Modifier.size(48.dp).align(Alignment.CenterStart).clickable(onClick = onBack), Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "返回", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Row(Modifier.widthIn(max = (maxWidth - 160.dp).coerceAtLeast(120.dp)).align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp, 48.dp).clickable(onClick = onPrevious), Alignment.Center) {
                Icon(Icons.Filled.ArrowLeft, "上一只", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.widthIn(max = maxTitleWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stock?.name ?: "加载中…", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stock?.code.orEmpty(), color = Color.White, fontSize = 12.sp, maxLines = 1)
                    if (!largeText && stock != null) {
                        if (isFundQuote(stock.code)) Text("ETF", color = Color.White, fontSize = 9.sp,
                            modifier = Modifier.background(Color(0xFFD99C4E), RoundedCornerShape(1.dp)).padding(horizontal = 3.dp))
                        Text("L1", color = Color.White, fontSize = 9.sp,
                            modifier = Modifier.background(Color(0xFFD99C4E), RoundedCornerShape(1.dp)).padding(horizontal = 3.dp))
                    }
                }
            }
            Box(Modifier.size(24.dp, 48.dp).clickable(onClick = onNext), Alignment.Center) {
                Icon(Icons.Filled.ArrowRight, "下一只", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SmartToy, "行情助手", tint = Color(0xFFE7EEFF),
                modifier = Modifier.size(24.dp).background(Color(0xFF1754B7), RoundedCornerShape(8.dp)).padding(2.dp))
            Box(Modifier.size(48.dp).clickable(onClick = onSearch), Alignment.Center) {
                Icon(Icons.Filled.Search, "搜索股票", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
internal fun QuoteFittingValue(value: String, modifier: Modifier, color: Color = TextPrimary, maxSize: Float = 15f, alignment: TextAlign = TextAlign.End) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = TextStyle(fontSize = maxSize.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum")
    BoxWithConstraints(modifier) {
        val width = measurer.measure(value, style, maxLines = 1).size.width.coerceAtLeast(1)
        val ratio = (with(density) { maxWidth.toPx() } / width).coerceAtMost(1f)
        Text(value, color = color, style = style.copy(fontSize = (maxSize * ratio).coerceAtLeast(9f).sp),
            textAlign = alignment, maxLines = 1, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
internal fun ReferenceQuoteHeader(stock: Stock, extra: QuoteExtra?) {
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val color = quoteColor(stock.change)
    BoxWithConstraints(Modifier.fillMaxWidth().background(CardWhite)) {
        Row(Modifier.horizontalScroll(rememberScrollState()).width(maxOf(maxWidth, 360.dp * scale))
            .padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                QuoteFittingValue(detailPrice(stock.code, stock.price), Modifier.fillMaxWidth(), color, 32f, TextAlign.Start)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(detailPrice(stock.code, stock.change), color = color, fontSize = 10.sp, maxLines = 1)
                    Text(String.format(Locale.CHINA, "%.2f%%", stock.changePercent), color = color, fontSize = 10.sp, maxLines = 1)
                }
            }
            QuoteMetricColumn(listOf("高" to detailPrice(stock.code, stock.high), "低" to detailPrice(stock.code, stock.low), "开" to detailPrice(stock.code, stock.open)),
                Modifier.weight(1.05f), listOf(quoteColor(stock.high - stock.prevClose), quoteColor(stock.low - stock.prevClose), quoteColor(stock.open - stock.prevClose)))
            val marketCap = extra?.totalCap?.takeIf { it > 0 } ?: stock.marketCap.takeIf { it > 0 }
            QuoteMetricColumn(listOf("市值" to (marketCap?.let { QuoteFormat.bigNumber(it) } ?: "—"),
                (if (isFundQuote(stock.code)) "溢价" else "流通") to (if (isFundQuote(stock.code)) "—" else extra?.circulationCap?.takeIf { it > 0 }?.let { QuoteFormat.bigNumber(it) } ?: "—"),
                "振幅" to String.format(Locale.CHINA, "%.2f%%", stock.amplitude)), Modifier.weight(1.1f))
            QuoteMetricColumn(listOf("量" to quoteVolume(stock.volume),
                "换" to (extra?.turnoverRate?.let { String.format(Locale.CHINA, "%.2f%%", it) } ?: "—"),
                "额" to QuoteFormat.bigNumber(stock.turnover)), Modifier.weight(1.05f))
        }
    }
}

@Composable
private fun QuoteMetricColumn(items: List<Pair<String, String>>, modifier: Modifier, colors: List<Color> = emptyList()) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEachIndexed { index, (label, value) ->
            Row(Modifier.fillMaxWidth().heightIn(min = 20.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, color = Color(0xFF777777), fontSize = 13.sp, maxLines = 1)
                QuoteFittingValue(value, Modifier.weight(1f), colors.getOrElse(index) { TextPrimary }, 15f)
            }
        }
    }
}

@Composable
internal fun ReferenceQuoteContext(state: DetailUiState, onProfile: () -> Unit) {
    val stock = state.stock ?: return
    val fund = isFundQuote(stock.code)
    var showNews by remember(stock.code) { mutableStateOf(true) }
    Column(Modifier.fillMaxWidth().background(CardWhite)) {
        HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 8.dp))
        if (fund) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onProfile).padding(horizontal = 10.dp).heightIn(min = 32.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ETF概览", color = Color(0xFF7283AA), fontSize = 10.sp,
                    modifier = Modifier.border(0.5.dp, Color(0xFFAAB4CD), RoundedCornerShape(2.dp)).padding(horizontal = 3.dp))
                Text(stock.name, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("实时行情", color = TextSecondary, fontSize = 11.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = QuoteMuted, modifier = Modifier.size(16.dp))
            }
            HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 10.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp).heightIn(min = 32.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("跟踪指数：${state.referenceIndex?.name ?: if (stock.code == "510980") "上证指数" else "—"}", color = TextPrimary, fontSize = 12.sp)
                Text(state.referenceIndex?.let { String.format(Locale.CHINA, "%.2f", it.point) } ?: "—", color = QuoteRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(state.referenceIndex?.let { String.format(Locale.CHINA, "%.2f%%", it.changePercent) } ?: "—", color = state.referenceIndex?.let { quoteColor(it.change) } ?: QuoteMuted, fontSize = 12.sp)
            }
        }
        if (showNews && state.news.isNotEmpty()) {
            HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 10.dp))
            Row(Modifier.fillMaxWidth().heightIn(min = 38.dp).padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("行情\n速递", color = QuoteRed, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 11.sp)
                Text(state.news.first().title, color = TextPrimary, fontSize = 13.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                IconButton(onClick = { showNews = false }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Close, "关闭快讯", tint = QuoteMuted, modifier = Modifier.size(16.dp)) }
            }
        }
        HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).heightIn(min = 36.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("最新盘口", color = TextPrimary, fontSize = 12.sp)
            Text(detailPrice(stock.code, stock.price), color = quoteColor(stock.change), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("量 ${quoteVolume(stock.volume)}", color = TextSecondary, fontSize = 11.sp)
            Text("买一 ${state.orderBook.bids.firstOrNull()?.let { quoteVolume(it.volume) } ?: "—"}", color = QuoteRed, fontSize = 11.sp)
            Text("卖一 ${state.orderBook.asks.lastOrNull()?.let { quoteVolume(it.volume) } ?: "—"}", color = QuoteGreen, fontSize = 11.sp)
        }
        Box(Modifier.fillMaxWidth().height(4.dp).background(TradePageBackground))
    }
}

@Composable
internal fun ReferenceChartSection(state: DetailUiState, period: ChartPeriod, onSelect: (ChartPeriod) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(CardWhite)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 40.dp).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            ChartPeriod.entries.forEach { candidate ->
                Column(Modifier.clickable { onSelect(candidate) }.padding(horizontal = 12.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(candidate.label, fontSize = 16.sp, color = if (candidate == period) TextPrimary else QuoteLabelColor,
                        fontWeight = if (candidate == period) FontWeight.Bold else FontWeight.Normal)
                    Box(Modifier.padding(top = 3.dp).width(12.dp).height(3.dp).background(if (candidate == period) QuoteRed else Color.Transparent, RoundedCornerShape(2.dp)))
                }
            }
            Text("五日", color = Color(0xFFBBBBBB), fontSize = 16.sp, modifier = Modifier.padding(horizontal = 12.dp).semantics { disabled() })
            Box {
                Text("更多▾", color = QuoteLabelColor, fontSize = 15.sp, modifier = Modifier.clickable { menu = true }.padding(horizontal = 12.dp, vertical = 8.dp))
                DropdownMenu(menu, onDismissRequest = { menu = false }) {
                    ChartPeriod.entries.forEach { candidate -> DropdownMenuItem(text = { Text(candidate.label) }, onClick = { menu = false; onSelect(candidate) }) }
                }
            }
            Icon(Icons.Filled.Settings, "图表设置", tint = QuoteMuted, modifier = Modifier.size(20.dp).clickable { menu = true })
        }
        HorizontalDivider(color = DividerGray)
        if (period == ChartPeriod.TIME_SHARE) {
            ReferenceChartMeta(state)
            val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val graphWidth = maxOf(maxWidth, 360.dp * scale)
                val stock = state.stock
                val side = maxOf(graphWidth * 0.28f, (if ((stock?.price ?: 0.0) >= 100) 140.dp else 112.dp) * scale)
                Row(Modifier.horizontalScroll(rememberScrollState()).width(graphWidth)) {
                    ReferenceTimeShareChart(state.timeShare, state.prevClose, stock?.code.orEmpty(), Modifier.width(graphWidth - side))
                    Column(Modifier.width(side).border(0.5.dp, DividerGray)) {
                        ReferenceDepthPanel(state)
                        ReferenceMinuteDetails(state)
                    }
                }
            }
        } else {
            val code = state.stock?.code.orEmpty()
            val entries = state.klineOf(period)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(5 to MA5Color, 10 to MA10Color, 20 to MA20Color).forEach { (window, color) ->
                    val ma = if (entries.size >= window) detailPrice(code, entries.takeLast(window).map { it.close }.average()) else "—"
                    Text("MA$window $ma", color = color, fontSize = 11.sp)
                }
            }
            if (entries.isEmpty()) Box(Modifier.fillMaxWidth().height(300.dp), Alignment.Center) { Text("暂无K线数据", color = TextSecondary) }
            else CandleChart(entries)
        }
    }
}

@Composable
private fun ReferenceChartMeta(state: DetailUiState) {
    val stock = state.stock ?: return
    Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("均价:${state.timeShare.lastOrNull()?.let { detailPrice(stock.code, it.avgPrice) } ?: "—"}", color = MA5Color, fontSize = 10.sp)
            Text("最新:${detailPrice(stock.code, stock.price)}", color = TextPrimary, fontSize = 10.sp)
            Text(detailPrice(stock.code, stock.change), color = quoteColor(stock.change), fontSize = 10.sp)
            Text(String.format(Locale.CHINA, "%.2f%%", stock.changePercent), color = quoteColor(stock.change), fontSize = 10.sp)
            if (isFundQuote(stock.code)) Text("实时参考净值（IOPV）：—", color = QuotePurple, fontSize = 10.sp)
        }
        if (isFundQuote(stock.code)) Text("溢价率：—", color = QuoteRed, fontSize = 10.sp)
    }
}

@Composable
private fun ReferenceDepthPanel(state: DetailUiState) {
    val stock = state.stock ?: return
    val book = state.orderBook
    val maxVolume = (book.asks + book.bids).maxOfOrNull { it.volume }?.coerceAtLeast(1) ?: 1L
    Column {
        (5 downTo 1).forEach { n -> ReferenceDepthRow("卖$n", book.asks.firstOrNull { it.label == "卖$n" }, stock, maxVolume, true) }
        val buy = book.bids.sumOf { it.volume }.toFloat()
        val sell = book.asks.sumOf { it.volume }.toFloat()
        val ratio = if (buy + sell > 0) (buy / (buy + sell)).coerceIn(0.01f, 0.99f) else 0.5f
        Row(Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 3.dp).height(3.dp)) {
            Box(Modifier.weight(ratio).fillMaxHeight().background(QuoteRed))
            Box(Modifier.weight(1f - ratio).fillMaxHeight().background(QuoteGreen))
        }
        (1..5).forEach { n -> ReferenceDepthRow("买$n", book.bids.firstOrNull { it.label == "买$n" }, stock, maxVolume, false) }
    }
}

@Composable
private fun ReferenceDepthRow(label: String, level: OrderBookLevel?, stock: Stock, maxVolume: Long, ask: Boolean) {
    Row(Modifier.fillMaxWidth().heightIn(min = 28.dp).padding(horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = QuoteLabelColor, fontSize = 14.sp, maxLines = 1)
        QuoteFittingValue(level?.let { detailPrice(stock.code, it.price) } ?: "—", Modifier.weight(1f),
            level?.let { quoteColor(it.price - stock.prevClose) } ?: QuoteMuted, 14f)
        Box(Modifier.weight(0.95f), contentAlignment = Alignment.CenterEnd) {
            if (level != null) Box(Modifier.fillMaxWidth((level.volume.toFloat() / maxVolume).coerceIn(0.02f, 1f))
                .height(23.dp).align(Alignment.CenterEnd).background((if (ask) QuoteGreen else QuoteRed).copy(alpha = 0.17f)))
            QuoteFittingValue(level?.let { quoteVolume(it.volume) } ?: "—", Modifier.fillMaxWidth(), TextPrimary, 14f)
        }
    }
}

@Composable
private fun ReferenceMinuteDetails(state: DetailUiState) {
    val stock = state.stock ?: return
    Text("分时明细 ▴", color = QuoteBodyColor, fontSize = 12.sp, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().background(TradePageBackground).padding(vertical = 4.dp))
    val entries = state.timeShare.filter { it.volume > 0 }.takeLast(10)
    if (entries.isEmpty()) Box(Modifier.fillMaxWidth().height(96.dp), Alignment.Center) { Text("暂无明细", color = QuoteMuted, fontSize = 11.sp) }
    entries.forEachIndexed { index, entry ->
        val previous = entries.getOrNull(index - 1)?.price ?: state.prevClose
        Row(Modifier.fillMaxWidth().heightIn(min = 24.dp).padding(horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            QuoteFittingValue(entry.time, Modifier.weight(1f), TextPrimary, 12f, TextAlign.Start)
            QuoteFittingValue(detailPrice(stock.code, entry.price), Modifier.weight(1.1f), quoteColor(entry.price - previous), 12f)
            QuoteFittingValue(quoteVolume(entry.volume), Modifier.weight(1f), quoteColor(entry.price - previous), 12f)
        }
    }
}

@Composable
internal fun ReferenceDetailToolbar(stock: Stock, reference: MarketIndex?, favorite: Boolean,
    onOrder: (String) -> Unit, onFavorite: () -> Unit, onDiagnosis: () -> Unit, onInfo: (Int) -> Unit, onReference: () -> Unit) {
    var orderMenu by remember { mutableStateOf(false) }
    var functionMenu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().background(Color(0xFF252525)).navigationBarsPadding().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1.25f).clickable(onClick = onReference).heightIn(min = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("▴", color = Color.LightGray, fontSize = 10.sp)
            Text(reference?.name ?: "行情", color = Color(0xFFAAAAAA), fontSize = 10.sp, maxLines = 1)
            Text(reference?.let { String.format(Locale.CHINA, "%.2f", it.point) } ?: detailPrice(stock.code, stock.price),
                color = quoteColor(reference?.change ?: stock.change), fontSize = 12.sp, maxLines = 1)
        }
        Box(Modifier.weight(1f)) {
            ReferenceToolbarButton("下单", Icons.Outlined.NoteAlt) { orderMenu = true }
            DropdownMenu(orderMenu, onDismissRequest = { orderMenu = false }) {
                DropdownMenuItem(text = { Text("买入") }, onClick = { orderMenu = false; onOrder("buy") })
                DropdownMenuItem(text = { Text("卖出") }, onClick = { orderMenu = false; onOrder("sell") })
            }
        }
        Box(Modifier.weight(1f)) { ReferenceToolbarButton("诊股", Icons.Outlined.AddBox, onDiagnosis) }
        Box(Modifier.weight(1f)) { ReferenceToolbarButton(if (favorite) "删自选" else "加自选", if (favorite) Icons.Outlined.IndeterminateCheckBox else Icons.Outlined.AddBox, onFavorite) }
        Box(Modifier.weight(1f)) {
            ReferenceToolbarButton("功能", Icons.Filled.Menu) { functionMenu = true }
            DropdownMenu(functionMenu, onDismissRequest = { functionMenu = false }) {
                listOf("资讯" to 1, "盘口" to 2, "资金" to 3, "简况" to 4).forEach { (name, index) ->
                    DropdownMenuItem(text = { Text(name) }, onClick = { functionMenu = false; onInfo(index) })
                }
            }
        }
    }
}

@Composable
private fun ReferenceToolbarButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(icon, null, tint = Color(0xFFDDDDDD), modifier = Modifier.size(24.dp))
        Text(label, color = Color(0xFFDDDDDD), fontSize = 11.sp)
    }
}

// Detail-specific aliases keep subdued chart labels separate from the darker quote numbers.
private val QuoteLabelColor = Color(0xFF888888)
private val QuoteBodyColor = Color(0xFF666666)
