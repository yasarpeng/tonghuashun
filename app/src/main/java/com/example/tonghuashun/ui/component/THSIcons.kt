package com.example.tonghuashun.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 高仿同花顺功能图标：统一为彩色圆角方块背景 + 白色线性符号，用 Canvas 自绘，
 * 尽量贴近同花顺 App 的视觉风格（不使用其版权图标资源）。
 */
enum class THSIcon {
    BUY,        // 买入：上升箭头
    SELL,       // 卖出：下降箭头
    CANCEL,     // 撤单：叉
    POSITION,   // 持仓：柱状/仓
    QUERY,      // 查询：放大镜
    IPO,        // 新股申购
    REPO,       // 逆回购
    CONDITION,  // 条件单：灯泡
    STAR_MARKET,// 科创板
    NOTE,       // 我的笔记
    HK,         // 港股通
    PORTFOLIO,  // 我的组合
    PANORAMA,   // 行情全景
    ETF,        // 港股ETF
    MORE,       // 敬请期待：省略号
    MANAGE,     // 持仓管理
    BATCH_BUY,  // 批量买入
    BATCH_SELL, // 批量卖出
    STOP_PROFIT,// 止盈止损
    // 首页/理财
    STOCK,      // 股票
    DRAGON,     // 龙虎榜(奖杯)
    FUTURES,    // 商品期货
    DECISION,   // 决策(灯泡)
    ANALYSIS,   // 自选分析
    ETF_HOME,   // ETF首页(方块)
    FILTER,     // 条件选股(漏斗)
    HK_CONNECT, // 沪深港通
    CALENDAR,   // 打新日历
    APPS,       // 应用商店(九宫格)
    FUND,       // 基金
    FUND_OPEN,  // 基金开户
    SCHOOL,     // 基民学堂
    WALLET,     // 钱包
    RANK,       // 排行
    GIFT,       // 福利/红包
    HIGH_END,   // 高端理财
    GLOBAL,     // 全球投资
    STRATEGY,   // 策略广场
    SKILL,      // 技能
}

private val THSBlue = Color(0xFF2E7BF6)
private val THSOrange = Color(0xFFFF8A00)
private val THSRedC = Color(0xFFF5222D)
private val THSGreen = Color(0xFF00A85A)
private val THSPurple = Color(0xFF8E5CF7)

fun colorFor(icon: THSIcon): Color = when (icon) {
    THSIcon.BUY, THSIcon.BATCH_BUY -> THSRedC
    THSIcon.SELL, THSIcon.BATCH_SELL -> THSGreen
    THSIcon.CANCEL -> THSBlue
    THSIcon.POSITION, THSIcon.MANAGE -> THSBlue
    THSIcon.QUERY -> THSBlue
    THSIcon.IPO -> THSOrange
    THSIcon.REPO -> THSOrange
    THSIcon.CONDITION -> THSPurple
    THSIcon.STAR_MARKET -> THSBlue
    THSIcon.NOTE -> THSBlue
    THSIcon.HK -> THSGreen
    THSIcon.PORTFOLIO -> THSOrange
    THSIcon.PANORAMA -> THSOrange
    THSIcon.ETF -> THSGreen
    THSIcon.MORE -> THSOrange
    THSIcon.STOP_PROFIT -> THSRedC
    THSIcon.STOCK, THSIcon.DRAGON, THSIcon.ETF_HOME, THSIcon.FILTER, THSIcon.HK_CONNECT -> THSRedC
    THSIcon.FUTURES, THSIcon.ANALYSIS, THSIcon.APPS, THSIcon.FUND_OPEN, THSIcon.SCHOOL -> THSBlue
    THSIcon.DECISION, THSIcon.CALENDAR, THSIcon.HIGH_END -> THSOrange
    THSIcon.FUND, THSIcon.WALLET, THSIcon.GLOBAL -> THSBlue
    THSIcon.RANK, THSIcon.GIFT, THSIcon.STRATEGY, THSIcon.SKILL -> THSRedC
}

/** 圆角方块背景 + 白色符号 */
@Composable
fun THSFuncIcon(icon: THSIcon, size: Dp = 40.dp, corner: Dp = 10.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner)),
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawRoundBg(colorFor(icon), corner.toPx())
            drawSymbol(icon)
        }
    }
}

/** 圆形描边 + 彩色符号（用于交易首页买卖撤持仓查询圆标） */
@Composable
fun THSCircleIcon(icon: THSIcon, size: Dp = 46.dp) {
    Box(modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val c = colorFor(icon)
            // 圆形描边
            drawCircle(color = c, style = Stroke(width = 2f), radius = this.size.minDimension / 2 - 1f)
            drawSymbolColored(icon, c)
        }
    }
}

private fun DrawScope.drawRoundBg(color: Color, cornerPx: Float) {
    drawRoundRect(
        color = color,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
    )
}

/** 在方块内画白色符号 */
private fun DrawScope.drawSymbol(icon: THSIcon) = drawSymbolColored(icon, Color.White)

private fun DrawScope.drawSymbolColored(icon: THSIcon, color: Color) {
    val w = size.width
    val h = size.height
    val sw = w * 0.08f  // 线宽
    val cx = w / 2
    val cy = h / 2
    when (icon) {
        THSIcon.BUY, THSIcon.BATCH_BUY -> {
            // 上升箭头
            val p = Path().apply {
                moveTo(w * 0.28f, h * 0.62f)
                lineTo(w * 0.45f, h * 0.42f)
                lineTo(w * 0.57f, h * 0.54f)
                lineTo(w * 0.72f, h * 0.36f)
            }
            drawPath(p, color, style = Stroke(width = sw, cap = StrokeCap.Round))
            // 箭头头部
            drawLine(color, Offset(w * 0.72f, h * 0.36f), Offset(w * 0.60f, h * 0.36f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.72f, h * 0.36f), Offset(w * 0.72f, h * 0.48f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.SELL, THSIcon.BATCH_SELL -> {
            val p = Path().apply {
                moveTo(w * 0.28f, h * 0.40f)
                lineTo(w * 0.45f, h * 0.58f)
                lineTo(w * 0.57f, h * 0.46f)
                lineTo(w * 0.72f, h * 0.64f)
            }
            drawPath(p, color, style = Stroke(width = sw, cap = StrokeCap.Round))
            drawLine(color, Offset(w * 0.72f, h * 0.64f), Offset(w * 0.60f, h * 0.64f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.72f, h * 0.64f), Offset(w * 0.72f, h * 0.52f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.CANCEL -> {
            drawLine(color, Offset(w * 0.35f, h * 0.35f), Offset(w * 0.65f, h * 0.65f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.65f, h * 0.35f), Offset(w * 0.35f, h * 0.65f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.POSITION, THSIcon.MANAGE, THSIcon.PANORAMA -> {
            // 三根高低柱
            val bw = w * 0.11f
            drawLine(color, Offset(w * 0.34f, h * 0.64f), Offset(w * 0.34f, h * 0.48f), strokeWidth = bw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.50f, h * 0.64f), Offset(w * 0.50f, h * 0.38f), strokeWidth = bw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.66f, h * 0.64f), Offset(w * 0.66f, h * 0.52f), strokeWidth = bw, cap = StrokeCap.Round)
        }
        THSIcon.QUERY -> {
            val r = w * 0.15f
            drawCircle(color, radius = r, center = Offset(w * 0.45f, h * 0.45f), style = Stroke(width = sw))
            drawLine(color, Offset(w * 0.56f, h * 0.56f), Offset(w * 0.68f, h * 0.68f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.IPO -> {
            // "新" 用一个星形近似
            drawStar(cx, cy, w * 0.22f, color)
        }
        THSIcon.REPO, THSIcon.ETF -> {
            // 循环/双箭头
            drawLine(color, Offset(w * 0.32f, h * 0.42f), Offset(w * 0.62f, h * 0.42f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.62f, h * 0.42f), Offset(w * 0.54f, h * 0.34f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.68f, h * 0.58f), Offset(w * 0.38f, h * 0.58f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.38f, h * 0.58f), Offset(w * 0.46f, h * 0.66f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.CONDITION -> {
            drawCircle(color, radius = w * 0.16f, center = Offset(cx, h * 0.42f), style = Stroke(width = sw))
            drawLine(color, Offset(w * 0.44f, h * 0.62f), Offset(w * 0.56f, h * 0.62f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.STAR_MARKET -> {
            drawStar(cx, cy, w * 0.20f, color)
        }
        THSIcon.NOTE -> {
            // 文档
            drawRoundRect(color, topLeft = Offset(w * 0.33f, h * 0.30f), size = Size(w * 0.34f, h * 0.40f), style = Stroke(width = sw), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            drawLine(color, Offset(w * 0.40f, h * 0.44f), Offset(w * 0.60f, h * 0.44f), strokeWidth = sw * 0.8f, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.40f, h * 0.55f), Offset(w * 0.60f, h * 0.55f), strokeWidth = sw * 0.8f, cap = StrokeCap.Round)
        }
        THSIcon.HK -> {
            // 港：画个"港"用圆点近似 -> 用一个建筑/双线
            drawLine(color, Offset(w * 0.34f, h * 0.66f), Offset(w * 0.66f, h * 0.66f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.40f, h * 0.66f), Offset(w * 0.40f, h * 0.40f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.60f, h * 0.66f), Offset(w * 0.60f, h * 0.40f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.40f, h * 0.40f), Offset(w * 0.60f, h * 0.40f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.PORTFOLIO -> {
            // 立方体
            drawRoundRect(color, topLeft = Offset(w * 0.34f, h * 0.34f), size = Size(w * 0.32f, h * 0.32f), style = Stroke(width = sw), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            drawLine(color, Offset(cx, h * 0.34f), Offset(cx, h * 0.66f), strokeWidth = sw * 0.8f)
        }
        THSIcon.MORE -> {
            drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.38f, cy))
            drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.50f, cy))
            drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.62f, cy))
        }
        THSIcon.STOP_PROFIT -> {
            // 盾牌近似：菱形
            val p = Path().apply {
                moveTo(cx, h * 0.32f); lineTo(w * 0.66f, cy); lineTo(cx, h * 0.68f); lineTo(w * 0.34f, cy); close()
            }
            drawPath(p, color, style = Stroke(width = sw))
        }
        THSIcon.STOCK, THSIcon.ANALYSIS, THSIcon.FUTURES -> {
            // 上升折线(股票/分析)
            val p = Path().apply {
                moveTo(w * 0.30f, h * 0.62f)
                lineTo(w * 0.44f, h * 0.48f)
                lineTo(w * 0.55f, h * 0.56f)
                lineTo(w * 0.72f, h * 0.36f)
            }
            drawPath(p, color, style = Stroke(width = sw, cap = StrokeCap.Round))
        }
        THSIcon.DRAGON, THSIcon.RANK -> {
            // 奖杯/排行(阶梯柱)
            drawLine(color, Offset(w * 0.34f, h * 0.66f), Offset(w * 0.34f, h * 0.54f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.50f, h * 0.66f), Offset(w * 0.50f, h * 0.40f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.66f, h * 0.66f), Offset(w * 0.66f, h * 0.48f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
        }
        THSIcon.DECISION, THSIcon.SKILL -> {
            drawCircle(color, radius = w * 0.16f, center = Offset(cx, h * 0.44f), style = Stroke(width = sw))
            drawLine(color, Offset(w * 0.44f, h * 0.64f), Offset(w * 0.56f, h * 0.64f), strokeWidth = sw, cap = StrokeCap.Round)
        }
        THSIcon.ETF_HOME, THSIcon.APPS, THSIcon.STRATEGY -> {
            // 九宫格四块
            val s = w * 0.13f
            val g = w * 0.06f
            listOf(
                Offset(cx - s - g / 2, cy - s - g / 2), Offset(cx + g / 2, cy - s - g / 2),
                Offset(cx - s - g / 2, cy + g / 2), Offset(cx + g / 2, cy + g / 2),
            ).forEach { drawRoundRect(color, topLeft = it, size = Size(s, s), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)) }
        }
        THSIcon.FILTER -> {
            // 漏斗
            val p = Path().apply {
                moveTo(w * 0.32f, h * 0.36f); lineTo(w * 0.68f, h * 0.36f)
                lineTo(w * 0.55f, h * 0.52f); lineTo(w * 0.55f, h * 0.66f)
                lineTo(w * 0.45f, h * 0.66f); lineTo(w * 0.45f, h * 0.52f); close()
            }
            drawPath(p, color, style = Stroke(width = sw))
        }
        THSIcon.HK_CONNECT, THSIcon.GLOBAL -> {
            // 地球(圆+经线)
            val r = w * 0.17f
            drawCircle(color, radius = r, center = Offset(cx, cy), style = Stroke(width = sw))
            drawLine(color, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = sw * 0.7f)
            drawLine(color, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = sw * 0.7f)
        }
        THSIcon.CALENDAR -> {
            drawRoundRect(color, topLeft = Offset(w * 0.32f, h * 0.34f), size = Size(w * 0.36f, h * 0.34f), style = Stroke(width = sw), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            drawLine(color, Offset(w * 0.32f, h * 0.44f), Offset(w * 0.68f, h * 0.44f), strokeWidth = sw * 0.8f)
        }
        THSIcon.FUND, THSIcon.HIGH_END -> {
            // 钱袋/¥ 近似
            drawLine(color, Offset(cx, h * 0.36f), Offset(cx, h * 0.64f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.40f, h * 0.42f), Offset(cx, h * 0.50f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.60f, h * 0.42f), Offset(cx, h * 0.50f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.42f, h * 0.54f), Offset(w * 0.58f, h * 0.54f), strokeWidth = sw * 0.8f, cap = StrokeCap.Round)
        }
        THSIcon.FUND_OPEN, THSIcon.WALLET -> {
            drawRoundRect(color, topLeft = Offset(w * 0.32f, h * 0.38f), size = Size(w * 0.36f, h * 0.26f), style = Stroke(width = sw), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            drawCircle(color, radius = w * 0.03f, center = Offset(w * 0.60f, h * 0.51f))
        }
        THSIcon.SCHOOL -> {
            // 学士帽
            val p = Path().apply {
                moveTo(cx, h * 0.38f); lineTo(w * 0.68f, h * 0.48f); lineTo(cx, h * 0.58f); lineTo(w * 0.32f, h * 0.48f); close()
            }
            drawPath(p, color, style = Stroke(width = sw))
        }
        THSIcon.GIFT -> {
            drawRoundRect(color, topLeft = Offset(w * 0.34f, h * 0.44f), size = Size(w * 0.32f, h * 0.22f), style = Stroke(width = sw), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
            drawLine(color, Offset(cx, h * 0.44f), Offset(cx, h * 0.66f), strokeWidth = sw * 0.8f)
            drawLine(color, Offset(w * 0.34f, h * 0.44f), Offset(w * 0.66f, h * 0.44f), strokeWidth = sw, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    for (i in 0 until 5) {
        val angleOuter = Math.toRadians((-90 + i * 72).toDouble())
        val angleInner = Math.toRadians((-90 + i * 72 + 36).toDouble())
        val ox = cx + r * Math.cos(angleOuter).toFloat()
        val oy = cy + r * Math.sin(angleOuter).toFloat()
        val ix = cx + r * 0.45f * Math.cos(angleInner).toFloat()
        val iy = cy + r * 0.45f * Math.sin(angleInner).toFloat()
        if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
        path.lineTo(ix, iy)
    }
    path.close()
    drawPath(path, color)
}
