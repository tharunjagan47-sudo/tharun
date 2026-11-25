package com.simats.diawell123.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simats.diawell123.ui.theme.AppTheme

/**
 * Adds a subtle, slow diagonal glass sheen across hero surfaces without distracting motion.
 */
fun Modifier.lightShimmerSheen(
    enabled: Boolean = true,
    durationMillis: Int = 3800,
    peakAlpha: Float = 0.08f,
): Modifier = composed {
    if (!enabled) return@composed this

    val transition = rememberInfiniteTransition(label = "LightShimmerSheen")
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "SheenProgress",
    )

    drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        if (w > 0f && h > 0f) {
            val centerX = w * progress
            val bandWidth = (w * 0.45f).coerceAtLeast(120f)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = peakAlpha * 0.45f),
                        Color.White.copy(alpha = peakAlpha),
                        Color.White.copy(alpha = peakAlpha * 0.45f),
                        Color.Transparent,
                    ),
                    start = Offset(centerX - bandWidth, 0f),
                    end = Offset(centerX + bandWidth, h),
                ),
            )
        }
    }
}

/**
 * Soft skeleton shimmer fill adapting cleanly to Light and Dark themes.
 */
fun Modifier.shimmerSkeleton(
    shape: Shape = RoundedCornerShape(14.dp),
    durationMillis: Int = 1400,
): Modifier = composed {
    val isDark = AppTheme.colors.isDark
    val baseColor = if (isDark) AppTheme.colors.cardBackground else Color(0xFFF1EEF7)
    val highlightColor = if (isDark) {
        AppTheme.colors.elevatedSurface.copy(alpha = 0.95f)
    } else {
        Color(0xFFFAF8FF)
    }

    val transition = rememberInfiniteTransition(label = "SkeletonShimmer")
    val progress by transition.animateFloat(
        initialValue = -0.7f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "SkeletonProgress",
    )

    this
        .clip(shape)
        .drawWithContent {
            val w = size.width.coerceAtLeast(1f)
            val h = size.height.coerceAtLeast(1f)
            val cx = w * progress
            val band = (w * 0.55f).coerceAtLeast(100f)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    start = Offset(cx - band, 0f),
                    end = Offset(cx + band, h),
                ),
            )
            drawContent()
        }
}

@Composable
fun ShimmerPlaceholderBox(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    cornerRadius: Dp = 18.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shimmerSkeleton(shape = shape)
            .border(1.dp, AppTheme.colors.border.copy(alpha = 0.6f), shape),
    )
}

@Composable
fun ShimmerCardList(
    count: Int = 3,
    cardHeight: Dp = 76.dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(count) {
            val shape = RoundedCornerShape(18.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .shimmerSkeleton(shape = shape)
                    .border(1.dp, AppTheme.colors.border.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shimmerSkeleton(shape = CircleShape),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(12.dp)
                            .shimmerSkeleton(shape = RoundedCornerShape(6.dp)),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(10.dp)
                            .shimmerSkeleton(shape = RoundedCornerShape(5.dp)),
                    )
                }
            }
        }
    }
}
