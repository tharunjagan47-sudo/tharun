package com.simats.diawell123.ui.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.launch

@Composable
fun OnboardingContainerScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 0) { 3 }
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            0 -> FoodOnboardingScreen(
                onNextClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                onSkipClick = onFinishOnboarding,
            )

            1 -> MedicationOnboardingScreen(
                onNextClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
                onSkipClick = onFinishOnboarding,
            )

            2 -> ProgressOnboardingScreen(
                onGetStartedClick = onFinishOnboarding,
                onSkipClick = onFinishOnboarding,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingContainerScreenPreview() {
    Diawell123Theme {
        OnboardingContainerScreen(onFinishOnboarding = {})
    }
}
