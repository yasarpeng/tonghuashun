package com.example.tonghuashun.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = THSRed,
    onPrimary = Color.White,
    secondary = THSRedDark,
    background = BackgroundGray,
    surface = CardWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val DarkColors = darkColorScheme(
    primary = THSRed,
    onPrimary = Color.White,
    secondary = THSRedDark,
)

@Composable
fun TongHuaShunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 高仿同花顺，默认使用浅色行情风格
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val gutter = if (LocalConfiguration.current.screenWidthDp >= 600) 24.dp else 16.dp
    CompositionLocalProvider(LocalPageGutter provides gutter) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
