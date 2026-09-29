package com.example.tonghuashun.ui.screen.detail.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.tonghuashun.data.model.TimeShareEntry
import com.example.tonghuashun.ui.theme.KLineGrid
import com.example.tonghuashun.ui.theme.MA5Color
import kotlin.math.abs
import kotlin.math.max

/**
 * 自绘分时图：价格线（蓝）、均价线（黄）、昨收基准虚线（灰），价格线下方渐变填充。
 */
@Composable
fun TimeShareChart(
    entries: List<TimeShareEntry>,
    prevClose: Double,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return
    val priceColor = Color(0xFF2962FF)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            drawTimeShare(entries, prevClose, priceColor)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp),
        ) {
            drawTimeVolume(entries, prevClose)
        }
    }
}

private fun DrawScope.drawTimeShare(entries: List<TimeShareEntry>, prevClose: Double, priceColor: Color) {
    val prices = entries.map { it.price } + entries.map { it.avgPrice } + prevClose
    val maxP = prices.max()
    val minP = prices.min()
    // 以昨收为中心对称，涨跌幅一致
    val maxDev = max(abs(maxP - prevClose), abs(minP - prevClose)).coerceAtLeast(0.01)
    val top = prevClose + maxDev
    val bottom = prevClose - maxDev
    val range = (top - bottom).coerceAtLeast(0.0001)

    val w = size.width
    val h = size.height

    fun priceToY(p: Double): Float = (h * (1 - (p - bottom) / range)).toFloat()
    fun idxToX(i: Int): Float = w * i / (entries.size - 1).coerceAtLeast(1)

    // 网格
    for (i in 0..4) {
        val y = h * i / 4
        drawLine(KLineGrid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
    }
    // 昨收基准虚线
    val baseY = priceToY(prevClose)
    drawLine(
        color = Color(0xFFAAAAAA),
        start = Offset(0f, baseY),
        end = Offset(w, baseY),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
    )

    // 价格填充
    val fillPath = Path().apply {
        moveTo(0f, h)
        entries.forEachIndexed { i, e -> lineTo(idxToX(i), priceToY(e.price)) }
        lineTo(idxToX(entries.size - 1), h)
        close()
    }
    drawPath(
        fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(priceColor.copy(alpha = 0.25f), priceColor.copy(alpha = 0.02f)),
        ),
    )

    // 价格线
    val pricePath = Path()
    entries.forEachIndexed { i, e ->
        val x = idxToX(i); val y = priceToY(e.price)
        if (i == 0) pricePath.moveTo(x, y) else pricePath.lineTo(x, y)
    }
    drawPath(pricePath, priceColor, style = Stroke(width = 2f, cap = StrokeCap.Round))

    // 均价线
    val avgPath = Path()
    entries.forEachIndexed { i, e ->
        val x = idxToX(i); val y = priceToY(e.avgPrice)
        if (i == 0) avgPath.moveTo(x, y) else avgPath.lineTo(x, y)
    }
    drawPath(avgPath, MA5Color, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
}

private fun DrawScope.drawTimeVolume(entries: List<TimeShareEntry>, prevClose: Double) {
    val maxVol = entries.maxOf { it.volume }.toFloat().coerceAtLeast(1f)
    val w = size.width
    val h = size.height
    val slot = w / entries.size
    val barWidth = slot * 0.6f
    var prevPrice = prevClose
    entries.forEachIndexed { i, e ->
        val x = slot * i + slot / 2
        val barH = h * (e.volume / maxVol)
        val color = if (e.price >= prevPrice) com.example.tonghuashun.ui.theme.StockUp else com.example.tonghuashun.ui.theme.StockDown
        drawRect(
            color = color,
            topLeft = Offset(x - barWidth / 2, h - barH),
            size = androidx.compose.ui.geometry.Size(barWidth, barH),
        )
        prevPrice = e.price
    }
}
