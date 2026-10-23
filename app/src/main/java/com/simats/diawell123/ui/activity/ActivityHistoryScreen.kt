package com.simats.diawell123.ui.activity

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme

data class ActivityRecord(
    val id: String,
    val type: String, // "Walking", "Cycling", "Running"
    val icon: String,
    val date: String,
    val durationMins: Int,
    val distanceKm: Float,
    val caloriesKcal: Int,
    val glucoseBefore: Int,
    val glucoseAfter: Int,
)

@Composable
fun ActivityHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onStartNewWorkoutClick: () -> Unit = {},
) {
    var selectedFilter by remember { mutableStateOf("All") }

    // Sample Saved Workouts History
    val historyRecords = remember {
        mutableStateListOf(
            ActivityRecord(
                id = "1",
                type = "Walking",
                icon = "🚶‍♂️",
                date = "Today, 15 Oct • 7:45 AM",
                durationMins = 33,
                distanceKm = 2.8f,
                caloriesKcal = 185,
                glucoseBefore = 128,
                glucoseAfter = 106,
            ),
            ActivityRecord(
                id = "2",
                type = "Cycling",
                icon = "🚴",
                date = "Yesterday, 14 Oct • 6:30 PM",
                durationMins = 25,
                distanceKm = 8.2f,
                caloriesKcal = 240,
                glucoseBefore = 142,
                glucoseAfter = 118,
            ),
            ActivityRecord(
                id = "3",
                type = "Running",
                icon = "🏃",
                date = "13 Oct • 6:15 AM",
                durationMins = 20,
                distanceKm = 3.5f,
                caloriesKcal = 210,
                glucoseBefore = 135,
                glucoseAfter = 112,
            ),
            ActivityRecord(
                id = "4",
                type = "Walking",
                icon = "🚶‍♂️",
                date = "12 Oct • 7:15 AM",
                durationMins = 30,
                distanceKm = 2.4f,
                caloriesKcal = 160,
                glucoseBefore = 130,
                glucoseAfter = 110,
            ),
        )
    }

    val filteredList = historyRecords.filter {
        selectedFilter == "All" || it.type == selectedFilter
    }

    val totalWorkouts = historyRecords.size
    val totalDistance = historyRecords.sumOf { it.distanceKm.toDouble() }
    val totalCalories = historyRecords.sumOf { it.caloriesKcal }

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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Activity History & Logs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "Track your workout progress & glucose improvements.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Summary Statistics Overview Card (Pops Up)
            ElementPopUpContainer(delayMillis = 60) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B156E), Color(0xFFB81D85), Color(0xFFFF5722)),
                            ),
                        )
                        .padding(18.dp),
                ) {
                    Column {
                        Text(
                            text = "Weekly Workout Summary 🏆",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            SummaryStatItem(title = "Workouts", value = "$totalWorkouts Done")
                            SummaryStatItem(title = "Distance", value = "${String.format("%.1f", totalDistance)} km")
                            SummaryStatItem(title = "Burned", value = "$totalCalories kcal")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Filter Category Pills (Pops Up)
            ElementPopUpContainer(delayMillis = 120) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("All", "Walking", "Cycling", "Running").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFB81D85) else Color(0xFFF8F7FA))
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = Color(0xFFEFECEF),
                                    shape = RoundedCornerShape(20.dp),
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF6E6877),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Saved Activity Log Items (Pop Up Staggered)
            ElementPopUpContainer(delayMillis = 180) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No workouts recorded for $selectedFilter yet.",
                                fontSize = 13.sp,
                                color = Color(0xFF8A8595),
                            )
                        }
                    } else {
                        filteredList.forEach { item ->
                            ActivityHistoryCard(item = item)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Start New Workout Action Button (Pops Up)
            ElementPopUpContainer(delayMillis = 240) {
                IosLiquidGlassButton(
                    text = "🏃‍♂️  Start New Workout",
                    onClick = onStartNewWorkoutClick,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummaryStatItem(
    title: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
fun ActivityHistoryCard(
    item: ActivityRecord,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDEEF7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = item.icon, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.type,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = item.date,
                            fontSize = 11.sp,
                            color = Color(0xFF8A8595),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Completed",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Workout Metrics Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8F7FA))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "⏱ ${item.durationMins} mins",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = "📍 ${String.format("%.1f", item.distanceKm)} km",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = "🔥 ${item.caloriesKcal} kcal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1E22),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glucose Effect Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Glucose Impact:",
                    fontSize = 11.sp,
                    color = Color(0xFF6E6877),
                )

                Text(
                    text = "🩸 ${item.glucoseBefore} ➔ ${item.glucoseAfter} mg/dL (-${item.glucoseBefore - item.glucoseAfter} mg/dL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivityHistoryScreenPreview() {
    Diawell123Theme {
        ActivityHistoryScreen()
    }
}
