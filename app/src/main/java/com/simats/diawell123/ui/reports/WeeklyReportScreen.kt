package com.simats.diawell123.ui.reports
import com.simats.diawell123.ui.theme.AppTheme

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
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
    onExportReportClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Reports") }
    val initialWeekText = remember {
        val cal = java.util.Calendar.getInstance()
        val endSdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
        val endStr = endSdf.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
        val startSdf = java.text.SimpleDateFormat("d", java.util.Locale.getDefault())
        val startStr = startSdf.format(cal.time)
        "$startStr–$endStr"
    }
    var selectedWeekText by remember { mutableStateOf(initialWeekText) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val report = uiState.reportData
    val chartData = report?.chartData ?: emptyList()

    LaunchedEffect(Unit) {
        if (report == null) {
            viewModel.loadWeeklyReport(isPullToRefresh = false)
        }
    }

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
    val avgGlucose = report?.glucose?.average?.takeIf { it.isNotBlank() && it != "0 mg/dL" } ?: "-- mg/dL"
    val timeInRange = report?.glucose?.timeInRangePercent ?: 0
    val glucoseTag = report?.glucose?.summaryTag?.takeIf { it.isNotBlank() } ?: "No readings logged"
    val mealsLoggedText = if (report != null && report.nutrition.totalMealsLogged > 0) {
        "${report.nutrition.totalMealsLogged} meals logged"
    } else {
        "0 meals logged"
    }
    val medAdherence = report?.medication?.adherencePercent ?: 0
    val medStatus = report?.medication?.statusLabel?.takeIf { it.isNotBlank() } ?: "No medications scheduled"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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

                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Reports",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (AppTheme.colors.isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF5F3FF))
                                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(14.dp))
                                    .clickable { onExportReportClick() }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Export",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFFA855F7),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Date Selector Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppTheme.colors.cardBackground)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                                .clickable { showCalendarDialog = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = "‹", fontSize = 18.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                                Text(
                                    text = "📅  $displayWeekLabel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(text = "›", fontSize = 18.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
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
                            color = Color(0xFFA855F7),
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(18.dp),
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
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(
                                    text = "mg/dL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7),
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val hasReadings = chartData.any { it.averageGlucose > 0f } || (report != null && report.glucose.averageNum > 0f)

                            // Animated Sweep-and-Stop Glucose ECG Pulse Line Canvas or Empty Placeholder
                            if (!hasReadings) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(95.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "📊 No logs recorded yet.",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AppTheme.colors.textSecondary,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap + to add your first entry",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFA855F7),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp),
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        val validReadings = chartData.filter { it.averageGlucose > 0f }.map { it.averageGlucose }
                                        val minVal = (validReadings.minOrNull() ?: 70f).coerceAtMost(70f)
                                        val maxVal = (validReadings.maxOrNull() ?: 180f).coerceAtLeast(180f)
                                        val span = (maxVal - minVal).coerceAtLeast(40f)

                                        val numDays = chartData.size.coerceAtLeast(1)
                                        val points = chartData.mapIndexed { i, pt ->
                                            val x = if (numDays > 1) {
                                                (w * 0.06f) + (i.toFloat() / (numDays - 1)) * (w * 0.88f)
                                            } else {
                                                w * 0.5f
                                            }
                                            val y = if (pt.averageGlucose > 0f) {
                                                h * 0.80f - ((pt.averageGlucose - minVal) / span) * (h * 0.60f)
                                            } else {
                                                h * 0.82f
                                            }
                                            Offset(x, y)
                                        }

                                        val progress = chartProgress.value
                                        val maxAllowedX = w * progress

                                        val ecgPath = Path().apply {
                                            if (points.isNotEmpty()) {
                                                moveTo(0f, points.first().y)
                                                lineTo(points.first().x, points.first().y)
                                                for (i in 1 until points.size) {
                                                    val pPrev = points[i - 1]
                                                    val pCurr = points[i]
                                                    val midX = (pPrev.x + pCurr.x) / 2f
                                                    cubicTo(midX, pPrev.y, midX, pCurr.y, pCurr.x, pCurr.y)
                                                }
                                                lineTo(w, points.last().y)
                                            }
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
                                                        Color(0x35A855F7),
                                                        Color(0x00A855F7),
                                                    ),
                                                ),
                                            )

                                            drawPath(
                                                path = ecgPath,
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF1E1B4B),
                                                        Color(0xFF7E22CE),
                                                        Color(0xFFA855F7),
                                                        Color(0xFFF0ABFC),
                                                    ),
                                                ),
                                                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
                                            )

                                            points.forEachIndexed { idx, pt ->
                                                if (pt.x <= maxAllowedX && (chartData.getOrNull(idx)?.averageGlucose ?: 0f) > 0f) {
                                                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                                                    drawCircle(color = Color(0xFFA855F7), radius = 3.5.dp.toPx(), center = pt)
                                                }
                                            }
                                        }

                                        if (progress > 0f && progress < 1f && points.isNotEmpty()) {
                                            val currentTipX = maxAllowedX.coerceIn(0f, w)
                                            val tipY = points.lastOrNull { it.x <= currentTipX }?.y ?: (h * 0.5f)

                                            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(currentTipX, tipY))
                                            drawCircle(
                                                color = Color(0xFFF0ABFC),
                                                radius = 8.dp.toPx(),
                                                center = Offset(currentTipX, tipY),
                                                style = Stroke(width = 2.dp.toPx()),
                                            )
                                        }
                                    }
                                }

                                if (chartData.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        chartData.forEach { pt ->
                                            Text(
                                                text = pt.day,
                                                fontSize = 10.sp,
                                                fontWeight = if (pt.averageGlucose > 0f) FontWeight.Bold else FontWeight.Normal,
                                                color = if (pt.averageGlucose > 0f) Color(0xFFA855F7) else AppTheme.colors.textMuted,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Weekly average: $avgGlucose • $timeInRange% time in range ($glucoseTag)",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary,
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(18.dp),
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
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(
                                    text = mealsLoggedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7),
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val mealData = report?.nutrition?.mealChartData ?: emptyList()
                            val totalMealsCount = report?.nutrition?.totalMealsLogged ?: 0

                            if (report == null || totalMealsCount <= 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No meals logged for this week",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textSecondary,
                                    )
                                }
                            } else {
                                val displayMealCounts = if (mealData.size == 7) mealData else List(7) { 0 }
                                val maxDayMeals = (displayMealCounts.maxOrNull() ?: 3).coerceAtLeast(3).toFloat()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(65.dp),
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        val barWidth = 18.dp.toPx()
                                        val barSpacing = (w - (barWidth * 7)) / 8

                                        displayMealCounts.forEachIndexed { i, count ->
                                            val left = barSpacing + i * (barWidth + barSpacing)
                                            val factor = if (count > 0) (count.toFloat() / maxDayMeals).coerceIn(0.20f, 1f) else 0.04f
                                            val animatedFactor = factor * chartProgress.value
                                            val barH = (h * animatedFactor).coerceAtLeast(3.dp.toPx())
                                            val barColor = if (count > 0) Color(0xFFA855F7) else AppTheme.colors.border

                                            drawRoundRect(
                                                color = barColor,
                                                topLeft = Offset(left, h - barH),
                                                size = Size(barWidth, barH),
                                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    val daysLabelList = if (chartData.size == 7) chartData.map { it.day.take(1) } else listOf("M", "T", "W", "T", "F", "S", "S")
                                    daysLabelList.forEachIndexed { idx, dayChar ->
                                        val count = displayMealCounts.getOrElse(idx) { 0 }
                                        Text(
                                            text = dayChar,
                                            fontSize = 10.sp,
                                            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (count > 0) Color(0xFFA855F7) else AppTheme.colors.textMuted,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Avg daily calories: ${report?.nutrition?.dailyAverageCalories?.takeIf { it.isNotBlank() && it != "0 kcal" } ?: "0 kcal"}",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary,
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(18.dp),
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
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(
                                    text = "$medAdherence% adherence",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA855F7),
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { ((medAdherence / 100f) * chartProgress.value).coerceIn(0f, 1f) },
                                color = Color(0xFFA855F7),
                                trackColor = if (AppTheme.colors.isDark) AppTheme.colors.border else Color(0xFFF5F3FF),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = medStatus,
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Single concise health insight card (max 1 short sentence, maxLines = 2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (report != null && report.activity.totalActiveMins > 0)
                                "Great consistency: ${report.activity.totalActiveMins} active minutes recorded this week."
                            else
                                "Logging meals and glucose consistently improves health trends.",
                            fontSize = 13.sp,
                            color = AppTheme.colors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                IosLiquidGlassButton(
                    text = "Export PDF",
                    onClick = onExportReportClick,
                )

                Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding())
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
    }
}

@Composable
fun ReportCalendarDialog(
    onWeekSelected: (String, String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val currentCal = remember { java.util.Calendar.getInstance() }
    var calendarMonthOffset by remember { mutableIntStateOf(0) }

    val activeCal = remember(calendarMonthOffset) {
        (currentCal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.MONTH, calendarMonthOffset)
        }
    }

    val maxDaysInMonth = activeCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val defaultDay = if (calendarMonthOffset == 0) currentCal.get(java.util.Calendar.DAY_OF_MONTH) else 15
    var selectedDay by remember(calendarMonthOffset) { mutableIntStateOf(defaultDay.coerceIn(1, maxDaysInMonth)) }

    val monthYearFormat = remember { java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()) }
    val monthYearText = remember(activeCal) { monthYearFormat.format(activeCal.time) }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.5.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
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
                        color = AppTheme.colors.textPrimary,
                    )

                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textMuted,
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
                        .background(AppTheme.colors.elevatedSurface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "‹",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7),
                        modifier = Modifier
                            .clickable { calendarMonthOffset-- }
                            .padding(horizontal = 6.dp)
                    )
                    Text(
                        text = monthYearText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                    )
                    Text(
                        text = "›",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7),
                        modifier = Modifier
                            .clickable { calendarMonthOffset++ }
                            .padding(horizontal = 6.dp)
                    )
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
                            color = AppTheme.colors.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(32.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Grid
                val daysList = (1..maxDaysInMonth).toList()
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
                                    .background(if (isSelected) Color(0xFFA855F7) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = if (isSelected) Color.Transparent else AppTheme.colors.border,
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
                                    color = if (isSelected) Color.White else AppTheme.colors.textPrimary,
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
                            .background(AppTheme.colors.secondaryBackground)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textSecondary,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFFF0ABFC)),
                                ),
                            )
                            .clickable {
                                val selCal = (activeCal.clone() as java.util.Calendar).apply {
                                    set(java.util.Calendar.DAY_OF_MONTH, selectedDay.coerceIn(1, maxDaysInMonth))
                                }
                                val endIsoSdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val endDateIso = endIsoSdf.format(selCal.time)

                                val endDisplaySdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
                                val endDisplayStr = endDisplaySdf.format(selCal.time)

                                val startCal = (selCal.clone() as java.util.Calendar).apply {
                                    add(java.util.Calendar.DAY_OF_YEAR, -6)
                                }
                                val startDisplaySdf = java.text.SimpleDateFormat("d", java.util.Locale.getDefault())
                                val startDisplayStr = startDisplaySdf.format(startCal.time)

                                onWeekSelected("$startDisplayStr–$endDisplayStr", endDateIso)
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
