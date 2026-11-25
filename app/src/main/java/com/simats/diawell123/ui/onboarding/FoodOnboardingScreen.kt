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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun FoodOnboardingScreen(
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
                // Food Illustration
                FoodIllustration(
                    isActive = isActive,
                    modifier = Modifier.size(240.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                OnboardingAnimatedCopy(
                    badgeText = "EAT WITH CONFIDENCE",
                    title = "A little balance.\nA better every day.",
                    description = "Discover healthy meals and personalized diet suggestions that fit your preferences and daily routine.",
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
                OnboardingPageIndicator(pageCount = 3, currentPage = 0)

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
fun FoodIllustration(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    val isDark = AppTheme.colors.isDark
    val bgCircleColor = if (isDark) Color(0xFF3B0764).copy(alpha = 0.20f) else Color(0xFFF5F3FF)
    val plateColor = if (isDark) AppTheme.colors.cardElevated else Color.White
    val plateStrokeColor = if (isDark) AppTheme.colors.border else Color(0xFFF2E0EE)
    val rimColor = if (isDark) Color(0xFFA855F7).copy(alpha = 0.25f) else Color(0xFFFBF2FA)

    val entranceScale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "FoodEntranceScale",
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.2f,
        animationSpec = tween(durationMillis = 360),
        label = "FoodEntranceAlpha",
    )
    val utensilSlide by animateFloatAsState(
        targetValue = if (isActive) 0f else 18f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "UtensilSlide",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "FoodIdleMotion")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "FoodFloatY",
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "FoodHaloScale",
    )
    val leafSway by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "LeafSway",
    )
    val garnishBob by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "GarnishBob",
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
            val center = Offset(size.width / 2f, size.height / 2f + floatY.dp.toPx() * 0.4f)
            val bgRadius = size.width * 0.48f * haloScale

            drawCircle(
                color = bgCircleColor,
                radius = bgRadius,
                center = Offset(size.width / 2f, size.height / 2f)
            )

            val plateRadius = size.width * 0.34f
            drawCircle(
                color = plateColor,
                radius = plateRadius,
                center = center
            )
            drawCircle(
                color = plateStrokeColor,
                radius = plateRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = rimColor,
                radius = plateRadius * 0.78f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            val bobPx = garnishBob.dp.toPx()
            val green = Color(0xFF53A17B)
            val lightGreen = Color(0xFF6CBD92)
            drawCircle(color = green, radius = 18.dp.toPx(), center = Offset(center.x - 22.dp.toPx(), center.y - 12.dp.toPx() + bobPx * 0.6f))
            drawCircle(color = lightGreen, radius = 14.dp.toPx(), center = Offset(center.x - 30.dp.toPx(), center.y + 12.dp.toPx() - bobPx * 0.5f))
            drawCircle(color = green, radius = 16.dp.toPx(), center = Offset(center.x - 8.dp.toPx(), center.y + 20.dp.toPx() + bobPx * 0.4f))

            val orange = Color(0xFFE8735C)
            drawCircle(color = orange, radius = 12.dp.toPx(), center = Offset(center.x + 22.dp.toPx(), center.y - 18.dp.toPx() - bobPx * 0.6f))
            drawCircle(color = orange, radius = 14.dp.toPx(), center = Offset(center.x + 28.dp.toPx(), center.y + 8.dp.toPx() + bobPx * 0.5f))

            val beige = Color(0xFFD6BA96)
            drawCircle(color = beige, radius = 18.dp.toPx(), center = Offset(center.x + 4.dp.toPx(), center.y - 10.dp.toPx() - bobPx * 0.4f))

            val forkX = center.x - plateRadius - 18.dp.toPx() - utensilSlide.dp.toPx()
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

            val leafPivot = Offset(center.x + bgRadius * 0.65f, center.y - bgRadius * 0.65f)
            rotate(degrees = leafSway, pivot = leafPivot) {
                val leafPath = Path().apply {
                    moveTo(leafPivot.x, leafPivot.y)
                    cubicTo(
                        center.x + bgRadius * 0.85f, center.y - bgRadius * 0.75f,
                        center.x + bgRadius * 0.85f, center.y - bgRadius * 0.45f,
                        leafPivot.x, leafPivot.y
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodOnboardingScreenPreview() {
    Diawell123Theme {
        FoodOnboardingScreen(onNextClick = {}, onSkipClick = {})
    }
}
