package com.simats.diawell123.ui.meds
import com.simats.diawell123.ui.theme.AppTheme

import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.CalendarDatePickerDialog
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetMedicationReminderScreen(
    modifier: Modifier = Modifier,
    medicationName: String = "Metformin",
    medicationDose: String = "500 mg • 1 Tablet",
    viewModel: MedsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaveReminderClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val reminderState by viewModel.setReminderState.collectAsState()

    var reminderDateText by remember {
        mutableStateOf(SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date()))
    }
    var selectedTime by remember(reminderState.selectedTime) {
        mutableStateOf(reminderState.selectedTime.ifBlank { "8:00 PM" })
    }
    var selectedTiming by remember(reminderState.selectedTiming) {
        mutableStateOf(reminderState.selectedTiming.ifBlank { "After Meal" })
    }
    var notes by remember {
        mutableStateOf("Take with a glass of water.")
    }

    var showDatePickerDialog by remember { mutableStateOf(value = false) }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    val activeName = reminderState.medicationName.ifBlank { medicationName }
    val activeDose = reminderState.medicationDose.ifBlank { medicationDose }

    // Interactive custom time picker dialog allowing any flexible hour and minute
    fun showCustomTimePicker() {
        val (initHour, initMinute) = try {
            val format = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = format.parse(selectedTime)
            val cal = Calendar.getInstance()
            if (date != null) cal.time = date
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        } catch (_: Exception) {
            Pair(20, 0)
        }

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                selectedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
            },
            initHour,
            initMinute,
            false // 12-hour AM/PM dialog
        ).show()
    }

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
            .background(AppTheme.colors.background)
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
                    color = AppTheme.colors.textPrimary,
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
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Configure time, meal timing & care plan.",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
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
                                text = "Reminder active for $activeName at $selectedTime ($selectedTiming).",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Error message if any
            if (reminderState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "⚠️ ${reminderState.errorMessage}",
                        fontSize = 12.sp,
                        color = Color(0xFFC62828)
                    )
                }
            }

            // Enriched Selected Tablet Details Card (From CSV Dataset)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.elevatedSurface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconChar = when {
                            reminderState.dosageForm.contains("inject", ignoreCase = true) || activeName.contains("insulin", ignoreCase = true) -> "💉"
                            reminderState.dosageForm.contains("inhal", ignoreCase = true) -> "💨"
                            reminderState.dosageForm.contains("syrup", ignoreCase = true) -> "💧"
                            else -> "💊"
                        }
                        Text(text = iconChar, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SELECTED MEDICINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB81D85),
                                )
                                if (reminderState.therapeuticClass.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (AppTheme.colors.isDark) AppTheme.colors.cardBackground else Color.White.copy(alpha = 0.8f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = reminderState.therapeuticClass,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB81D85),
                                        )
                                    }
                                }
                            }
                            Text(
                                text = activeName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                            )
                            Text(
                                text = activeDose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF7A1C7D),
                            )
                        }
                    }

                    // Enriched Information Section from Dataset
                    if (reminderState.primaryIngredient.isNotBlank() || reminderState.manufacturer.isNotBlank() || reminderState.priceInr.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (AppTheme.colors.isDark) AppTheme.colors.cardBackground else Color.White.copy(alpha = 0.7f))
                                .padding(10.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                if (reminderState.primaryIngredient.isNotBlank()) {
                                    Text(
                                        text = "🔬 Active Ingredient: ${reminderState.primaryIngredient}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                                if (reminderState.manufacturer.isNotBlank()) {
                                    Text(
                                        text = "🏭 Manufacturer: ${reminderState.manufacturer}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF4A4453),
                                    )
                                }
                                if (reminderState.packaging.isNotBlank() || reminderState.priceInr.isNotBlank()) {
                                    val packPrice = listOf(reminderState.packaging, reminderState.priceInr)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • MRP: ")
                                    Text(
                                        text = "📦 Packaging: $packPrice",
                                        fontSize = 10.5.sp,
                                        color = AppTheme.colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Selector Bar
            Text(
                text = "Reminder Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.cardBackground)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
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
                        color = AppTheme.colors.textPrimary,
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

            // Flexible Time Selector Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Reminder Time",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )

                Text(
                    text = "Tap to pick any time ⏱",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB81D85),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Time Picker Bar (Opens Flexible Clock Dialog)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.cardBackground)
                    .border(1.2.dp, Color(0xFFB81D85).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { showCustomTimePicker() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏰", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF7A1C7D),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.elevatedSurface)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Set Custom Time ✎",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Times + Custom Time Chips
            val defaultPresets = listOf("8:00 AM", "1:00 PM", "8:00 PM", "10:00 PM")
            val presetList = if (defaultPresets.any { it.equals(selectedTime.trim(), ignoreCase = true) }) {
                defaultPresets
            } else {
                listOf(selectedTime) + defaultPresets
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                presetList.forEach { time ->
                    val isSelected = selectedTime.equals(time, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else AppTheme.colors.secondaryBackground)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { selectedTime = time }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = time,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textSecondary,
                        )
                    }
                }

                // Extra "Other Time..." chip to trigger picker
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF0F4FF))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF5363FF),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable { showCustomTimePicker() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "+ More Times...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF303F9F),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Meal Timing Relation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meal Schedule Timing",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )
                if (reminderState.selectedTiming.isNotBlank()) {
                    Text(
                        text = "Suggested: ${reminderState.selectedTiming}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB81D85),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("Before Meal", "After Meal", "With Meal", "With Food", "At Bedtime", "Empty Stomach").forEach { timing ->
                    val isSelected = selectedTiming.equals(timing, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else AppTheme.colors.secondaryBackground)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { selectedTiming = timing }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = timing,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textSecondary,
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
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    focusedContainerColor = AppTheme.colors.inputBackground,
                    unfocusedContainerColor = AppTheme.colors.inputBackground,
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = AppTheme.colors.inputBorder,
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
                    .clickable(enabled = !reminderState.isSaving) {
                        viewModel.updateReminderDate(reminderDateText)
                        viewModel.updateReminderTime(selectedTime)
                        viewModel.updateReminderTiming(selectedTiming)
                        viewModel.updateReminderNotes(notes)
                        viewModel.saveMedicationReminder(onSuccess = {
                            showSuccessBanner = true
                        })
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (reminderState.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Set Medication Reminder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Calendar Date Picker Modal Dialog
        if (showDatePickerDialog) {
            CalendarDatePickerDialog(
                selectedDateText = reminderDateText,
                onDateSelected = { year, month, day ->
                    val cal = Calendar.getInstance().apply { set(year, month, day) }
                    reminderDateText = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(cal.time)
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
