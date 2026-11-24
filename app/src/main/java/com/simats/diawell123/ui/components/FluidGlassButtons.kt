package com.simats.diawell123.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simats.diawell123.ui.theme.AppTheme

/**
 * Apple iOS / VisionOS Fluid Glass Bubble Back Navigation Button.
 * Features a circular shape, frosted translucent glass fill, specular rim highlight,
 * subtle depth shadow, spring touch compression, and a crisp rounded chevron (<).
 */
@Composable
fun FluidGlassBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 44.dp,
    contentDescription: String = "Back",
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "FluidGlassBackScale",
    )

    val isDark = AppTheme.colors.isDark

    // Glass fill gradient matching VisionOS / iOS floating glass bubbles
    val glassFillBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.32f),
                    Color.White.copy(alpha = 0.16f),
                    Color(0xFF1E1B4B).copy(alpha = 0.28f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.82f),
                    Color(0xFFEDE9FE).copy(alpha = 0.50f),
                )
            )
        }
    }

    // Specular border rim with prominent top light reflection
    val rimBorderBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.30f),
                    Color(0xFFA855F7).copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.20f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.40f),
                    Color(0xFF7C3AED).copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.60f),
                )
            )
        }
    }

    val iconColor = if (isDark) Color.White else Color(0xFF0F172A)

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDark) 8.dp else 6.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.12f),
                spotColor = Color(0xFF7C3AED).copy(alpha = if (isDark) 0.30f else 0.15f),
            )
            .clip(CircleShape)
            .background(glassFillBrush)
            .border(1.2.dp, rimBorderBrush, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    var upOrCancel = false
                    while (!upOrCancel) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.changedToUpIgnoreConsumed() }) {
                            upOrCancel = true
                        }
                    }
                    isPressed = false
                    onClick()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Pixel-perfect Apple iOS chevron (<)
        Canvas(
            modifier = Modifier.size(sizeDp * 0.45f),
        ) {
            val w = size.width
            val h = size.height

            val chevronPath = Path().apply {
                moveTo(w * 0.62f, h * 0.22f)
                lineTo(w * 0.34f, h * 0.50f)
                lineTo(w * 0.62f, h * 0.78f)
            }

            drawPath(
                path = chevronPath,
                color = iconColor,
                style = Stroke(
                    width = 2.6.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}

/**
 * Apple iOS / VisionOS Fluid Glass Circular Action Button.
 * Used for actions like Edit, Close, Share, Add, Filter, and More (...).
 */
@Composable
fun FluidGlassCircleButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 44.dp,
    iconSizeDp: Dp = 20.dp,
    contentDescription: String? = null,
    tint: Color? = null,
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "FluidGlassCircleScale",
    )

    val isDark = AppTheme.colors.isDark

    val glassFillBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.32f),
                    Color.White.copy(alpha = 0.16f),
                    Color(0xFF1E1B4B).copy(alpha = 0.28f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.82f),
                    Color(0xFFEDE9FE).copy(alpha = 0.50f),
                )
            )
        }
    }

    val rimBorderBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.30f),
                    Color(0xFFA855F7).copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.20f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.40f),
                    Color(0xFF7C3AED).copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.60f),
                )
            )
        }
    }

    val finalIconTint = tint ?: if (isDark) Color.White else Color(0xFF0F172A)

    Box(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDark) 8.dp else 6.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.12f),
                spotColor = Color(0xFF7C3AED).copy(alpha = if (isDark) 0.30f else 0.15f),
            )
            .clip(CircleShape)
            .background(glassFillBrush)
            .border(1.2.dp, rimBorderBrush, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    var upOrCancel = false
                    while (!upOrCancel) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.changedToUpIgnoreConsumed() }) {
                            upOrCancel = true
                        }
                    }
                    isPressed = false
                    onClick()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = finalIconTint,
            modifier = Modifier.size(iconSizeDp),
        )
    }
}

/**
 * Apple iOS / VisionOS Fluid Glass Capsule / Pill Container.
 * Holds multiple action items in a floating liquid glass pill, matching
 * the grouped controls seen in iOS Notes and VisionOS UI.
 */
@Composable
fun FluidGlassPillContainer(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 6.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val isDark = AppTheme.colors.isDark
    val shape = remember { RoundedCornerShape(32.dp) }

    val glassFillBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.14f),
                    Color(0xFF1E1B4B).copy(alpha = 0.25f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.85f),
                    Color(0xFFEDE9FE).copy(alpha = 0.45f),
                )
            )
        }
    }

    val rimBorderBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.25f),
                    Color(0xFFA855F7).copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.20f),
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.90f),
                    Color.White.copy(alpha = 0.35f),
                    Color(0xFF7C3AED).copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.55f),
                )
            )
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 10.dp else 6.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.50f else 0.15f),
                spotColor = Color(0xFF7C3AED).copy(alpha = if (isDark) 0.30f else 0.15f),
            )
            .clip(shape)
            .background(glassFillBrush)
            .border(1.2.dp, rimBorderBrush, shape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}
