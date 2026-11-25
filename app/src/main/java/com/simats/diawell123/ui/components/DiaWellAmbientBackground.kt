package com.simats.diawell123.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.simats.diawell123.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Global Ambient Gradient Background with Continuous Sinusoidal Fluid Motion.
 * Matches AppTheme.colors.background cleanly across Light (#F8FAFC) and Dark (#0F0E17) modes
 * without tearing down animations when the user switches themes.
 */
@Composable
fun DiaWellAmbientBackground(
    modifier: Modifier = Modifier,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val isDark = AppTheme.colors.isDark
    val glowAlpha by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "AmbientGlowVisibility"
    )

    val ambientTransition = rememberInfiniteTransition(label = "GlobalAmbientGradientMotion")
    val phase1 by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    val pulseScale by ambientTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        if (glowAlpha > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val offsetX1 = w * (0.16f + 0.08f * sin(phase1))
                val offsetY1 = h * (0.07f + 0.04f * cos(phase1))
                val radius1 = w * (0.95f * pulseScale)

                val offsetX2 = w * (0.84f - 0.08f * cos(phase2))
                val offsetY2 = h * (0.11f + 0.05f * sin(phase2))
                val radius2 = w * (0.90f * (2f - pulseScale))

                // 1. Broad Top-Half Ambient Horizontal Glow with subtle lateral drift
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0D9488).copy(alpha = 0.58f * glowAlpha),
                            Color(0xFF6366F1).copy(alpha = 0.40f * glowAlpha),
                            Color(0xFF7C3AED).copy(alpha = 0.58f * glowAlpha),
                        ),
                        startX = -w * 0.10f + w * 0.10f * sin(phase1),
                        endX = w * 1.10f + w * 0.10f * sin(phase1)
                    ),
                    size = Size(w, h * 0.48f)
                )

                // 2. High-saturation Top-Left Radial Bloom (Emerald & Teal)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.65f * glowAlpha),
                            Color(0xFF0D9488).copy(alpha = 0.45f * glowAlpha),
                            Color(0xFF064E3B).copy(alpha = 0.18f * glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(offsetX1, offsetY1),
                        radius = radius1
                    )
                )

                // 3. High-saturation Top-Right Radial Bloom (Royal Purple & Deep Violet)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA855F7).copy(alpha = 0.62f * glowAlpha),
                            Color(0xFF7C3AED).copy(alpha = 0.45f * glowAlpha),
                            Color(0xFF4C1D95).copy(alpha = 0.18f * glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(offsetX2, offsetY2),
                        radius = radius2
                    )
                )

                // 4. Subtle central floating aura node
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.28f * glowAlpha),
                            Color(0xFF4C1D95).copy(alpha = 0.10f * glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(w * (0.50f + 0.06f * sin(phase2)), h * (0.16f + 0.03f * cos(phase1))),
                        radius = w * 0.75f
                    )
                )

                // 5. Smooth atmospheric diffusion fading seamlessly into Obsidian base (#0F0E17)
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.22f to Color.Transparent,
                            0.35f to Color(0xFF0F0E17).copy(alpha = 0.35f * glowAlpha),
                            0.46f to Color(0xFF0F0E17).copy(alpha = 0.85f * glowAlpha),
                            0.52f to Color(0xFF0F0E17).copy(alpha = glowAlpha),
                            1.0f to Color(0xFF0F0E17).copy(alpha = glowAlpha)
                        )
                    )
                )
            }
        }

        content?.invoke(this)
    }
}
