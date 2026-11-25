package com.simats.diawell123.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class HealthMetricZoomType {
    GLUCOSE,
    NUTRITION,
    MEDICATION,
    WEEKLY_REPORT,
}

data class HealthMetricZoomData(
    val type: HealthMetricZoomType,
    val title: String,
    val value: String,
    val subtitle: String,
    val statusText: String,
    val statusColor: Color,
    val accentColor: Color,
    val secondaryAccent: Color,
    val icon: ImageVector,
    val actionLabel: String,
    val progressFraction: Float,
    val primaryDetailLabel: String,
    val primaryDetailValue: String,
    val secondaryDetailLabel: String,
    val secondaryDetailValue: String,
    val onOpenScreen: () -> Unit,
)

@Composable
fun HealthMetricZoomOverlay(
    metric: HealthMetricZoomData?,
    onDismiss: () -> Unit,
) {
    val visible = metric != null
    BackHandler(enabled = visible) {
        onDismiss()
    }

    var lastMetric by remember { mutableStateOf(metric) }
    if (metric != null) {
        lastMetric = metric
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(
            initialScale = 0.74f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ),
        exit = fadeOut(animationSpec = tween(180)) + scaleOut(
            targetScale = 0.74f,
            animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing),
        ),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(90f),
    ) {
        val active = metric ?: lastMetric ?: return@AnimatedVisibility
        val isDark = AppTheme.colors.isDark
        var animateGraph by remember(active.type) { mutableStateOf(false) }
        LaunchedEffect(active.type, visible) {
            animateGraph = visible
        }

        val graphProgress by animateFloatAsState(
            targetValue = if (animateGraph) 1f else 0f,
            animationSpec = tween(durationMillis = 760, delayMillis = 80, easing = FastOutSlowInEasing),
            label = "MetricZoomGraphProgress",
        )

        val backdropColor = if (isDark) Color(0xFF0A0D1A) else Color(0xFFF6F5FC)
        val glowTop = active.accentColor.copy(alpha = if (isDark) 0.28f else 0.14f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .systemBarsPadding(),
        ) {
            // Subtle atmospheric radial aura matching metric accent
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glowTop, Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.30f),
                        radius = size.width * 0.85f,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Header: Title + Circular Close (X) Button
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = active.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .align(Alignment.CenterEnd)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f))
                            .border(1.dp, AppTheme.colors.border, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Metric Detail",
                            tint = AppTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Central Animated Graph Stage (Zoomed Reveal)
                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MetricDetailAnimatedGraph(
                        type = active.type,
                        progress = graphProgress,
                        targetFraction = active.progressFraction,
                        accentColor = active.accentColor,
                        secondaryColor = active.secondaryAccent,
                        isDark = isDark,
                    )

                    // Center Highlight Pill matching reference video
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.92f),
                            )
                            .border(
                                width = 1.4.dp,
                                color = active.accentColor.copy(alpha = 0.55f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = active.icon,
                                contentDescription = null,
                                tint = active.accentColor,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = active.value.substringBefore(" "),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTheme.colors.textPrimary,
                                maxLines = 1,
                            )
                            val unit = active.value.substringAfter(" ", "")
                            if (unit.isNotBlank()) {
                                Text(
                                    text = unit,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = active.statusText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = active.subtitle,
                    fontSize = 13.5.sp,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Breakdown Details Card (matching video bottom panel)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    MetricBreakdownRow(
                        label = active.title,
                        value = active.value,
                        accent = active.accentColor,
                        progress = (active.progressFraction * graphProgress).coerceIn(0.08f, 1f),
                    )
                    MetricBreakdownRow(
                        label = active.primaryDetailLabel,
                        value = active.primaryDetailValue,
                        accent = active.secondaryAccent,
                        progress = (0.78f * graphProgress).coerceIn(0.08f, 1f),
                    )
                    MetricBreakdownRow(
                        label = active.secondaryDetailLabel,
                        value = active.secondaryDetailValue,
                        accent = active.statusColor,
                        progress = (0.90f * graphProgress).coerceIn(0.08f, 1f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                IosLiquidGlassButton(
                    text = active.actionLabel,
                    onClick = {
                        onDismiss()
                        active.onOpenScreen()
                    },
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun MetricDetailAnimatedGraph(
    type: HealthMetricZoomType,
    progress: Float,
    targetFraction: Float,
    accentColor: Color,
    secondaryColor: Color,
    isDark: Boolean,
) {
    val trackColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
    val pathMeasure = remember { PathMeasure() }
    val drawPath = remember { Path() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val outerR = size.minDimension * 0.43f

        when (type) {
            HealthMetricZoomType.WEEKLY_REPORT -> {
                // Pentagon Radar Graph inspired directly by the reference video
                val sides = 5
                val ratios = listOf(0.88f, 0.72f, 0.94f, 0.80f, 0.86f)
                val pts = (0 until sides).map { i ->
                    val angle = (-PI / 2.0) + (2.0 * PI * i / sides)
                    val r = outerR * (0.45f + 0.55f * ratios[i] * progress)
                    Offset(
                        center.x + (r * cos(angle)).toFloat(),
                        center.y + (r * sin(angle)).toFloat(),
                    )
                }

                // Spokes
                pts.forEach { p ->
                    drawLine(
                        color = trackColor,
                        start = center,
                        end = p,
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }

                val polyPath = Path().apply {
                    pts.forEachIndexed { idx, p ->
                        if (idx == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                    close()
                }
                pathMeasure.setPath(polyPath, true)
                drawPath.reset()
                pathMeasure.getSegment(0f, pathMeasure.length * progress, drawPath, true)

                drawPath(
                    path = drawPath,
                    brush = Brush.linearGradient(listOf(accentColor, secondaryColor)),
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                pts.forEachIndexed { idx, p ->
                    val nodeAlpha = (progress * 1.4f - idx * 0.08f).coerceIn(0f, 1f)
                    drawCircle(
                        color = if (idx % 2 == 0) accentColor.copy(alpha = nodeAlpha) else secondaryColor.copy(alpha = nodeAlpha),
                        radius = 6.dp.toPx(),
                        center = p,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = nodeAlpha),
                        radius = 2.5.dp.toPx(),
                        center = p,
                    )
                }
            }

            HealthMetricZoomType.GLUCOSE -> {
                // Concentric ring + animated trend wave wrapping outside the score orb
                drawCircle(
                    color = trackColor,
                    radius = outerR,
                    center = center,
                    style = Stroke(width = 8.dp.toPx()),
                )
                val sweep = 360f * targetFraction.coerceIn(0.25f, 0.95f) * progress
                drawArc(
                    brush = Brush.sweepGradient(listOf(accentColor, secondaryColor, accentColor)),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - outerR, center.y - outerR),
                    size = Size(outerR * 2f, outerR * 2f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                )

                val wavePath = Path().apply {
                    val startX = center.x - outerR * 0.85f
                    val endX = center.x + outerR * 0.85f
                    val stepW = (endX - startX) / 6f
                    val baseY = center.y + outerR * 0.72f
                    moveTo(startX, baseY)
                    lineTo(startX + stepW, baseY - 12.dp.toPx())
                    lineTo(startX + stepW * 2f, baseY + 8.dp.toPx())
                    lineTo(startX + stepW * 3f, baseY - 18.dp.toPx())
                    lineTo(startX + stepW * 4f, baseY + 6.dp.toPx())
                    lineTo(startX + stepW * 5f, baseY - 10.dp.toPx())
                    lineTo(endX, baseY - 4.dp.toPx())
                }
                pathMeasure.setPath(wavePath, false)
                drawPath.reset()
                pathMeasure.getSegment(0f, pathMeasure.length * progress, drawPath, true)
                drawPath(
                    path = drawPath,
                    color = secondaryColor,
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            else -> {
                // Nutrition & Medication: Sleek segmented radial arc gauge
                val totalTicks = 24
                val filledTicks = (totalTicks * targetFraction.coerceIn(0.25f, 1f) * progress).toInt()
                for (i in 0 until totalTicks) {
                    val angle = (-215.0 + (250.0 * i / (totalTicks - 1))) * (PI / 180.0)
                    val inner = outerR * 0.82f
                    val outer = outerR
                    val start = Offset(
                        center.x + (inner * cos(angle)).toFloat(),
                        center.y + (inner * sin(angle)).toFloat(),
                    )
                    val end = Offset(
                        center.x + (outer * cos(angle)).toFloat(),
                        center.y + (outer * sin(angle)).toFloat(),
                    )
                    drawLine(
                        color = if (i <= filledTicks) accentColor else trackColor,
                        start = start,
                        end = end,
                        strokeWidth = 3.2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBreakdownRow(
    label: String,
    value: String,
    accent: Color,
    progress: Float,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textSecondary,
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppTheme.colors.border.copy(alpha = 0.5f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent),
            )
        }
    }
}
