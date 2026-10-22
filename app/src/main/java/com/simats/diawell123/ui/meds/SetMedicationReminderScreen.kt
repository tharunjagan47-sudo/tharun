package com.simats.diawell123.ui.meds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.simats.diawell123.ui.diet.CalendarDatePickerDialog
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetMedicationReminderScreen(
    modifier: Modifier = Modifier,
    medicationName: String = "Metformin",
    medicationDose: String = "500 mg • 1 Tablet",
    onBackClick: () -> Unit = {},
    onSaveReminderClick: () -> Unit = {},
) {
    var reminderDateText by remember { mutableStateOf("15 Oct 2026") }
    var selectedTime by remember { mutableStateOf("8:00 PM") }
    var selectedTiming by remember { mutableStateOf("After Meal") }
    var notes by remember { mutableStateOf("Take with a glass of water after dinner.") }

    var showDatePickerDialog by remember { mutableStateOf(value = false) }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onSaveReminderClick()
        }
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
                        text = "Set Medication Reminder",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Configure time and schedule for your tablet.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1-Second Success Confirmation Banner
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✓", fontSize = 22.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Medication Reminder Set Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Reminder added for $medicationName at $selectedTime.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Selected Tablet Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDEEF7))
                    .border(1.dp, Color(0xFFF3D5E9), RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💊", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SELECTED TABLET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                        Text(
                            text = medicationName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = medicationDose,
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Selector Bar
            Text(
                text = "Reminder Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                    .clickable { showDatePickerDialog = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📅  $reminderDateText",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Text(
                        text = "Change Date ›",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Selector
            Text(
                text = "Select Time",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("8:00 AM", "1:00 PM", "8:00 PM", "10:00 PM").forEach { time ->
                    val isSelected = selectedTime == time
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { selectedTime = time }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = time,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Meal Timing Relation
            Text(
                text = "Meal Schedule Timing",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("Before Meal", "After Meal", "With Food", "Empty Stomach").forEach { timing ->
                    val isSelected = selectedTiming == timing
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { selectedTiming = timing }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = timing,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instructions / Notes
            Text(
                text = "Instructions & Notes (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Reminder Button
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
                    .clickable { showSuccessBanner = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Set Medication Reminder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Calendar Date Picker Modal Dialog
        if (showDatePickerDialog) {
            CalendarDatePickerDialog(
                selectedDateText = reminderDateText,
                onDateSelected = { newDate ->
                    reminderDateText = newDate
                },
                onDismissRequest = { showDatePickerDialog = false },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SetMedicationReminderScreenPreview() {
    Diawell123Theme {
        SetMedicationReminderScreen()
    }
}
