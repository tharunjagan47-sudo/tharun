package com.simats.diawell123.ui.onboarding
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun ProgressOnboardingScreen(
    onGetStartedClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
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
                    isActive = isActive,
                    modifier = Modifier.size(240.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                OnboardingAnimatedCopy(
                    badgeText = "SEE YOUR PROGRESS",
                    title = "Small steps.\nA clearer picture.",
                    description = "Log your glucose readings and explore weekly reports. See your habits over time, one day at a time.",
                    isActive = isActive,
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

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingGradientButton(
                    text = "→  Get Started",
                    onClick = onGetStartedClick
                )
            }
        }
    }
}

@Composable
fun ProgressIllustration(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    val isDark = AppTheme.colors.isDark
    val bgCircleColor = if (isDark) Color(0xFF3B0764).copy(alpha = 0.20f) else Color(0xFFF5F3FF)
    val cardFillColor = if (isDark) AppTheme.colors.cardElevated else Color.White
    val cardStrokeColor = if (isDark) AppTheme.colors.border else Color(0xFFF2E0EE)
    val barBgColor = if (isDark) AppTheme.colors.elevatedSurface else Color(0xFFF7EBF6)

    val entranceScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "ProgressEntranceScale",
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.2f,
        animationSpec = tween(durationMillis = 360),
        label = "ProgressEntranceAlpha",
    )
    val chartProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.05f,
        animationSpec = tween(
            durationMillis = 850,
            delayMillis = 120,
            easing = FastOutSlowInEasing,
        ),
        label = "ProgressChartDraw",
    )
    val badgeScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "ProgressBadgeScale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ProgressIdleMotion")
    val cardFloatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ProgressFloatY",
    )
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ProgressBadgePulse",
    )

    val pathMeasure = remember { PathMeasure() }
    val animatedChartPath = remember { Path() }

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = entranceScale
            scaleY = entranceScale
            alpha = entranceAlpha
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val bgRadius = size.width * 0.48f

            drawCircle(
                color = bgCircleColor,
                radius = bgRadius,
                center = center
            )

            val cardWidth = 150.dp.toPx()
            val cardHeight = 120.dp.toPx()
            val cardTopLeft = Offset(
                center.x - cardWidth / 2f,
                center.y - cardHeight / 2f - 10.dp.toPx() + cardFloatY.dp.toPx(),
            )

            drawRoundRect(
                color = cardFillColor,
                topLeft = cardTopLeft,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
            )
            drawRoundRect(
                color = cardStrokeColor,
                topLeft = cardTopLeft,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            drawRoundRect(
                color = barBgColor,
                topLeft = Offset(cardTopLeft.x + 16.dp.toPx(), cardTopLeft.y + 16.dp.toPx()),
                size = Size((60.dp * chartProgress.coerceAtLeast(0.2f)).toPx(), 8.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            val fullChartPath = Path().apply {
                val startX = cardTopLeft.x + 16.dp.toPx()
                val startY = cardTopLeft.y + cardHeight - 24.dp.toPx()

                moveTo(startX, startY)
                lineTo(startX + 24.dp.toPx(), startY - 20.dp.toPx())
                lineTo(startX + 44.dp.toPx(), startY - 8.dp.toPx())
                lineTo(startX + 70.dp.toPx(), startY - 44.dp.toPx())
                lineTo(startX + 92.dp.toPx(), startY - 28.dp.toPx())
                lineTo(startX + 118.dp.toPx(), startY - 60.dp.toPx())
            }

            pathMeasure.setPath(fullChartPath, false)
            animatedChartPath.reset()
            val targetDistance = pathMeasure.length * chartProgress
            pathMeasure.getSegment(0f, targetDistance, animatedChartPath, true)

            drawPath(
                path = animatedChartPath,
                color = Color(0xFFA855F7),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            if (pathMeasure.length > 0f) {
                val tipPos = pathMeasure.getPosition(targetDistance.coerceIn(0f, pathMeasure.length))
                drawCircle(
                    color = Color(0xFFF0ABFC).copy(alpha = 0.45f),
                    radius = 7.dp.toPx(),
                    center = tipPos,
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = tipPos,
                )
            }

            val checkBadgeCenter = Offset(cardTopLeft.x + cardWidth - 8.dp.toPx(), cardTopLeft.y + cardHeight - 8.dp.toPx())
            val checkBadgeRadius = 22.dp.toPx() * badgeScale

            if (badgeScale > 0.05f) {
                drawCircle(
                    color = Color(0xFFA855F7).copy(alpha = (1f - badgePulse) * 0.45f * badgeScale),
                    radius = checkBadgeRadius + (10.dp * badgePulse).toPx(),
                    center = checkBadgeCenter,
                )

                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFFF0ABFC))
                    ),
                    radius = checkBadgeRadius,
                    center = checkBadgeCenter
                )

                val checkPath = Path().apply {
                    moveTo(checkBadgeCenter.x - 8.dp.toPx() * badgeScale, checkBadgeCenter.y)
                    lineTo(checkBadgeCenter.x - 2.dp.toPx() * badgeScale, checkBadgeCenter.y + 6.dp.toPx() * badgeScale)
                    lineTo(checkBadgeCenter.x + 8.dp.toPx() * badgeScale, checkBadgeCenter.y - 5.dp.toPx() * badgeScale)
                }

                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
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
