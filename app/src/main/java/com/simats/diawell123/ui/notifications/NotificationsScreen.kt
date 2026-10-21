package com.simats.diawell123.ui.notifications

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Home") }

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

                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Header & Clear All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Today • 15 October",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6E6877),
                    )

                    Text(
                        text = "Clear all",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notification Cards
                NotificationItem(
                    title = "Evening medication reminder",
                    body = "Metformin • scheduled at 8:00 PM. Follow your prescribed schedule.",
                    time = "7:50 PM • Unread",
                    isUnread = true,
                    onClick = onNotificationClick,
                )

                Spacer(modifier = Modifier.height(10.dp))

                NotificationItem(
                    title = "A little meal check-in",
                    body = "Remember to log your dinner and portion when you're ready.",
                    time = "6:30 PM • Unread",
                    isUnread = true,
                    onClick = onNotificationClick,
                )

                Spacer(modifier = Modifier.height(10.dp))

                NotificationItem(
                    title = "Time for a glucose check-in",
                    body = "Log a reading according to the routine agreed with your care team.",
                    time = "7:15 AM • Read",
                    isUnread = false,
                    onClick = onNotificationClick,
                )

                Spacer(modifier = Modifier.height(10.dp))

                NotificationItem(
                    title = "Your weekly report is ready",
                    body = "See your food, glucose and medication tracking for 8-15 October.",
                    time = "8:00 AM • Read",
                    isUnread = false,
                    onClick = onNotificationClick,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFEFECEF)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(
                    "Home" to "🏠",
                    "Diet" to "🥗",
                    "Meds" to "💊",
                    "Reports" to "📊",
                    "Settings" to "⚙️",
                ).forEach { (name, icon) ->
                    val isSelected = selectedTab == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedTab = name
                            when (name) {
                                "Home" -> onHomeTabClick()
                                "Diet" -> onDietTabClick()
                                "Meds" -> onMedsTabClick()
                                "Reports" -> onReportsTabClick()
                                "Settings" -> onSettingsTabClick()
                            }
                        },
                    ) {
                        Text(text = icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF8A8595),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    body: String,
    time: String,
    isUnread: Boolean,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isUnread) Color(0xFFFDEEF7) else Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                color = Color(0xFF6E6877),
                lineHeight = 17.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = time,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnread) Color(0xFFB81D85) else Color(0xFF8A8595),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationsScreenPreview() {
    Diawell123Theme {
        NotificationsScreen()
    }
}
