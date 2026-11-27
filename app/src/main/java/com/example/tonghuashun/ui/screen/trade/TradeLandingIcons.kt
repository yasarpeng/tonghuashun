package com.example.tonghuashun.ui.screen.trade

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class LandingFeature { IPO, REPO, CONDITION, MARGIN, STAR, NOTES, HONG_KONG, PORTFOLIO, MARKET, SERVICES, ETF, MORE }

private val FeatureBlue = Color(0xFF4D98E2)
private val FeatureOrange = Color(0xFFFF8A19)
private val FeaturePurple = Color(0xFF6652CF)

/** 首页服务入口使用统一尺寸的原生矢量图形，品牌标识由账户区单独呈现。 */
@Composable
internal fun LandingFeatureIcon(feature: LandingFeature, modifier: Modifier = Modifier) {
    // 图形内的字样属于图标；服务名称在相邻文本中按系统字号缩放。
    val iconFontScale = LocalDensity.current.fontScale
    when (feature) {
        LandingFeature.IPO -> Box(modifier.background(FeatureBlue, CircleShape), contentAlignment = Alignment.Center) {
            Text("IPO", color = Color.White, fontSize = (12f / iconFontScale).sp, fontWeight = FontWeight.Medium)
        }
        LandingFeature.MARGIN, LandingFeature.STAR, LandingFeature.HONG_KONG -> {
            val glyph = when (feature) { LandingFeature.MARGIN -> "融"; LandingFeature.STAR -> "科"; else -> "港" }
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(glyph, color = if (feature == LandingFeature.MARGIN) FeatureOrange else FeatureBlue,
                    fontSize = (27f / iconFontScale).sp, lineHeight = (28f / iconFontScale).sp, fontWeight = FontWeight.Bold)
            }
        }
        LandingFeature.ETF -> Box(modifier.background(FeatureOrange, RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
            Text("T+0", color = Color.White, fontSize = (11f / iconFontScale).sp, fontWeight = FontWeight.Bold)
        }
        LandingFeature.REPO -> Canvas(modifier) {
            val w = size.width
            val h = size.height
            val ticket = Path().apply {
                moveTo(0f, h * .2f); lineTo(w, h * .2f); lineTo(w, h * .4f)
                cubicTo(w * .86f, h * .4f, w * .86f, h * .6f, w, h * .6f)
                lineTo(w, h * .8f); lineTo(0f, h * .8f); lineTo(0f, h * .6f)
                cubicTo(w * .14f, h * .6f, w * .14f, h * .4f, 0f, h * .4f); close()
            }
            drawPath(ticket, Color(0xFFFFAB16))
            repeat(3) { i -> drawLine(Color.White, Offset(w * .35f, h * (.28f + i * .17f)), Offset(w * .35f, h * (.37f + i * .17f)), 1.dp.toPx()) }
        }
        else -> {
            val icon = when (feature) {
                LandingFeature.CONDITION -> Icons.Outlined.Lightbulb
                LandingFeature.NOTES -> Icons.Outlined.Assignment
                LandingFeature.PORTFOLIO -> Icons.Outlined.ViewInAr
                LandingFeature.MARKET -> Icons.Outlined.ShowChart
                LandingFeature.SERVICES -> Icons.Outlined.ListAlt
                else -> Icons.Outlined.EmojiEvents
            }
            val color = when (feature) {
                LandingFeature.CONDITION -> FeaturePurple
                LandingFeature.PORTFOLIO, LandingFeature.MARKET, LandingFeature.MORE -> FeatureOrange
                else -> FeatureBlue
            }
            if (feature in listOf(LandingFeature.CONDITION, LandingFeature.MARKET)) {
                Icon(icon, null, tint = Color.White, modifier = modifier.background(color, RoundedCornerShape(2.dp)).padding(2.dp))
            } else Icon(icon, null, tint = color, modifier = modifier)
        }
    }
}

/** 撤单、持仓和查询与买卖圆环保持同一笔画粗细。 */
@Composable
internal fun LandingQuickIcon(index: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val factor = size.width / 30f
        scale(factor, factor, pivot = Offset.Zero) {
            val c = PositionBodyColor
            val stroke = Stroke(0.9f)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(c, Offset(x1, y1), Offset(x2, y2), 0.9f)
            when (index) {
                2 -> {
                    drawRect(c, Offset(6f, 5f), Size(18f, 23f), style = stroke)
                    drawRect(Color.White, Offset(11f, 2f), Size(8f, 5f))
                    drawRect(c, Offset(11f, 2f), Size(8f, 5f), style = stroke)
                    line(6f, 9f, 24f, 9f); line(11f, 15f, 19f, 23f); line(19f, 15f, 11f, 23f)
                }
                3 -> {
                    drawOval(c, Offset(6f, 3f), Size(18f, 6f), style = stroke)
                    line(6f, 6f, 6f, 25f); line(24f, 6f, 24f, 25f)
                    listOf(12f, 18f, 24f).forEach { y ->
                        drawArc(c, 0f, 180f, false, Offset(6f, y - 3f), Size(18f, 6f), style = stroke)
                    }
                }
                else -> {
                    val page = Path().apply { moveTo(6f, 2f); lineTo(18f, 2f); lineTo(25f, 9f); lineTo(25f, 28f); lineTo(6f, 28f); close() }
                    drawPath(page, c, style = stroke)
                    line(18f, 2f, 18f, 9f); line(18f, 9f, 25f, 9f)
                    listOf(13f, 17f, 21f).forEach { line(10f, it, 21f, it) }
                }
            }
        }
    }
}
