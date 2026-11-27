package com.example.tonghuashun.ui.screen.detail.chart

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tonghuashun.data.model.TimeShareEntry
import com.example.tonghuashun.ui.screen.detail.isFundQuote
import com.example.tonghuashun.ui.screen.detail.detailPrice
import com.example.tonghuashun.ui.screen.detail.quoteMinuteIndex
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockUp
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
internal fun ReferenceTimeShareChart(entries: List<TimeShareEntry>, prevClose: Double, code: String, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val axisSize = with(density) { 11.sp.toPx() }
    Column(modifier.background(CardWhite)) {
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(290.dp), Alignment.Center) { Text("暂无分时数据", color = TextSecondary, fontSize = 12.sp) }
        } else {
            Canvas(Modifier.fillMaxWidth().height(272.dp).padding(horizontal = 4.dp)) {
                val base = prevClose.takeIf { it > 0 } ?: entries.first().price
                val prices = entries.flatMap { listOf(it.price, it.avgPrice) }.filter { it > 0 }
                val tick = if (isFundQuote(code)) 0.001 else 0.01
                val dev = max(prices.maxOfOrNull { abs(it - base) } ?: tick, tick * 2) * 1.03
                val top = base + dev
                val bottom = base - dev
                val plotHeight = size.height
                val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = axisSize; typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD) }
                fun y(price: Double) = (plotHeight * (top - price) / (top - bottom)).toFloat()
                fun x(index: Int): Float = size.width * ((quoteMinuteIndex(entries[index].time)?.toFloat()?.div(240f))
                    ?: (index.toFloat() / (entries.size - 1).coerceAtLeast(1)))
                repeat(5) { i ->
                    val gridY = plotHeight * i / 4
                    drawLine(DividerGray, Offset(0f, gridY), Offset(size.width, gridY), 1f)
                    val gridX = size.width * i / 4
                    drawLine(DividerGray, Offset(gridX, 0f), Offset(gridX, plotHeight), 1f)
                }
                drawLine(Color(0xFF999999), Offset(0f, y(base)), Offset(size.width, y(base)), 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
                val pricePath = Path()
                val averagePath = Path()
                entries.forEachIndexed { index, entry ->
                    val px = x(index)
                    if (index == 0) { pricePath.moveTo(px, y(entry.price)); averagePath.moveTo(px, y(entry.avgPrice.takeIf { it > 0 } ?: entry.price)) }
                    else { pricePath.lineTo(px, y(entry.price)); averagePath.lineTo(px, y(entry.avgPrice.takeIf { it > 0 } ?: entry.price)) }
                }
                drawPath(averagePath, Color(0xFFD5A52C), style = Stroke(1.dp.toPx()))
                drawPath(pricePath, Color(0xFF292929), style = Stroke(1.3.dp.toPx()))
                drawPath(pricePath, Color(0xFF86108C), style = Stroke(0.7.dp.toPx()))
                listOf(Triple(top, StockUp, axisSize), Triple(base, Color(0xFF999999), plotHeight / 2 - 3.dp.toPx()),
                    Triple(bottom, StockDown, plotHeight - 5.dp.toPx())).forEach { (price, color, baseline) ->
                    text.color = color.toArgb(); text.textAlign = Paint.Align.LEFT
                    drawContext.canvas.nativeCanvas.drawText(detailPrice(code, price), 3.dp.toPx(), baseline, text)
                    text.textAlign = Paint.Align.RIGHT
                    val percent = if (base > 0) (price - base) / base * 100 else 0.0
                    drawContext.canvas.nativeCanvas.drawText(String.format(Locale.CHINA, "%+.2f%%", percent), size.width - 3.dp.toPx(), baseline, text)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
                Text("09:30", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                Text("11:30", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("15:00", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
        repeat(2) { ReferenceVolumePane(entries, prevClose) }
    }
}

@Composable
private fun ReferenceVolumePane(entries: List<TimeShareEntry>, prevClose: Double) {
    var cumulative by remember { mutableStateOf(false) }
    val lastVolume = entries.lastOrNull()?.volume ?: 0L
    val total = entries.sumOf { it.volume }
    HorizontalDivider(color = DividerGray)
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if (cumulative) "累计量 ▾" else "分时量 ▾", color = TextPrimary, fontSize = 10.sp,
            fontWeight = FontWeight.Medium, modifier = Modifier.clickable { cumulative = !cumulative })
        Text(" 量:$total", color = TextSecondary, fontSize = 10.sp, maxLines = 1, modifier = Modifier.weight(1f))
        Text("分钟:$lastVolume", color = StockUp, fontSize = 10.sp, maxLines = 1)
    }
    Canvas(Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 4.dp)) {
        var accumulator = 0L
        val values = entries.map { accumulator += it.volume; if (cumulative) accumulator else it.volume }
        val maximum = (values.maxOrNull() ?: 0L).coerceAtLeast(1).toDouble()
        repeat(3) { i -> drawLine(DividerGray, Offset(0f, size.height * i / 2), Offset(size.width, size.height * i / 2), 1f) }
        repeat(5) { i -> drawLine(DividerGray, Offset(size.width * i / 4, 0f), Offset(size.width * i / 4, size.height), 1f) }
        var previous = prevClose
        entries.forEachIndexed { index, entry ->
            val fraction = quoteMinuteIndex(entry.time)?.toFloat()?.div(240f) ?: (index.toFloat() / entries.size.coerceAtLeast(1))
            val barHeight = (size.height * values[index] / maximum).toFloat()
            val width = (size.width / 240 * 0.65f).coerceAtLeast(1f)
            drawRect(if (entry.price >= previous) StockUp else StockDown,
                Offset((size.width * fraction - width / 2).coerceIn(0f, (size.width - width).coerceAtLeast(0f)), size.height - barHeight), Size(width, barHeight))
            previous = entry.price
        }
    }
}
