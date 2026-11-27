package com.example.tonghuashun.ui.screen.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@Composable
internal fun ReferenceStockSearch(onDismiss: () -> Unit, search: suspend (String) -> List<Stock>, onOpen: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<Stock>()) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(query) {
        val term = query.trim()
        results = emptyList(); failed = false
        if (term.isBlank()) { busy = false; return@LaunchedEffect }
        busy = true
        delay(300)
        try {
            val found = search(term)
            if (query.trim() == term) results = found
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (query.trim() == term) failed = true
        } finally {
            if (query.trim() == term) busy = false
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = CardWhite,
            modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp, max = 520.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("搜索行情", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(query, onValueChange = { query = it }, singleLine = true,
                    label = { Text("股票名称或代码") }, placeholder = { Text("例如 510980") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    when {
                        busy -> CircularProgressIndicator(color = TradeHeaderRed, modifier = Modifier.size(28.dp))
                        failed -> Text("行情查询失败，请重试", color = TextSecondary, fontSize = 13.sp)
                        query.isBlank() -> Text("输入名称或六位代码查看行情", color = TextSecondary, fontSize = 13.sp)
                        results.isEmpty() -> Text("未找到相关股票", color = TextSecondary, fontSize = 13.sp)
                        else -> LazyColumn(Modifier.fillMaxSize()) {
                            items(results, key = { it.fullCode }) { stock ->
                                Row(Modifier.fillMaxWidth().clickable { onOpen(stock.fullCode) }.padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(stock.name, color = TextPrimary, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(stock.fullCode, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Text(detailPrice(stock.code, stock.price), color = if (stock.change >= 0) QuoteRed else QuoteGreen, fontSize = 14.sp)
                                }
                                HorizontalDivider(color = DividerGray)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("关闭") }
            }
        }
    }
}
