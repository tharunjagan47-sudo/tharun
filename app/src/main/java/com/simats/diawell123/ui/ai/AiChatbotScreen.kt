package com.simats.diawell123.ui.ai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.theme.Diawell123Theme

/**
 * Backwards-compatible composable forwarding to Dr. Jarvis ChatScreen.
 * Seamlessly integrates local Ollama Qwen 2.5 backend and speech-to-text.
 */
@Composable
fun AiChatbotScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    ChatScreen(
        modifier = modifier,
        viewModel = viewModel,
        onBackClick = onBackClick
    )
}

fun lerpFloat(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp = start + (stop - start) * fraction

/**
 * Iron Man Doctor Avatar Canvas Graphics.
 * Metallic red helmet with golden faceplate, cyan glowing eye slits,
 * white clinical coat, and mini arc reactor node.
 */
@Composable
fun IronmanDoctorAvatar(
    modifier: Modifier = Modifier,
    sizeDp: Int = 40,
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(0xFF8E0000)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Red Metallic Base Head
            drawCircle(color = Color(0xFFC62828), radius = w / 2)

            // 2. Iron Man Gold Faceplate Path
            val goldPlatePath = Path().apply {
                moveTo(w * 0.28f, h * 0.22f)
                lineTo(w * 0.72f, h * 0.22f)
                lineTo(w * 0.80f, h * 0.42f)
                lineTo(w * 0.65f, h * 0.68f)
                lineTo(w * 0.35f, h * 0.68f)
                lineTo(w * 0.20f, h * 0.42f)
                close()
            }

            drawPath(path = goldPlatePath, color = Color(0xFFFFD54F))
            drawPath(path = goldPlatePath, color = Color(0xFFFFA000), style = Stroke(width = 1.2.dp.toPx()))

            // 3. Iron Man Glowing Cyan Slanted Eye Slits
            val leftEye = Path().apply {
                moveTo(w * 0.30f, h * 0.38f)
                lineTo(w * 0.46f, h * 0.40f)
                lineTo(w * 0.45f, h * 0.44f)
                lineTo(w * 0.32f, h * 0.43f)
                close()
            }

            val rightEye = Path().apply {
                moveTo(w * 0.70f, h * 0.38f)
                lineTo(w * 0.54f, h * 0.40f)
                lineTo(w * 0.55f, h * 0.44f)
                lineTo(w * 0.68f, h * 0.43f)
                close()
            }

            drawPath(path = leftEye, color = Color(0xFF00E5FF))
            drawPath(path = rightEye, color = Color(0xFF00E5FF))

            // 4. White Doctor Lab Coat Shoulders
            val coatPath = Path().apply {
                moveTo(0f, h)
                lineTo(w * 0.22f, h * 0.62f)
                lineTo(w * 0.50f, h * 0.76f)
                lineTo(w * 0.78f, h * 0.62f)
                lineTo(w, h)
                close()
            }
            drawPath(coatPath, color = Color.White)
            drawPath(coatPath, color = Color(0xFFB0BEC5), style = Stroke(width = 1.dp.toPx()))

            // 5. Stethoscope Tubing across coat with Mini Arc Reactor Node
            val stethoPath = Path().apply {
                moveTo(w * 0.32f, h * 0.68f)
                cubicTo(w * 0.35f, h * 0.88f, w * 0.65f, h * 0.88f, w * 0.68f, h * 0.68f)
            }
            drawPath(stethoPath, color = Color(0xFF37474F), style = Stroke(width = 2.5.dp.toPx()))
            drawCircle(color = Color(0xFF00E5FF), radius = 3.dp.toPx(), center = Offset(w * 0.5f, h * 0.82f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiChatbotScreenPreview() {
    Diawell123Theme {
        AiChatbotScreen()
    }
}
