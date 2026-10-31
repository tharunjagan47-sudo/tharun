package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun NotificationPreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSavePreferencesClick: () -> Unit = {},
) {
    val prefsState by viewModel.prefsState.collectAsState()
    var vibration by remember { mutableStateOf(true) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1200)
            showSuccessBanner = false
            onSavePreferencesClick()
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
                        text = "Notification Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Settings",
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
                                text = "Notification Preferences Saved!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your reminder settings have been updated.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            if (prefsState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = prefsState.errorMessage ?: "An error occurred.",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Text(
                text = "Choose the check-ins that assist for you.",
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reminders and updates",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            NotificationToggleRow(
                title = "Medication Reminders",
                subtitle = "For the dose schedule you set.",
                icon = "💊",
                checked = prefsState.medicationReminders,
                onCheckedChange = { viewModel.toggleMedicationReminders(it) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Meal Logging Reminders",
                subtitle = "A nudge to record meals and portions.",
                icon = "🥗",
                checked = prefsState.mealLoggingReminders,
                onCheckedChange = { viewModel.toggleMealLoggingReminders(it) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Glucose Logging Reminders",
                subtitle = "Follow your agreed care routine.",
                icon = "🩸",
                checked = prefsState.glucoseCheckins,
                onCheckedChange = { viewModel.toggleGlucoseCheckins(it) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Weekly Report Alerts",
                subtitle = "Know when your weekly summary is ready.",
                icon = "📊",
                checked = prefsState.weeklyReportAlerts,
                onCheckedChange = { viewModel.toggleWeeklyReportAlerts(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sound & Vibration Settings
            NotificationOptionRow(
                icon = "🔔",
                title = "Reminder Sound",
                value = "Gentle Chime ›",
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Vibration",
                subtitle = "With reminder alerts",
                icon = "📳",
                checked = vibration,
                onCheckedChange = { vibration = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quiet Hours Callout Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🌙  Quiet hours",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        )
                        Text(
                            text = "›",
                            fontSize = 18.sp,
                            color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "10:00 PM - 7:00 AM • active",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Review scheduled medication reminders with your care team before changing quiet hours.",
                        fontSize = 11.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        lineHeight = 15.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Preferences Button
            IosLiquidGlassButton(
                text = if (prefsState.isSaving) "Saving..." else "Save Preferences",
                onClick = {
                    viewModel.saveNotificationPreferences(onSuccess = {
                        showSuccessBanner = true
                    })
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preferences automatically sync with your DiaWell profile.",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFB81D85),
                    uncheckedThumbColor = AppTheme.colors.textMuted,
                    uncheckedTrackColor = AppTheme.colors.border,
                ),
            )
        }
    }
}

@Composable
fun NotificationOptionRow(
    icon: String,
    title: String,
    value: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )
            }

            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFB81D85),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationPreferencesScreenPreview() {
    Diawell123Theme {
        NotificationPreferencesScreen()
    }
}
