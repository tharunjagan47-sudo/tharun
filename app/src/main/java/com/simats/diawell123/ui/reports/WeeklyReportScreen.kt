package com.simats.diawell123.ui.reports

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun WeeklyReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Reports") }
    var selectedWeekText by remember { mutableStateOf("8–15 Oct 2026") }
    var showCalendarDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val report = uiState.reportData

    // Smooth Line Sweep & Stop Graph Animation
    val chartProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedWeekText, report) {
        chartProgress.snapTo(0f)
        chartProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    val displayWeekLabel = report?.weekLabel?.takeIf { it.isNotBlank() } ?: selectedWeekText
    val avgGlucose = report?.glucose?.average ?: "120 mg/dL"
    val timeInRange = report?.glucose?.timeInRangePercent ?: 78
    val glucoseTag = report?.glucose?.summaryTag ?: "Optimal Glycemic Control"
    val mealsLoggedText = if (report != null && report.nutrition.totalMealsLogged > 0) {
        "${report.nutrition.totalMealsLogged} of 28"
    } else {
        "25 of 28"
    }
    val medAdherence = report?.medication?.adherencePercent ?: 90
    val medStatus = report?.medication?.statusLabel ?: "18 marked taken • 1 skipped • 1 upcoming"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar & Date Selector (Pops Up)
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StyledBackButton(onBackClick = onBackClick)

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Your weekly report",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "A clearer view of your everyday habits.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${ProfileState.userName} • Personalized health summary",
                            fontSize = 11.sp,
                            color = Color(0xFF811978),
                            fontWeight = FontWeight.Medium,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Date Selector Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                                .clickable { showCalendarDialog = true }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = "‹", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                                Text(
                                    text = "📅  $displayWeekLabel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(text = "›", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFB81D85),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Glucose Overview Animated Pulse Line Box (Pops Up)
                ElementPopUpContainer(delayMillis = 80) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Glucose overview",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "mg/dL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB81D85),
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Animated Sweep-and-Stop Glucose ECG Pulse Line Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    val points = listOf(
                                        Offset(w * 0.08f, h * 0.65f),
                                        Offset(w * 0.28f, h * 0.38f),
                                        Offset(w * 0.50f, h * 0.22f),
                                        Offset(w * 0.72f, h * 0.58f),
                                        Offset(w * 0.92f, h * 0.32f),
                                    )

                                    val progress = chartProgress.value
                                    val maxAllowedX = w * progress

                                    val ecgPath = Path().apply {
                                        val startX = points.first().x
                                        moveTo(0f, points.first().y)
                                        lineTo(startX, points.first().y)

                                        for (i in 0 until points.size - 1) {
                                            val pA = points[i]
                                            val pB = points[i + 1]
                                            val segW = pB.x - pA.x

                                            lineTo(pA.x + segW * 0.20f, pA.y)
                                            lineTo(pA.x + segW * 0.35f, pA.y - 12.dp.toPx())
                                            lineTo(pA.x + segW * 0.45f, pA.y)
                                            lineTo(pA.x + segW * 0.52f, pA.y + 16.dp.toPx())
                                            lineTo(pA.x + segW * 0.60f, (pA.y + pB.y) / 2 - 32.dp.toPx())
                                            lineTo(pA.x + segW * 0.68f, (pA.y + pB.y) / 2 + 24.dp.toPx())
                                            lineTo(pA.x + segW * 0.78f, pB.y - 10.dp.toPx())
                                            lineTo(pA.x + segW * 0.88f, pB.y)
                                            lineTo(pB.x, pB.y)
                                        }
                                        lineTo(w, points.last().y)
                                    }

                                    clipRect(left = 0f, top = 0f, right = maxAllowedX, bottom = h) {
                                        val fillEcgPath = Path().apply {
                                            addPath(ecgPath)
                                            lineTo(w, h)
                                            lineTo(0f, h)
                                            close()
                                        }

                                        drawPath(
                                            path = fillEcgPath,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0x35B81D85),
                                                    Color(0x00B81D85),
                                                ),
                                            ),
                                        )

                                        drawPath(
                                            path = ecgPath,
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF00E5FF),
                                                    Color(0xFFB81D85),
                                                    Color(0xFFFF5722),
                                                ),
                                            ),
                                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
                                        )

                                        points.forEach { pt ->
                                            if (pt.x <= maxAllowedX) {
                                                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                                                drawCircle(color = Color(0xFFB81D85), radius = 3.5.dp.toPx(), center = pt)
                                            }
                                        }
                                    }

                                    if (progress > 0f && progress < 1f) {
                                        val currentTipX = maxAllowedX.coerceIn(0f, w)
                                        val tipY = h * 0.45f

                                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(currentTipX, tipY))
                                        drawCircle(
                                            color = Color(0xFFFF5722),
                                            radius = 8.dp.toPx(),
                                            center = Offset(currentTipX, tipY),
                                            style = Stroke(width = 2.dp.toPx()),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Weekly average: $avgGlucose • $timeInRange% time in range ($glucoseTag)",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meals Logged Animated Bar Chart Box (Pops Up)
                ElementPopUpContainer(delayMillis = 160) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Meals logged",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = mealsLoggedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB81D85),
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val barWidth = 16.dp.toPx()
                                    val barSpacing = (w - (barWidth * 7)) / 8

                                    val heights = listOf(0.9f, 1.0f, 0.8f, 1.0f, 0.95f, 1.0f, 0.85f)

                                    heights.forEachIndexed { i, factor ->
                                        val left = barSpacing + i * (barWidth + barSpacing)
                                        val animatedFactor = factor * chartProgress.value
                                        val barH = h * animatedFactor
                                        drawRoundRect(
                                            color = Color(0xFFB81D85),
                                            topLeft = Offset(left, h - barH),
                                            size = Size(barWidth, barH),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Avg daily calories: ${report?.nutrition?.dailyAverageCalories ?: "1,450 kcal"}",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medication Reminders Box (Pops Up)
                ElementPopUpContainer(delayMillis = 240) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Medication reminders",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "$medAdherence% adherence",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB81D85),
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { ((medAdherence / 100f) * chartProgress.value).coerceIn(0f, 1f) },
                                color = Color(0xFFB81D85),
                                trackColor = Color(0xFFFDEEF7),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = medStatus,
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7 days badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "7 days of health & glucose logging",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF811978),
                            )
                            Text(
                                text = if (report != null && report.activity.totalActiveMins > 0)
                                    "Active minutes: ${report.activity.totalActiveMins} mins across ${report.activity.workoutCount} workouts!"
                                else
                                    "Keep up the consistent routine!",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Calendar Date Picker Modal Dialog
        if (showCalendarDialog) {
            ReportCalendarDialog(
                onWeekSelected = { newWeek, endDate ->
                    selectedWeekText = newWeek
                    viewModel.loadWeeklyReport(endDate = endDate)
                },
                onDismissRequest = { showCalendarDialog = false },
            )
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock (Pops Up)
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = onHomeTabClick,
                onDietTabClick = onDietTabClick,
                onMedsTabClick = onMedsTabClick,
                onReportsTabClick = { selectedTab = "Reports" },
                onSettingsTabClick = onSettingsTabClick,
            )
        }
    }
}

@Composable
fun ReportCalendarDialog(
    onWeekSelected: (String, String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedDay by remember { mutableIntStateOf(15) }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFEFECEF), RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📅 Select Report Week",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8A8595),
                        modifier = Modifier
                            .clickable { onDismissRequest() }
                            .padding(4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Navigator Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "‹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81D85))
                    Text(
                        text = "October 2026",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )
                    Text(text = "›", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81D85))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of Week Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A8595),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(32.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Grid (1 to 31)
                val daysList = (1..31).toList()
                val rows = daysList.chunked(7)

                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        week.forEach { dayNumber ->
                            val isSelected = dayNumber == selectedDay
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFB81D85) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = if (isSelected) Color.Transparent else Color(0xFFEFECEF),
                                        shape = CircleShape,
                                    )
                                    .clickable { selectedDay = dayNumber }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF1E1E22),
                                )
                            }
                        }

                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F7FA))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6E6877),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                ),
                            )
                            .clickable {
                                val startDay = (selectedDay - 7).coerceAtLeast(1)
                                val formattedDay = if (selectedDay < 10) "0$selectedDay" else "$selectedDay"
                                val endDateIso = "2026-10-$formattedDay"
                                onWeekSelected("$startDay–$selectedDay Oct 2026", endDateIso)
                                onDismissRequest()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Select Week",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WeeklyReportScreenPreview() {
    Diawell123Theme {
        WeeklyReportScreen()
    }
}
