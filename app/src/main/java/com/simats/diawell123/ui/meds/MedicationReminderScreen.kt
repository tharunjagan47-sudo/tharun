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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationReminderScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTakenClick: () -> Unit = {},
    onSnoozeClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
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
            horizontalAlignment = Alignment.CenterHorizontally,
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

                Text(
                    text = "Medication reminder",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sample • Reminder reminder",
                fontSize = 11.sp,
                color = Color(0xFF811978),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Pill Circle Illustration
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFDEEF7)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "💊", fontSize = 42.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "A gentle nudge for\nyour evening routine.",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder Detail Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                    .padding(18.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFDEEF7))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "Today • 15 Oct",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                        }

                        Text(
                            text = "8:00 PM",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A1C7D),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Metformin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "1 tablet (500 mg) as prescribed",
                        fontSize = 13.sp,
                        color = Color(0xFF6E6877),
                    )

                    Text(
                        text = "Daily at 8:00 AM and 8:00 PM",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8595),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Follow your prescribed schedule.\nMark Taken after taking your medication.",
                fontSize = 12.sp,
                color = Color(0xFF6E6877),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Taken Button
            IosLiquidGlassButton(
                text = "✓  Taken",
                onClick = onTakenClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Snooze Button
            IosLiquidGlassButton(
                text = "⏰  Snooze - 15 minutes",
                onClick = onSnoozeClick,
                gradientColors = listOf(Color(0xFF26A69A), Color(0xFF00897B)),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skip Button
            Text(
                text = "Skip",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSkipClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "For dose changes or missed dose advice, ask your pharmacist or clinician if you are unsure.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicationReminderScreenPreview() {
    Diawell123Theme {
        MedicationReminderScreen()
    }
}
