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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.profile.ProfileAvatarView
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.profile.ProfileViewModel
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPersonalProfileClick: () -> Unit = {},
    onNotificationPrefsClick: () -> Unit = {},
    onFoodPrefsClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onAiChatbotClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Settings") }
    val profileUiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val currentName = profileUiState.profile?.fullName ?: ProfileState.userName
    val currentEmail = profileUiState.profile?.email ?: ProfileState.userEmail

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

                // Top Bar & Profile Card (Pops Up)
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StyledBackButton(onBackClick = onBackClick)

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Settings",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "Make DiaWell feel like you.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // User Profile Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                                .clickable { onPersonalProfileClick() }
                                .padding(16.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProfileAvatarView(sizeDp = 48.dp, fontSizeSp = 16)

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = currentName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1E22),
                                    )
                                    Text(
                                        text = currentEmail,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6E6877),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Settings Rows (Pop Up Staggered)
                ElementPopUpContainer(delayMillis = 80) {
                    Column {
                        SettingsRow(icon = "👤", title = "Personal Profile", onClick = onPersonalProfileClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(icon = "🔔", title = "Notification Preferences", onClick = onNotificationPrefsClick)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ElementPopUpContainer(delayMillis = 160) {
                    Column {
                        SettingsRow(icon = "🥗", title = "Food Preferences", onClick = onFoodPrefsClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(icon = "🔒", title = "Privacy and Security", onClick = onPrivacySecurityClick)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ElementPopUpContainer(delayMillis = 240) {
                    Column {
                        SettingsRow(icon = "❓", title = "Help and Support", onClick = onHelpSupportClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(icon = "🚪", title = "Log Out", isDestructive = true, onClick = onLogoutClick)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Version Text
                ElementPopUpContainer(delayMillis = 280) {
                    Text(
                        text = "DiaWell Version 1.0.0 (Build 123)\nConnected to Local PHP Backend",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8595),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock (Pops Up)
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = onHomeTabClick,
                onDietTabClick = onDietTabClick,
                onMedsTabClick = onMedsTabClick,
                onReportsTabClick = onReportsTabClick,
                onSettingsTabClick = { selectedTab = "Settings" },
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFFDEEF7)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = icon, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) Color(0xFFD32F2F) else Color(0xFF1E1E22),
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A8595),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    Diawell123Theme {
        SettingsScreen()
    }
}
