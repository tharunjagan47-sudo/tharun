package com.simats.diawell123.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationOnboardingScreen(
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            OnboardingTopBar(onSkipClick = onSkipClick)

            // Center Illustration + Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Medication Illustration
                MedicationIllustration(
                    modifier = Modifier.size(260.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Badge
                OnboardingBadge(text = "BUILD A DAILY ROUTINE")

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "A gentle reminder.\nRight on time.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Keep your prescribed medication schedule in one place. Get friendly reminders and mark each dose as taken.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6E6877),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }

            // Bottom Navigation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
            ) {
                OnboardingPageIndicator(pageCount = 3, currentPage = 1)

                Spacer(modifier = Modifier.height(24.dp))

                OnboardingGradientButton(
                    text = "→  Next",
                    onClick = onNextClick
                )
            }
        }
    }
}

@Composable
fun MedicationIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val bgRadius = size.width * 0.48f

            // Soft pink circular background
            drawCircle(
                color = Color(0xFFFDEEF7),
                radius = bgRadius,
                center = center
            )

            // 1. Angled Pill/Capsule at top-left
            rotate(degrees = -30f, pivot = Offset(center.x - 30.dp.toPx(), center.y - 40.dp.toPx())) {
                val pillWidth = 110.dp.toPx()
                val pillHeight = 52.dp.toPx()
                val pillTopLeft = Offset(
                    center.x - 30.dp.toPx() - pillWidth / 2,
                    center.y - 40.dp.toPx() - pillHeight / 2
                )

                // White full pill background & border
                drawRoundRect(
                    color = Color.White,
                    topLeft = pillTopLeft,
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillHeight / 2, pillHeight / 2)
                )

                // Magenta right half of capsule
                drawRoundRect(
                    color = Color(0xFFB81D85),
                    topLeft = Offset(pillTopLeft.x + pillWidth / 2, pillTopLeft.y),
                    size = Size(pillWidth / 2, pillHeight),
                    cornerRadius = CornerRadius(pillHeight / 2, pillHeight / 2)
                )

                // Capsule Stroke outline
                drawRoundRect(
                    color = Color(0xFFB81D85),
                    topLeft = pillTopLeft,
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillHeight / 2, pillHeight / 2),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // 2. Clock on lower-right
            val clockCenter = Offset(center.x + 35.dp.toPx(), center.y + 25.dp.toPx())
            val clockRadius = 34.dp.toPx()

            // Clock face background & outline
            drawCircle(
                color = Color.White,
                radius = clockRadius,
                center = clockCenter
            )
            drawCircle(
                color = Color(0xFF9E1D81),
                radius = clockRadius,
                center = clockCenter,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Clock hands
            drawLine(
                color = Color(0xFF9E1D81),
                start = clockCenter,
                end = Offset(clockCenter.x, clockCenter.y - 18.dp.toPx()),
                strokeWidth = 2.5.dp.toPx()
            )
            drawLine(
                color = Color(0xFF9E1D81),
                start = clockCenter,
                end = Offset(clockCenter.x + 12.dp.toPx(), clockCenter.y),
                strokeWidth = 2.5.dp.toPx()
            )

            // 3. Small Alarm Bell outline on lower-left
            val bellCenter = Offset(center.x - 50.dp.toPx(), center.y + 30.dp.toPx())
            drawCircle(
                color = Color(0xFFD8A8CD),
                radius = 12.dp.toPx(),
                center = bellCenter,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicationOnboardingScreenPreview() {
    Diawell123Theme {
        MedicationOnboardingScreen(onNextClick = {}, onSkipClick = {})
    }
}
