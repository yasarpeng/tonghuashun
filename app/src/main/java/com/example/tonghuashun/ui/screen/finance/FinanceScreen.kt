package com.example.tonghuashun.ui.screen.finance

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.FinanceProduct
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
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { WelfareCardsRow() }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { FinanceGrid() }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { ActivityBanner() }
        item { Spacer(Modifier.height(THSLayout.ItemGap)) }
        item { ProductTabRow(productTab) { productTab = it } }
        items(products, key = { it.name }) { product ->
            ProductRow(product)
            HorizontalDivider(color = DividerGray)
        }
        item { ViewAllButton() }
        item { NewUserFooter() }
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
    }
}

@Composable
private fun FinanceTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(THSRed)
            .heightIn(min = THSLayout.HeaderHeight)
            .padding(horizontal = THSLayout.PageGutter, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
    ) {
        Text("理财", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Text("假期享8天收益", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Image, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RedPacketBanner() {
    val largeText = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
    val summary: @Composable () -> Unit = {
        Text("现金红包", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Column(Modifier.clip(RoundedCornerShape(THSLayout.CardRadius)).background(Color(0xFFFFC93C))
            .padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¥188", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("仅一次机会", color = Color(0xFFB3261E), fontSize = 9.sp)
        }
        Text("100%可得", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
    }
    val summaryModifier = Modifier.fillMaxWidth()
        .background(Brush.horizontalGradient(listOf(Color(0xFFFF5A3C), Color(0xFFFF8A50))))
        .padding(horizontal = THSLayout.PageGutter, vertical = 16.dp)
    if (largeText) {
        Column(summaryModifier, horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(THSLayout.ItemGap)) { summary() }
    } else {
        Row(summaryModifier, verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap)) { summary() }
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
            .fillMaxWidth().height(IntrinsicSize.Min)
            .padding(horizontal = THSLayout.PageGutter),
        horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
    ) {
        WelfareCard("新人福利", "无门槛领0折卡", "申购基金没有手续费！", Modifier.weight(1f).fillMaxHeight())
        WelfareCard("股民福利", "Level-2免费领！", "学知识体验理财，最高领4个月！", Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun WelfareCard(tag: String, title: String, desc: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(THSLayout.CardRadius))
            .background(CardWhite)
            .clickable { }
            .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
    ) {
        Text(tag, color = THSRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(THSLayout.ItemGap))
        Text(desc, color = TextTertiary, fontSize = 11.sp, maxLines = 2)
        Spacer(Modifier.height(8.dp))
        Text("去查看→", color = THSRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FinanceGrid() {
    com.example.tonghuashun.ui.component.THSFunctionGrid(financeGridItems) { item ->
        Box(contentAlignment = Alignment.TopEnd) {
            com.example.tonghuashun.ui.component.THSFuncIcon(item.icon, size = 44.dp, corner = 12.dp)
            item.badge?.let {
                Text(it, color = Color.White, fontSize = 7.sp, maxLines = 1,
                    modifier = Modifier.clip(RoundedCornerShape(THSLayout.ControlRadius)).background(THSRed)
                        .padding(horizontal = 3.dp, vertical = 1.dp))
            }
        }
        Text(item.label, fontSize = 11.sp, color = TextPrimary, maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ActivityBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = THSLayout.PageGutter)
            .clip(RoundedCornerShape(THSLayout.CardRadius))
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
                .padding(horizontal = THSLayout.PageGutter, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("查看", color = THSRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = THSRed, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ProductTabRow(selected: Int, onSelect: (Int) -> Unit) {
    THSTabRow(tabs = productTabs, selected = selected, onSelect = onSelect)
}

@Composable
private fun ProductRow(product: FinanceProduct) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .clickable { }
            .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap)) {
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
                .padding(vertical = THSLayout.ContentPadding),
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
            .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
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
