package com.simats.diawell123.ui.onboarding
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.diet.IosLiquidGlassButton

@Composable
fun OnboardingTopBar(
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "DiaWell",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7E22CE),
        )

        Box(
            modifier = Modifier
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(if (AppTheme.colors.isDark) AppTheme.colors.cardElevated else Color.White.copy(alpha = 0.85f))
                .border(1.dp, if (AppTheme.colors.isDark) AppTheme.colors.border else Color.White, RoundedCornerShape(20.dp))
                .clickable { onSkipClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Skip",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA855F7),
            )
        }
    }
}

@Composable
fun OnboardingBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFBEBF6))
            .border(1.dp, if (AppTheme.colors.isDark) AppTheme.colors.border else Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA855F7),
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
fun OnboardingAnimatedCopy(
    badgeText: String,
    title: String,
    description: String,
    isActive: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val badgeProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 360, delayMillis = 60, easing = FastOutSlowInEasing),
        label = "OnboardingBadgeEntrance",
    )
    val titleProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 130, easing = FastOutSlowInEasing),
        label = "OnboardingTitleEntrance",
    )
    val descProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "OnboardingDescEntrance",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        OnboardingBadge(
            text = badgeText,
            modifier = Modifier.graphicsLayer {
                alpha = badgeProgress
                translationY = (1f - badgeProgress) * 14.dp.toPx()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
            modifier = Modifier.graphicsLayer {
                alpha = titleProgress
                translationY = (1f - titleProgress) * 18.dp.toPx()
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.graphicsLayer {
                alpha = descProgress
                translationY = (1f - descProgress) * 20.dp.toPx()
            },
        )
    }
}

@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val inactiveColor = if (AppTheme.colors.isDark) AppTheme.colors.border else Color(0xFFE8D6E5)
    val activeColor = if (AppTheme.colors.isDark) Color(0xFFA855F7) else Color(0xFF9E1D81)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 26.dp else 7.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "DotWidth",
            )
            val dotColor by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 220),
                label = "DotColor",
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
fun OnboardingGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IosLiquidGlassButton(
        text = text,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    )
}
