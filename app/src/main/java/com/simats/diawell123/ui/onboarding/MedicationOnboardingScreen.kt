package com.simats.diawell123.ui.onboarding
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationOnboardingScreen(
    onNextClick: () -> Unit,
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
                // Medication Illustration
                MedicationIllustration(
                    isActive = isActive,
                    modifier = Modifier.size(240.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                OnboardingAnimatedCopy(
                    badgeText = "BUILD A DAILY ROUTINE",
                    title = "A gentle reminder.\nRight on time.",
                    description = "Keep your prescribed medication schedule in one place. Get friendly reminders and mark each dose as taken.",
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
                OnboardingPageIndicator(pageCount = 3, currentPage = 1)

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingGradientButton(
                    text = "→  Next",
                    onClick = onNextClick
                )
            }
        }
    }
}

@Composable
fun MedicationIllustration(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    val isDark = AppTheme.colors.isDark
    val bgCircleColor = if (isDark) Color(0xFF3B0764).copy(alpha = 0.20f) else Color(0xFFF5F3FF)
    val pillBaseColor = if (isDark) AppTheme.colors.cardElevated else Color.White
    val clockTint = if (isDark) Color(0xFFF0ABFC) else Color(0xFF9E1D81)

    val entranceScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "MedEntranceScale",
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.2f,
        animationSpec = tween(durationMillis = 360),
        label = "MedEntranceAlpha",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "MedIdleMotion")
    val pillFloatY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PillFloatY",
    )
    val pillTilt by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PillTilt",
    )
    val minuteAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ClockMinuteHand",
    )
    val hourAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ClockHourHand",
    )
    val bellRipple by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "BellRipple",
    )

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

            val pillPivot = Offset(center.x - 30.dp.toPx(), center.y - 40.dp.toPx() + pillFloatY.dp.toPx())
            rotate(degrees = pillTilt, pivot = pillPivot) {
                val pillWidth = 110.dp.toPx()
                val pillHeight = 52.dp.toPx()
                val pillTopLeft = Offset(
                    pillPivot.x - pillWidth / 2f,
                    pillPivot.y - pillHeight / 2f
                )

                drawRoundRect(
                    color = pillBaseColor,
                    topLeft = pillTopLeft,
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f)
                )

                drawRoundRect(
                    color = Color(0xFFA855F7),
                    topLeft = Offset(pillTopLeft.x + pillWidth / 2f, pillTopLeft.y),
                    size = Size(pillWidth / 2f, pillHeight),
                    cornerRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f)
                )

                drawRoundRect(
                    color = Color(0xFFA855F7),
                    topLeft = pillTopLeft,
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            val clockCenter = Offset(center.x + 35.dp.toPx(), center.y + 25.dp.toPx() - pillFloatY.dp.toPx() * 0.35f)
            val clockRadius = 34.dp.toPx()

            drawCircle(
                color = pillBaseColor,
                radius = clockRadius,
                center = clockCenter
            )
            drawCircle(
                color = clockTint,
                radius = clockRadius,
                center = clockCenter,
                style = Stroke(width = 2.5.dp.toPx())
            )

            rotate(degrees = minuteAngle, pivot = clockCenter) {
                drawLine(
                    color = clockTint,
                    start = clockCenter,
                    end = Offset(clockCenter.x, clockCenter.y - 18.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
            rotate(degrees = hourAngle, pivot = clockCenter) {
                drawLine(
                    color = clockTint,
                    start = clockCenter,
                    end = Offset(clockCenter.x + 12.dp.toPx(), clockCenter.y),
                    strokeWidth = 2.5.dp.toPx()
                )
            }

            val bellCenter = Offset(center.x - 50.dp.toPx(), center.y + 30.dp.toPx() + pillFloatY.dp.toPx() * 0.4f)
            drawCircle(
                color = Color(0xFFA855F7).copy(alpha = (1f - bellRipple) * 0.55f),
                radius = (12.dp + (12.dp * bellRipple)).toPx(),
                center = bellCenter,
                style = Stroke(width = 1.8.dp.toPx())
            )
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
