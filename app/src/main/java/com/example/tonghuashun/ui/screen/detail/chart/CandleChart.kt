package com.example.tonghuashun.ui.screen.detail.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.tonghuashun.data.model.KLineEntry
import com.example.tonghuashun.ui.theme.KLineGrid
import com.example.tonghuashun.ui.theme.MA10Color
import com.example.tonghuashun.ui.theme.MA20Color
import com.example.tonghuashun.ui.theme.MA5Color
import com.example.tonghuashun.ui.theme.StockDown
import com.example.tonghuashun.ui.theme.StockUp
import kotlin.math.max

/**
 * 自绘日K蜡烛图：包含价格区（蜡烛 + MA5/MA10/MA20 均线）与下方成交量柱。
 * 红涨绿跌，符合 A 股习惯。
 */
@Composable
fun CandleChart(
    entries: List<KLineEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // 价格区
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            drawCandles(entries)
        }
        // 成交量区
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp),
        ) {
            drawVolume(entries)
        }
    }
}

private fun DrawScope.drawCandles(entries: List<KLineEntry>) {
    val maxPrice = entries.maxOf { it.high }
    val minPrice = entries.minOf { it.low }
    val range = max(maxPrice - minPrice, 0.0001)

    val w = size.width
    val h = size.height
    val count = entries.size
    val slot = w / count
    val candleWidth = slot * 0.6f

    // 网格线
    val gridLines = 4
    for (i in 0..gridLines) {
        val y = h * i / gridLines
        drawLine(KLineGrid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
    }

    fun priceToY(p: Double): Float = (h * (1 - (p - minPrice) / range)).toFloat()

    // 蜡烛
    entries.forEachIndexed { i, e ->
        val cx = slot * i + slot / 2
        val color = if (e.close >= e.open) StockUp else StockDown
        // 影线
        drawLine(
            color = color,
            start = Offset(cx, priceToY(e.high)),
            end = Offset(cx, priceToY(e.low)),
            strokeWidth = 1.5f,
        )
        // 实体
        val top = priceToY(max(e.open, e.close))
        val bottom = priceToY(minOf(e.open, e.close))
        val bodyHeight = max(bottom - top, 1.5f)
        drawRect(
            color = color,
            topLeft = Offset(cx - candleWidth / 2, top),
            size = androidx.compose.ui.geometry.Size(candleWidth, bodyHeight),
        )
    }

    // 均线
    drawMaLine(entries, 5, MA5Color, slot, ::priceToY)
    drawMaLine(entries, 10, MA10Color, slot, ::priceToY)
    drawMaLine(entries, 20, MA20Color, slot, ::priceToY)
}

private fun DrawScope.drawMaLine(
    entries: List<KLineEntry>,
    period: Int,
    color: androidx.compose.ui.graphics.Color,
    slot: Float,
    priceToY: (Double) -> Float,
) {
    if (entries.size < period) return
    val path = Path()
    var started = false
    for (i in entries.indices) {
        if (i < period - 1) continue
        val avg = (i - period + 1..i).sumOf { entries[it].close } / period
        val cx = slot * i + slot / 2
        val cy = priceToY(avg)
        if (!started) {
            path.moveTo(cx, cy)
            started = true
        } else {
            path.lineTo(cx, cy)
        }
    }
    drawPath(path, color, style = Stroke(width = 2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawVolume(entries: List<KLineEntry>) {
    val maxVol = entries.maxOf { it.volume }.toFloat().coerceAtLeast(1f)
    val w = size.width
    val h = size.height
    val count = entries.size
    val slot = w / count
    val barWidth = slot * 0.6f

    entries.forEachIndexed { i, e ->
        val cx = slot * i + slot / 2
        val barHeight = h * (e.volume / maxVol)
        val color = if (e.close >= e.open) StockUp else StockDown
        drawRect(
            color = color,
            topLeft = Offset(cx - barWidth / 2, h - barHeight),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
        )
    }
}
