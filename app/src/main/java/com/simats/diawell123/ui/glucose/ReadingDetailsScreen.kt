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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.simats.diawell123.data.model.GlucoseRecentPoint
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun ReadingDetailsScreen(
    readingId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: GlucoseViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val uiState by viewModel.detailsState.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(readingId) {
        viewModel.loadReadingDetails(readingId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
        }
    }

    val details = uiState.details

    if (showDeleteConfirmDialog && details != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Delete Reading", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete this blood sugar reading of ${details.readingValue} mg/dL?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteReading(details.id)
                    }
                ) {
                    Text("Delete", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFF6E6877))
                }
            }
        )
    }

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
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Reading details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "A closer look at your recorded glucose.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                if (details != null && !uiState.isDeleting) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFEBEE))
                            .clickable { showDeleteConfirmDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "🗑️ Delete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                        )
                    }
                } else if (uiState.isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFC62828),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text(
                        text = uiState.errorMessage!!,
                        fontSize = 12.sp,
                        color = Color(0xFFC62828),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (uiState.isLoading && details == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFBD1E85),
                        strokeWidth = 2.dp,
                    )
                }
            } else if (details != null) {
                val numVal = details.readingValueNum
                val (statusColor, statusBg, statusLabel) = when {
                    numVal < 70f -> Triple(Color(0xFFC62828), Color(0xFFFFEBEE), "Hypoglycemia (<70 mg/dL)")
                    numVal > 180f -> Triple(Color(0xFFC62828), Color(0xFFFFEBEE), "High Glucose (>180 mg/dL)")
                    numVal >= 140f -> Triple(Color(0xFFE65100), Color(0xFFFFF3E0), "Elevated (140-180 mg/dL)")
                    else -> Triple(Color(0xFF2E7D32), Color(0xFFE8F5E9), "In Target Range (70-139 mg/dL)")
                }

                // Reading Display Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDEEF7))
                        .border(1.dp, Color(0xFFF3E2F0), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = details.readingType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = details.readingValue,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7A1C7D),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = details.unit,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7A1C7D),
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = details.dateTimeLabel,
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recent fasting readings Chart Box
                Text(
                    text = "Recent ${details.readingType.lowercase()} readings",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF232226),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                ) {
                    if (details.recentReadings.isNotEmpty()) {
                        RecentReadingsLineChart(readings = details.recentReadings)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Single reading recorded in this category.",
                                fontSize = 12.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Notes Box
                Text(
                    text = "Notes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF232226),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F7FA))
                        .padding(14.dp),
                ) {
                    Text(
                        text = details.notes.ifBlank { "No notes recorded for this reading." },
                        fontSize = 13.sp,
                        color = Color(0xFF1E1E22),
                        lineHeight = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AI Trend Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFDEEF7))
                        .border(1.dp, Color(0xFFF3E2F0), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Trend Summary",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF811978),
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = details.aiTrendSummary.ifBlank { "Single reading recorded. Continue regular logging to generate personalized trend analysis." },
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                            lineHeight = 18.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "About glucose tracking:\nLogging the time, reading types and notes helps your medical team spot glycemic patterns. A single reading is contextual.",
                    fontSize = 11.sp,
                    color = Color(0xFF8A8595),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )
            }
        }
    }
}

@Composable
fun RecentReadingsLineChart(readings: List<GlucoseRecentPoint>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Reference line
        drawLine(
            color = Color(0xFFF3E2F0),
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 1.dp.toPx(),
        )

        if (readings.size == 1) {
            val pt = Offset(w * 0.5f, h * 0.5f)
            drawCircle(color = Color.White, radius = 7.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFFB81D85), radius = 5.dp.toPx(), center = pt)
            return@Canvas
        }

        val values = readings.map { it.value }
        val minVal = (values.minOrNull() ?: 70f).coerceAtLeast(40f) - 10f
        val maxVal = (values.maxOrNull() ?: 180f).coerceAtMost(400f) + 10f
        val rangeVal = (maxVal - minVal).coerceAtLeast(15f)

        val points = readings.mapIndexed { index, point ->
            val x = (w * 0.15f) + (index.toFloat() / (readings.size - 1)) * (w * 0.70f)
            val normalizedY = (point.value - minVal) / rangeVal
            val y = (h * 0.85f) - (normalizedY * (h * 0.70f))
            Offset(x, y)
        }

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

        points.forEach { pt ->
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFFB81D85), radius = 4.dp.toPx(), center = pt)
        }
    }
}
