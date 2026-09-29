package com.example.tonghuashun.ui.screen.news

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.NewsItem
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.ui.theme.THSRed

private val topCategories = listOf("要闻", "快讯", "深度研究", "概念广场", "视")
private val headlineTabs = listOf("头条", "PCB概念", "算力租赁", "脑机接口", "培育钻石")

@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel(),
) {
    val news by viewModel.news.collectAsStateWithLifecycle()
    var category by remember { mutableIntStateOf(0) }
    var headlineTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray),
    ) {
        item { NewsTopBar(category) { category = it } }
        item { HeadlineTabRow(headlineTab) { headlineTab = it } }
        item { NumberedHeadlines(news) }
        item { Spacer(Modifier.height(10.dp)) }
        item { TwoCardsRow() }
        item { Spacer(Modifier.height(10.dp)) }
        items(news, key = { it.id }) { item ->
            NewsFlowRow(item)
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun NewsTopBar(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(topCategories.size) { i ->
                Text(
                    topCategories[i],
                    color = if (i == selected) Color.White else Color(0xCCFFFFFF),
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (i == selected) 18.sp else 15.sp,
                    modifier = Modifier.clickable { onSelect(i) },
                )
            }
        }
        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Icon(Icons.Filled.SmartToy, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun HeadlineTabRow(selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(headlineTabs.size) { i ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    headlineTabs[i],
                    color = if (i == selected) TextPrimary else TextSecondary,
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
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
private fun NumberedHeadlines(news: List<NewsItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        news.take(3).forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    "${index + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFFF7A29))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(item.title, color = TextPrimary, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.tag?.let { Text("$it!", color = THSRed, fontSize = 11.sp) }
                        Text(item.source, color = TextTertiary, fontSize = 11.sp)
                        Text(item.time, color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
            if (index < 2) HorizontalDivider(color = DividerGray)
        }
    }
}

@Composable
private fun TwoCardsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NewsSmallCard(
            title = "午间公告",
            highlight = "风光股份",
            highlightValue = "1.52%",
            highlightColor = StockUp,
            desc = "9月28日午间公告一览：风光股份营口厂区改造完成并恢复生产",
            modifier = Modifier.weight(1f),
        )
        NewsSmallCard(
            title = "图个明白",
            highlight = "同花顺全A(沪深京)",
            highlightValue = "-2.46%",
            highlightColor = StockDown,
            desc = "国庆节持股还是持币？复盘历次节后行情走势>>>",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NewsSmallCard(
    title: String,
    highlight: String,
    highlightValue: String,
    highlightColor: Color,
    desc: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardWhite)
            .clickable { }
            .padding(10.dp),
    ) {
        Text(title, color = THSRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(highlight, color = TextPrimary, fontSize = 12.sp, maxLines = 1)
            Text(highlightValue, color = highlightColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(desc, color = TextSecondary, fontSize = 11.sp, maxLines = 2)
    }
}

@Composable
private fun NewsFlowRow(item: NewsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = TextPrimary, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.source, color = TextTertiary, fontSize = 11.sp)
                Text(item.time, color = TextTertiary, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(96.dp, 64.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BackgroundGray),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Image, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(28.dp))
        }
    }
}
