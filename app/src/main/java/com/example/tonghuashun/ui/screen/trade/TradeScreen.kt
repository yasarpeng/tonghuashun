package com.example.tonghuashun.ui.screen.trade

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.Order
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.Position
import com.example.tonghuashun.data.model.TradeRecord
import com.example.tonghuashun.ui.screen.detail.chart.TimeShareChart
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.util.QuoteFormat
import kotlinx.coroutines.launch

/* 交易模块配色：同花顺的交易页跟随券商（广发证券）——
   盈利/涨用红，亏损/跌用蓝，与行情页的红涨绿跌不同。 */
private val BuyRed = Color(0xFFF5222D)
private val SellBlue = Color(0xFF3A7AFE)
private val FieldRedBorder = Color(0xFFF3B0B4)
private val FieldBlueBorder = Color(0xFFAFC7F5)
private val LinkBlue = Color(0xFF2E7BF6)
private val OrangeTag = Color(0xFFFF8A00)
private val BrokerName = "广发证券"
private val BrokerAccount = "8800****2373"

/** 盈利红 / 亏损蓝 */
private fun brokerColor(v: Double): Color = if (v >= 0) BuyRed else SellBlue

/** 交易模块页面：首页 + 5 个二级页 */
private enum class TradePage(val label: String) {
    LANDING(""),
    BUY("买入"),
    SELL("卖出"),
    CANCEL("撤单"),
    POSITION("持仓"),
    QUERY("查询"),
}

private val subPages = listOf(
    TradePage.BUY, TradePage.SELL, TradePage.CANCEL, TradePage.POSITION, TradePage.QUERY,
)

/** 交易频道：A股为原生页面，其余在真机上是 H5 页面 */
private val channels = listOf("A股", "基金", "期货", "黄金", "模拟")

@Composable
fun TradeScreen(
    onStockClick: (String) -> Unit,
    onOrder: (String, String) -> Unit,
    viewModel: TradeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    // 用 Int 下标 + rememberSaveable：跳到个股详情再返回时，交易页不会被打回首页
    var pageIndex by rememberSaveable { mutableIntStateOf(TradePage.LANDING.ordinal) }
    var prevIndex by rememberSaveable { mutableIntStateOf(TradePage.LANDING.ordinal) }
    var channel by rememberSaveable { mutableIntStateOf(0) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showCapitalDialog by remember { mutableStateOf(false) }

    fun toast(msg: String) { scope.launch { snackbar.showMessage(msg) } }
    val page = TradePage.entries[pageIndex.coerceIn(0, TradePage.entries.lastIndex)]
    /** 从当前页跳到目标页，记住返回目标 */
    fun open(target: TradePage) {
        prevIndex = pageIndex
        pageIndex = target.ordinal
    }
    fun back() {
        pageIndex = prevIndex
        prevIndex = TradePage.LANDING.ordinal
    }

    // 外层 AppRoot 的 Scaffold 已经处理过状态栏/底部导航内边距，
    // 这里必须清零，否则会再叠一次状态栏高度（顶格多出一截空白）。
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { pad ->
        Column(modifier = Modifier.fillMaxWidth().padding(pad).background(BackgroundGray)) {
            if (page == TradePage.LANDING) {
                TradeChannelBar(selected = channel) { channel = it }
                if (channel == 0) {
                    TradeLanding(
                        state = state,
                        onQuickAction = { open(it) },
                        onAccountSwitch = { showAccountSheet = true },
                        onEditCapital = { showCapitalDialog = true },
                        onToast = ::toast,
                    )
                } else {
                    ChannelPlaceholder(channels[channel])
                }
            } else {
                TradeSubHeader(
                    title = page.label,
                    onBack = { back() },
                    onToast = ::toast,
                    onRefresh = { viewModel.refreshNow(); toast("已刷新") },
                    // 真机差异：持仓页多一个"分享"，查询页没有"刷新"
                    showShare = page == TradePage.POSITION,
                    showRefresh = page != TradePage.QUERY,
                )
                TradeTabRow(selected = subPages.indexOf(page)) { open(subPages[it]) }
                when (page) {
                    TradePage.BUY -> key("buy") {
                        OrderPage(OrderSide.BUY, viewModel, state, onStockClick, ::toast, showSelfTab = true)
                    }
                    TradePage.SELL -> key("sell") {
                        OrderPage(OrderSide.SELL, viewModel, state, onStockClick, ::toast, showSelfTab = false)
                    }
                    TradePage.CANCEL -> CancelPage(
                        state,
                        onCancel = { toast(viewModel.cancelOrder(it)) },
                        onCancelAll = { toast(viewModel.cancelAll()) },
                        onCancelBuy = { toast(viewModel.cancelAll(OrderSide.BUY)) },
                        onCancelSell = { toast(viewModel.cancelAll(OrderSide.SELL)) },
                    )
                    TradePage.POSITION -> PositionPage(
                        state,
                        onStockClick = onStockClick,
                        onEditCapital = { showCapitalDialog = true },
                        onOrder = { code, side ->
                            viewModel.prepareOrder(code)
                            open(if (side == OrderSide.BUY) TradePage.BUY else TradePage.SELL)
                        },
                        onToast = ::toast,
                    )
                    TradePage.QUERY -> QueryPage(state)
                    TradePage.LANDING -> Unit
                }
            }
        }
    }

    if (showAccountSheet) {
        AccountSheet(
            onDismiss = { showAccountSheet = false },
            onManage = { showAccountSheet = false; showCapitalDialog = true },
            onToast = ::toast,
        )
    }

    if (showCapitalDialog) {
        CustomCapitalDialog(
            currentTotal = state.totalAsset,
            currentMarketValue = state.marketValue,
            onDismiss = { showCapitalDialog = false },
            onSetTotal = { showCapitalDialog = false; toast(viewModel.setTotalCapital(it)) },
            onReset = { showCapitalDialog = false; toast(viewModel.resetAccount(it)) },
        )
    }
}

private suspend fun SnackbarHostState.showMessage(msg: String) {
    currentSnackbarData?.dismiss()
    showSnackbar(msg)
}

/* ========================= 交易首页：频道栏 ========================= */

@Composable
private fun TradeChannelBar(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(THSRed).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.weight(1f))
        channels.forEachIndexed { i, name ->
            Text(
                name,
                color = if (i == selected) Color.White else Color.White.copy(alpha = 0.75f),
                fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (i == selected) 16.sp else 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSelect(i) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.Refresh, "刷新", tint = Color.White, modifier = Modifier.size(20.dp).clickable { })
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun ChannelPlaceholder(name: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, color = THSRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(10.dp))
            Text("该频道在真机上为 H5 页面（$name）", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text("本地演示未接入，A股频道为完整原生实现", color = TextTertiary, fontSize = 12.sp)
        }
    }
}

/* ========================= 交易首页：主体 ========================= */

private data class GridEntry(
    val title: String,
    val subtitle: String,
    val glyph: String,
    val glyphColor: Color,
    val badge: String? = null,
)

private val tradeGrid = listOf(
    GridEntry("新股/债申购", "新债 2 只", "IPO", Color(0xFF2E7BF6)),
    GridEntry("通用回购逆回购", "14天期 1.425%", "购", Color(0xFFFF8A00)),
    GridEntry("条件单", "监控条件 触发委托", "条", Color(0xFF7B61FF), badge = "NEW"),
    GridEntry("科创板", "科创板交易", "科", Color(0xFF2E7BF6)),
    GridEntry("我的笔记", "每日记录 心得积累", "笔", Color(0xFF2E7BF6)),
    GridEntry("港股通交易", "买卖港股 轻松方便", "港", Color(0xFF2E7BF6)),
    GridEntry("我的组合", "创建组合 批量买卖", "组", Color(0xFFFF8A00)),
    GridEntry("行情全景", "产业板块 全球宏观", "全", Color(0xFFFF8A00)),
    GridEntry("港股ETF", "T+0交易 当天买卖", "T+0", Color(0xFFFF8A00)),
    GridEntry("敬请期待", "更多精彩功能", "⏳", Color(0xFFFF8A00)),
)

private val landingRows = listOf(
    "银证转账" to "",
    "盘后固定价格交易" to "",
    "风险测评" to "即将推出，敬请期待",
    "交易设置" to "买卖预设、交易提醒、修改密码",
    "帮助与反馈" to "",
)

@Composable
private fun TradeLanding(
    state: TradeUiState,
    onQuickAction: (TradePage) -> Unit,
    onAccountSwitch: () -> Unit,
    onEditCapital: () -> Unit,
    onToast: (String) -> Unit,
) {
    val dayProfit = state.dayProfit
    val dayBase = state.prevMarketValue
    val dayPercent = if (dayBase > 0) dayProfit / dayBase * 100 else 0.0

    LazyColumn(modifier = Modifier.fillMaxWidth().background(BackgroundGray)) {
        item { AccountCard(state, dayProfit, dayPercent, onAccountSwitch, onEditCapital) }
        item { QuickActionRow(onQuickAction) }
        item { Spacer(Modifier.height(8.dp)) }
        item { MonthProfitCard() }
        item { Spacer(Modifier.height(8.dp)) }
        item { TradeGrid() }
        item { Spacer(Modifier.height(8.dp)) }
        item { LandingRowList(onToast) }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Text(
                "退出委托",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(6.dp)).background(THSRed)
                    .clickable { onToast("已退出委托（演示）") }
                    .padding(vertical = 13.dp),
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun AccountCard(
    state: TradeUiState,
    dayProfit: Double,
    dayPercent: Double,
    onAccountSwitch: () -> Unit,
    onEditCapital: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(BuyRed), contentAlignment = Alignment.Center) {
                Text("广发", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(BrokerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("资金账户: $BrokerAccount", color = TextTertiary, fontSize = 11.sp)
            }
            Row(
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onAccountSwitch).padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("多账号登录", color = TextSecondary, fontSize = 12.sp)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(15.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        // 2×2 指标：总资产 / 总盈亏 / 总市值 / 当日参考盈亏
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).clickable(onClick = onEditCapital)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("总资产", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Filled.Visibility, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                }
                Text("%.2f".format(state.totalAsset), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("总盈亏", color = TextSecondary, fontSize = 12.sp)
                Text(QuoteFormat.signed(state.totalProfit), color = brokerColor(state.totalProfit), fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("总市值", color = TextSecondary, fontSize = 12.sp)
                Text("%.2f".format(state.marketValue), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("当日参考盈亏", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.Filled.Info, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(QuoteFormat.signed(dayProfit), color = brokerColor(dayProfit), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text(QuoteFormat.percent(dayPercent), color = brokerColor(dayProfit), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(onClick: (TradePage) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuickAction("买", TradePage.BUY, onClick)
        QuickAction("卖", TradePage.SELL, onClick)
        QuickActionIcon(Icons.Filled.Cancel, "撤单", TradePage.CANCEL, onClick)
        QuickActionIcon(Icons.Filled.PieChart, "持仓", TradePage.POSITION, onClick)
        QuickActionIcon(Icons.Filled.Search, "查询", TradePage.QUERY, onClick)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.QuickAction(
    glyph: String,
    page: TradePage,
    onClick: (TradePage) -> Unit,
) {
    Column(
        modifier = Modifier.weight(1f).clickable { onClick(page) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).border(1.dp, TextSecondary, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(glyph, color = TextPrimary, fontSize = 13.sp) }
        Spacer(Modifier.height(6.dp))
        Text(page.label, color = TextPrimary, fontSize = 12.sp)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.QuickActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    page: TradePage,
    onClick: (TradePage) -> Unit,
) {
    Column(
        modifier = Modifier.weight(1f).clickable { onClick(page) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(CircleShape).border(1.dp, TextSecondary, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, label, tint = TextPrimary, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.height(6.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp)
    }
}

@Composable
private fun MonthProfitCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFF3F3), Color(0xFFFFE9E9))))
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("9月参考盈亏", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text("想看？就点我！", color = THSRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(Modifier.height(6.dp))
            Text("收益率****，跑赢上证指数****", color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TradeGrid() {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        tradeGrid.chunked(2).forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) HorizontalDivider(color = DividerGray, modifier = Modifier.padding(start = 14.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { entry ->
                    Row(
                        modifier = Modifier.weight(1f).clickable { }.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(26.dp).clip(RoundedCornerShape(6.dp)).background(entry.glyphColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                entry.glyph,
                                color = Color.White,
                                fontSize = if (entry.glyph.length > 1) 7.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                entry.badge?.let {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        it, color = THSRed, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0x14E63946))
                                            .padding(horizontal = 3.dp, vertical = 1.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(entry.subtitle, color = TextTertiary, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LandingRowList(onToast: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        landingRows.forEachIndexed { index, (title, desc) ->
            if (index > 0) HorizontalDivider(color = DividerGray, modifier = Modifier.padding(start = 14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToast("$title（演示）") }.padding(horizontal = 14.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                if (desc.isNotEmpty()) {
                    Text(desc, color = TextTertiary, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.width(6.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/* ========================= 二级页头部 + Tab ========================= */

@Composable
private fun TradeSubHeader(
    title: String,
    onBack: () -> Unit,
    onToast: (String) -> Unit,
    onRefresh: () -> Unit,
    showShare: Boolean = false,
    showRefresh: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(THSRed).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White,
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(BrokerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Icon(Icons.Filled.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text("**2373", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
        }
        Spacer(Modifier.weight(1f))
        if (showShare) {
            Icon(Icons.Filled.Share, "分享", tint = Color.White, modifier = Modifier.size(20.dp).clickable { onToast("分享") })
            Spacer(Modifier.width(16.dp))
        }
        Icon(Icons.Filled.Search, "搜索", tint = Color.White, modifier = Modifier.size(22.dp).clickable { onToast("搜索") })
        if (showRefresh) {
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Filled.Refresh, "刷新", tint = Color.White, modifier = Modifier.size(22.dp).clickable(onClick = onRefresh))
        }
    }
}

@Composable
private fun TradeTabRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        subPages.forEachIndexed { i, t ->
            Column(
                modifier = Modifier.weight(1f).clickable { onSelect(i) }.padding(top = 12.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    t.label,
                    color = if (i == selected) THSRed else TextPrimary,
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.width(28.dp).height(2.dp).background(if (i == selected) THSRed else Color.Transparent))
            }
        }
    }
    HorizontalDivider(color = DividerGray)
}

/* ========================= 买入 / 卖出 下单页 ========================= */

@Composable
private fun OrderPage(
    side: OrderSide,
    viewModel: TradeViewModel,
    state: TradeUiState,
    onStockClick: (String) -> Unit,
    toast: (String) -> Unit,
    showSelfTab: Boolean,
) {
    val form by viewModel.orderForm.collectAsStateWithLifecycle()
    val accent = if (side == OrderSide.BUY) BuyRed else SellBlue
    val fieldBorder = if (side == OrderSide.BUY) FieldRedBorder else FieldBlueBorder
    var subTab by remember { mutableIntStateOf(0) }
    var chartExpanded by remember { mutableStateOf(false) }
    val stock = form.stock
    val positionRatio = if (state.totalAsset > 0) state.marketValue / state.totalAsset * 100 else 0.0
    val thisPos = state.positions.firstOrNull { stock != null && it.market + it.code == stock.fullCode }
    val singleRatio = if (state.totalAsset > 0 && thisPos != null) thisPos.marketValue / state.totalAsset * 100 else 0.0

    LazyColumn(modifier = Modifier.fillMaxWidth().background(BackgroundGray)) {
        // 均价 / 最新 / 行情 + 展开
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("均价:", color = TextSecondary, fontSize = 12.sp)
                Text(if (stock != null) QuoteFormat.price(form.avgPrice) else "--", color = Color(0xFFEB9A00), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(10.dp))
                Text("最新:", color = TextSecondary, fontSize = 12.sp)
                val chColor = if (stock != null) brokerColor(stock.change) else TextSecondary
                Text(if (stock != null) QuoteFormat.price(stock.price) else "--", color = chColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(6.dp))
                Text(if (stock != null) QuoteFormat.percent(stock.changePercent) else "", color = chColor, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("行情", color = TextSecondary, fontSize = 12.sp)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { chartExpanded = !chartExpanded }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⊙", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(if (chartExpanded) "收起" else "展开", color = TextSecondary, fontSize = 12.sp)
                    Icon(
                        Icons.Filled.ArrowDropDown, "展开分时",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp).rotate(if (chartExpanded) 180f else 0f),
                    )
                }
            }
        }
        // 展开后的分时图（与真机一致：默认收起，点"展开"看该股分时）
        if (chartExpanded) {
            item {
                Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
                    if (form.timeShare.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(140.dp), Alignment.Center) {
                            Text("暂无分时数据", color = TextSecondary, fontSize = 13.sp)
                        }
                    } else {
                        TimeShareChart(entries = form.timeShare, prevClose = form.basePrice)
                    }
                }
            }
        }
        // 表单 + 盘口
        item {
            Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Column(modifier = Modifier.weight(1.38f)) {
                    // 代码 + 名称
                    Box(
                        modifier = Modifier.fillMaxWidth().height(46.dp).border(1.dp, fieldBorder, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                BasicTextField(
                                    value = form.codeInput,
                                    onValueChange = { viewModel.onCodeInput(it) },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                    cursorBrush = SolidColor(fieldBorder),
                                    decorationBox = { inner ->
                                        if (form.codeInput.isEmpty()) Text("股票代码/简拼", color = TextTertiary, fontSize = 14.sp)
                                        inner()
                                    },
                                )
                            }
                            if (stock != null) Text(stock.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // 限价▼ | − 价格 +
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.height(46.dp).border(1.dp, fieldBorder, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("限价", color = TextPrimary, fontSize = 13.sp)
                                Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        StepperField(
                            value = form.priceText, placeholder = "价格", border = fieldBorder, accent = accent,
                            onMinus = { viewModel.stepPrice(-0.01) }, onPlus = { viewModel.stepPrice(0.01) },
                            onValueChange = { viewModel.setPriceText(it) }, keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // 明日 跌停 ... 涨停
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "明日", color = TextSecondary, fontSize = 10.sp,
                            modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(BackgroundGray).padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("跌停", color = TextSecondary, fontSize = 11.sp)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            if (form.basePrice > 0) QuoteFormat.price(form.basePrice * 0.9) else "--",
                            color = SellBlue, fontSize = 11.sp,
                        )
                        Spacer(Modifier.weight(1f))
                        Text("涨停", color = TextSecondary, fontSize = 11.sp)
                        Spacer(Modifier.width(3.dp))
                        Text(
                            if (form.basePrice > 0) QuoteFormat.price(form.basePrice * 1.1) else "--",
                            color = BuyRed, fontSize = 11.sp,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    // − 数量 +
                    StepperField(
                        value = form.sharesText, placeholder = "数量", border = fieldBorder, accent = accent,
                        onMinus = { viewModel.stepShares(-100) }, onPlus = { viewModel.stepShares(100) },
                        onValueChange = { viewModel.setSharesText(it) }, keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(viewModel.availableCashOrShares(side), color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    // 仓位快捷
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "全仓" to 1.0, "1/2仓" to 0.5, "1/3仓" to (1.0 / 3),
                            "1/4仓" to 0.25, "2/3仓" to (2.0 / 3),
                        ).forEach { (label, r) ->
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BackgroundGray)
                                    .clickable { viewModel.setByRatio(side, r) }.padding(horizontal = 12.dp, vertical = 7.dp),
                            ) { Text(label, color = TextPrimary, fontSize = 12.sp) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // ⬡ + 买入/卖出
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(46.dp).border(1.dp, DividerGray, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("⬡", color = TextSecondary, fontSize = 18.sp) }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (side == OrderSide.BUY) "买  入" else "卖  出",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(4.dp)).background(accent)
                                .clickable { toast(viewModel.submitOrder(side)) }.padding(vertical = 12.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (side == OrderSide.BUY) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("个股/总仓位", color = TextSecondary, fontSize = 11.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("%.1f%%".format(singleRatio), color = OrangeTag, fontSize = 11.sp)
                            Text(" / ", color = TextSecondary, fontSize = 11.sp)
                            Text("%.1f%%".format(positionRatio), color = OrangeTag, fontSize = 11.sp)
                            Spacer(Modifier.weight(1f))
                            Text("预估加仓后成本 --", color = TextSecondary, fontSize = 11.sp)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("个股仓位 ", color = TextSecondary, fontSize = 11.sp)
                            Text("%.1f%%".format(singleRatio), color = OrangeTag, fontSize = 11.sp)
                            Spacer(Modifier.weight(1f))
                            Text("总仓位 ", color = TextSecondary, fontSize = 11.sp)
                            Text("%.1f%%".format(positionRatio), color = OrangeTag, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                MiniLevels(form.orderBook, stock?.prevClose ?: 0.0, modifier = Modifier.weight(1f))
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item { OrderSubTabRow(subTab, showSelfTab) { subTab = it } }
        item { OrderSubTabHeader(subTab) }
        when (subTab) {
            0 -> items(state.positions.size) { i ->
                OrderPositionRow(state.positions[i]) { onStockClick(state.positions[i].market + state.positions[i].code) }
            }
            1 -> items(state.orders.size) { i -> SimpleOrderRow(state.orders[i]) }
            2 -> items(state.trades.size) { i -> SimpleTradeRow(state.trades[i]) }
            else -> item {
                Box(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 32.dp), Alignment.Center) {
                    Text("自选股（在自选页管理）", color = TextTertiary, fontSize = 13.sp)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun StepperField(
    value: String, placeholder: String, border: Color, accent: Color,
    onMinus: () -> Unit, onPlus: () -> Unit, onValueChange: (String) -> Unit,
    keyboardType: KeyboardType, modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(46.dp).border(1.dp, border, RoundedCornerShape(4.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp, 46.dp).clickable(onClick = onMinus), Alignment.Center) {
            Icon(Icons.Filled.Remove, null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Box(Modifier.width(1.dp).height(46.dp).background(border))
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) Text(placeholder, color = TextTertiary, fontSize = 14.sp)
                        inner()
                    }
                },
            )
        }
        Box(Modifier.width(1.dp).height(46.dp).background(border))
        Box(Modifier.size(42.dp, 46.dp).clickable(onClick = onPlus), Alignment.Center) {
            Icon(Icons.Filled.Add, null, tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MiniLevels(orderBook: OrderBook, prevClose: Double, modifier: Modifier = Modifier) {
    val asks = orderBook.asks
    val bids = orderBook.bids
    Column(modifier = modifier.border(1.dp, DividerGray, RoundedCornerShape(2.dp))) {
        (5 downTo 1).forEach { n ->
            val lvl = asks.getOrNull(asks.size - n)
            LevelLine("卖$n", lvl?.price, lvl?.volume, prevClose)
        }
        HorizontalDivider(color = DividerGray)
        (1..5).forEach { n ->
            val lvl = bids.getOrNull(n - 1)
            LevelLine("买$n", lvl?.price, lvl?.volume, prevClose)
        }
    }
}

@Composable
private fun LevelLine(label: String, price: Double?, vol: Long?, prevClose: Double) {
    val priceColor = when {
        price == null -> TextTertiary
        prevClose > 0 && price > prevClose -> BuyRed
        prevClose > 0 && price < prevClose -> SellBlue
        else -> TextPrimary
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(26.dp))
        Text(price?.let { QuoteFormat.price(it) } ?: "--", color = priceColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text(vol?.let { formatLot(it) } ?: "--", color = TextTertiary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

private fun formatLot(hands: Long): String =
    if (hands >= 1_0000) "%.2f万".format(hands / 1_0000.0) else "$hands"

@Composable
private fun OrderSubTabRow(selected: Int, showSelfTab: Boolean, onSelect: (Int) -> Unit) {
    val tabs = if (showSelfTab) listOf("持仓", "委托", "成交", "自选") else listOf("持仓", "委托", "成交")
    Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 12.dp)) {
        tabs.forEachIndexed { i, t ->
            Text(
                t, color = if (i == selected) THSRed else TextPrimary,
                fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clickable { onSelect(i) },
            )
        }
    }
    HorizontalDivider(color = DividerGray)
}

@Composable
private fun OrderSubTabHeader(subTab: Int) {
    if (subTab != 0) return
    Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text("市值 ▼", color = THSRed, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text("盈亏 ⇅", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("持仓/可用 ⇅", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("成本/现价 ▸", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
    HorizontalDivider(color = DividerGray)
}

@Composable
private fun OrderPositionRow(p: Position, onClick: () -> Unit) {
    val color = brokerColor(p.profit)
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(p.name, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(QuoteFormat.signed(p.profit), color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${p.shares}", color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(QuoteFormat.price(p.costPrice), color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("%.2f".format(p.marketValue), color = color, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("%.3f%%".format(p.profitPercent), color = color, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${p.available}", color = color, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(QuoteFormat.price(p.currentPrice), color = color, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 12.dp))
}

@Composable
private fun SimpleOrderRow(o: Order) {
    val c = if (o.side == OrderSide.BUY) BuyRed else SellBlue
    Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1.3f)) {
            Text(o.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("${o.side.label} ${o.time}", color = TextTertiary, fontSize = 10.sp)
        }
        Text(QuoteFormat.price(o.price), color = c, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("${o.shares}", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(o.status.label, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 12.dp))
}

@Composable
private fun SimpleTradeRow(t: TradeRecord) {
    val c = if (t.side == OrderSide.BUY) BuyRed else SellBlue
    Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1.3f)) {
            Text(t.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("${t.side.label} ${t.time}", color = TextTertiary, fontSize = 10.sp)
        }
        Text(QuoteFormat.price(t.price), color = c, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("${t.shares}", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("%.2f".format(t.amount), color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 12.dp))
}

/* ========================= 撤单页 ========================= */

@Composable
private fun CancelPage(
    state: TradeUiState,
    onCancel: (String) -> Unit,
    onCancelAll: () -> Unit,
    onCancelBuy: () -> Unit,
    onCancelSell: () -> Unit,
) {
    val pending = state.pendingOrders
    val todayTrades = state.trades
    LazyColumn(modifier = Modifier.fillMaxWidth().background(BackgroundGray)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text("委托时间", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                Text("委托/均价", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("委托/成交", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("状态", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
            }
            HorizontalDivider(color = DividerGray)
        }
        if (pending.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 30.dp), Alignment.Center) {
                    Text("无可撤委托", color = TextSecondary)
                }
            }
        } else {
            items(pending.size) { i -> CancelOrderRow(pending[i], onCancel) }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().background(CardWhite), verticalAlignment = Alignment.CenterVertically) {
                CancelActionCell("全撤", Modifier.weight(1f), onCancelAll)
                Box(Modifier.width(1.dp).height(40.dp).background(DividerGray))
                CancelActionCell("撤买", Modifier.weight(1f), onCancelBuy)
                Box(Modifier.width(1.dp).height(40.dp).background(DividerGray))
                CancelActionCell("撤卖", Modifier.weight(1f), onCancelSell)
            }
            HorizontalDivider(color = DividerGray)
        }
        item {
            Box(Modifier.fillMaxWidth().background(BackgroundGray).padding(vertical = 10.dp), Alignment.Center) {
                Text("今日成交单", color = TextSecondary, fontSize = 13.sp)
            }
        }
        if (todayTrades.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 20.dp), Alignment.Center) {
                    Text("暂无成交", color = TextTertiary, fontSize = 12.sp)
                }
            }
        } else {
            items(todayTrades.size) { i -> CancelTradeRow(todayTrades[i]) }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CancelOrderRow(o: Order, onCancel: (String) -> Unit) {
    val c = if (o.side == OrderSide.BUY) BuyRed else SellBlue
    Row(
        modifier = Modifier.fillMaxWidth().background(CardWhite).clickable { onCancel(o.id) }.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(o.name, color = c, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    o.side.label.take(1), color = Color.White, fontSize = 9.sp,
                    modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(c).padding(horizontal = 3.dp, vertical = 1.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(o.time, color = c, fontSize = 11.sp)
            }
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(QuoteFormat.price(o.price), color = c, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("%.3f".format(0.0), color = c, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text("${o.shares}", color = c, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${o.filledShares}", color = c, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
            Text(o.side.label, color = c, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(o.status.label, color = c, fontSize = 12.sp)
        }
    }
    HorizontalDivider(color = DividerGray)
}

@Composable
private fun CancelActionCell(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.clickable(onClick = onClick).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(text, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CancelTradeRow(t: TradeRecord) {
    Row(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(t.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t.side.label.take(1), color = Color.White, fontSize = 9.sp,
                    modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(TextTertiary).padding(horizontal = 3.dp, vertical = 1.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(t.time, color = TextTertiary, fontSize = 11.sp)
            }
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(QuoteFormat.price(t.price), color = TextSecondary, fontSize = 14.sp)
            Text(QuoteFormat.price(t.price), color = TextSecondary, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text("${t.shares}", color = TextSecondary, fontSize = 14.sp)
            Text("${t.shares}", color = TextSecondary, fontSize = 12.sp)
        }
        Text("${t.side.label}\n全部成交", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
    }
    HorizontalDivider(color = DividerGray)
}

/* ========================= 持仓页 ========================= */

@Composable
private fun PositionPage(
    state: TradeUiState,
    onStockClick: (String) -> Unit,
    onEditCapital: () -> Unit,
    onOrder: (String, OrderSide) -> Unit,
    onToast: (String) -> Unit,
) {
    val positionRatio = if (state.totalAsset > 0) state.marketValue / state.totalAsset * 100 else 0.0
    // 真机行为：点击持仓行不跳详情，而是在行下方展开操作条，同时只允许展开一行
    var expandedCode by remember { mutableStateOf<String?>(null) }
    // "明细"：持仓明细弹层，按代码查找，保证弹层里的数字跟着行情走
    var detailCode by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier = Modifier.fillMaxWidth().background(BackgroundGray)) {
        item { PositionAccountCard(state, positionRatio, onEditCapital) }
        item { Spacer(Modifier.height(8.dp)) }
        item { PositionTableHeader() }
        if (state.positions.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 40.dp), Alignment.Center) {
                    Text("暂无持仓，去买入吧", color = TextSecondary)
                }
            }
        } else {
            items(state.positions.size) { i ->
                val p = state.positions[i]
                val fullCode = p.market + p.code
                PositionTableRow(
                    p = p,
                    expanded = expandedCode == fullCode,
                    onToggle = { expandedCode = if (expandedCode == fullCode) null else fullCode },
                    onDetail = { detailCode = fullCode },
                    onBuy = { onOrder(fullCode, OrderSide.BUY) },
                    onSell = { onOrder(fullCode, OrderSide.SELL) },
                    onCondition = { onToast("${p.name} 条件单（演示）") },
                    onQuote = { onStockClick(fullCode) },
                )
            }
        }
        item { ClearedRow() }
        item { Spacer(Modifier.height(8.dp)) }
        item { PositionBottomActions() }
        item { Spacer(Modifier.height(24.dp)) }
    }

    val detailPos = detailCode?.let { c -> state.positions.firstOrNull { it.market + it.code == c } }
    if (detailPos != null) {
        PositionDetailSheet(
            p = detailPos,
            totalAsset = state.totalAsset,
            onDismiss = { detailCode = null },
            onQuote = { detailCode = null; onStockClick(detailPos.market + detailPos.code) },
            onBuy = { detailCode = null; onOrder(detailPos.market + detailPos.code, OrderSide.BUY) },
            onSell = { detailCode = null; onOrder(detailPos.market + detailPos.code, OrderSide.SELL) },
        )
    }
}

@Composable
private fun PositionAccountCard(state: TradeUiState, ratio: Double, onEditCapital: () -> Unit) {
    val dayBase = state.prevMarketValue
    val dayPercent = if (dayBase > 0) state.dayProfit / dayBase * 100 else 0.0
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🇨🇳", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text("人民币账户A股", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BackgroundGray).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text("仓位 %.1f%%".format(ratio), color = TextSecondary, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1.1f).clickable(onClick = onEditCapital)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("总资产", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Filled.Visibility, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                }
                Text("%.2f".format(state.totalAsset), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
            }
            MetricCol("浮动盈亏", QuoteFormat.signed(state.totalProfit), brokerColor(state.totalProfit), Modifier.weight(1f))
            DayProfitCol(dayProfit = state.dayProfit, dayPercent = dayPercent, modifier = Modifier.weight(1.2f))
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCol("总市值", "%.2f".format(state.marketValue), TextPrimary, Modifier.weight(1.1f))
            MetricColTag("可用", "逆回购", "%.2f".format(state.cash), Modifier.weight(1f))
            MetricColTag("可取", "转账", "%.2f".format(state.cash), Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun MetricCol(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
    }
}

@Composable
private fun MetricColTag(label: String, tag: String, value: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            Text(tag, color = OrangeTag, fontSize = 10.sp)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = OrangeTag, modifier = Modifier.size(12.dp))
        }
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
    }
}

@Composable
private fun DayProfitCol(dayProfit: Double, dayPercent: Double, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("当日参考盈亏", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Filled.Info, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(QuoteFormat.signed(dayProfit), color = brokerColor(dayProfit), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(QuoteFormat.percent(dayPercent), color = brokerColor(dayProfit), fontSize = 11.sp)
        }
    }
}

@Composable
private fun PositionTableHeader() {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("持仓股", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            // 持仓分时 / 持仓报价模式 开关（真机是右上角两个小图标）
            Icon(Icons.Filled.ShowChart, "持仓分时开关", tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(18.dp))
            Icon(Icons.Filled.BarChart, "持仓报价模式开关", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
            Text("市值 ▼", color = THSRed, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("盈亏 ⇅", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("持仓/可用 ⇅", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("成本/现价 ⇸", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = DividerGray)
    }
}

/**
 * 持仓表一行：两行文本 × 四列，与同花顺一致。
 * 第一行 名称 | 盈亏 | 持仓 | 成本价；第二行 市值 | 盈亏% | 可用 | 现价。
 *
 * 点击整行不跳详情，而是在行下方展开操作条
 * `明细 | 买入 | 卖出 | 条件单 | 行情`（真机行为），再次点击收起。
 */
@Composable
private fun PositionTableRow(
    p: Position,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDetail: () -> Unit,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onCondition: () -> Unit,
    onQuote: () -> Unit,
) {
    val color = brokerColor(p.profit)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (expanded) Color(0xFFF7F8FA) else CardWhite)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                p.name, color = color, fontWeight = FontWeight.Medium, fontSize = 17.sp,
                maxLines = 1, modifier = Modifier.weight(1f),
            )
            Text(QuoteFormat.signed(p.profit), color = color, fontSize = 15.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${p.shares}", color = color, fontSize = 15.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(QuoteFormat.price(p.costPrice), color = color, fontSize = 15.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(3.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("%.2f".format(p.marketValue), color = color, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("%.3f%%".format(p.profitPercent), color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${p.available}", color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(QuoteFormat.price(p.currentPrice), color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
    if (expanded) {
        HorizontalDivider(color = DividerGray)
        Row(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
            PositionAction("明细", onDetail)
            PositionAction("买入", onBuy)
            PositionAction("卖出", onSell)
            PositionAction("条件单", onCondition)
            PositionAction("行情", onQuote)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.PositionAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 14.dp),
    )
}

/**
 * 持仓明细（点持仓行操作条的「明细」）。
 * 全部字段由实时行情计算：现价、市值、浮动盈亏、当日盈亏都会随行情刷新。
 */
@Composable
private fun PositionDetailSheet(
    p: Position,
    totalAsset: Double,
    onDismiss: () -> Unit,
    onQuote: () -> Unit,
    onBuy: () -> Unit,
    onSell: () -> Unit,
) {
    val profitColor = brokerColor(p.profit)
    val dayColor = brokerColor(p.dayProfit)
    // 回本涨幅：(成本价 - 现价) / 现价，与同花顺"回本涨幅"列一致
    val breakEven = if (p.currentPrice > 0) (p.costPrice - p.currentPrice) / p.currentPrice * 100 else 0.0
    val singleRatio = if (totalAsset > 0) p.marketValue / totalAsset * 100 else 0.0

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Surface(
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                color = CardWhite,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(p.market + p.code, color = TextTertiary, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "✕", color = TextSecondary, fontSize = 16.sp,
                            modifier = Modifier.clickable(onClick = onDismiss).padding(4.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("持仓明细", color = TextTertiary, fontSize = 11.sp)
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = DividerGray)
                    Spacer(Modifier.height(12.dp))

                    DetailRow("持仓数量", "${p.shares}", "可用数量", "${p.available}")
                    DetailRow("成本价", QuoteFormat.price(p.costPrice), "现价", QuoteFormat.price(p.currentPrice), valueColor = profitColor)
                    DetailRow("昨收", QuoteFormat.price(p.prevClose), "市值", "%.2f".format(p.marketValue), valueColor = profitColor)
                    DetailRow(
                        "浮动盈亏", QuoteFormat.signed(p.profit),
                        "盈亏比例", QuoteFormat.percent(p.profitPercent),
                        valueColor = profitColor,
                    )
                    DetailRow(
                        "当日盈亏", QuoteFormat.signed(p.dayProfit),
                        "当日盈亏比", QuoteFormat.percent(p.dayProfitPercent),
                        valueColor = dayColor,
                    )
                    DetailRow("持仓成本", "%.2f".format(p.costPrice * p.shares), "个股仓位", "%.1f%%".format(singleRatio))
                    DetailRow("回本涨幅", QuoteFormat.percent(breakEven), "", "")

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetButton("查看行情", TextPrimary, BackgroundGray, Modifier.weight(1f), onQuote)
                        SheetButton("买入", Color.White, BuyRed, Modifier.weight(1f), onBuy)
                        SheetButton("卖出", Color.White, SellBlue, Modifier.weight(1f), onSell)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String,
    valueColor: Color = TextPrimary,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Text(label1, color = TextTertiary, fontSize = 12.sp, modifier = Modifier.width(76.dp))
        Text(value1, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (label2.isNotEmpty()) {
            Text(label2, color = TextTertiary, fontSize = 12.sp, modifier = Modifier.width(76.dp))
            Text(value2, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(2f))
        }
    }
}

@Composable
private fun SheetButton(
    text: String,
    textColor: Color,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
    )
}

@Composable
private fun ClearedRow() {
    Row(
        modifier = Modifier.fillMaxWidth().background(CardWhite).clickable { }.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("查看已清仓股票", color = TextSecondary, fontSize = 13.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun PositionBottomActions() {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
            listOf("持仓管理", "批量买入", "批量卖出", "止盈止损").forEach {
                Text(it, color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clickable { })
            }
        }
        HorizontalDivider(color = DividerGray)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.Center) {
            Text("持仓资讯", color = LinkBlue, fontSize = 14.sp, modifier = Modifier.clickable { })
            Box(Modifier.width(1.dp).height(16.dp).background(DividerGray))
            Spacer(Modifier.width(20.dp))
            Text("资产分析", color = LinkBlue, fontSize = 14.sp, modifier = Modifier.clickable { })
        }
    }
}

/* ========================= 查询页 ========================= */

@Composable
private fun QueryPage(state: TradeUiState) {
    val groups = listOf(
        listOf("当日成交" to state.trades.size, "当日委托" to state.orders.size),
        listOf("历史成交" to -1, "历史委托" to -1),
        listOf("已清仓股票" to -1),
        listOf("对账单" to -1),
        listOf("转账记录" to -1),
        listOf("T操作" to -1),
    )
    LazyColumn(modifier = Modifier.fillMaxWidth().background(BackgroundGray)) {
        groups.forEach { group ->
            item {
                Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
                    group.forEachIndexed { idx, (title, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(title, color = TextPrimary, fontSize = 15.sp)
                            if (count > 0) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "$count", color = THSRed, fontSize = 12.sp,
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x1AE63946)).padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                        }
                        if (idx < group.size - 1) HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                Text("新股配号、中签查询，请前往", color = TextSecondary, fontSize = 12.sp)
                Text("新股申购", color = LinkBlue, fontSize = 12.sp)
                Text(" -- 查询页面", color = TextSecondary, fontSize = 12.sp)
            }
        }
        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), Alignment.Center) {
                Text("功能帮助", color = LinkBlue, fontSize = 14.sp)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/* ========================= 账户切换弹层 ========================= */

@Composable
private fun AccountSheet(onDismiss: () -> Unit, onManage: () -> Unit, onToast: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("✕", color = TextSecondary, fontSize = 16.sp, modifier = Modifier.clickable(onClick = onDismiss))
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(BrokerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Text("**2373", color = TextTertiary, fontSize = 10.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("?", color = TextTertiary, fontSize = 14.sp)
                }
                HorizontalDivider(color = DividerGray)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onToast("已切换到 $BrokerName") }.padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(BuyRed), contentAlignment = Alignment.Center) {
                        Text("广发", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(BrokerName, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("资金账户: $BrokerAccount", color = TextTertiary, fontSize = 11.sp)
                    }
                    Icon(Icons.Filled.Check, null, tint = THSRed, modifier = Modifier.size(18.dp))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp)) {
                    Text(
                        "添加账号", color = TextPrimary, fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clickable { onToast("添加账号") },
                    )
                    Text(
                        "账户管理", color = TextPrimary, fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clickable(onClick = onManage),
                    )
                }
            }
        }
    }
}

/* ========================= 自定义总资产（本地演示能力） ========================= */

@Composable
private fun CustomCapitalDialog(
    currentTotal: Double,
    currentMarketValue: Double,
    onDismiss: () -> Unit,
    onSetTotal: (Double) -> Unit,
    onReset: (Double) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val presets = listOf(50_000.0, 100_000.0, 500_000.0, 1_000_000.0, 5_000_000.0, 10_000_000.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("自定义总资产", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text("当前总资产 %.2f 元".format(currentTotal), color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                Text("其中持仓市值 %.2f 元（保留不动）".format(currentMarketValue), color = TextTertiary, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = input, onValueChange = { s -> input = s.filter { it.isDigit() || it == '.' } }, singleLine = true,
                    placeholder = { Text("输入总资产金额（元）", color = TextTertiary, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = THSRed, cursorColor = THSRed),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                presets.chunked(3).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { amount ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(BackgroundGray)
                                    .clickable { input = "%.0f".format(amount) }.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) { Text(presetLabel(amount), color = TextPrimary, fontSize = 12.sp) }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "设为总资产（保留持仓，调整可用资金）", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(THSRed)
                        .clickable { input.toDoubleOrNull()?.let(onSetTotal) }.padding(vertical = 12.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "重置账户（清空持仓，全部作为现金）", color = THSRed, fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                        .clickable { input.toDoubleOrNull()?.let(onReset) }.padding(vertical = 10.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "取消", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onDismiss).padding(vertical = 8.dp),
                )
            }
        }
    }
}

private fun presetLabel(amount: Double): String = when {
    amount >= 1_0000_0000 -> "%.0f亿".format(amount / 1_0000_0000)
    amount >= 1_0000 -> "%.0f万".format(amount / 1_0000)
    else -> "%.0f".format(amount)
}
