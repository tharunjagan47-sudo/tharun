package com.simats.diawell123.ui.meds

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
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
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                            text = "Your medications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "A routine for your daily medication schedule.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Demo profile • Prescription schedule sample",
                    fontSize = 11.sp,
                    color = Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selector Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "‹", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                        Text(
                            text = "📅  Thu, 15 Oct 2026",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(text = "›", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Medication Card 1
                MedicationCard(
                    name = "Metformin",
                    dosage = "1 tablet (500 mg) as prescribed",
                    frequency = "Daily • 2 times a day",
                    timings = listOf("8:00 AM" to true, "8:00 PM" to false),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Medication Card 2
                MedicationCard(
                    name = "Vitamin D3",
                    dosage = "1 capsule (1,000 IU) as prescribed",
                    frequency = "Daily • once a day",
                    timings = listOf("9:00 AM" to true),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Add Medication Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7A1C7D),
                                    Color(0xFFBD1E85),
                                    Color(0xFFFF5722),
                                ),
                            ),
                        )
                        .clickable { onAddMedicationClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+  Add Medication",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter only the dosage prescribed by your physician. DiaWell assists you with your schedule, it does not recommend or change medication doses.",
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
fun MedicationCard(
    name: String,
    dosage: String,
    frequency: String,
    timings: List<Pair<String, Boolean>>,
) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💊", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                }
                Text(text = "•••", fontSize = 14.sp, color = Color(0xFF8A8595))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dosage,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6E6877),
            )

            Text(
                text = frequency,
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
            )

            Spacer(modifier = Modifier.height(12.dp))

            timings.forEach { (time, isTaken) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "⏰  $time",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTaken) Color(0xFFE8F5E9) else Color(0xFFFDEEF7))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (isTaken) "Taken" else "Upcoming",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTaken) Color(0xFF2E7D32) else Color(0xFFB81D85),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicationsScreenPreview() {
    Diawell123Theme {
        MedicationsScreen()
    }
}
