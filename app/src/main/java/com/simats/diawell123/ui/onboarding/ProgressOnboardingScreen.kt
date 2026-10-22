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
import androidx.compose.ui.graphics.Brush
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
fun ProgressOnboardingScreen(
    onGetStartedClick: () -> Unit,
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
                // Progress Illustration
                ProgressIllustration(
                    modifier = Modifier.size(260.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Badge
                OnboardingBadge(text = "SEE YOUR PROGRESS")

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Small steps.\nA clearer picture.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Log your glucose readings and explore weekly reports. See your habits over time, one day at a time.",
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
                OnboardingPageIndicator(pageCount = 3, currentPage = 2)

                Spacer(modifier = Modifier.height(24.dp))

                OnboardingGradientButton(
                    text = "→  Get Started",
                    onClick = onGetStartedClick
                )
            }
        }
    }
}

@Composable
fun ProgressIllustration(modifier: Modifier = Modifier) {
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

            val cardWidth = 150.dp.toPx()
            val cardHeight = 120.dp.toPx()
            val cardTopLeft = Offset(center.x - cardWidth / 2, center.y - cardHeight / 2 - 10.dp.toPx())

            drawRoundRect(
                color = Color.White,
                topLeft = cardTopLeft,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFF2E0EE),
                topLeft = cardTopLeft,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            drawRoundRect(
                color = Color(0xFFF7EBF6),
                topLeft = Offset(cardTopLeft.x + 16.dp.toPx(), cardTopLeft.y + 16.dp.toPx()),
                size = Size(60.dp.toPx(), 8.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            val chartPath = Path().apply {
                val startX = cardTopLeft.x + 16.dp.toPx()
                val startY = cardTopLeft.y + cardHeight - 24.dp.toPx()

                moveTo(startX, startY)
                lineTo(startX + 24.dp.toPx(), startY - 20.dp.toPx())
                lineTo(startX + 44.dp.toPx(), startY - 8.dp.toPx())
                lineTo(startX + 70.dp.toPx(), startY - 44.dp.toPx())
                lineTo(startX + 92.dp.toPx(), startY - 28.dp.toPx())
                lineTo(startX + 118.dp.toPx(), startY - 60.dp.toPx())
            }

            drawPath(
                path = chartPath,
                color = Color(0xFFB81D85),
                style = Stroke(width = 3.5.dp.toPx())
            )

            val checkBadgeCenter = Offset(cardTopLeft.x + cardWidth - 8.dp.toPx(), cardTopLeft.y + cardHeight - 8.dp.toPx())
            val checkBadgeRadius = 22.dp.toPx()

            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF7A1C7D), Color(0xFFFF5722))
                ),
                radius = checkBadgeRadius,
                center = checkBadgeCenter
            )

            val checkPath = Path().apply {
                moveTo(checkBadgeCenter.x - 8.dp.toPx(), checkBadgeCenter.y)
                lineTo(checkBadgeCenter.x - 2.dp.toPx(), checkBadgeCenter.y + 6.dp.toPx())
                lineTo(checkBadgeCenter.x + 8.dp.toPx(), checkBadgeCenter.y - 5.dp.toPx())
            }

            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProgressOnboardingScreenPreview() {
    Diawell123Theme {
        ProgressOnboardingScreen(onGetStartedClick = {}, onSkipClick = {})
    }
}
