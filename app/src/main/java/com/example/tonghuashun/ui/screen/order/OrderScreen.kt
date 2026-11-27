package com.example.tonghuashun.ui.screen.order

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tonghuashun.data.model.Order
import com.example.tonghuashun.data.model.OrderBook
import com.example.tonghuashun.data.model.OrderBookLevel
import com.example.tonghuashun.data.model.OrderSide
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.ui.component.THSActionButton
import com.example.tonghuashun.ui.component.THSIconButton
import com.example.tonghuashun.ui.component.THSPageHeader
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.THSLayout
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.util.QuoteFormat
import kotlinx.coroutines.launch

@Composable
fun OrderScreen(
    onBack: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val shares by viewModel.shares.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val side = viewModel.side
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }

    val sideColor = if (side == OrderSide.BUY) StockUp else StockDown

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        // 外层 Scaffold 已处理过状态栏内边距，这里清零避免顶格多出一截空白
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).background(BackgroundGray).imePadding()) {
            THSPageHeader(title = side.label, subtitle = state.stock?.let { "${it.name} ${it.code}" },
                background = sideColor,
                navigation = { THSIconButton(Icons.AutoMirrored.Filled.ArrowBack, "返回", onBack) })

            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = sideColor) }
                state.stock == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message ?: "加载失败", color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text("点击重试", color = sideColor, modifier = Modifier.clickable { viewModel.load() })
                    }
                }
                else -> {
                    val stock = state.stock!!
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        item { StockPriceBar(stock) }
                        item { Spacer(Modifier.height(THSLayout.SectionGap)) }
                        item {
                            OrderForm(
                                side = side,
                                price = price,
                                shares = shares,
                                stock = stock,
                                available = state.available,
                                availableShares = state.availableShares,
                                estimated = viewModel.estimatedAmount(),
                                onPriceStep = { viewModel.stepPrice(it) },
                                onPriceInput = { viewModel.setPrice(it) },
                                onSharesStep = { viewModel.stepShares(it) },
                                onSharesInput = { viewModel.setShares(it) },
                                onRatio = { viewModel.setByRatio(it) },
                            )
                        }
                        item { Spacer(Modifier.height(THSLayout.SectionGap)) }
                        item { MiniOrderBook(state.orderBook, code = stock.code, onPick = { viewModel.setPriceToLevel(it) }) }
                        item { Spacer(Modifier.height(THSLayout.SectionGap)) }
                        item { TodayOrders(orders) }
                        item { Spacer(Modifier.height(THSLayout.PageBottom)) }
                    }
                    // 底部提交按钮
                    SubmitBar(side, sideColor) {
                        if (shares <= 0) {
                            scope.launch { snackbar.showMessage("请输入委托数量") }
                        } else {
                            showConfirm = true
                        }
                    }
                }
            }
        }
    }

    if (showConfirm && state.stock != null) {
        ConfirmDialog(
            side = side,
            sideColor = sideColor,
            name = state.stock!!.name,
            code = state.stock!!.fullCode,
            price = if (price > 0) price else state.stock!!.price,
            shares = shares,
            amount = viewModel.estimatedAmount(),
            onConfirm = {
                showConfirm = false
                val msg = viewModel.submit()
                scope.launch { snackbar.showMessage(msg) }
            },
            onDismiss = { showConfirm = false },
        )
    }
}

private suspend fun SnackbarHostState.showMessage(msg: String) {
    currentSnackbarData?.dismiss()
    showSnackbar(msg)
}

@Composable
private fun StockPriceBar(stock: Stock) {
    val color = QuoteFormat.colorOf(stock.change)
    Row(
        modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(QuoteFormat.price(stock.code, stock.price), color = color, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.width(10.dp))
        Text((if (stock.change > 0) "+" else "") + QuoteFormat.price(stock.code, stock.change), color = color, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(QuoteFormat.percent(stock.changePercent), color = color, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("涨停 ${QuoteFormat.price(stock.code, stock.prevClose * 1.1)}", color = StockUp, fontSize = 11.sp)
            Text("跌停 ${QuoteFormat.price(stock.code, stock.prevClose * 0.9)}", color = StockDown, fontSize = 11.sp)
        }
    }
}

@Composable
private fun OrderForm(
    side: OrderSide,
    price: Double,
    shares: Long,
    stock: Stock,
    available: Double,
    availableShares: Long,
    estimated: Double,
    onPriceStep: (Double) -> Unit,
    onPriceInput: (Double) -> Unit,
    onSharesStep: (Long) -> Unit,
    onSharesInput: (Long) -> Unit,
    onRatio: (Double) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
        // 委托价
        StepperRow(
            label = "委托价",
            value = if (price > 0) QuoteFormat.price(stock.code, price) else "",
            hint = "市价",
            onMinus = { onPriceStep(if (QuoteFormat.priceDecimals(stock.code) == 3) -0.001 else -0.01) },
            onPlus = { onPriceStep(if (QuoteFormat.priceDecimals(stock.code) == 3) 0.001 else 0.01) },
            onInput = { it.toDoubleOrNull()?.let(onPriceInput) },
            keyboardType = KeyboardType.Decimal,
        )
        Spacer(Modifier.height(12.dp))
        // 委托量
        StepperRow(
            label = "委托量",
            value = if (shares > 0) shares.toString() else "",
            hint = "股",
            onMinus = { onSharesStep(-100) },
            onPlus = { onSharesStep(100) },
            onInput = { it.toLongOrNull()?.let(onSharesInput) },
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(12.dp))
        // 仓位快捷键
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("全仓" to 1.0, "1/2" to 0.5, "1/3" to (1.0 / 3), "1/4" to 0.25).forEach { (label, r) ->
                Box(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, DividerGray, RoundedCornerShape(4.dp))
                        .clickable { onRatio(r) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = DividerGray)
        Spacer(Modifier.height(THSLayout.ItemGap))
        // 可用信息
        Row(modifier = Modifier.fillMaxWidth()) {
            if (side == OrderSide.BUY) {
                InfoCol("可用资金", "%.2f".format(available), Modifier.weight(1f))
                InfoCol("可买", "${maxBuyable(available, price, stock.price)}股", Modifier.weight(1f))
            } else {
                InfoCol("可卖", "${availableShares}股", Modifier.weight(1f))
                InfoCol("现价", QuoteFormat.price(stock.code, stock.price), Modifier.weight(1f))
            }
            InfoCol("预估金额", "%.2f".format(estimated), Modifier.weight(1f))
        }
    }
}

private fun maxBuyable(cash: Double, price: Double, marketPrice: Double): Long {
    val p = if (price > 0) price else marketPrice
    if (p <= 0) return 0
    return (cash / p).toLong() / 100 * 100
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    hint: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onInput: (String) -> Unit,
    keyboardType: KeyboardType,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(56.dp))
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                .clip(RoundedCornerShape(THSLayout.ControlRadius))
                .border(1.dp, DividerGray, RoundedCornerShape(THSLayout.ControlRadius)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepBtn(minus = true, onClick = onMinus)
            HorizontalDividerVertical()
            TextField(
                value = value,
                onValueChange = onInput,
                singleLine = true,
                placeholder = { Text(hint, color = TextTertiary, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
            HorizontalDividerVertical()
            StepBtn(minus = false, onClick = onPlus)
        }
    }
}

@Composable
private fun HorizontalDividerVertical() {
    Box(modifier = Modifier.width(1.dp).height(THSLayout.TouchTarget).background(DividerGray))
}

@Composable
private fun StepBtn(minus: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(THSLayout.TouchTarget).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (minus) Icons.Filled.Remove else Icons.Filled.Add,
            contentDescription = if (minus) "减少" else "增加", tint = TextSecondary, modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun InfoCol(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
    }
}

@Composable
private fun MiniOrderBook(orderBook: OrderBook, code: String, onPick: (Double) -> Unit) {
    if (orderBook.asks.isEmpty() && orderBook.bids.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = THSLayout.PageGutter, vertical = 12.dp)) {
        Text("五档盘口（点击填入委托价）", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        orderBook.asks.forEach { LevelRow(it, StockUp, code, onPick) }
        HorizontalDivider(color = DividerGray, modifier = Modifier.padding(vertical = 6.dp))
        orderBook.bids.forEach { LevelRow(it, StockDown, code, onPick) }
    }
}

@Composable
private fun LevelRow(level: OrderBookLevel, color: Color, code: String, onPick: (Double) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onPick(level.price) }.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(level.label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(QuoteFormat.price(code, level.price), color = color, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text("${level.volume}", color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TodayOrders(orders: List<Order>) {
    Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(vertical = 8.dp)) {
        Text("当日委托", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(horizontal = THSLayout.PageGutter, vertical = 6.dp))
        HorizontalDivider(color = DividerGray)
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                Text("暂无委托", color = TextTertiary, fontSize = 13.sp)
            }
        } else {
            orders.forEach { o ->
                val c = if (o.side == OrderSide.BUY) StockUp else StockDown
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(o.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("${o.side.label} ${o.time}", color = TextTertiary, fontSize = 11.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(QuoteFormat.price(o.code, o.price), color = c, fontSize = 14.sp)
                        Text("${o.shares}股", color = TextSecondary, fontSize = 11.sp)
                    }
                    Text(o.status.label, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
                }
                HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = THSLayout.PageGutter))
            }
        }
    }
}

@Composable
private fun SubmitBar(side: OrderSide, sideColor: Color, onSubmit: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(CardWhite)
        .padding(horizontal = THSLayout.PageGutter, vertical = THSLayout.ContentPadding)) {
        THSActionButton(side.label, sideColor, Modifier.fillMaxWidth(), onClick = onSubmit)
    }
}

@Composable
private fun ConfirmDialog(
    side: OrderSide,
    sideColor: Color,
    name: String,
    code: String,
    price: Double,
    shares: Long,
    amount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${side.label}确认", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ConfirmRow("股票", "$name  $code")
                ConfirmRow("方向", side.label)
                ConfirmRow("委托价", QuoteFormat.price(code, price))
                ConfirmRow("委托量", "${shares}股")
                ConfirmRow("预估金额", "%.2f 元".format(amount))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认${side.label}", color = sideColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = TextSecondary) }
        },
    )
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.width(72.dp))
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
