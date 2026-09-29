package com.example.tonghuashun.ui.screen.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.ui.component.StockRow
import com.example.tonghuashun.util.QuoteFormat

private val categories = listOf("全球", "A股", "港股", "美股", "ETF", "期货", "黄金", "可转债", "其他")
private val topTabs = listOf("大盘", "板块", "个股")
private val rankTabs = listOf("涨幅榜", "跌幅榜")

private data class MarketEntry(val label: String, val icon: ImageVector, val color: Color)

private val marketEntries = listOf(
    MarketEntry("选股", Icons.Filled.FilterAlt, Color(0xFF2196F3)),
    MarketEntry("打新日历", Icons.Filled.CalendarMonth, Color(0xFFFF9800)),
    MarketEntry("ETF基金", Icons.Filled.ViewModule, THSRed),
    MarketEntry("同花顺热榜", Icons.Filled.EmojiEvents, Color(0xFFFF9800)),
    MarketEntry("大盘回顾", Icons.Filled.PlayCircle, THSRed),
)

@Composable
fun MarketScreen(
    onStockClick: (String) -> Unit,
    viewModel: MarketViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var category by remember { mutableIntStateOf(1) }
    var topTab by remember { mutableIntStateOf(0) }
    var rankTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray),
    ) {
        item { MarketTopBar() }
        item { CategoryRow(category) { category = it } }
        item { TopTabRow(topTab) { topTab = it } }
        item { OpenStatusRow() }
        item { IndexCardsRow(state.indices, onStockClick) }
        item { RiseFallBar() }
        item { Spacer(Modifier.height(10.dp)) }
        item { NoonReviewCard() }
        item { Spacer(Modifier.height(10.dp)) }
        item { MarketEntryRow() }
        item { Spacer(Modifier.height(10.dp)) }
        item { CompareCardsRow() }
        item { Spacer(Modifier.height(10.dp)) }
        item { RankHeader(rankTab) { rankTab = it } }
        val list = if (rankTab == 0) state.gainers else state.losers
        items(list, key = { it.fullCode }) { stock ->
            Box(modifier = Modifier.background(CardWhite)) {
                StockRow(stock = stock, onClick = { onStockClick(stock.fullCode) })
            }
            HorizontalDivider(color = DividerGray)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MarketTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("同花顺", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("资产分析，灵活控制仓位", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(16.dp).background(DividerGray))
            Text("搜索", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Filled.SmartToy, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun CategoryRow(selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(categories.size) { i ->
            Text(
                categories[i],
                color = if (i == selected) Color.White else Color(0xCCFFFFFF),
                fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (i == selected) 17.sp else 15.sp,
                modifier = Modifier.clickable { onSelect(i) },
            )
        }
    }
}

@Composable
private fun TopTabRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        topTabs.forEachIndexed { i, tab ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    tab,
                    color = if (i == selected) TextPrimary else TextSecondary,
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onSelect(i) },
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i == selected) THSRed else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun OpenStatusRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("开盘中", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text("2026-09-28 星期一", color = TextTertiary, fontSize = 11.sp)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("-925.99亿", color = StockDown, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text("大盘资金净流入", color = TextTertiary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun IndexCardsRow(indices: List<MarketIndex>, onStockClick: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.background(CardWhite),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(indices, key = { it.code }) { idx ->
            val color = QuoteFormat.colorOf(idx.change)
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    // 同花顺大盘卡片底色跟随涨跌（淡红/淡绿），而不是中性灰
                    .background(color.copy(alpha = 0.08f))
                    .clickable { onStockClick(idx.code) }
                    .padding(10.dp),
            ) {
                Text(idx.name, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Text(QuoteFormat.price(idx.point), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(QuoteFormat.signed(idx.change), color = color, fontSize = 12.sp)
                    Text(QuoteFormat.percent(idx.changePercent), color = color, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RiseFallBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("跌4800", color = StockDown, fontWeight = FontWeight.Bold)
            Text("涨703", color = StockUp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
        ) {
            Box(modifier = Modifier.weight(0.87f).height(6.dp).background(StockDown))
            Box(modifier = Modifier.weight(0.13f).height(6.dp).background(StockUp))
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("今日实时成交额 13823亿", color = TextSecondary, fontSize = 12.sp)
            Text("较上一日此时 +929亿", color = StockUp, fontSize = 12.sp)
        }
    }
}

@Composable
private fun NoonReviewCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "午盘点评",
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(THSRed)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("2026-09-28", color = TextTertiary, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("A股午评：创业板指低开低走大跌4.32%，CPO、元件、光纤...", color = TextPrimary, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
        Spacer(Modifier.height(6.dp))
        Text("A股三大指数早盘集体下跌，截至午盘，上证指数跌1.74%，深证成指跌...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
    }
}

@Composable
private fun MarketEntryRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(vertical = 14.dp),
    ) {
        marketEntries.forEach { entry ->
            Column(
                modifier = Modifier.weight(1f).clickable { },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(entry.color),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(entry.icon, contentDescription = entry.label, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(entry.label, fontSize = 11.sp, color = TextPrimary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun CompareCardsRow() {
    val cards = listOf(
        Triple("涨跌停对比", "27 : 60", StockDown),
        Triple("昨日涨停表现", "-1.65%", StockDown),
        Triple("大小盘对比", "-2.5% : -4.0%", StockDown),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cards.forEach { (title, value, color) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardWhite)
                    .clickable { }
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(value, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun RankHeader(selected: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("股票排行", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text("更多", color = TextTertiary, fontSize = 12.sp)
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            rankTabs.forEachIndexed { i, tab ->
                Text(
                    tab,
                    color = if (i == selected) THSRed else TextSecondary,
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i == selected) Color(0x22E63946) else Color.Transparent)
                        .clickable { onSelect(i) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        HorizontalDivider(color = DividerGray)
    }
}
