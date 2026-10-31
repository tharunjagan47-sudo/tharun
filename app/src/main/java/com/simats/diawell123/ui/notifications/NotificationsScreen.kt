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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Home") }
    val uiState by viewModel.uiState.collectAsState()

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

                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    if (uiState.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFB81D85))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${uiState.unreadCount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Header & Clear All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = uiState.dateHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6E6877),
                    )

                    if (uiState.notifications.isNotEmpty()) {
                        Text(
                            text = "Clear all",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.clearAll() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Loading Indicator
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFB81D85),
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                } else if (uiState.notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔔",
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All caught up!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E22)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No unread alerts or reminders right now.",
                                fontSize = 12.sp,
                                color = Color(0xFF6E6877)
                            )
                        }
                    }
                } else {
                    // Notification Cards
                    uiState.notifications.forEach { item ->
                        NotificationItem(
                            title = item.title,
                            body = item.body,
                            time = item.time,
                            isUnread = item.isUnread,
                            onClick = {
                                if (item.isUnread) {
                                    viewModel.markAsRead(item.id)
                                }
                                onNotificationClick()
                            },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock
        IosLiquidGlassBottomNav(
            selectedTab = selectedTab,
            onHomeTabClick = onHomeTabClick,
            onDietTabClick = onDietTabClick,
            onMedsTabClick = onMedsTabClick,
            onReportsTabClick = onReportsTabClick,
            onSettingsTabClick = onSettingsTabClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
