package com.example.tonghuashun.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

/** 底部导航 6 Tab：首页 / 行情 / 自选 / 交易 / 资讯 / 理财 */
enum class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home("home", "首页", Icons.Filled.Home),
    Market("market", "行情", Icons.Filled.ShowChart),
    Watchlist("watchlist", "自选", Icons.Filled.Person),
    Trade("trade", "交易", Icons.Filled.MonetizationOn),
    News("news", "资讯", Icons.Filled.Article),
    Finance("finance", "理财", Icons.Filled.CandlestickChart),
}

object Routes {
    const val STOCK_DETAIL = "stock_detail"
    const val STOCK_DETAIL_ARG = "code"
    const val STOCK_DETAIL_PATTERN = "$STOCK_DETAIL/{$STOCK_DETAIL_ARG}"
    fun stockDetail(fullCode: String) = "$STOCK_DETAIL/$fullCode"

    // 下单页：order/{code}/{side}  side = buy | sell
    const val ORDER = "order"
    const val ORDER_ARG_CODE = "code"
    const val ORDER_ARG_SIDE = "side"
    const val ORDER_PATTERN = "$ORDER/{$ORDER_ARG_CODE}/{$ORDER_ARG_SIDE}"
    fun order(fullCode: String, side: String) = "$ORDER/$fullCode/$side"
}
