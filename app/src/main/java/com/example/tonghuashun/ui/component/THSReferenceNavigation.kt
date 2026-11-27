package com.example.tonghuashun.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tonghuashun.ui.theme.THSLayout
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TradeHeaderRed
import com.example.tonghuashun.ui.theme.TradePageBackground

/** 参考图中的线性导航图标，交易项使用红底人民币符号。 */
@Composable
fun THSReferenceNavigation(items: List<Pair<String, String>>, selectedRoute: String?, onSelect: (String) -> Unit) {
    Box(Modifier.fillMaxWidth().background(TradePageBackground).navigationBarsPadding(), contentAlignment = Alignment.Center) {
        Row(Modifier.widthIn(max = THSLayout.MaxContentWidth).fillMaxWidth().selectableGroup()) {
            items.forEach { (route, label) ->
                val selected = route == selectedRoute
                Column(Modifier.weight(1f).selectable(selected, role = Role.Tab, onClick = { onSelect(route) })
                    .heightIn(min = 64.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    ReferenceNavigationIcon(route, selected)
                    Spacer(Modifier.height(4.dp))
                    Text(label, color = if (selected) TradeHeaderRed else TextPrimary, fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ReferenceNavigationIcon(route: String, selected: Boolean) {
    val color = if (selected) TradeHeaderRed else TextPrimary
    Canvas(Modifier.size(24.dp)) {
        withTransform({ scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) }) {
            val stroke = Stroke(1.3f, cap = StrokeCap.Round)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float, tint: Color = color) =
                drawLine(tint, Offset(x1, y1), Offset(x2, y2), 1.3f, StrokeCap.Round)
            fun path(vararg points: Pair<Float, Float>, close: Boolean = false) {
                val p = Path().apply {
                    points.forEachIndexed { index, (x, y) -> if (index == 0) moveTo(x, y) else lineTo(x, y) }
                    if (close) close()
                }
                drawPath(p, color, style = stroke)
            }
            when (route) {
                "home" -> {
                    line(4f, 11f, 4f, 22f); drawRect(color, Offset(2f, 14f), Size(4f, 5f), style = stroke)
                    line(12f, 6f, 12f, 21f); drawRect(color, Offset(10f, 9f), Size(4f, 8f), style = stroke)
                    line(20f, 1f, 20f, 17f); drawRect(color, Offset(18f, 4f), Size(4f, 9f), style = stroke)
                }
                "market" -> {
                    drawRoundRect(color, Offset(2f, 3f), Size(20f, 18f), CornerRadius(1.5f), style = stroke)
                    path(6f to 16f, 10f to 11f, 14f to 13f, 19f to 8f)
                }
                "watchlist" -> {
                    drawCircle(color, 3.2f, Offset(12f, 6f), style = stroke)
                    path(9f to 8f, 9f to 12f, 3f to 16f, 3f to 22f, 21f to 22f, 21f to 16f, 15f to 12f, 15f to 8f)
                    path(9f to 16f, 12f to 19f, 15f to 16f)
                }
                "trade" -> {
                    if (selected) drawCircle(color, 11.5f, Offset(12f, 12f))
                    else drawCircle(color, 11.5f, Offset(12f, 12f), style = stroke)
                    val ink = if (selected) Color.White else color
                    line(8f, 6f, 12f, 11f, ink); line(16f, 6f, 12f, 11f, ink)
                    line(8f, 12f, 16f, 12f, ink); line(8f, 15f, 16f, 15f, ink); line(12f, 11f, 12f, 19f, ink)
                }
                "news" -> {
                    drawRoundRect(color, Offset(4f, 2f), Size(16f, 20f), CornerRadius(1.5f), style = stroke)
                    listOf(7f, 11f, 15f).forEach { line(7f, it, 17f, it) }
                }
                else -> {
                    path(8f to 2f, 16f to 2f, 15f to 5f, 9f to 5f, close = true)
                    val bag = Path().apply {
                        moveTo(9f, 6f); cubicTo(4f, 11f, 1f, 21f, 8f, 22f)
                        lineTo(16f, 22f); cubicTo(23f, 21f, 20f, 11f, 15f, 6f); close()
                    }
                    drawPath(bag, color, style = stroke)
                    path(12f to 11f, 15f to 15f, 12f to 19f, 9f to 15f, close = true)
                }
            }
        }
    }
}
