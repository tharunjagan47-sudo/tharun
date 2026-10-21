package com.simats.diawell123.ui.glucose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun ReadingDetailsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Demo profile • Illustrative sample data",
                    fontSize = 11.sp,
                    color = Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reading Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Fasting",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "128",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A1C7D),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "mg/dL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7A1C7D),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "15 Oct 2026 at 7:30 AM",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent fasting readings Chart Box
            Text(
                text = "Recent fasting readings",
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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val p1 = Offset(w * 0.15f, h * 0.65f)
                    val p2 = Offset(w * 0.5f, h * 0.2f)
                    val p3 = Offset(w * 0.85f, h * 0.4f)

                    val path = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                    }

                    drawPath(path, color = Color(0xFFB81D85), style = Stroke(width = 3.dp.toPx()))

                    listOf(p1, p2, p3).forEach { pt ->
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = Color(0xFFB81D85), radius = 3.5.dp.toPx(), center = pt)
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
                    .padding(12.dp),
            ) {
                Text(
                    text = "Before breakfast, after an overnight fast.",
                    fontSize = 13.sp,
                    color = Color(0xFF1E1E22),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Sample Trend Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "✨ AI • sample trend summary",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your sample readings were 118, 132 and 128 mg/dL. This 128 mg/dL is 4 mg/dL below the previous entry.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                        lineHeight = 17.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "About glucose tracking:\nLogging the time, reading types and notes can help you spot trends. A single reading doesn't describe the full picture.",
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReadingDetailsScreenPreview() {
    Diawell123Theme {
        ReadingDetailsScreen()
    }
}
