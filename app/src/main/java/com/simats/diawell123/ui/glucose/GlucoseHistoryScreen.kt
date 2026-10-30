package com.simats.diawell123.ui.glucose

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.GlucoseChartPoint
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun GlucoseHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: GlucoseViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onLogGlucoseClick: () -> Unit = {},
    onReadingClick: (readingId: Int) -> Unit = {},
) {
    val uiState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    val history = uiState.historyData
    val stats = history?.stats
    val readings = history?.readings ?: emptyList()
    val chartData = history?.chartData ?: emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar with Styled Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Glucose History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "Track your blood sugar trends over time",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFBD1E85),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Range Segmented Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3E2F0))
                        .padding(4.dp),
                ) {
                    listOf("7 days", "30 days", "3 months").forEach { range ->
                        val isSelected = uiState.selectedRange.equals(range, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent),
                                        )
                                    },
                                )
                                .clickable { viewModel.onRangeSelected(range) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = range,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date Title & Fasting Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = history?.dateRangeLabel?.takeIf { it.isNotBlank() } ?: "Date Range",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFDEEF7))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "🩸 Tracked",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error Banner
                if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "Retry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                modifier = Modifier
                                    .clickable { viewModel.loadHistory() }
                                    .padding(4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Dynamic Glucose Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                        .clickable {
                            if (readings.isNotEmpty()) {
                                onReadingClick(readings.first().id)
                            }
                        }
                        .padding(16.dp),
                ) {
                    if (chartData.isNotEmpty()) {
                        DynamicGlucoseLineChart(chartData = chartData)
                    } else if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFBD1E85),
                                strokeWidth = 2.dp,
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No glucose readings recorded in this range.\nTap + Log glucose below to record your first reading.",
                                fontSize = 12.sp,
                                color = Color(0xFF8A8595),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryStatCard(
                        value = stats?.average ?: "--",
                        label = "Average",
                        modifier = Modifier.weight(1f),
                    )
                    HistoryStatCard(
                        value = stats?.lowest ?: "--",
                        label = "Lowest",
                        modifier = Modifier.weight(1f),
                    )
                    HistoryStatCard(
                        value = stats?.readingsCount ?: "0 logged",
                        label = "Readings",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recent Readings Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Recent readings (${readings.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFDEEF7))
                            .clickable { onLogGlucoseClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "+ Log glucose",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Reading List Items
                if (readings.isNotEmpty()) {
                    readings.forEach { item ->
                        ReadingHistoryItem(
                            date = item.date,
                            time = item.time,
                            value = item.value,
                            onClick = { onReadingClick(item.id) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else if (!uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F7FA))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🩸", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No readings logged for this timeframe",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E1E22),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Start tracking to monitor your daily glycemic patterns.",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicGlucoseLineChart(chartData: List<GlucoseChartPoint>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Reference grid lines
        drawLine(
            color = Color(0xFFF3E2F0),
            start = Offset(0f, h * 0.25f),
            end = Offset(w, h * 0.25f),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = Color(0xFFF3E2F0),
            start = Offset(0f, h * 0.75f),
            end = Offset(w, h * 0.75f),
            strokeWidth = 1.dp.toPx(),
        )

        if (chartData.size == 1) {
            val single = chartData[0]
            val pt = Offset(w * 0.5f, h * 0.5f)
            drawCircle(color = Color.White, radius = 7.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFFB81D85), radius = 5.dp.toPx(), center = pt)
            return@Canvas
        }

        val values = chartData.map { it.value }
        val minVal = (values.minOrNull() ?: 70f).coerceAtLeast(40f) - 15f
        val maxVal = (values.maxOrNull() ?: 180f).coerceAtMost(400f) + 15f
        val rangeVal = (maxVal - minVal).coerceAtLeast(20f)

        val points = chartData.mapIndexed { index, point ->
            val x = (w * 0.08f) + (index.toFloat() / (chartData.size - 1)) * (w * 0.84f)
            val normalizedY = (point.value - minVal) / rangeVal
            val y = (h * 0.85f) - (normalizedY * (h * 0.70f))
            Offset(x, y)
        }

        // Draw connected path
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = Color(0xFFB81D85),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Draw points
        points.forEach { pt ->
            drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFF7A1C7D), radius = 3.5.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun HistoryStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFDEEF7))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A1C7D),
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF6E6877),
            )
        }
    }
}

@Composable
fun ReadingHistoryItem(
    date: String,
    time: String,
    value: String,
    onClick: () -> Unit = {},
) {
    val numVal = value.toFloatOrNull() ?: 120f
    val (statusColor, statusBg) = when {
        numVal < 70f -> Color(0xFFC62828) to Color(0xFFFFEBEE)
        numVal > 180f -> Color(0xFFC62828) to Color(0xFFFFEBEE)
        numVal >= 140f -> Color(0xFFE65100) to Color(0xFFFFF3E0)
        else -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = date,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color(0xFF6E6877),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A1C7D),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "mg/dL",
                            fontSize = 11.sp,
                            color = Color(0xFF6E6877),
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = if (numVal < 70f) "Low" else if (numVal > 180f) "High" else if (numVal >= 140f) "Elevated" else "In Target",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "›", fontSize = 18.sp, color = Color(0xFFA09BAA), fontWeight = FontWeight.Bold)
            }
        }
    }
}
