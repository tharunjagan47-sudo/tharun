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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun GlucoseHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLogGlucoseClick: () -> Unit = {},
    onReadingClick: () -> Unit = {},
) {
    var selectedRange by remember { mutableStateOf("7 days") }

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

                // Top Bar with Back Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "←",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBackClick() }
                            .padding(4.dp),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Demo profile • Sample data",
                        fontSize = 11.sp,
                        color = Color(0xFF811978),
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Range Segmented Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3E2F0))
                        .padding(4.dp),
                ) {
                    listOf("7 days", "30 days", "3 months").forEach { range ->
                        val isSelected = selectedRange == range
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
                                .clickable { selectedRange = range },
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
                        text = "8–15 October",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Fasting",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glucose Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                        .clickable { onReadingClick() }
                        .padding(16.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Grid lines
                        drawLine(Color(0xFFF3E2F0), Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 1.dp.toPx())
                        drawLine(Color(0xFFF3E2F0), Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 1.dp.toPx())

                        // Line Graph
                        val p1 = Offset(w * 0.08f, h * 0.6f)
                        val p2 = Offset(w * 0.3f, h * 0.35f)
                        val p3 = Offset(w * 0.52f, h * 0.22f)
                        val p4 = Offset(w * 0.74f, h * 0.58f)
                        val p5 = Offset(w * 0.92f, h * 0.3f)

                        val path = Path().apply {
                            moveTo(p1.x, p1.y)
                            lineTo(p2.x, p2.y)
                            lineTo(p3.x, p3.y)
                            lineTo(p4.x, p4.y)
                            lineTo(p5.x, p5.y)
                        }

                        drawPath(path, color = Color(0xFFB81D85), style = Stroke(width = 3.dp.toPx()))

                        listOf(p1, p2, p3, p4, p5).forEach { pt ->
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                            drawCircle(color = Color(0xFFB81D85), radius = 3.5.dp.toPx(), center = pt)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryStatCard(value = "120 mg/dL", label = "Average", modifier = Modifier.weight(1f))
                    HistoryStatCard(value = "114 mg/dL", label = "Lowest", modifier = Modifier.weight(1f))
                    HistoryStatCard(value = "7 logged", label = "Readings", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recent Readings Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Recent readings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "+ Log glucose",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        modifier = Modifier.clickable { onLogGlucoseClick() },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reading List Items
                ReadingHistoryItem(
                    date = "Today, 15 Oct",
                    time = "7:30 AM • Fasting",
                    value = "128",
                    onClick = onReadingClick,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReadingHistoryItem(
                    date = "Wed, 14 Oct",
                    time = "7:30 AM • Fasting",
                    value = "132",
                    onClick = onReadingClick,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReadingHistoryItem(
                    date = "Tue, 13 Oct",
                    time = "7:30 AM • Fasting",
                    value = "118",
                    onClick = onReadingClick,
                )
            }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
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
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GlucoseHistoryScreenPreview() {
    Diawell123Theme {
        GlucoseHistoryScreen()
    }
}
