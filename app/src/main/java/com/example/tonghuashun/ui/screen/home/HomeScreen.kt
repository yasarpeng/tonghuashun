package com.example.tonghuashun.ui.screen.home

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.ui.component.THSTabRow
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.THSLayout
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.util.QuoteFormat

private data class HomeGridItem(val label: String, val icon: com.example.tonghuashun.ui.component.THSIcon)

private val homeGridItems = listOf(
    HomeGridItem("股票", com.example.tonghuashun.ui.component.THSIcon.STOCK),
    HomeGridItem("龙虎榜", com.example.tonghuashun.ui.component.THSIcon.DRAGON),
    HomeGridItem("商品(期货)", com.example.tonghuashun.ui.component.THSIcon.FUTURES),
    HomeGridItem("决策", com.example.tonghuashun.ui.component.THSIcon.DECISION),
    HomeGridItem("自选分析", com.example.tonghuashun.ui.component.THSIcon.ANALYSIS),
    HomeGridItem("ETF首页", com.example.tonghuashun.ui.component.THSIcon.ETF_HOME),
    HomeGridItem("条件选股", com.example.tonghuashun.ui.component.THSIcon.FILTER),
    HomeGridItem("沪深港通", com.example.tonghuashun.ui.component.THSIcon.HK_CONNECT),
    HomeGridItem("打新日历", com.example.tonghuashun.ui.component.THSIcon.CALENDAR),
    HomeGridItem("应用商店", com.example.tonghuashun.ui.component.THSIcon.APPS),
)

private val newsTabs = listOf("关注", "推荐", "热榜", "咨询")

@Composable
fun HomeScreen(
    onStockClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var newsTab by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray),
    ) {
        item { HomeTopBar() }
        item { FunctionGrid() }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { HorizontalCards() }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { RealtimeHotCard() }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item {
            TodayPlayCard(
                hotStocks = state.hotStocks,
                upCount = state.upCount,
                downCount = state.downCount,
                limitUpCount = state.limitUpCount,
            )
        }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { NewsTabBar(newsTab) { newsTab = it } }
        items(state.news, key = { it.id }) { news ->
            HomeNewsRow(news)
        }
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed)
            .heightIn(min = THSLayout.HeaderHeight)
            .padding(horizontal = THSLayout.PageGutter, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(THSLayout.ControlRadius))
                .background(Color(0x33FFFFFF))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("去签到", color = Color.White, fontSize = 10.sp)
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .height(THSLayout.SearchHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = THSLayout.PageGutter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Box(Modifier.width(1.dp).height(16.dp).background(DividerGray))
            Text("5g行业股东增持战法", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Search, contentDescription = "搜索", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Icon(Icons.Filled.SmartToy, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(24.dp))
        Icon(Icons.Filled.Email, contentDescription = "消息", tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun FunctionGrid() {
    com.example.tonghuashun.ui.component.THSFunctionGrid(homeGridItems) { item ->
        com.example.tonghuashun.ui.component.THSFuncIcon(item.icon, size = 44.dp, corner = 12.dp)
        Text(item.label, fontSize = 11.sp, color = TextPrimary, maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun HorizontalCards() {
    val cards = listOf(
        Triple("自选股概览", "重点关注·浙...", "+0.39%"),
        Triple("学投资播客", "宇树们不想重蹈新造车的...", "百家讲堂"),
        Triple("投资必读", "A股午评：开低走大...", "A股午评"),
    )
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = THSLayout.PageGutter),
        horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
    ) {
        items(cards) { (title, sub, tail) ->
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardWhite)
                    .clickable { }
                    .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(THSLayout.ItemGap))
                Text(sub, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Spacer(Modifier.height(THSLayout.ItemGap))
                Text(tail, color = StockUp, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RealtimeHotCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = THSRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("实时热点", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(THSLayout.ItemGap))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("英伟达加码玻璃基板", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(6.dp))
            Text(
                "热",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFF9800))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun TodayPlayCard(
    hotStocks: List<Stock>,
    upCount: Int,
    downCount: Int,
    limitUpCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("今天炒什么", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(6.dp))
                Text("2026-09-28", color = TextTertiary, fontSize = 11.sp)
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(THSLayout.ItemGap))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "1",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(THSRed)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("人形机器人", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(6.dp))
            Text("-2.83%", color = com.example.tonghuashun.ui.theme.StockDown, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(THSLayout.ItemGap))
        Text("特斯拉近几个月已将其Optimus人形机器人的产量提升...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        Spacer(Modifier.height(THSLayout.ItemGap))
        // 左右双栏：相关个股 | 涨跌分布（对齐同花顺首页"今天炒什么"卡片）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(THSLayout.CardRadius))
                    .background(BackgroundGray)
                    .padding(10.dp),
            ) {
                Text("相关个股", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(THSLayout.ItemGap))
                hotStocks.take(3).forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(s.name, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            QuoteFormat.percent(s.changePercent),
                            color = QuoteFormat.colorOf(s.change),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(THSLayout.CardRadius))
                    .background(BackgroundGray)
                    .padding(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("涨跌分布", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${limitUpCount}家涨停",
                        color = THSRed,
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFFFE1E1))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(2.dp)),
                ) {
                    // weight 必须 > 0，家数为 0 时给一个极小权重占位
                    val upW = upCount.coerceAtLeast(0).toFloat().coerceAtLeast(0.02f)
                    val downW = downCount.coerceAtLeast(0).toFloat().coerceAtLeast(0.02f)
                    Box(Modifier.weight(upW).height(10.dp).background(StockUp))
                    Box(Modifier.weight(downW).height(10.dp).background(com.example.tonghuashun.ui.theme.StockDown))
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("上涨 $upCount", color = StockUp, fontSize = 11.sp)
                    Text("下跌 $downCount", color = com.example.tonghuashun.ui.theme.StockDown, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun NewsTabBar(selected: Int, onSelect: (Int) -> Unit) {
    THSTabRow(tabs = newsTabs, selected = selected, onSelect = onSelect)
}

@Composable
private fun HomeNewsRow(news: NewsItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .clickable { }
            .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(BackgroundGray),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Extension, contentDescription = null, tint = THSRed, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(news.source, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(6.dp))
            Text(news.time, color = TextTertiary, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Text("+ 关注", color = THSRed, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
            news.tag?.let {
                Text(
                    it,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(THSRed)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(news.title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(THSLayout.ItemGap))
        androidx.compose.material3.HorizontalDivider(color = DividerGray)
    }
}
