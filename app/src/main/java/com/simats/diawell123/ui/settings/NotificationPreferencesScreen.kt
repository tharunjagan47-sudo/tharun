package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
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
    val context = LocalContext.current
    val sessionManager = remember {
        try { com.simats.diawell123.data.local.SessionManager.getInstance() }
        catch (_: Exception) { com.simats.diawell123.data.local.SessionManager.init(context) }
    }
    var smartNudgesEnabled by remember { mutableStateOf(sessionManager.isSmartNudgesEnabled()) }
    var drJarvisVoiceEnabled by remember { mutableStateOf(sessionManager.isDrJarvisVoiceEnabled()) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var showQuietHoursDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StyledBackButton(onBackClick = onBackClick)

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Notification Preferences",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
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
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
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
                vectorIcon = Icons.Rounded.Medication,
                checked = prefsState.medicationReminders,
                onCheckedChange = {
                    sessionManager.setMedAlarmsEnabled(it)
                    viewModel.toggleMedicationReminders(it)
                    Toast.makeText(context, if (it) "Medication reminders enabled" else "Medication reminders disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Smart Health Nudges",
                subtitle = "Swiggy/Zomato-style daily contextual health tips.",
                vectorIcon = Icons.Rounded.Notifications,
                checked = smartNudgesEnabled,
                onCheckedChange = {
                    smartNudgesEnabled = it
                    sessionManager.setSmartNudgesEnabled(it)
                    if (it) {
                        com.simats.diawell123.util.SmartNudgeEngine.scheduleAllDailyNudges(context)
                    } else {
                        com.simats.diawell123.util.SmartNudgeEngine.cancelAllNudges(context)
                    }
                    Toast.makeText(context, if (it) "Smart Health Nudges enabled" else "Smart Health Nudges disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "DIVA Voice Assistant",
                subtitle = "Automatic two-way voice reading & audio speech for DIVA.",
                vectorIcon = Icons.Rounded.SmartToy,
                checked = drJarvisVoiceEnabled,
                onCheckedChange = {
                    drJarvisVoiceEnabled = it
                    sessionManager.setDrJarvisVoiceEnabled(it)
                    Toast.makeText(context, if (it) "DIVA Voice Assistant enabled" else "DIVA Voice Assistant muted", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Meal Logging Reminders",
                subtitle = "A nudge to record meals and portions.",
                vectorIcon = Icons.Rounded.Restaurant,
                checked = prefsState.mealLoggingReminders,
                onCheckedChange = {
                    viewModel.toggleMealLoggingReminders(it)
                    Toast.makeText(context, if (it) "Meal logging reminders enabled" else "Meal logging reminders disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Glucose Logging Reminders",
                subtitle = "Follow your agreed care routine.",
                vectorIcon = Icons.Rounded.WaterDrop,
                checked = prefsState.glucoseCheckins,
                onCheckedChange = {
                    viewModel.toggleGlucoseCheckins(it)
                    Toast.makeText(context, if (it) "Glucose logging reminders enabled" else "Glucose logging reminders disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Weekly Report Alerts",
                subtitle = "Know when your weekly summary is ready.",
                vectorIcon = Icons.Rounded.InsertChart,
                checked = prefsState.weeklyReportAlerts,
                onCheckedChange = {
                    viewModel.toggleWeeklyReportAlerts(it)
                    Toast.makeText(context, if (it) "Weekly report alerts enabled" else "Weekly report alerts disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Vibration",
                subtitle = "With reminder alerts",
                vectorIcon = Icons.Rounded.Vibration,
                checked = prefsState.vibration,
                onCheckedChange = {
                    viewModel.toggleVibration(it)
                    Toast.makeText(context, if (it) "Vibration alerts enabled" else "Vibration alerts disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quiet Hours Callout Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
                    .clickable { showQuietHoursDialog = true }
                    .padding(14.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DarkMode,
                                contentDescription = null,
                                tint = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quiet Hours",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                            )
                        }
                        Text(
                            text = "›",
                            fontSize = 18.sp,
                            color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${prefsState.quietHours} • active",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Review scheduled medication reminders with your care team before changing quiet hours.",
                        fontSize = 11.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
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
                        Toast.makeText(context, "Notification preferences saved successfully!", Toast.LENGTH_SHORT).show()
                    })
                },
            )

            Spacer(modifier = Modifier.height(100.dp))



            // Quiet Hours Selection Dialog
            if (showQuietHoursDialog) {
                AlertDialog(
                    onDismissRequest = { showQuietHoursDialog = false },
                    title = {
                        Text(
                            text = "Select Quiet Hours",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AppTheme.colors.textPrimary
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "10:00 PM - 7:00 AM",
                                "11:00 PM - 6:00 AM",
                                "10:00 PM - 6:00 AM",
                                "Disabled"
                            ).forEach { schedule ->
                                val isSelected = prefsState.quietHours == schedule
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF5F3FF)) else Color.Transparent)
                                        .clickable {
                                            viewModel.setQuietHours(schedule)
                                            showQuietHoursDialog = false
                                            Toast.makeText(context, "Quiet hours updated: $schedule", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = Color(0xFFA855F7),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = schedule,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFFA855F7) else AppTheme.colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showQuietHoursDialog = false }) {
                            Text("Done", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = AppTheme.colors.cardBackground
                )
            }
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    vectorIcon: ImageVector? = null,
    icon: String = "",
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (AppTheme.colors.isDark) Color(0xFF2E1C55) else Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (vectorIcon != null) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (icon.isNotEmpty()) {
                        Text(text = icon, fontSize = 20.sp)
                    }
                }
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
                    checkedTrackColor = Color(0xFFA855F7),
                    uncheckedThumbColor = AppTheme.colors.textMuted,
                    uncheckedTrackColor = AppTheme.colors.border,
                ),
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
