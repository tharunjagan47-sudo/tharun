package com.simats.diawell123.ui.settings

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import com.simats.diawell123.util.BiometricHelper

@Composable
fun PrivacySecurityScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsOfUseClick: () -> Unit = {},
    onExportRecordsClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
) {
    val privacyState by viewModel.privacyState.collectAsState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance() }
    var showSetPinDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
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
                    text = "Privacy & Security",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Change Password Row
            PrivacyLinkRow(vectorIcon = Icons.Rounded.Key, title = "Change Password", onClick = onChangePasswordClick)

            Spacer(modifier = Modifier.height(10.dp))

            // App Lock Toggle Row
            PrivacyToggleRow(
                vectorIcon = Icons.Rounded.Lock,
                title = "App Lock",
                subtitle = "Require passcode on open",
                checked = privacyState.appLock,
                onCheckedChange = { enable ->
                    if (enable) {
                        if (!sessionManager.hasAppLockPin()) {
                            showSetPinDialog = true
                        } else {
                            viewModel.toggleAppLock(true)
                            Toast.makeText(context, "App Lock enabled (Passcode required on open)", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        viewModel.toggleAppLock(false)
                        Toast.makeText(context, "App Lock disabled", Toast.LENGTH_SHORT).show()
                    }
                },
            )

            if (privacyState.appLock) {
                Spacer(modifier = Modifier.height(10.dp))
                PrivacyLinkRow(
                    vectorIcon = Icons.Rounded.Pin,
                    title = "Change 4-Digit Passcode",
                    onClick = { showSetPinDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Biometric Unlock Toggle Row
            PrivacyToggleRow(
                vectorIcon = Icons.Rounded.Fingerprint,
                title = "Biometric Unlock",
                subtitle = "Use Fingerprint or Face ID",
                checked = privacyState.biometricUnlock,
                onCheckedChange = { enable ->
                    if (enable) {
                        val authStatus = BiometricHelper.canAuthenticate(context)
                        if (authStatus == BiometricManager.BIOMETRIC_SUCCESS) {
                            viewModel.toggleBiometricUnlock(true)
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                BiometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    title = "Confirm Biometric Unlock",
                                    subtitle = "Verify your fingerprint or face to activate",
                                    onSuccess = {
                                        Toast.makeText(context, "Biometric Unlock enabled and verified!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { _, _ -> }
                                )
                            } else {
                                Toast.makeText(context, "Biometric Unlock enabled!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val msg = BiometricHelper.getBiometricStatusMessage(context)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        viewModel.toggleBiometricUnlock(false)
                        Toast.makeText(context, "Biometric Unlock disabled", Toast.LENGTH_SHORT).show()
                    }
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Data and AI Preferences Toggle Row
            PrivacyToggleRow(
                vectorIcon = Icons.Rounded.InsertChart,
                title = "Data and AI Preferences",
                subtitle = "Use anonymized data for general AI suggestions",
                checked = privacyState.dataAiPrefs,
                onCheckedChange = {
                    viewModel.toggleDataAiPrefs(it)
                    Toast.makeText(context, if (it) "AI data personalization enabled" else "AI data personalization disabled", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyLinkRow(vectorIcon = Icons.Rounded.Description, title = "Privacy Policy", onClick = onPrivacyPolicyClick)
            Spacer(modifier = Modifier.height(10.dp))
            PrivacyLinkRow(vectorIcon = Icons.Rounded.Article, title = "Terms of Use", onClick = onTermsOfUseClick)

            Spacer(modifier = Modifier.height(24.dp))

            // Manage Your Data Section
            Text(
                text = "Manage Your Data",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyLinkRow(vectorIcon = Icons.Rounded.Delete, title = "Delete Account", isDestructive = true, onClick = onDeleteAccountClick)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showSetPinDialog) {
        SetAppLockPinDialog(
            onDismiss = { showSetPinDialog = false },
            onPinSaved = { pin ->
                sessionManager.setAppLockPin(pin)
                viewModel.toggleAppLock(true)
                showSetPinDialog = false
                Toast.makeText(context, "4-digit passcode saved! App Lock is now active.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SetAppLockPinDialog(
    onDismiss: () -> Unit,
    onPinSaved: (String) -> Unit
) {
    var step by rememberSaveable { mutableStateOf(1) } // 1: Enter PIN, 2: Confirm PIN
    var firstPin by rememberSaveable { mutableStateOf("") }
    var currentInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun handleDigit(d: String) {
        if (currentInput.length < 4) {
            val next = currentInput + d
            currentInput = next
            errorMessage = null

            if (next.length == 4) {
                if (step == 1) {
                    firstPin = next
                    currentInput = ""
                    step = 2
                } else {
                    if (next == firstPin) {
                        onPinSaved(next)
                    } else {
                        errorMessage = "Passcodes do not match. Please try again."
                        step = 1
                        firstPin = ""
                        currentInput = ""
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            errorMessage = null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (step == 1) "Set 4-Digit Passcode" else "Confirm Passcode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (step == 1) "Enter a 4-digit code to protect your DiaWell app" else "Re-enter the 4-digit code to confirm",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFFA855F7) else AppTheme.colors.border)
                                .border(1.dp, if (isFilled) Color(0xFFA855F7) else AppTheme.colors.textMuted, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFE53935),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Numeric Keypad
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { digit ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.elevatedSurface)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { handleDigit(digit) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bottom row: Clear, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { currentInput = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "C", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.elevatedSurface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { handleDigit("0") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "0", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.elevatedSurface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { handleBackspace() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Backspace,
                            contentDescription = "Backspace",
                            tint = AppTheme.colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel", color = AppTheme.colors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun PrivacyLinkRow(
    vectorIcon: ImageVector? = null,
    icon: String = "",
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDestructive) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF7C3AED).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (vectorIcon != null) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = null,
                            tint = if (isDestructive) Color(0xFFEF4444) else Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (icon.isNotEmpty()) {
                        Text(text = icon, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) Color(0xFFEF4444) else AppTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PrivacyToggleRow(
    vectorIcon: ImageVector? = null,
    icon: String = "",
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp),
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
                        .background(Color(0xFF7C3AED).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (vectorIcon != null) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (icon.isNotEmpty()) {
                        Text(text = icon, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
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
fun PrivacySecurityScreenPreview() {
    Diawell123Theme {
        PrivacySecurityScreen()
    }
}
