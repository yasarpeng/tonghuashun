package com.example.tonghuashun.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tonghuashun.data.model.Stock
import com.example.tonghuashun.ui.theme.BackgroundGray
import com.example.tonghuashun.ui.theme.TextTertiary
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary
import com.example.tonghuashun.util.QuoteFormat

/** 行情/自选股列表的一行：名称+代码 | 最新价 | 涨跌幅色块 */
@Composable
fun StockRow(
    stock: Stock,
    onClick: () -> Unit,
) {
    val color = QuoteFormat.colorOf(stock.change)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
            )
            Text(
                text = stock.code,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Text(
            text = QuoteFormat.price(stock.price),
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = QuoteFormat.percent(stock.changePercent),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(78.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .padding(vertical = 6.dp),
            )
        }
    }
}

/**
 * 自选页样式的一行：名称+代码标签 | 迷你走势图 | 涨跌幅色块+价格。
 *
 * 对齐同花顺自选页：
 * - 股票名按涨跌着色（涨红跌绿），名称下方是 [融]/代码/[T+0] 标签行；
 * - 中间是分时迷你走势（带昨收虚线基准 + 渐变填充）；
 * - 右侧是涨跌幅色块，色块右下角是对应价格。
 */
@Composable
fun WatchStockRow(
    stock: Stock,
    onClick: () -> Unit,
) {
    val color = QuoteFormat.colorOf(stock.change)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.25f)) {
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!stock.code.isFundCode()) {
                    TagChip(text = "融", color = Color(0xFFE08A2E), filled = false)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = stock.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )
                if (stock.code.isFundCode()) {
                    Spacer(Modifier.width(4.dp))
                    TagChip(text = "T+0", color = TextSecondary, filled = true)
                }
            }
        }
        // 迷你走势图
        MiniTrend(
            data = stock.trend,
            prevClose = stock.prevClose,
            color = color,
            modifier = Modifier.weight(1.3f).height(40.dp).padding(horizontal = 8.dp),
        )
        // 涨跌幅色块 + 价格
        Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
            Text(
                text = QuoteFormat.percent(stock.changePercent),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(84.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .padding(vertical = 6.dp),
            )
            Text(
                text = QuoteFormat.price(stock.price),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

/** 名称下方的极小标签：融（橙色描边）/ T+0（灰色底） */
@Composable
private fun TagChip(text: String, color: Color, filled: Boolean) {
    val shape = RoundedCornerShape(2.dp)
    val base = Modifier.clip(shape)
    val decorated = if (filled) {
        base.background(BackgroundGray)
    } else {
        base.border(0.8.dp, color.copy(alpha = 0.7f), shape)
    }
    Text(
        text = text,
        color = color,
        fontSize = 9.sp,
        maxLines = 1,
        modifier = decorated.padding(horizontal = 3.dp, vertical = 0.5.dp),
    )
}

/** 基金/ETF 代码：沪市 5 开头、深市 1 开头（15/16/18） */
private fun String.isFundCode(): Boolean =
    startsWith("5") || startsWith("15") || startsWith("16") || startsWith("18")

/** 迷你分时走势图（自选页用），以昨收为基准，涨红跌绿，线下渐变填充 */
@Composable
fun MiniTrend(
    data: List<Double>,
    prevClose: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (data.size < 2 || prevClose <= 0.0) return@Canvas
        val maxP = maxOf(data.max(), prevClose)
        val minP = minOf(data.min(), prevClose)
        val range = (maxP - minP).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        fun toY(p: Double) = (h * (1 - (p - minP) / range)).toFloat()
        fun toX(i: Int) = w * i / (data.size - 1)
        // 昨收虚线基准
        val baseY = toY(prevClose)
        drawLine(
            color = Color(0xFFCDCDCD),
            start = Offset(0f, baseY),
            end = Offset(w, baseY),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)),
        )
        // 渐变填充
        val fill = Path().apply {
            moveTo(0f, h)
            data.forEachIndexed { i, p -> lineTo(toX(i), toY(p)) }
            lineTo(w, h)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.02f)),
            ),
        )
        val path = Path()
        data.forEachIndexed { i, p ->
            val x = toX(i); val y = toY(p)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 1.6f))
    }
}
