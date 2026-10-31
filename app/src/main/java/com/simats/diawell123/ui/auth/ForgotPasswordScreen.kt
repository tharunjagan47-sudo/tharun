package com.simats.diawell123.ui.auth
import com.simats.diawell123.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ArcReactorCanvas
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.profile.AiSpeechOrbLoadingView
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSendResetLinkClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isUpdatingPassword by remember { mutableStateOf(false) }

    // 3D Saturn AI Orb Loading Transition & Success Return
    LaunchedEffect(isUpdatingPassword) {
        if (isUpdatingPassword) {
            delay(2200) // 2.2 Seconds 3D Rotating Saturn AI Orb
            isUpdatingPassword = false
            Toast.makeText(context, "Password successfully changed!", Toast.LENGTH_SHORT).show()
            onSendResetLinkClick() // Return directly to Login Screen
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar with Floating Bubble Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Reset Password",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )
                        Text(
                            text = "Update your credentials securely.",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Arc Reactor Logo
                ArcReactorCanvas(
                    rotationAngle = 0f,
                    modifier = Modifier.size(68.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "DiaWell",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A1C7D),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error Message Callout
                if (errorMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                        )
                    }
                }

                // 1. Email Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Registered Email Address",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        placeholder = {
                            Text(
                                text = "you@example.com",
                                fontSize = 14.sp,
                                color = AppTheme.colors.textMuted,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = null,
                                tint = Color(0xFF9A96A4),
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.inputBackground,
                            unfocusedContainerColor = AppTheme.colors.inputBackground,
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = AppTheme.colors.inputBorder,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. New Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "New Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = ""
                        },
                        placeholder = {
                            Text(
                                text = "Enter new password",
                                fontSize = 14.sp,
                                color = AppTheme.colors.textMuted,
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isNewPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off,
                                    ),
                                    contentDescription = null,
                                    tint = Color(0xFF9A96A4),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.inputBackground,
                            unfocusedContainerColor = AppTheme.colors.inputBackground,
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = AppTheme.colors.inputBorder,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Confirm New Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Confirm New Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        placeholder = {
                            Text(
                                text = "Re-enter new password",
                                fontSize = 14.sp,
                                color = AppTheme.colors.textMuted,
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isConfirmPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off,
                                    ),
                                    contentDescription = null,
                                    tint = Color(0xFF9A96A4),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.inputBackground,
                            unfocusedContainerColor = AppTheme.colors.inputBackground,
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = AppTheme.colors.inputBorder,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reset & Update Password Button
                IosLiquidGlassButton(
                    text = "Update Password",
                    onClick = {
                        when {
                            email.isBlank() -> errorMessage = "Please enter your email address."
                            newPassword.isBlank() -> errorMessage = "Please enter your new password."
                            confirmPassword.isBlank() -> errorMessage = "Please confirm your new password."
                            newPassword != confirmPassword -> errorMessage = "New password and confirm password do not match!"
                            else -> isUpdatingPassword = true
                        }
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Back to Login text
                Text(
                    text = "Back to Login",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                    modifier = Modifier.clickable { onBackClick() },
                )
            }

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your personal information stays private.",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textMuted,
                )
            }
        }

        // ROTATING 3D SATURN AI SPEECH ORB LOADING OVERLAY FOR PASSWORD RESET
        AnimatedVisibility(
            visible = isUpdatingPassword,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.96f))
                    .clickable(enabled = false) {}, // Intercept clicks during loading
                contentAlignment = Alignment.Center,
            ) {
                AiSpeechOrbLoadingView(
                    statusText = "Updating & Securing Your Password...",
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    Diawell123Theme {
        ForgotPasswordScreen()
    }
}
