package com.simats.diawell123.ui.activity

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFinishActivityClick: () -> Unit = {},
) {
    var selectedActivity by remember { mutableStateOf("Walking") } // "Walking", "Cycling", "Running"
    var isTrackingActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showCompletedBanner by remember { mutableStateOf(false) }

    // Dynamic AI Activity Recommendations based on Glucose & Weight
    val (recommendedMins, targetDistanceKm, metValue) = remember(selectedActivity, glucoseLevel, weightKg) {
        val baseGlucoseFactor = if (glucoseLevel > 140) 1.25f else if (glucoseLevel > 100) 1.1f else 1.0f
        when (selectedActivity) {
            "Walking" -> Triple((30 * baseGlucoseFactor).toInt(), 2.5f * baseGlucoseFactor, 3.8f)
            "Cycling" -> Triple((25 * baseGlucoseFactor).toInt(), 8.0f * baseGlucoseFactor, 6.8f)
            else -> Triple((20 * baseGlucoseFactor).toInt(), 3.5f * baseGlucoseFactor, 9.8f) // Running
        }
    }

    // Live Activity Timer Effect
    LaunchedEffect(isTrackingActive) {
        while (isTrackingActive) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    // Calculated Live Statistics
    val elapsedMins = elapsedSeconds / 60
    val progressFraction = (elapsedMins.toFloat() / recommendedMins.toFloat()).coerceIn(0f, 1f)
    val distanceCoveredKm = targetDistanceKm * progressFraction
    val caloriesBurned = (metValue * weightKg * (elapsedSeconds / 3600f)).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
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
                            Text(
                                text = "Activity & Workout",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E22),
                            )
                            Text(
                                text = "Target for Glucose $glucoseLevel mg/dL • $weightKg kg",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }

                    // History Logs Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDEEF7))
                            .border(1.dp, Color(0xFFF3D5E9), RoundedCornerShape(12.dp))
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
                                text = "Workout Completed Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Great job! $caloriesBurned kcal burned for optimal glucose balance.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Activity Mode Selection Pills (Pops Up)
            ElementPopUpContainer(delayMillis = 60) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf("Walking" to "🚶‍♂️", "Cycling" to "🚴", "Running" to "🏃").forEach { (type, icon) ->
                        val isSelected = selectedActivity == type
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
                                            colors = listOf(Color.White, Color.White),
                                        )
                                    },
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = Color(0xFFEFECEF),
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable {
                                    if (!isTrackingActive) {
                                        selectedActivity = type
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
                                    color = if (isSelected) Color.White else Color(0xFF1E1E22),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Central Interactive Progress Dial Card (Pops Up)
            ElementPopUpContainer(delayMillis = 120) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Circular Progress Ring Arc Canvas
                        Box(
                            modifier = Modifier.size(170.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                // Background Track Arc
                                drawArc(
                                    color = Color(0xFFFDEEF7),
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
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "Goal: $recommendedMins mins",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8A8595),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Target Recommendation Subtitle
                        Text(
                            text = "Target Distance: ${String.format("%.1f", targetDistanceKm)} km",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
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

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Dr. JARVIS AI Recommendation Box (Pops Up)
            ElementPopUpContainer(delayMillis = 240) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDEEF7))
                        .border(1.dp, Color(0xFFF3E2F0), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dr. JARVIS Recommendation",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF811978),
                            )
                            Text(
                                text = "Based on your fasting glucose ($glucoseLevel mg/dL) & weight ($weightKg kg), a $recommendedMins-min $selectedActivity will optimize insulin sensitivity today!",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                                lineHeight = 16.sp,
                            )
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
                            text = "✓ Complete",
                            onClick = {
                                isTrackingActive = false
                                showCompletedBanner = true
                                onFinishActivityClick()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        IosLiquidGlassButton(
                            text = if (elapsedSeconds > 0) "▶ Resume Activity" else "▶ Start $selectedActivity",
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
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
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
                color = Color(0xFF8A8595),
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
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
