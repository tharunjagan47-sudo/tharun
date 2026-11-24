package com.simats.diawell123.ui.settings

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.TextButton
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
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.ThemeManager
import com.simats.diawell123.ui.theme.ThemeMode
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPersonalProfileClick: () -> Unit = {},
    onNotificationPrefsClick: () -> Unit = {},
    onFoodPrefsClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onExportRecordsClick: () -> Unit = {},
    onAiChatbotClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Settings") }
    var showResetDialog by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }
    val profileUiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (profileUiState.profile == null) {
            profileViewModel.loadProfile()
        }
    }

    val currentName = profileUiState.profile?.fullName ?: ProfileState.userName
    val currentEmail = profileUiState.profile?.email ?: ProfileState.userEmail

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Settings",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // User Profile Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppTheme.colors.cardBackground)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                                .clickable { onPersonalProfileClick() }
                                .padding(18.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileAvatarView(sizeDp = 48.dp, fontSizeSp = 16)

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = currentName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.textPrimary,
                                        softWrap = true,
                                        lineHeight = 20.sp,
                                    )
                                    Text(
                                        text = currentEmail,
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textSecondary,
                                        softWrap = true,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Appearance & Theme Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppTheme.colors.cardBackground)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                                .padding(18.dp),
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (AppTheme.colors.isDark) Color(0xFF2E1C55) else Color(0xFFF3E8FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Palette,
                                                contentDescription = null,
                                                tint = Color(0xFFA855F7),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Appearance & Theme",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppTheme.colors.textPrimary
                                            )
                                            Text(
                                                text = "Active: ${ThemeManager.themeMode.value.title}",
                                                fontSize = 11.sp,
                                                color = AppTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Segmented Theme Selector (Light, Dark, System)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppTheme.colors.secondaryBackground)
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        Triple(ThemeMode.LIGHT, "Light", Icons.Rounded.LightMode),
                                        Triple(ThemeMode.DARK, "Dark", Icons.Rounded.DarkMode),
                                        Triple(ThemeMode.SYSTEM, "System", Icons.Rounded.Settings)
                                    ).forEach { (mode, label, iconVec) ->
                                        val isSelected = ThemeManager.themeMode.value == mode
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isSelected) {
                                                        if (AppTheme.colors.isDark) Color(0xFF202C40) else Color.White
                                                    } else Color.Transparent
                                                )
                                                .clickable {
                                                    ThemeManager.setThemeMode(mode, context)
                                                    Toast.makeText(context, "Theme set to ${mode.title}", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = iconVec,
                                                    contentDescription = null,
                                                    tint = if (isSelected) AppTheme.colors.primaryAccent else AppTheme.colors.textSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) AppTheme.colors.primaryAccent else AppTheme.colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Settings Rows (Pop Up Staggered)
                ElementPopUpContainer(delayMillis = 80) {
                    Column {
                        SettingsRow(vectorIcon = Icons.Rounded.Person, title = "Personal Profile", onClick = onPersonalProfileClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(vectorIcon = Icons.Rounded.Notifications, title = "Notification Preferences", onClick = onNotificationPrefsClick)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ElementPopUpContainer(delayMillis = 160) {
                    Column {
                        SettingsRow(vectorIcon = Icons.Rounded.Restaurant, title = "Food Preferences", onClick = onFoodPrefsClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(vectorIcon = Icons.Rounded.Lock, title = "Privacy and Security", onClick = onPrivacySecurityClick)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ElementPopUpContainer(delayMillis = 200) {
                    Column {
                        SettingsRow(vectorIcon = Icons.Rounded.Download, title = "Export Medical Records", onClick = onExportRecordsClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(
                            vectorIcon = Icons.Rounded.Emergency,
                            title = "Emergency Medical Hotline (108)",
                            onClick = {
                                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:108"))
                                try {
                                    context.startActivity(dialIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Emergency Helpline: 108", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ElementPopUpContainer(delayMillis = 240) {
                    Column {
                        SettingsRow(
                            vectorIcon = Icons.Rounded.CleaningServices,
                            title = "Clear App Cache",
                            onClick = {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    Toast.makeText(context, "App cache cleared successfully.", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Cache already clean.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(
                            vectorIcon = Icons.Rounded.Delete,
                            title = "Reset / Clear My Logs",
                            isDestructive = true,
                            onClick = { showResetDialog = true }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(vectorIcon = Icons.Rounded.Help, title = "Help and Support", onClick = onHelpSupportClick)
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(vectorIcon = Icons.Rounded.Logout, title = "Log Out", isDestructive = true, onClick = onLogoutClick)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Version Text
                ElementPopUpContainer(delayMillis = 280) {
                    Text(
                        text = "DiaWell Version 1.0.0 (Build 123)",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                Toast.makeText(context, "DiaWell v1.0.0 is up to date.", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding())
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { if (!isResetting) showResetDialog = false },
                title = {
                    Text(
                        text = "Reset Health Logs?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                },
                text = {
                    Text(
                        text = "This will permanently wipe all glucose readings, active medications, meals, and exercise logs from your account and reset all dashboard counters to zero. This cannot be undone.",
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isResetting = true
                            settingsViewModel.resetUserLogs(
                                context = context,
                                onSuccess = { msg ->
                                    isResetting = false
                                    showResetDialog = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    isResetting = false
                                    showResetDialog = false
                                    Toast.makeText(context, "Logs cleared locally: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isResetting
                    ) {
                        Text(
                            text = if (isResetting) "Clearing..." else "Reset Everything",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetDialog = false },
                        enabled = !isResetting
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsRow(
    vectorIcon: ImageVector? = null,
    icon: String = "",
    title: String,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {},
) {
    val isDark = AppTheme.colors.isDark
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
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
                        .background(
                            if (isDestructive) {
                                if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else Color(0xFFFFEBEE)
                            } else {
                                if (isDark) Color(0xFF2E1C55) else Color(0xFFF3E8FF)
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (vectorIcon != null) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = null,
                            tint = if (isDestructive) {
                                if (isDark) Color(0xFFF87171) else Color(0xFFD32F2F)
                            } else {
                                Color(0xFFA855F7)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (icon.isNotEmpty()) {
                        Text(text = icon, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) {
                        if (isDark) Color(0xFFF87171) else Color(0xFFD32F2F)
                    } else {
                        AppTheme.colors.textPrimary
                    },
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textMuted,
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
