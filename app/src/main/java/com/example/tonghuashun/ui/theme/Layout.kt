package com.example.tonghuashun.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 保留行情页的信息密度，为所有页面提供同一套布局尺寸。 */
object THSLayout {
    val PageGutter: Dp
        @Composable get() = LocalPageGutter.current
    val SectionGap = 8.dp
    val ContentPadding = 12.dp
    val ItemGap = 8.dp
    val SmallGap = 4.dp
    val HeaderHeight = 56.dp
    val TouchTarget = 48.dp
    val SearchHeight = 36.dp
    val RowHeight = 64.dp
    val CardRadius = 8.dp
    val ControlRadius = 6.dp
    val PageBottom = 24.dp
    val MaxContentWidth = 840.dp
}

val LocalPageGutter = staticCompositionLocalOf { 16.dp }
