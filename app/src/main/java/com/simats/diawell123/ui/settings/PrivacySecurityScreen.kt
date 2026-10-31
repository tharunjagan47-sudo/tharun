package com.simats.diawell123.ui.settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun PrivacySecurityScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsOfUseClick: () -> Unit = {},
    onExportRecordsClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
) {
    var appLock by remember { mutableStateOf(value = true) }
    var biometricUnlock by remember { mutableStateOf(value = false) }
    var dataAiPrefs by remember { mutableStateOf(value = true) }

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
                        text = "Privacy and Security",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Your choices, your control",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Good design privacy – these controls let you change security or data settings.",
                            fontSize = 11.sp,
                            color = AppTheme.colors.textSecondary,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Change Password Row
            PrivacyLinkRow(icon = "🔑", title = "Change Password", onClick = onChangePasswordClick)

            Spacer(modifier = Modifier.height(10.dp))

            // App Lock Toggle Row
            PrivacyToggleRow(
                icon = "🔒",
                title = "App Lock",
                subtitle = "Require passcode on open",
                checked = appLock,
                onCheckedChange = { appLock = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Biometric Unlock Toggle Row
            PrivacyToggleRow(
                icon = "👆",
                title = "Biometric Unlock",
                subtitle = "Use Fingerprint or Face ID",
                checked = biometricUnlock,
                onCheckedChange = { biometricUnlock = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Data and AI Preferences Toggle Row
            PrivacyToggleRow(
                icon = "📊",
                title = "Data and AI Preferences",
                subtitle = "Use anonymized data for general AI suggestions",
                checked = dataAiPrefs,
                onCheckedChange = { dataAiPrefs = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyLinkRow(icon = "📄", title = "Privacy Policy", onClick = onPrivacyPolicyClick)
            Spacer(modifier = Modifier.height(10.dp))
            PrivacyLinkRow(icon = "📜", title = "Terms of Use", onClick = onTermsOfUseClick)

            Spacer(modifier = Modifier.height(24.dp))

            // Manage Your Data Section
            Text(
                text = "Manage Your Data",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyLinkRow(icon = "📥", title = "Export Records", onClick = onExportRecordsClick)
            Spacer(modifier = Modifier.height(10.dp))
            PrivacyLinkRow(icon = "🗑️", title = "Delete Account", isDestructive = true, onClick = onDeleteAccountClick)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Review your data preferences regularly. All security and data actions shown here represent static preview controls.",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
fun PrivacyLinkRow(
    icon: String,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) Color(0xFFD32F2F) else AppTheme.colors.textPrimary,
                )
            }
            Text(text = "›", fontSize = 18.sp, color = AppTheme.colors.textMuted)
        }
    }
}

@Composable
fun PrivacyToggleRow(
    icon: String,
    title: String,
    subtitle: String,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivacySecurityScreenPreview() {
    Diawell123Theme {
        PrivacySecurityScreen()
    }
}
