package com.example.tonghuashun.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.tonghuashun.ui.component.THSReferenceNavigation
import com.example.tonghuashun.ui.theme.TradeHeaderRed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.THSLayout

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevelRoutes = TopDestination.entries.map { it.route }
    val showBottomBar = currentRoute in topLevelRoutes

    val isTrade = currentRoute == TopDestination.Trade.route
    val isQuoteDetail = currentRoute == Routes.STOCK_DETAIL_PATTERN
    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isTrade && !isQuoteDetail
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isQuoteDetail
        }
    }
    Scaffold(
        contentWindowInsets = if (isQuoteDetail) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        containerColor = if (isTrade) TradeHeaderRed else BackgroundGray,
        bottomBar = {
            if (showBottomBar) {
                THSReferenceNavigation(TopDestination.entries.map { it.route to it.label }, currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(TopDestination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding).background(BackgroundGray), contentAlignment = Alignment.TopCenter) {
            NavHost(
                navController = navController,
                startDestination = TopDestination.Home.route,
                modifier = Modifier.widthIn(max = THSLayout.MaxContentWidth).fillMaxSize(),
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
                    onNavigateStock = { code ->
                        navController.navigate(Routes.stockDetail(code)) {
                            popUpTo(Routes.STOCK_DETAIL_PATTERN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
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
}
