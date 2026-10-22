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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun FoodOnboardingScreen(
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
                // Food Illustration
                FoodIllustration(
                    modifier = Modifier.size(260.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Badge
                OnboardingBadge(text = "EAT WITH CONFIDENCE")

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "A little balance.\nA better every day.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Discover healthy meals and personalized diet suggestions that fit your preferences and daily routine.",
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
                OnboardingPageIndicator(pageCount = 3, currentPage = 0)

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
fun FoodIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val bgRadius = size.width * 0.48f

            drawCircle(
                color = Color(0xFFFDEEF7),
                radius = bgRadius,
                center = center
            )

            val plateRadius = size.width * 0.34f
            drawCircle(
                color = Color.White,
                radius = plateRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFFF2E0EE),
                radius = plateRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = Color(0xFFFBF2FA),
                radius = plateRadius * 0.78f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            val green = Color(0xFF53A17B)
            val lightGreen = Color(0xFF6CBD92)
            drawCircle(color = green, radius = 18.dp.toPx(), center = Offset(center.x - 22.dp.toPx(), center.y - 12.dp.toPx()))
            drawCircle(color = lightGreen, radius = 14.dp.toPx(), center = Offset(center.x - 30.dp.toPx(), center.y + 12.dp.toPx()))
            drawCircle(color = green, radius = 16.dp.toPx(), center = Offset(center.x - 8.dp.toPx(), center.y + 20.dp.toPx()))

            val orange = Color(0xFFE8735C)
            drawCircle(color = orange, radius = 12.dp.toPx(), center = Offset(center.x + 22.dp.toPx(), center.y - 18.dp.toPx()))
            drawCircle(color = orange, radius = 14.dp.toPx(), center = Offset(center.x + 28.dp.toPx(), center.y + 8.dp.toPx()))

            val beige = Color(0xFFD6BA96)
            drawCircle(color = beige, radius = 18.dp.toPx(), center = Offset(center.x + 4.dp.toPx(), center.y - 10.dp.toPx()))

            val forkX = center.x - plateRadius - 18.dp.toPx()
            drawLine(
                color = Color(0xFFD4A3C8),
                start = Offset(forkX, center.y - 20.dp.toPx()),
                end = Offset(forkX, center.y + 24.dp.toPx()),
                strokeWidth = 2.5.dp.toPx()
            )
            drawLine(
                color = Color(0xFFD4A3C8),
                start = Offset(forkX - 4.dp.toPx(), center.y - 20.dp.toPx()),
                end = Offset(forkX - 4.dp.toPx(), center.y - 8.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color(0xFFD4A3C8),
                start = Offset(forkX + 4.dp.toPx(), center.y - 20.dp.toPx()),
                end = Offset(forkX + 4.dp.toPx(), center.y - 8.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            val knifeX = forkX + 8.dp.toPx()
            drawLine(
                color = Color(0xFFD4A3C8),
                start = Offset(knifeX, center.y - 20.dp.toPx()),
                end = Offset(knifeX, center.y + 24.dp.toPx()),
                strokeWidth = 2.5.dp.toPx()
            )

            val leafPath = Path().apply {
                moveTo(center.x + bgRadius * 0.65f, center.y - bgRadius * 0.65f)
                cubicTo(
                    center.x + bgRadius * 0.85f, center.y - bgRadius * 0.75f,
                    center.x + bgRadius * 0.85f, center.y - bgRadius * 0.45f,
                    center.x + bgRadius * 0.65f, center.y - bgRadius * 0.65f
                )
            }
            drawPath(
                path = leafPath,
                color = Color(0xFFB570A5),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodOnboardingScreenPreview() {
    Diawell123Theme {
        FoodOnboardingScreen(onNextClick = {}, onSkipClick = {})
    }
}
