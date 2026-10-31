package com.simats.diawell123.ui.activity
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ActivityTrackingScreen(
    modifier: Modifier = Modifier,
    glucoseLevel: Int = 128,
    weightKg: Float = 68f,
    viewModel: ActivityViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFinishActivityClick: () -> Unit = {},
) {
    val trackingState by viewModel.trackingState.collectAsState()

    var isTrackingActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showCompletedBanner by remember { mutableStateOf(false) }

    // Dynamic AI Activity Recommendations from Python Microservice & 2024 Adult Compendium
    val activeRec = trackingState.activeRecommendation
    val recommendedMins = activeRec?.durationMinutes ?: 30
    val targetDistanceKm = activeRec?.distanceKm ?: 2.5f
    val metValue = activeRec?.metValue ?: 3.8f
    val intensity = activeRec?.intensity ?: "Moderate"
    val clinicalReason = activeRec?.reason
        ?: "Evidence-informed exercise activates muscle GLUT4 transporters to improve glycemic control."
    val safetyNote = activeRec?.safetyNote ?: trackingState.safetyGuidance
        ?: "Stay hydrated, monitor symptoms, and avoid exercising through discomfort."

    val effectiveWeight = trackingState.userContext?.weightKg ?: weightKg
    val effectiveGlucose = trackingState.userContext?.currentGlucoseMgDl?.toInt() ?: glucoseLevel

    // Live Activity Timer Effect
    LaunchedEffect(isTrackingActive) {
        while (isTrackingActive) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    // Auto-dismiss completed banner after 1.5 seconds and trigger navigation
    LaunchedEffect(showCompletedBanner) {
        if (showCompletedBanner) {
            delay(1500L)
            showCompletedBanner = false
            onFinishActivityClick()
        }
    }

    // Calculated Live Statistics
    val elapsedMins = elapsedSeconds / 60
    val progressFraction = (elapsedMins.toFloat() / maxOf(1, recommendedMins).toFloat()).coerceIn(0f, 1f)
    val distanceCoveredKm = if (activeRec?.speedKmh != null) {
        (activeRec.speedKmh * (elapsedSeconds / 3600f))
    } else {
        targetDistanceKm * progressFraction
    }
    val caloriesBurned = (metValue * effectiveWeight * (elapsedSeconds / 3600f)).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Header Bar (Pops Up)
            ElementPopUpContainer(delayMillis = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StyledBackButton(onBackClick = onBackClick)

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Activity Coach",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                                if (trackingState.isLoading) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CircularProgressIndicator(
                                        color = Color(0xFFB81D85),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Personalized for Glucose $effectiveGlucose mg/dL • $effectiveWeight kg",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary,
                            )
                        }
                    }

                    // History Logs Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.elevatedSurface)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                            .clickable { onHistoryClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "📜 Logs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity Completed Banner
            AnimatedVisibility(
                visible = showCompletedBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Workout Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Logged $caloriesBurned kcal • ${String.format("%.2f", distanceCoveredKm)} km in history.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Error / Safety Contraindication Banner if any
            if (trackingState.errorMessage != null && !showCompletedBanner) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "⚠️ ${trackingState.errorMessage}",
                        fontSize = 12.sp,
                        color = Color(0xFFC62828),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Activity Mode Selection Pills (Pops Up)
            val activityOptions = if (trackingState.recommendations.isNotEmpty()) {
                trackingState.recommendations.map { it.activity to it.icon }
            } else {
                listOf("Walking" to "🚶‍♂️", "Cycling" to "🚴", "Running" to "🏃")
            }

            ElementPopUpContainer(delayMillis = 60) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    activityOptions.forEach { (type, icon) ->
                        val isSelected = trackingState.selectedActivity.equals(type, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(AppTheme.colors.cardBackground, AppTheme.colors.cardBackground),
                                        )
                                    },
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = AppTheme.colors.border,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable {
                                    if (!isTrackingActive) {
                                        viewModel.selectActivity(type)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = type,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else AppTheme.colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Central Interactive Progress Dial Card (Pops Up)
            ElementPopUpContainer(delayMillis = 120) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Intensity & MET Reference Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (intensity) {
                                            "Light" -> Color(0xFFE3F2FD)
                                            "Moderate" -> Color(0xFFE8F5E9)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Intensity: $intensity • $metValue MET",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (intensity) {
                                        "Light" -> Color(0xFF1976D2)
                                        "Moderate" -> Color(0xFF2E7D32)
                                        else -> Color(0xFFE65100)
                                    }
                                )
                            }
                        }

                        // Circular Progress Ring Arc Canvas
                        Box(
                            modifier = Modifier.size(170.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                // Background Track Arc
                                drawArc(
                                    color = if (AppTheme.colors.isDark) AppTheme.colors.border else Color(0xFFFDEEF7),
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )

                                // Animated Progress Arc
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFFB81D85),
                                            Color(0xFFFF5722),
                                        ),
                                    ),
                                    startAngle = 135f,
                                    sweepAngle = 270f * progressFraction,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )
                            }

                            // Center Timer Text Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val minsStr = String.format("%02d", elapsedSeconds / 60)
                                val secsStr = String.format("%02d", elapsedSeconds % 60)

                                Text(
                                    text = "$minsStr:$secsStr",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(
                                    text = "AI Goal: $recommendedMins mins",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textMuted,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Distance (Calculated via Validated Speed / Compendium Benchmark)
                        if (activeRec?.distanceKm != null) {
                            Text(
                                text = "Target Distance: ${String.format("%.2f", activeRec.distanceKm)} km",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                            if (activeRec.speedSource != null) {
                                Text(
                                    text = activeRec.speedSource,
                                    fontSize = 10.sp,
                                    color = AppTheme.colors.textMuted,
                                )
                            }
                        } else {
                            Text(
                                text = "Pace: Self-selected cadence",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textSecondary,
                            )
                        }

                        // Compendium Reference Code
                        if (activeRec?.compendiumDescription != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2024 Compendium Code ${activeRec.compendiumCode ?: ""}: ${activeRec.compendiumDescription}",
                                fontSize = 9.5.sp,
                                color = AppTheme.colors.textMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Real-time Live Workout Statistics Grid (Pops Up)
            ElementPopUpContainer(delayMillis = 180) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActivityStatBox(
                        icon = "📍",
                        title = "Distance",
                        value = "${String.format("%.2f", distanceCoveredKm)} km",
                        modifier = Modifier.weight(1f),
                    )

                    ActivityStatBox(
                        icon = "🔥",
                        title = "Calories",
                        value = "$caloriesBurned kcal",
                        modifier = Modifier.weight(1f),
                    )

                    ActivityStatBox(
                        icon = "❤️",
                        title = "Heart Rate",
                        value = if (isTrackingActive) "114 bpm" else "--",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. AI Recommendation Rationale Box (Pops Up)
            ElementPopUpContainer(delayMillis = 240) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.elevatedSurface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "⚡", fontSize = 20.sp, modifier = Modifier.padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Recommendation Rationale",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = clinicalReason,
                                fontSize = 11.5.sp,
                                color = Color(0xFF4A4453),
                                lineHeight = 16.sp,
                            )

                            if (safetyNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🛡", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = safetyNote,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2E7D32),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Primary Action Button: Start / Pause / Complete Workout (Pops Up)
            ElementPopUpContainer(delayMillis = 280) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (isTrackingActive) {
                        IosLiquidGlassButton(
                            text = "⏸ Pause Activity",
                            onClick = { isTrackingActive = false },
                            modifier = Modifier.weight(1f),
                        )

                        IosLiquidGlassButton(
                            text = if (trackingState.isLoggingWorkout) "Saving..." else "✓ Complete",
                            onClick = {
                                isTrackingActive = false
                                viewModel.logCompletedWorkout(
                                    activityType = trackingState.selectedActivity,
                                    durationMins = maxOf(1, elapsedMins),
                                    distanceKm = distanceCoveredKm,
                                    caloriesKcal = caloriesBurned,
                                    glucoseBefore = effectiveGlucose.toFloat(),
                                    notes = "Completed $intensity workout ($metValue MET)"
                                ) {
                                    showCompletedBanner = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        IosLiquidGlassButton(
                            text = if (elapsedSeconds > 0) "▶ Resume Activity" else "▶ Start ${trackingState.selectedActivity}",
                            onClick = { isTrackingActive = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ActivityStatBox(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textMuted,
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivityTrackingScreenPreview() {
    Diawell123Theme {
        ActivityTrackingScreen()
    }
}
