package com.example.tonghuashun.ui.screen.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.FinanceProduct
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.util.QuoteFormat

private data class FinanceGridItem(val label: String, val icon: com.example.tonghuashun.ui.component.THSIcon, val badge: String? = null)

private val financeGridItems = listOf(
    FinanceGridItem("基金自选", com.example.tonghuashun.ui.component.THSIcon.FUND),
    FinanceGridItem("基金开户", com.example.tonghuashun.ui.component.THSIcon.FUND_OPEN),
    FinanceGridItem("基民学堂", com.example.tonghuashun.ui.component.THSIcon.SCHOOL),
    FinanceGridItem("同花顺钱包", com.example.tonghuashun.ui.component.THSIcon.WALLET, "收益率1.4%"),
    FinanceGridItem("基金排行", com.example.tonghuashun.ui.component.THSIcon.RANK),
    FinanceGridItem("新客福利", com.example.tonghuashun.ui.component.THSIcon.GIFT, "188元红包"),
    FinanceGridItem("高端理财", com.example.tonghuashun.ui.component.THSIcon.HIGH_END, "高胜率"),
    FinanceGridItem("全球投资", com.example.tonghuashun.ui.component.THSIcon.GLOBAL, "限额打包"),
    FinanceGridItem("策略广场", com.example.tonghuashun.ui.component.THSIcon.STRATEGY, "Ai策略"),
    FinanceGridItem("基金skill", com.example.tonghuashun.ui.component.THSIcon.SKILL),
)

private val productTabs = listOf("假期理财", "全球投资", "CPO", "红利投资")

@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var productTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray),
    ) {
        item { FinanceTopBar() }
        item { RedPacketBanner() }
        item { Spacer(Modifier.height(10.dp)) }
        item { WelfareCardsRow() }
        item { Spacer(Modifier.height(10.dp)) }
        item { FinanceGrid() }
        item { Spacer(Modifier.height(10.dp)) }
        item { ActivityBanner() }
        item { Spacer(Modifier.height(10.dp)) }
        item { ProductTabRow(productTab) { productTab = it } }
        items(products, key = { it.name }) { product ->
            ProductRow(product)
            HorizontalDivider(color = DividerGray)
        }
        item { ViewAllButton() }
        item { NewUserFooter() }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun FinanceTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("理财", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Text("假期享8天收益", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Image, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RedPacketBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFFF5A3C), Color(0xFFFF8A50)))
            )
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("现金红包", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFC93C))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("¥188", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("仅一次机会", color = Color(0xFFB3261E), fontSize = 9.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text("100%可得", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6A45))
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "免费领取",
            color = Color(0xFFB3261E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFFD84D))
                .clickable { }
                .padding(horizontal = 40.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun WelfareCardsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        WelfareCard("新人福利", "无门槛领0折卡", "申购基金没有手续费！", Modifier.weight(1f))
        WelfareCard("股民福利", "Level-2免费领！", "学知识体验理财，最高领4个月！", Modifier.weight(1f))
    }
}

@Composable
private fun WelfareCard(tag: String, title: String, desc: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardWhite)
            .clickable { }
            .padding(12.dp),
    ) {
        Text(tag, color = THSRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(6.dp))
        Text(desc, color = TextTertiary, fontSize = 11.sp, maxLines = 2)
        Spacer(Modifier.height(8.dp))
        Text("去查看→", color = THSRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FinanceGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(vertical = 12.dp),
    ) {
        financeGridItems.chunked(5).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .clickable { },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            com.example.tonghuashun.ui.component.THSFuncIcon(item.icon, size = 40.dp, corner = 10.dp)
                            item.badge?.let {
                                Text(
                                    it,
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(THSRed)
                                        .padding(horizontal = 3.dp, vertical = 1.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(item.label, fontSize = 11.sp, color = TextPrimary, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFE63946), Color(0xFFFF6B5C)))
            )
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("国庆假期理财指南", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("9月29日15点前转入 享8天收益", color = Color(0xFFFFE0DD), fontSize = 12.sp)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("查看", color = THSRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = THSRed, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ProductTabRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        productTabs.forEachIndexed { i, tab ->
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
private fun ProductRow(product: FinanceProduct) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(product.name, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                product.tag?.let {
                    Text(
                        it,
                        color = THSRed,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x22E63946))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(product.desc, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(QuoteFormat.percent(product.yield7), color = StockUp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("7日年化", color = TextTertiary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ViewAllButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "立即查看",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(THSRed)
                .clickable { }
                .padding(vertical = 14.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun NewUserFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("新人福利", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = THSRed, modifier = Modifier.size(14.dp))
            Text("0折手续费", color = THSRed, fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}
