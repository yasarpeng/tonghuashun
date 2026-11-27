package com.example.tonghuashun.ui.screen.trade

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.tonghuashun.ui.theme.TradeHeaderRed
import com.example.tonghuashun.ui.theme.TradeProfitRed
import com.example.tonghuashun.ui.theme.TradePageBackground
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Storage
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.example.tonghuashun.ui.component.LocalTableScroll
import com.example.tonghuashun.ui.component.THSActionButton
import com.example.tonghuashun.ui.component.THSAdaptiveSplit
import com.example.tonghuashun.ui.component.THSDataRow
import com.example.tonghuashun.ui.component.THSIconButton
import com.example.tonghuashun.ui.component.THSPageHeader
import com.example.tonghuashun.ui.component.THSTabRow
import com.example.tonghuashun.ui.screen.detail.chart.TimeShareChart
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.THSLayout
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.util.QuoteFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

/* 交易模块配色：同花顺的交易页跟随券商（中信证券）——
   盈利/涨用红，亏损/跌用蓝，与行情页的红涨绿跌不同。 */
private val BuyRed = Color(0xFFF5222D)
private val SellBlue = Color(0xFF3A7AFE)
private val FieldRedBorder = Color(0xFFF3B0B4)
private val FieldBlueBorder = Color(0xFFAFC7F5)
private val LinkBlue = Color(0xFF2E7BF6)
private val OrangeTag = Color(0xFFFF8A00)
private val BrokerName = "中信证券"
private val BrokerShort = "中信"
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
    val tableScroll = rememberScrollState()
    CompositionLocalProvider(LocalTableScroll provides tableScroll) {
    Scaffold(
        containerColor = TradePageBackground,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).background(TradePageBackground).imePadding()) {
            if (page == TradePage.LANDING) {
                TradeChannelBar(selected = channel, onRefresh = { viewModel.refreshNow(); toast("正在刷新行情") }) { channel = it }
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
                    // 真机差异：持仓页多一个"分享"
                    showShare = page == TradePage.POSITION,
                    onRefresh = { viewModel.refreshNow() },
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
private fun TradeChannelBar(selected: Int, onRefresh: () -> Unit, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(TradeHeaderRed), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(56.dp))
        val largeText = LocalDensity.current.fontScale > 1.3f
        Row(
            Modifier.weight(1f).selectableGroup().then(if (largeText) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            channels.forEachIndexed { index, title ->
                Box(
                    Modifier.then(if (largeText) Modifier.width(96.dp) else Modifier.weight(1f))
                        .heightIn(min = 70.dp)
                        .selectable(selected == index, role = Role.Tab, onClick = { onSelect(index) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(title, fontSize = 19.sp, color = Color.White.copy(alpha = if (selected == index) 1f else 0.68f),
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        THSIconButton(Icons.Outlined.Refresh, "刷新行情", onRefresh)
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
            Spacer(Modifier.height(THSLayout.ItemGap))
            Text("该频道在真机上为 H5 页面（$name）", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text("本地演示未接入，A股频道为完整原生实现", color = TextTertiary, fontSize = 12.sp)
        }
    }
}

/* ========================= 交易首页：主体 ========================= */

private data class GridEntry(val title: String, val subtitle: String, val icon: LandingFeature)

private val tradeGrid = listOf(
    GridEntry("新股/债申购", "新股与新债申购", LandingFeature.IPO),
    GridEntry("通用回购逆回购", "短期资金管理", LandingFeature.REPO),
    GridEntry("条件单", "监控条件 触发委托", LandingFeature.CONDITION),
    GridEntry("融资融券", "借钱买多 借券卖空", LandingFeature.MARGIN),
    GridEntry("科创板", "科创板交易", LandingFeature.STAR),
    GridEntry("我的笔记", "每日记录 心得积累", LandingFeature.NOTES),
    GridEntry("港股通交易", "买卖港股 轻松方便", LandingFeature.HONG_KONG),
    GridEntry("我的组合", "创建组合 批量买卖", LandingFeature.PORTFOLIO),
    GridEntry("行情全景", "产业板块 全球宏观", LandingFeature.MARKET),
    GridEntry("业务办理", "权限开通 信息更新", LandingFeature.SERVICES),
    GridEntry("港股ETF", "T+0交易 当天买卖", LandingFeature.ETF),
    GridEntry("敬请期待", "更多精彩功能", LandingFeature.MORE),
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
    val dayPercent = if (state.prevMarketValue > 0) state.dayProfit / state.prevMarketValue * 100 else 0.0
    LazyColumn(modifier = Modifier.fillMaxWidth().background(TradePageBackground)) {
        item { AccountCard(state, state.dayProfit, dayPercent, onAccountSwitch, onEditCapital) }
        item { QuickActionRow(onQuickAction) }
        item { Spacer(Modifier.height(12.dp)) }
        item { MonthProfitCard(state) { onToast("参考盈亏按当前持仓浮盈估算；收益率为浮盈除以账户成本本金，不代表实际月度收益") } }
        item { Spacer(Modifier.height(12.dp)) }
        item { TradeGrid(onToast) }
        item { Spacer(Modifier.height(THSLayout.SectionGap)) }
        item { LandingRowList(onToast) }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Text(
                "退出委托",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter)
                    .clip(RoundedCornerShape(THSLayout.ControlRadius)).background(THSRed)
                    .clickable { onToast("已退出委托（演示）") }.padding(vertical = 13.dp),
            )
        }
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
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
    Column(Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter)) {
        Row(
            Modifier.fillMaxWidth().clickable(onClickLabel = "切换证券账户", onClick = onAccountSwitch).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(BuyRed), contentAlignment = Alignment.Center) {
                Text(BrokerShort, color = Color.White, fontSize = (9f / LocalDensity.current.fontScale).sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(BrokerName, color = TextPrimary, fontSize = 19.sp)
                Text("客户号 $BrokerAccount", color = PositionLabelColor, fontSize = 13.sp)
            }
            Icon(Icons.Filled.ArrowDropDown, null, tint = TextTertiary, modifier = Modifier.size(22.dp))
        }
        HorizontalDivider(color = DividerGray.copy(alpha = 0.55f))
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp)) {
            Column(Modifier.weight(0.55f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LandingAssetMetric("总资产", positionMoney(state.totalAsset),
                    modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "设置总资产", onClick = onEditCapital),
                    icon = Icons.Outlined.Visibility)
                LandingAssetMetric("总市值", positionMoney(state.marketValue))
            }
            Column(Modifier.weight(0.45f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LandingAssetMetric("浮动盈亏", positionMoney(state.totalProfit, signed = true), color = landingProfitColor(state.totalProfit))
                LandingAssetMetric("当日参考盈亏", positionMoney(dayProfit, signed = true), color = landingProfitColor(dayProfit),
                    suffix = "%.2f%%".format(dayPercent), icon = Icons.Outlined.Info)
            }
        }
        HorizontalDivider(color = DividerGray.copy(alpha = 0.55f))
    }
}

private fun landingProfitColor(value: Double) = if (value >= 0) TradeProfitRed else SellBlue

@Composable
private fun LandingAssetMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    suffix: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column(modifier.heightIn(min = 48.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = PositionLabelColor, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false))
            if (icon != null) Icon(icon, null, tint = PositionLabelColor, modifier = Modifier.size(15.dp))
        }
        val stackSuffix = suffix != null && LocalDensity.current.fontScale > 1.3f
        PositionAmount(value, color = color, suffix = if (stackSuffix) null else suffix, maxFontSize = 20f)
        if (stackSuffix) Text(suffix.orEmpty(), color = color, fontSize = 12.sp)
    }
}

@Composable
private fun QuickActionRow(onClick: (TradePage) -> Unit) {
    Row(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 12.dp)) {
        subPages.forEachIndexed { index, page ->
            Column(
                Modifier.weight(1f).clickable(role = Role.Button) { onClick(page) }.padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (index < 2) {
                    Box(Modifier.size(30.dp).border(1.dp, PositionBodyColor, CircleShape), contentAlignment = Alignment.Center) {
                        Text(if (index == 0) "买" else "卖", color = PositionBodyColor, fontSize = (16f / LocalDensity.current.fontScale).sp)
                    }
                } else LandingQuickIcon(index, Modifier.size(30.dp))
                Text(page.label, color = PositionLabelColor, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun MonthProfitCard(state: TradeUiState, onClick: () -> Unit) {
    val now = state.now
    // 尚无月初净值：沿用当前持仓浮盈口径，明确标注估算，避免冒充实际月收益。
    val referenceProfit = state.totalProfit
    val accountCost = state.totalAsset - referenceProfit
    val referenceReturn = if (accountCost > 0) referenceProfit / accountCost * 100 else 0.0
    val profitColor = landingProfitColor(referenceProfit)
    val month = remember(now / 60_000L) {
        SimpleDateFormat("M", Locale.CHINA).apply { timeZone = TimeZone.getTimeZone("Asia/Shanghai") }.format(Date(now))
    }
    Column(Modifier.fillMaxWidth().background(CardWhite).clickable(onClick = onClick)
        .padding(horizontal = THSLayout.PageGutter, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${month}月参考盈亏 · 持仓估算", color = PositionLabelColor, fontSize = 15.sp)
                PositionAmount(positionMoney(referenceProfit, signed = true), color = profitColor, maxFontSize = 21f)
            }
            // 当前账户没有月度净值序列，保留图表位置并明确呈现空态。
            Column(Modifier.width(116.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无走势", color = PositionLabelColor, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DividerGray)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("参考收益率 ", color = PositionLabelColor, fontSize = 14.sp)
            Text("%.2f%%".format(Locale.CHINA, referenceReturn), color = profitColor, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TradeGrid(onToast: (String) -> Unit) {
    val columns = if (LocalDensity.current.fontScale > 1.3f) 1 else 2
    Column(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 4.dp)) {
        tradeGrid.chunked(columns).forEach { rowItems ->
            Row(Modifier.fillMaxWidth()) {
                rowItems.forEach { entry ->
                    Row(
                        Modifier.weight(1f).heightIn(min = 68.dp)
                            .clickable(role = Role.Button) { onToast("${entry.title}暂未接入") }
                            .padding(horizontal = THSLayout.PageGutter, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LandingFeatureIcon(entry.icon, Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(entry.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(entry.subtitle, color = PositionLabelColor, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandingRowList(onToast: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite)) {
        landingRows.forEachIndexed { index, (title, desc) ->
            if (index > 0) HorizontalDivider(color = DividerGray, modifier = Modifier.padding(start = THSLayout.PageGutter))
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToast("$title（演示）") }.padding(horizontal = THSLayout.PageGutter, vertical = 16.dp),
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
    showShare: Boolean = false,
    onRefresh: () -> Unit,
) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    Box(Modifier.fillMaxWidth().background(TradeHeaderRed).heightIn(min = 58.dp)) {
        Box(Modifier.align(Alignment.CenterStart)) {
            THSIconButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "返回$title", onBack)
        }
        Column(Modifier.align(Alignment.Center).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Text(BrokerName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Filled.ArrowDropDown, null, tint = Color.White,
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = 20.dp).size(20.dp))
            }
            Text("**${BrokerAccount.takeLast(4)}", color = Color.White, fontSize = 11.sp)
        }
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
            if (showShare && !largeText) TradeHeaderAction(Icons.Outlined.OpenInNew, "分享", { onToast("分享") })
            TradeHeaderAction(Icons.Outlined.Search, "搜索", { onToast("搜索") })
            TradeHeaderAction(Icons.Outlined.Refresh, "刷新", onRefresh)
        }
    }
}

@Composable
private fun TradeHeaderAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp, 48.dp).clickable(onClick = onClick), Alignment.Center) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}


@Composable
private fun TradeTabRow(selected: Int, onSelect: (Int) -> Unit) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    if (largeText) {
        THSTabRow(subPages.map { it.label }, selected, onSelect, scrollable = true, selectedColor = TradeProfitRed)
        return
    }
    Column(Modifier.fillMaxWidth().background(CardWhite)) {
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            subPages.forEachIndexed { index, page ->
                Column(Modifier.weight(1f).selectable(index == selected, role = Role.Tab, onClick = { onSelect(index) }),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().height(42.dp), Alignment.Center) {
                        Text(page.label, color = if (index == selected) TradeProfitRed else PositionBodyColor,
                            fontSize = 16.sp, fontWeight = if (index == selected) FontWeight.Medium else FontWeight.Normal)
                    }
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (index == selected) TradeProfitRed else Color.Transparent))
                }
            }
        }
        HorizontalDivider(color = DividerGray)
    }
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
                modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = 6.dp),
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
            THSAdaptiveSplit(
                modifier = Modifier.fillMaxWidth().background(CardWhite)
                    .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
                primary = { formModifier ->
                Column(modifier = formModifier) {
                    // 代码 + 名称
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = THSLayout.TouchTarget).border(1.dp, fieldBorder, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                BasicTextField(
                                    value = form.codeInput,
                                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "股票代码或简拼" },
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
                            if (stock != null) Text(stock.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // 限价▼ | − 价格 +
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.heightIn(min = THSLayout.TouchTarget).border(1.dp, fieldBorder, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp),
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
                    Spacer(Modifier.height(THSLayout.ItemGap))
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
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap)) {
                        listOf(
                            "全仓" to 1.0, "1/2仓" to 0.5, "1/3仓" to (1.0 / 3),
                            "1/4仓" to 0.25, "2/3仓" to (2.0 / 3),
                        ).forEach { (label, r) ->
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BackgroundGray)
                                    .clickable { viewModel.setByRatio(side, r) }.padding(horizontal = THSLayout.PageGutter, vertical = 8.dp),
                            ) { Text(label, color = TextPrimary, fontSize = 12.sp) }
                        }
                    }
                    Spacer(Modifier.height(THSLayout.ItemGap))
                    THSActionButton(if (side == OrderSide.BUY) "买入" else "卖出", accent,
                        Modifier.fillMaxWidth(), onClick = { toast(viewModel.submitOrder(side)) })
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
                },
                secondary = { bookModifier ->
                    MiniLevels(form.orderBook, stock?.prevClose ?: 0.0, modifier = bookModifier)
                },
            )
        }
        item { Spacer(Modifier.height(THSLayout.SectionGap)) }
        item { OrderSubTabRow(subTab, showSelfTab) { subTab = it } }
        item { OrderSubTabHeader(subTab) }
        when (subTab) {
            0 -> items(state.positions.size) { i ->
                OrderPositionRow(state.positions[i]) { onStockClick(state.positions[i].market + state.positions[i].code) }
            }
            1 -> items(state.todayOrders.size) { i -> SimpleOrderRow(state.todayOrders[i]) }
            2 -> items(state.todayTrades.size) { i -> SimpleTradeRow(state.todayTrades[i]) }
            else -> item {
                Box(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 32.dp), Alignment.Center) {
                    Text("自选股（在自选页管理）", color = TextTertiary, fontSize = 13.sp)
                }
            }
        }
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
    }
}

@Composable
private fun StepperField(
    value: String, placeholder: String, border: Color, accent: Color,
    onMinus: () -> Unit, onPlus: () -> Unit, onValueChange: (String) -> Unit,
    keyboardType: KeyboardType, modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.heightIn(min = THSLayout.TouchTarget).border(1.dp, border, RoundedCornerShape(4.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(THSLayout.TouchTarget).clickable(onClick = onMinus), Alignment.Center) {
            Icon(Icons.Filled.Remove, "减少$placeholder", tint = accent, modifier = Modifier.size(20.dp))
        }
        Box(Modifier.width(1.dp).heightIn(min = THSLayout.TouchTarget).background(border))
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).semantics { contentDescription = placeholder },
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) Text(placeholder, color = TextTertiary, fontSize = 14.sp)
                        inner()
                    }
                },
            )
        }
        Box(Modifier.width(1.dp).heightIn(min = THSLayout.TouchTarget).background(border))
        Box(Modifier.size(THSLayout.TouchTarget).clickable(onClick = onPlus), Alignment.Center) {
            Icon(Icons.Filled.Add, "增加$placeholder", tint = accent, modifier = Modifier.size(20.dp))
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
    THSTabRow(tabs = tabs, selected = selected, onSelect = onSelect, selectedColor = THSRed)
}

@Composable
private fun OrderSubTabHeader(subTab: Int) {
    if (subTab != 0) return
    THSDataRow(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = 6.dp)) {
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
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).clickable(onClick = onClick).padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
        THSDataRow(modifier = Modifier.fillMaxWidth()) {
            Text(p.name, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(QuoteFormat.signed(p.profit), color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${p.shares}", color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(QuoteFormat.price(p.costPrice), color = color, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        THSDataRow(modifier = Modifier.fillMaxWidth()) {
            Text("%.2f".format(p.marketValue), color = color, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("%.3f%%".format(p.profitPercent), color = color, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${p.available}", color = color, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(QuoteFormat.price(p.currentPrice), color = color, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = THSLayout.PageGutter))
}

@Composable
private fun SimpleOrderRow(o: Order, showDate: Boolean = false) {
    val c = if (o.side == OrderSide.BUY) BuyRed else SellBlue
    Column(Modifier.fillMaxWidth().background(CardWhite)
        .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
        THSDataRow(Modifier.fillMaxWidth()) {
            Text(o.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                maxLines = 1, modifier = Modifier.weight(1.3f))
            Text(QuoteFormat.price(o.price), color = c, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${o.shares}", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text(o.status.label, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(THSLayout.SmallGap))
        Text("${o.side.label} ${if (showDate) recordDateTime(o.timestamp) else o.time}",
            color = TextSecondary, fontSize = 11.sp)
    }
    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = THSLayout.PageGutter))
}

@Composable
private fun SimpleTradeRow(t: TradeRecord, showDate: Boolean = false) {
    val c = if (t.side == OrderSide.BUY) BuyRed else SellBlue
    Column(Modifier.fillMaxWidth().background(CardWhite)
        .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
        THSDataRow(Modifier.fillMaxWidth()) {
            Text(t.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                maxLines = 1, modifier = Modifier.weight(1.3f))
            Text(QuoteFormat.price(t.price), color = c, fontSize = 14.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("${t.shares}", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            Text("%.2f".format(t.amount), color = TextPrimary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(THSLayout.SmallGap))
        Text("${t.side.label} ${if (showDate) recordDateTime(t.timestamp) else t.time}",
            color = TextSecondary, fontSize = 11.sp)
    }
    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = THSLayout.PageGutter))
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
    val todayTrades = state.todayTrades
    LazyColumn(modifier = Modifier.fillMaxWidth().background(BackgroundGray)) {
        item {
            THSDataRow(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
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
            Box(Modifier.fillMaxWidth().background(BackgroundGray).padding(vertical = THSLayout.ContentPadding), Alignment.Center) {
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
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
    }
}

@Composable
private fun CancelOrderRow(o: Order, onCancel: (String) -> Unit) {
    val c = if (o.side == OrderSide.BUY) BuyRed else SellBlue
    THSDataRow(
        modifier = Modifier.fillMaxWidth().background(CardWhite).clickable { onCancel(o.id) }.padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding),
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
    THSDataRow(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding), verticalAlignment = Alignment.CenterVertically) {
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
    LazyColumn(modifier = Modifier.fillMaxWidth().background(TradePageBackground)) {
        item { PositionAccountCard(state, positionRatio, onEditCapital) }
        item { Spacer(Modifier.height(THSLayout.SectionGap)) }
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
        item { PositionBottomActions() }
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
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
    val dayPercent = if (state.prevMarketValue > 0) state.dayProfit / state.prevMarketValue * 100 else 0.0
    val profitColor = if (state.totalProfit >= 0) TradeProfitRed else SellBlue
    val dayColor = if (state.dayProfit >= 0) TradeProfitRed else SellBlue
    Column(Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 36.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChineseMarketFlag()
            Text("人民币账户A股", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("仓位 %.1f%%".format(ratio), color = PositionBodyColor, fontSize = 11.sp,
                modifier = Modifier.background(TradePageBackground, RoundedCornerShape(2.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
        HorizontalDivider(color = DividerGray)
        Column(Modifier.padding(top = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            THSDataRow(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                PositionMetric("总资产", positionMoney(state.totalAsset), Modifier.weight(1f).clickable(onClick = onEditCapital), icon = Icons.Outlined.Visibility)
                PositionMetric("浮动盈亏", positionMoney(state.totalProfit, signed = true), Modifier.weight(1f), color = profitColor)
                PositionMetric("当日参考盈亏", positionMoney(state.dayProfit, signed = true), Modifier.weight(1f),
                    color = dayColor, suffix = "%.2f%%".format(dayPercent), icon = Icons.Outlined.Info)
            }
            THSDataRow(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                PositionMetric("总市值", positionMoney(state.marketValue), Modifier.weight(1f))
                PositionMetric("可用", positionMoney(state.cash), Modifier.weight(1f), tag = "逆回购")
                PositionMetric("可取", positionMoney(state.cash), Modifier.weight(1f), tag = "转账", icon = Icons.Outlined.Info)
            }
        }
    }
}











@Composable
private fun PositionTableHeader() {
    Column(Modifier.fillMaxWidth().background(CardWhite)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = THSLayout.PageGutter),
            verticalAlignment = Alignment.CenterVertically) {
            Text("持仓股", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ShowChart, "持仓分时开关", tint = PositionLabelColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(20.dp))
            Icon(Icons.Outlined.BarChart, "持仓报价模式开关", tint = PositionLabelColor, modifier = Modifier.size(22.dp))
        }
        HorizontalDivider(color = DividerGray)
        THSDataRow(Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter, vertical = 10.dp)) {
            PositionColumnLabel("市值", Modifier.weight(1.1f), start = true, active = true)
            PositionColumnLabel("盈亏", Modifier.weight(0.8f))
            PositionColumnLabel("持仓/可用", Modifier.weight(1f))
            PositionColumnLabel("成本/现价", Modifier.weight(1.1f))
        }
        HorizontalDivider(color = DividerGray)
    }
}

@Composable
private fun PositionColumnLabel(label: String, modifier: Modifier, start: Boolean = false, active: Boolean = false) {
    Row(modifier, horizontalArrangement = if (start) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = PositionLabelColor, fontSize = 13.sp, maxLines = 1)
        Spacer(Modifier.width(3.dp))
        PositionSortMark(active)
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
    val color = if (p.profit >= 0) TradeProfitRed else SellBlue
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (expanded) Color(0xFFF7F8FA) else CardWhite)
            .clickable(onClick = onToggle)
            .padding(horizontal = THSLayout.PageGutter, vertical = 8.dp),
    ) {
        THSDataRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                p.name, color = color, fontWeight = FontWeight.Medium, fontSize = 16.sp,
                maxLines = 1, modifier = Modifier.weight(1.1f),
            )
            PositionTableNumber(positionMoney(p.profit), color, Modifier.weight(0.8f))
            PositionTableNumber("${p.shares}", color, Modifier.weight(1f))
            PositionTableNumber("%.3f".format(p.costPrice), color, Modifier.weight(1.1f))
        }
        Spacer(Modifier.height(THSLayout.SmallGap))
        THSDataRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PositionAmount(positionMoney(p.marketValue), Modifier.weight(1.1f), color, maxFontSize = 16f)
            PositionTableNumber("%.3f%%".format(p.profitPercent), color, Modifier.weight(0.8f))
            PositionTableNumber("${p.available}", color, Modifier.weight(1f))
            PositionTableNumber("%.3f".format(p.currentPrice), color, Modifier.weight(1.1f))
        }
    }
    HorizontalDivider(color = DividerGray)
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
private fun PositionTableNumber(value: String, color: Color, modifier: Modifier) {
    PositionAmount(value, modifier, color, maxFontSize = 16f, alignEnd = true)
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.PositionAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = THSLayout.ContentPadding),
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
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
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
                    Spacer(Modifier.height(THSLayout.ItemGap))
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
                    Row(horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap)) {
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
    THSAdaptiveSplit(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        primaryWeight = 0.5f, minPrimaryWidth = 150.dp, minSecondaryWidth = 150.dp,
        primary = { DetailValuePair(label1, value1, valueColor, it) },
        secondary = { if (label2.isNotEmpty()) DetailValuePair(label2, value2, valueColor, it) })
}

@Composable
private fun DetailValuePair(label: String, value: String, valueColor: Color, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = TextTertiary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
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
    THSActionButton(text, background, modifier, textColor, onClick)
}

@Composable
private fun ClearedRow() {
    Row(Modifier.fillMaxWidth().background(TradePageBackground).clickable {}.padding(horizontal = THSLayout.PageGutter, vertical = 12.dp),
        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Text("查看已清仓股票", color = PositionLabelColor, fontSize = 13.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PositionLabelColor, modifier = Modifier.size(18.dp))
    }
}


@Composable
private fun PositionBottomActions() {
    Column(Modifier.fillMaxWidth().background(TradePageBackground)) {
        Row(Modifier.fillMaxWidth().background(CardWhite).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("持仓管理", "批量买入", "批量卖出", "止盈止损").forEachIndexed { index, label ->
                if (index > 0) Box(Modifier.width(1.dp).height(14.dp).background(DividerGray))
                Box(Modifier.weight(1f).heightIn(min = 48.dp).clickable {}, Alignment.Center) {
                    Text(label, color = PositionBodyColor, fontSize = 14.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text("持仓资讯", color = LinkBlue, fontSize = 14.sp, modifier = Modifier.clickable {}.padding(horizontal = 12.dp, vertical = 4.dp))
            Box(Modifier.width(1.dp).height(12.dp).background(Color(0xFFD3D3D3)))
            Text("资产分析", color = LinkBlue, fontSize = 14.sp, modifier = Modifier.clickable {}.padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}


private fun recordDateTime(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }.format(Date(timestamp))

/* ========================= 查询页 ========================= */

@Composable
private fun QueryPage(state: TradeUiState) {
    val queryScroll = rememberScrollState()
    var selectedQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val groups = listOf(
        listOf("当日成交" to state.todayTrades.size, "当日委托" to state.todayOrders.size),
        listOf("历史成交" to state.historyTrades.size, "历史委托" to state.historyOrders.size),
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
                            modifier = Modifier.fillMaxWidth().clickable { if (count >= 0) selectedQuery = title }.padding(horizontal = THSLayout.PageGutter, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(title, color = TextPrimary, fontSize = 15.sp)
                            if (count > 0) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "$count", color = THSRed, fontSize = 12.sp,
                                    modifier = Modifier.clip(RoundedCornerShape(THSLayout.CardRadius)).background(Color(0x1AE63946)).padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                        }
                        if (idx < group.size - 1) HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = THSLayout.PageGutter))
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
        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
    }
    selectedQuery?.let { title ->
        val orders = when (title) {
            "当日委托" -> state.todayOrders
            "历史委托" -> state.historyOrders
            else -> emptyList()
        }
        val trades = when (title) {
            "当日成交" -> state.todayTrades
            "历史成交" -> state.historyTrades
            else -> emptyList()
        }
        CompositionLocalProvider(LocalTableScroll provides queryScroll) {
        Dialog(onDismissRequest = { selectedQuery = null }) {
            Surface(shape = RoundedCornerShape(12.dp), color = CardWhite) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(start = THSLayout.PageGutter),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        androidx.compose.material3.TextButton(onClick = { selectedQuery = null }) { Text("关闭") }
                    }
                    HorizontalDivider(color = DividerGray)
                    THSDataRow(Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter, vertical = 8.dp)) {
                        Text("证券", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                        Text("价格", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("数量", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text(if (title.endsWith("委托")) "状态" else "金额", color = TextSecondary, fontSize = 11.sp,
                            textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                        if (orders.isEmpty() && trades.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                                    Text("暂无记录", color = TextSecondary)
                                }
                            }
                        }
                        items(orders.size) { SimpleOrderRow(orders[it], showDate = true) }
                        items(trades.size) { SimpleTradeRow(trades[it], showDate = true) }
                    }
                }
            }
        }
        }
    }

}

/* ========================= 账户切换弹层 ========================= */

@Composable
private fun AccountSheet(onDismiss: () -> Unit, onManage: () -> Unit, onToast: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter, vertical = 12.dp),
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
                    modifier = Modifier.fillMaxWidth().clickable { onToast("已切换到 $BrokerName") }.padding(horizontal = THSLayout.PageGutter, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(BuyRed), contentAlignment = Alignment.Center) {
                        Text(BrokerShort, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(BrokerName, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("资金账户: $BrokerAccount", color = TextTertiary, fontSize = 11.sp)
                    }
                    Icon(Icons.Filled.Check, null, tint = THSRed, modifier = Modifier.size(18.dp))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter, vertical = 16.dp)) {
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
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = THSLayout.PageGutter, vertical = 16.dp)) {
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(THSLayout.ControlRadius)).background(THSRed)
                        .clickable { input.toDoubleOrNull()?.let(onSetTotal) }.padding(vertical = 12.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "重置账户（清空持仓，全部作为现金）", color = THSRed, fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(THSLayout.ControlRadius))
                        .clickable { input.toDoubleOrNull()?.let(onReset) }.padding(vertical = THSLayout.ContentPadding),
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
