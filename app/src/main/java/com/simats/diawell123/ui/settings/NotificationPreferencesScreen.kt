package com.simats.diawell123.ui.settings

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
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun NotificationPreferencesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSavePreferencesClick: () -> Unit = {},
) {
    var medicationReminders by remember { mutableStateOf(value = true) }
    var mealReminders by remember { mutableStateOf(value = true) }
    var glucoseReminders by remember { mutableStateOf(value = true) }
    var weeklyReportAlerts by remember { mutableStateOf(value = true) }
    var vibration by remember { mutableStateOf(value = true) }

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
                        text = "Notification Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Settings",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose the check-ins that assist for you.",
                fontSize = 12.sp,
                color = Color(0xFF6E6877),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reminders and updates",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(12.dp))

            NotificationToggleRow(
                title = "Medication Reminders",
                subtitle = "For the dose schedule you set.",
                icon = "💊",
                checked = medicationReminders,
                onCheckedChange = { medicationReminders = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Meal Logging Reminders",
                subtitle = "A nudge to record meals and portions.",
                icon = "🥗",
                checked = mealReminders,
                onCheckedChange = { mealReminders = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Glucose Logging Reminders",
                subtitle = "Follow your agreed care routine.",
                icon = "🩸",
                checked = glucoseReminders,
                onCheckedChange = { glucoseReminders = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotificationToggleRow(
                title = "Weekly Report Alerts",
                subtitle = "Know when your weekly summary is ready.",
                icon = "📊",
                checked = weeklyReportAlerts,
                onCheckedChange = { weeklyReportAlerts = it },
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
                    .background(Color(0xFFFDEEF7))
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
                            color = Color(0xFF811978),
                        )
                        Text(
                            text = "›",
                            fontSize = 18.sp,
                            color = Color(0xFF811978),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "10:00 PM - 7:00 AM • example",
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Review scheduled medication reminders with your care team before changing quiet hours.",
                        fontSize = 11.sp,
                        color = Color(0xFF811978),
                        lineHeight = 15.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Preferences Button
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
                    .clickable { onSavePreferencesClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Save Preferences",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Demo preferences • Static preview",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
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
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
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
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFB81D85),
                    uncheckedThumbColor = Color(0xFF8A8595),
                    uncheckedTrackColor = Color(0xFFEFECEF),
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
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
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
                    color = Color(0xFF1E1E22),
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
