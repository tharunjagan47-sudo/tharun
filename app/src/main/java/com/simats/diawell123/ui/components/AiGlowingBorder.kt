package com.simats.diawell123.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated rotating gradient border ("line suthu varamari") for AI-powered features.
 * Sweeps cyan, purple, and hot-pink neon light continuously around the card perimeter
 * to clearly indicate active AI computation.
 */
@Composable
fun Modifier.aiRotatingBorder(
    strokeWidth: Dp = 1.6.dp,
    shape: CornerBasedShape = RoundedCornerShape(20.dp),
    durationMillis: Int = 3200,
    borderColors: List<Color> = listOf(
        Color(0xFF00E5FF), // Neon Cyan (Leading Head)
        Color(0xFFA855F7), // Electric Purple
        Color(0xFFEC4899), // Hot Pink
        Color(0xFF6366F1), // Indigo
        Color(0xFF00E5FF).copy(alpha = 0.35f), // Soft Trail
        Color(0xFF00E5FF)  // Back to Cyan
    )
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "aiBorderTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aiBorderAngle"
    )

    return this.drawWithContent {
        drawContent()

        val strokePx = strokeWidth.toPx()
        val halfStroke = strokePx / 2f
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) return@drawWithContent

        val cornerRadiusPx = shape.topStart.toPx(size, this)

        // 1. Subtle base border so the perimeter is always cleanly defined
        drawRoundRect(
            color = Color(0xFFA855F7).copy(alpha = 0.22f),
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(w - strokePx, h - strokePx),
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = strokePx)
        )

        // 2. Rotating glowing AI gradient line circulating around the card
        val colorsInt = borderColors.map { it.toArgb() }.toIntArray()
        val sweepShader = android.graphics.SweepGradient(
            w / 2f,
            h / 2f,
            colorsInt,
            null
        )
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle, w / 2f, h / 2f)
        sweepShader.setLocalMatrix(matrix)

        drawRoundRect(
            brush = ShaderBrush(sweepShader),
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(w - strokePx, h - strokePx),
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = strokePx)
        )
    }
}
