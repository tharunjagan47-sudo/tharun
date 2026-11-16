package com.simats.diawell123.ui.auth

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.util.BiometricHelper

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val sessionManager = remember { SessionManager.getInstance() }

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    val savedPin = sessionManager.getAppLockPin() ?: "1234"
    val isBiometricEnabled = sessionManager.isBiometricUnlockEnabled()
    val userName = sessionManager.getFullName() ?: "User"

    fun triggerVibration() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(120)
        }
    }

    fun launchBiometrics() {
        if (activity != null && isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Unlock DiaWell",
                subtitle = "Confirm your fingerprint or face to proceed",
                negativeButtonText = "Use Passcode",
                onSuccess = {
                    sessionManager.setAppUnlocked(true)
                    onUnlocked()
                },
                onError = { _, _ -> }
            )
        }
    }

    // Auto-prompt biometrics once on screen display
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            launchBiometrics()
        }
    }

    fun onNumberClick(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (newPin == savedPin) {
                    sessionManager.setAppUnlocked(true)
                    onUnlocked()
                } else {
                    triggerVibration()
                    errorMessage = "Incorrect passcode. Please try again."
                    enteredPin = ""
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF8B1575),
                                    Color(0xFFB81D85),
                                    Color(0xFF6B0E5A)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔒", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "DiaWell Locked",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (userName.isNotBlank()) "Welcome back, $userName" else "Enter your 4-digit passcode",
                    fontSize = 14.sp,
                    color = AppTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) Color(0xFFB81D85) else AppTheme.colors.border
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isFilled) Color(0xFFB81D85) else AppTheme.colors.textMuted,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE53935),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val row1 = listOf("1", "2", "3")
                val row2 = listOf("4", "5", "6")
                val row3 = listOf("7", "8", "9")

                KeypadRow(numbers = row1, onNumberClick = { onNumberClick(it) })
                KeypadRow(numbers = row2, onNumberClick = { onNumberClick(it) })
                KeypadRow(numbers = row3, onNumberClick = { onNumberClick(it) })

                // Bottom row: Biometric / Clear, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric or empty
                    if (isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.cardBackground)
                                .border(1.dp, AppTheme.colors.border, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    launchBiometrics()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👆", fontSize = 26.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    enteredPin = ""
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "C",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }

                    // Number 0
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNumberClick("0")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "0",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    }

                    // Backspace button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onBackspace()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⌫",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                }
            }

            // Forgot Passcode Footer
            TextButton(
                onClick = { showForgotPinDialog = true },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Forgot Passcode?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB81D85)
                )
            }
        }
    }

    if (showForgotPinDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            title = {
                Text(
                    text = "Reset App Passcode",
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "If you forgot your 4-digit passcode, you can sign out to your account and log back in using your email and password.",
                    fontSize = 13.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotPinDialog = false
                        Toast.makeText(context, "Signing out for security...", Toast.LENGTH_SHORT).show()
                        onSignOut()
                    }
                ) {
                    Text(
                        text = "Sign Out & Reset",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPinDialog = false }) {
                    Text(text = "Cancel", color = AppTheme.colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = AppTheme.colors.cardBackground
        )
    }
}

@Composable
private fun KeypadRow(
    numbers: List<String>,
    onNumberClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        numbers.forEach { num ->
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.cardBackground)
                    .border(1.dp, AppTheme.colors.border, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onNumberClick(num)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = num,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
            }
        }
    }
}
