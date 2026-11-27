package com.example.tonghuashun.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tonghuashun.ui.theme.CardWhite
import com.example.tonghuashun.ui.theme.DividerGray
import com.example.tonghuashun.ui.theme.THSLayout
import com.example.tonghuashun.ui.theme.THSRed
import com.example.tonghuashun.ui.theme.TextPrimary
import com.example.tonghuashun.ui.theme.TextSecondary

/** 相同层级的标签使用相同高度、文字基线、选中标记和触摸区域。 */
@Composable
fun THSTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    inverse: Boolean = false,
    selectedColor: Color = TextPrimary,
    showIndicator: Boolean = true,
    minHeight: Dp = THSLayout.TouchTarget,
) {
    val foreground = if (inverse) Color.White else selectedColor
    val muted = if (inverse) Color.White.copy(alpha = 0.8f) else TextSecondary
    val gutter = THSLayout.PageGutter
    val fontScale = LocalDensity.current.fontScale
    val gutterPx = with(LocalDensity.current) { gutter.roundToPx() }
    val scroll = rememberScrollState()
    val tabBounds = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }
    Column(modifier = modifier.fillMaxWidth().background(if (inverse) THSRed else CardWhite)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // 大字体和窄屏使用滚动标签，避免把长标签挤成多行。
            val estimatedWidth = (tabs.sumOf { it.length } * 15 * fontScale + tabs.size * 24).dp
            val shouldScroll = scrollable || estimatedWidth > maxWidth - gutter * 2
            val bounds = tabBounds[selected]
            val viewport = constraints.maxWidth
            LaunchedEffect(selected, shouldScroll, bounds, viewport) {
                if (shouldScroll && bounds != null) {
                    val (start, end) = bounds
                    val target = when {
                        start < scroll.value -> start
                        end > scroll.value + viewport -> end - viewport
                        else -> scroll.value
                    }
                    scroll.scrollTo(target.coerceAtLeast(0))
                }
            }
            val rowModifier = if (shouldScroll) {
                Modifier.horizontalScroll(scroll)
            } else Modifier.fillMaxWidth()
            Row(
                modifier = rowModifier.heightIn(min = minHeight).padding(horizontal = gutter).selectableGroup(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, title ->
                    Column(
                        modifier = (if (shouldScroll) Modifier else Modifier.weight(1f))
                            .onGloballyPositioned { coordinates ->
                                val start = coordinates.positionInParent().x.toInt() + gutterPx
                                tabBounds[index] = start to (start + coordinates.size.width)
                            }
                            .selectable(selected = index == selected, role = Role.Tab, onClick = { onSelect(index) })
                            .heightIn(min = THSLayout.TouchTarget)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            title,
                            color = if (index == selected) foreground else muted,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                        )
                        if (showIndicator) {
                            Box(Modifier.padding(top = 4.dp).width(20.dp).height(3.dp)
                                .background(if (index == selected) THSRed else Color.Transparent, RoundedCornerShape(2.dp)))
                        }
                    }
                }
            }
        }
        if (!inverse) HorizontalDivider(color = DividerGray)
    }
}

@Composable
fun THSIconButton(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = Color.White) {
    IconButton(onClick = onClick, modifier = Modifier.size(THSLayout.TouchTarget)) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/** 两侧留出等宽操作区，标题始终以屏幕为基准居中。 */
@Composable
fun THSPageHeader(
    title: String,
    subtitle: String? = null,
    background: Color = THSRed,
    navigation: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(Modifier.fillMaxWidth().background(background)
        .heightIn(min = THSLayout.HeaderHeight).padding(horizontal = 4.dp)) {
        Row(Modifier.align(Alignment.CenterStart).width(96.dp), verticalAlignment = Alignment.CenterVertically) {
            navigation()
        }
        Column(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 96.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrEmpty()) Text(subtitle, color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
        Row(Modifier.align(Alignment.CenterEnd).width(96.dp), horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun THSActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    onClick: () -> Unit,
) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = THSLayout.TouchTarget),
        shape = RoundedCornerShape(THSLayout.ControlRadius),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = textColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

/** 表单有足够宽度时与盘口并排，小屏和大字体下按相同间距纵向排列。 */
@Composable
fun THSAdaptiveSplit(
    modifier: Modifier = Modifier,
    primaryWeight: Float = 0.62f,
    minPrimaryWidth: Dp = 240.dp,
    minSecondaryWidth: Dp = 144.dp,
    primary: @Composable (Modifier) -> Unit,
    secondary: @Composable (Modifier) -> Unit,
) {
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(modifier) {
        if (maxWidth >= (minPrimaryWidth + minSecondaryWidth) * scale + THSLayout.ItemGap) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(THSLayout.ItemGap),
                verticalAlignment = Alignment.Top) {
                primary(Modifier.weight(primaryWeight))
                secondary(Modifier.weight(1f - primaryWeight))
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(THSLayout.ContentPadding)) {
                primary(Modifier.fillMaxWidth())
                secondary(Modifier.fillMaxWidth())
            }
        }
    }
}

val LocalTableScroll = compositionLocalOf<ScrollState?> { null }

/** 表头和各行共享滚动位置；大字体下保持列宽，避免价格和数量换行错位。 */
@Composable
fun THSDataRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    val scroll = LocalTableScroll.current ?: rememberScrollState()
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(modifier) {
        Row(Modifier.horizontalScroll(scroll).width(maxOf(maxWidth, 328.dp * scale)),
            verticalAlignment = verticalAlignment, content = content)
    }
}

/** 首页和理财使用同一网格；系统字号增大时减少列数，保持标签完整。 */
@Composable
fun <T> THSFunctionGrid(items: List<T>, itemContent: @Composable (T) -> Unit) {
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(Modifier.fillMaxWidth().background(CardWhite).padding(vertical = THSLayout.ContentPadding)) {
        val columns = (maxWidth / (68.dp * scale)).toInt().coerceIn(2, 5)
        Column(Modifier.fillMaxWidth()) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { item ->
                        Column(Modifier.weight(1f).clickable {}.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemContent(item)
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
