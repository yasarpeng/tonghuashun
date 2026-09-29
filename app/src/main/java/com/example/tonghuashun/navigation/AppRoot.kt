package com.example.tonghuashun.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tonghuashun.ui.screen.detail.StockDetailScreen
import com.example.tonghuashun.ui.screen.finance.FinanceScreen
import com.example.tonghuashun.ui.screen.home.HomeScreen
import com.example.tonghuashun.ui.screen.market.MarketScreen
import com.example.tonghuashun.ui.screen.news.NewsScreen
import com.example.tonghuashun.ui.screen.order.OrderScreen
import com.example.tonghuashun.ui.screen.trade.TradeScreen
import com.example.tonghuashun.ui.screen.watchlist.WatchlistScreen
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.ui.theme.THSRed

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevelRoutes = TopDestination.entries.map { it.route }
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // tonalElevation = 0：关闭 M3 默认的表面色调叠加，
                // 否则白色底栏会被 primary(同花顺红) 染成粉白，与真机纯白底栏不符。
                NavigationBar(containerColor = CardWhite, tonalElevation = 0.dp) {
                    TopDestination.entries.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(TopDestination.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = THSRed,
                                selectedTextColor = THSRed,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = CardWhite,
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopDestination.Home.route) {
                HomeScreen(onStockClick = { navController.navigate(Routes.stockDetail(it)) })
            }
            composable(TopDestination.Market.route) {
                MarketScreen(onStockClick = { navController.navigate(Routes.stockDetail(it)) })
            }
            composable(TopDestination.Watchlist.route) {
                WatchlistScreen(onStockClick = { navController.navigate(Routes.stockDetail(it)) })
            }
            composable(TopDestination.Trade.route) {
                TradeScreen(
                    onStockClick = { navController.navigate(Routes.stockDetail(it)) },
                    onOrder = { code, side -> navController.navigate(Routes.order(code, side)) },
                )
            }
            composable(TopDestination.News.route) {
                NewsScreen()
            }
            composable(TopDestination.Finance.route) {
                FinanceScreen()
            }
            composable(
                route = Routes.STOCK_DETAIL_PATTERN,
                arguments = listOf(navArgument(Routes.STOCK_DETAIL_ARG) { type = NavType.StringType }),
            ) {
                StockDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOrder = { code, side -> navController.navigate(Routes.order(code, side)) },
                )
            }
            composable(
                route = Routes.ORDER_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ORDER_ARG_CODE) { type = NavType.StringType },
                    navArgument(Routes.ORDER_ARG_SIDE) { type = NavType.StringType },
                ),
            ) {
                OrderScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
