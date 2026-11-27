package com.example.tonghuashun.ui.screen.trade

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TradeProfitRed
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

internal val PositionLabelColor = Color(0xFF999999)
internal val PositionBodyColor = Color(0xFF666666)

internal fun positionMoney(value: Double, signed: Boolean = false): String =
    (if (signed && value > 0) "+" else "") + String.format(Locale.CHINA, "%,.2f", value)

/** 三列资产指标采用同一基线；长金额只在需要时收紧字号，保持完整显示。 */
@Composable
internal fun PositionAmount(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    suffix: String? = null,
    maxFontSize: Float = 18f,
    alignEnd: Boolean = false,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val numberStyle = TextStyle(fontSize = maxFontSize.sp, fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif, fontFeatureSettings = "tnum")
    val suffixStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
    BoxWithConstraints(modifier) {
        val numberWidth = measurer.measure(value, numberStyle, maxLines = 1).size.width.coerceAtLeast(1)
        val suffixWidth = suffix?.let { measurer.measure(it, suffixStyle, maxLines = 1).size.width } ?: 0
        val room = with(density) { maxWidth.toPx() - if (suffix == null) 0f else 2.dp.toPx() } - suffixWidth
        val fontSize = (maxFontSize * (room / numberWidth).coerceAtMost(1f)).coerceAtLeast(11f).sp
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp, if (alignEnd) androidx.compose.ui.Alignment.End else androidx.compose.ui.Alignment.Start)) {
            Text(value, color = color, style = numberStyle.copy(fontSize = fontSize), maxLines = 1,
                modifier = Modifier.alignByBaseline())
            if (suffix != null) Text(suffix, color = color, style = suffixStyle, maxLines = 1,
                modifier = Modifier.alignByBaseline())
        }
    }
}

@Composable
internal fun PositionMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    suffix: String? = null,
    tag: String? = null,
    icon: ImageVector? = null,
) {
    Column(modifier.heightIn(min = 48.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(label, color = PositionLabelColor, fontSize = 12.sp, maxLines = 1)
            if (tag != null) Text("$tag›", color = Color(0xFFE47D39), fontSize = 11.sp, maxLines = 1,
                modifier = Modifier.background(Color(0xFFFAFAFA)).padding(horizontal = 4.dp, vertical = 1.dp))
            if (icon != null) Icon(icon, null, tint = PositionLabelColor, modifier = Modifier.size(14.dp))
        }
        PositionAmount(value, color = color, suffix = suffix)
    }
}

@Composable
internal fun ChineseMarketFlag() {
    Canvas(Modifier.width(15.dp).height(10.dp)) {
        drawRoundRect(Color(0xFFDE2910), cornerRadius = CornerRadius(1.dp.toPx()))
        fun star(x: Float, y: Float, radius: Float) {
            val path = Path()
            repeat(10) { index ->
                val angle = -Math.PI / 2 + index * Math.PI / 5
                val r = if (index % 2 == 0) radius else radius * 0.4f
                val px = x + cos(angle).toFloat() * r
                val py = y + sin(angle).toFloat() * r
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, Color(0xFFFFDE00))
        }
        star(size.width * 0.24f, size.height * 0.34f, size.height * 0.21f)
        listOf(0.45f to 0.15f, 0.55f to 0.3f, 0.55f to 0.49f, 0.44f to 0.64f).forEach { (x, y) ->
            star(size.width * x, size.height * y, size.height * 0.07f)
        }
    }
}

@Composable
internal fun PositionSortMark(active: Boolean = false) {
    Canvas(Modifier.width(7.dp).height(12.dp)) {
        val upper = Path().apply { moveTo(size.width / 2, 0f); lineTo(size.width, size.height * 0.4f); lineTo(0f, size.height * 0.4f); close() }
        val lower = Path().apply { moveTo(0f, size.height * 0.6f); lineTo(size.width, size.height * 0.6f); lineTo(size.width / 2, size.height); close() }
        drawPath(upper, if (active) TradeProfitRed else Color(0xFFCCCCCC))
        drawPath(lower, Color(0xFFCCCCCC))
    }
}
