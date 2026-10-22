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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
    onReminderClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Meds") }

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
                            text = "Reminders set according to your care plan.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Medication Bar Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .border(1.dp, Color(0xFFB81D85), RoundedCornerShape(12.dp))
                        .clickable { onAddMedicationClick() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+ Add medication or set reminder",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Medication Cards
                MedicationScheduleCard(
                    time = "8:00 AM",
                    name = "Metformin",
                    dose = "500 mg • 1 tablet",
                    status = "Taken",
                    statusColor = Color(0xFF2E7D32),
                    statusBg = Color(0xFFE8F5E9),
                    onClick = onReminderClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                MedicationScheduleCard(
                    time = "1:00 PM",
                    name = "Multivitamin",
                    dose = "1 capsule",
                    status = "Taken",
                    statusColor = Color(0xFF2E7D32),
                    statusBg = Color(0xFFE8F5E9),
                    onClick = onReminderClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                MedicationScheduleCard(
                    time = "8:00 PM",
                    name = "Metformin",
                    dose = "500 mg • 1 tablet",
                    status = "Upcoming",
                    statusColor = Color(0xFFB81D85),
                    statusBg = Color(0xFFFDEEF7),
                    onClick = onReminderClick,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Always consult your physician before changing doses.\nNotifications keep your daily routine consistent.",
                    fontSize = 11.sp,
                    color = Color(0xFF8A8595),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )
            }
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock
        IosLiquidGlassBottomNav(
            selectedTab = selectedTab,
            onHomeTabClick = onHomeTabClick,
            onDietTabClick = onDietTabClick,
            onMedsTabClick = { selectedTab = "Meds" },
            onReportsTabClick = onReportsTabClick,
            onSettingsTabClick = onSettingsTabClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun MedicationScheduleCard(
    time: String,
    name: String,
    dose: String,
    status: String,
    statusColor: Color,
    statusBg: Color,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💊", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                    )
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = dose,
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
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
