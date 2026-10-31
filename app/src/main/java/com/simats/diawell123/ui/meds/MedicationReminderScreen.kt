package com.simats.diawell123.ui.meds
import com.simats.diawell123.ui.theme.AppTheme

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.MedicationItem
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationReminderScreen(
    modifier: Modifier = Modifier,
    medication: MedicationItem? = null,
    viewModel: MedsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onTakenClick: () -> Unit = {},
    onSnoozeClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
) {
    val detailState by viewModel.detailState.collectAsState()
    val activeMed = medication ?: detailState.medication

    val medId = activeMed?.id ?: 0
    val medName = activeMed?.name?.ifBlank { "Metformin" } ?: "Metformin"
    val medDose = activeMed?.dose?.ifBlank { "500 mg • 1 Tablet" } ?: "500 mg • 1 Tablet"
    val medTime = activeMed?.reminderTime?.ifBlank { "8:00 PM" } ?: "8:00 PM"
    val medTiming = activeMed?.timing?.ifBlank { "After Meal" } ?: "After Meal"
    val medDate = activeMed?.reminderDate?.ifBlank { "Today" } ?: "Today"
    val medNotes = activeMed?.notes?.ifBlank { "Follow prescribed routine and drink water." } ?: "Follow prescribed routine and drink water."

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
                    color = AppTheme.colors.textPrimary,
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
                    color = AppTheme.colors.textPrimary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$medName • Scheduled reminder",
                fontSize = 11.sp,
                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Pill Circle Illustration
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "💊", fontSize = 42.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "A gentle nudge for\nyour daily routine.",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder Detail Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardBackground)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
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
                                .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = medDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                        }

                        Text(
                            text = medTime,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A1C7D),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = medName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = medDose,
                        fontSize = 13.sp,
                        color = AppTheme.colors.textSecondary,
                    )

                    Text(
                        text = "$medTiming • ${activeMed?.frequency ?: "Daily"}",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textMuted,
                    )

                    // Enriched Clinical Dataset Details
                    if (activeMed?.primaryIngredient?.isNotBlank() == true ||
                        activeMed?.therapeuticClass?.isNotBlank() == true ||
                        activeMed?.manufacturer?.isNotBlank() == true
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppTheme.colors.secondaryBackground)
                                .padding(10.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                if (!activeMed.primaryIngredient.isNullOrBlank()) {
                                    Text(
                                        text = "🔬 Active: ${activeMed.primaryIngredient}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                                if (!activeMed.therapeuticClass.isNullOrBlank()) {
                                    Text(
                                        text = "🏷️ Class: ${activeMed.therapeuticClass}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFB81D85),
                                    )
                                }
                                if (!activeMed.manufacturer.isNullOrBlank() || !activeMed.packaging.isNullOrBlank()) {
                                    val mfdPack = listOf(activeMed.manufacturer, activeMed.packaging)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • ")
                                    Text(
                                        text = "🏭 $mfdPack",
                                        fontSize = 10.5.sp,
                                        color = AppTheme.colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }

                    if (medNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: $medNotes",
                            fontSize = 11.sp,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (detailState.errorMessage != null) {
                Text(
                    text = "⚠️ ${detailState.errorMessage}",
                    fontSize = 12.sp,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = "Follow your prescribed schedule.\nMark Taken after taking your medication.",
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (detailState.isUpdating) {
                CircularProgressIndicator(
                    color = Color(0xFFB81D85),
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                // Taken Button
                IosLiquidGlassButton(
                    text = "✓  Taken",
                    onClick = {
                        if (medId > 0) {
                            viewModel.updateStatus(medId, "Taken", onSuccess = onTakenClick)
                        } else {
                            onTakenClick()
                        }
                    },
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
                        .clickable {
                            if (medId > 0) {
                                viewModel.updateStatus(medId, "Missed", onSuccess = onSkipClick)
                            } else {
                                onSkipClick()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )

                if (medId > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🗑 Delete Reminder",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.deleteMedication(medId, onSuccess = onBackClick)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "For dose changes or missed dose advice, ask your pharmacist or clinician if you are unsure.",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
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
