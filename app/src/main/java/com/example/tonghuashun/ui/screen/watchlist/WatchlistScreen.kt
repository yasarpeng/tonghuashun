package com.example.tonghuashun.ui.screen.watchlist

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.MarketIndex
import com.example.tonghuashun.ui.component.WatchStockRow
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.util.QuoteFormat

private val watchTabs = listOf("自选股", "持仓股", "ETF", "持有股份")

@Composable
fun WatchlistScreen(
    onStockClick: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val indices by viewModel.indices.collectAsStateWithLifecycle()
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray),
    ) {
        item { WatchTopBar(onSearch = { showSearch = true }) }
        item { IndexBar(indices) }
        item { WatchTabRow(tab) { tab = it } }
        item { WatchListHeader() }
        items(stocks, key = { it.fullCode }) { stock ->
            Box(modifier = Modifier.background(CardWhite)) {
                WatchStockRow(stock = stock, onClick = { onStockClick(stock.fullCode) })
            }
            HorizontalDivider(color = DividerGray)
        }
        item { AddStockRow(onClick = { showSearch = true }) }
        item { Spacer(Modifier.height(24.dp)) }
    }

    if (showSearch) {
        SearchAddDialog(
            viewModel = viewModel,
            onDismiss = {
                viewModel.clearSearch()
                showSearch = false
            },
            onOpenStock = { code ->
                viewModel.clearSearch()
                showSearch = false
                onStockClick(code)
            },
        )
    }
}

@Composable
private fun WatchTopBar(onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.weight(1f))
        Text("同花顺自选", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.Search, contentDescription = "搜索", tint = Color.White,
            modifier = Modifier.size(24.dp).clickable(onClick = onSearch),
        )
    }
}

@Composable
private fun IndexBar(indices: List<MarketIndex>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        indices.take(3).forEach { idx ->
            val color = QuoteFormat.colorOf(idx.change)
            Column(modifier = Modifier.weight(1f)) {
                Text(idx.name, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                Text(QuoteFormat.price(idx.point), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(QuoteFormat.signed(idx.change), color = color, fontSize = 10.sp)
                    Text(QuoteFormat.percent(idx.changePercent), color = color, fontSize = 10.sp)
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 6.dp)) {
            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text("资金", fontSize = 10.sp, color = TextSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Article, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Text("资讯", fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun WatchTabRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        watchTabs.forEachIndexed { i, tab ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    tab,
                    color = if (i == selected) TextPrimary else TextSecondary,
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { onSelect(i) },
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i == selected) THSRed else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun WatchListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧：编辑表头 / 自选设置（对齐同花顺自选页工具栏）
        Icon(
            Icons.Filled.Edit, contentDescription = "编辑",
            tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { },
        )
        Spacer(Modifier.width(16.dp))
        Icon(
            Icons.Filled.Tune, contentDescription = "设置",
            tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { },
        )
        Spacer(Modifier.weight(1f))
        Text("多股同列", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Text("涨幅", color = TextSecondary, fontSize = 12.sp)
    }
    HorizontalDivider(color = DividerGray)
}

@Composable
private fun AddStockRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text("添加股票", color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SearchAddDialog(
    viewModel: WatchlistViewModel,
    onDismiss: () -> Unit,
    onOpenStock: (String) -> Unit,
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val watchCodes by viewModel.watchCodes.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CardWhite,
            modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp, max = 520.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("搜索添加自选", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    singleLine = true,
                    placeholder = { Text("输入股票名称 / 6位代码", color = TextTertiary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = THSRed,
                        cursorColor = THSRed,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                when {
                    searching -> Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        CircularProgressIndicator(color = THSRed, modifier = Modifier.size(28.dp))
                    }
                    query.isNotBlank() && results.isEmpty() -> Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        Text("未找到相关股票", color = TextSecondary, fontSize = 13.sp)
                    }
                    query.isBlank() -> Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        Text("可搜索热门股，或输入 6 位代码", color = TextTertiary, fontSize = 13.sp)
                    }
                    else -> LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                        items(results, key = { it.fullCode }) { stock ->
                            SearchResultRow(
                                stock = stock,
                                added = watchCodes.contains(stock.fullCode),
                                onAdd = { viewModel.addToWatchlist(stock.fullCode) },
                                onOpen = { onOpenStock(stock.fullCode) },
                            )
                            HorizontalDivider(color = DividerGray)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "完成", color = THSRed, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End).clickable(onClick = onDismiss).padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    stock: com.example.tonghuashun.data.model.Stock,
    added: Boolean,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
) {
    val color = QuoteFormat.colorOf(stock.change)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stock.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(stock.fullCode, color = TextTertiary, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
            Text(QuoteFormat.price(stock.price), color = color, fontSize = 14.sp)
            Text(QuoteFormat.percent(stock.changePercent), color = color, fontSize = 11.sp)
        }
        if (added) {
            Text(
                "已添加", color = TextTertiary, fontSize = 12.sp,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BackgroundGray).padding(horizontal = 12.dp, vertical = 6.dp),
            )
        } else {
            Text(
                "+ 添加", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(THSRed).clickable(onClick = onAdd).padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
