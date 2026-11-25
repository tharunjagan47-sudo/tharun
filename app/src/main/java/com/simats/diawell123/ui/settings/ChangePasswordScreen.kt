package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSavePasswordClick: () -> Unit = {},
) {
    val passwordState by viewModel.passwordState.collectAsState()
    val context = LocalContext.current

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localValidationErr by remember { mutableStateOf<String?>(null) }

    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(passwordState.errorMessage) {
        passwordState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1200)
            showSuccessBanner = false
            onSavePasswordClick()
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
                    text = "Change Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Success Confirmation Banner
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
                        .background(AppTheme.colors.successBg)
                        .border(1.5.dp, AppTheme.colors.successBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = AppTheme.colors.successText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Password Changed Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.successText,
                            )
                            Text(
                                text = "Your security credentials have been updated.",
                                fontSize = 11.sp,
                                color = AppTheme.colors.successText,
                            )
                        }
                    }
                }
            }

            // Error Banner
            val errorText = localValidationErr ?: passwordState.errorMessage
            if (errorText != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppTheme.colors.errorBg)
                        .border(1.dp, AppTheme.colors.errorBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorText,
                        fontSize = 12.sp,
                        color = AppTheme.colors.errorText
                    )
                }
            }

            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Keep your DiaWell account safe with a strong, unique password.",
                        fontSize = 12.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Password
            PasswordInputField(
                label = "Current Password",
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    localValidationErr = null
                },
                placeholder = "Enter current password",
                isPasswordVisible = isCurrentPasswordVisible,
                onToggleVisibility = { isCurrentPasswordVisible = !isCurrentPasswordVisible },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            PasswordInputField(
                label = "New Password",
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    localValidationErr = null
                },
                placeholder = "Enter new password",
                isPasswordVisible = isNewPasswordVisible,
                onToggleVisibility = { isNewPasswordVisible = !isNewPasswordVisible },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm New Password
            PasswordInputField(
                label = "Confirm New Password",
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    localValidationErr = null
                },
                placeholder = "Re-enter new password",
                isPasswordVisible = isConfirmPasswordVisible,
                onToggleVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Security Guidelines Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardBackground)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "Password Requirements",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "At least 8 characters",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Include numbers or symbols for better security",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Update Password Button
            IosLiquidGlassButton(
                text = if (passwordState.isSubmitting) "Updating..." else "Update Password",
                onClick = {
                    when {
                        currentPassword.isBlank() -> {
                            localValidationErr = "Please enter your current password."
                            Toast.makeText(context, "Please enter your current password.", Toast.LENGTH_SHORT).show()
                        }
                        newPassword.length < 8 -> {
                            localValidationErr = "New password must be at least 8 characters long."
                            Toast.makeText(context, "New password must be at least 8 characters long.", Toast.LENGTH_SHORT).show()
                        }
                        newPassword != confirmPassword -> {
                            localValidationErr = "New password and confirmation do not match."
                            Toast.makeText(context, "New password and confirmation do not match.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            localValidationErr = null
                            viewModel.changePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword,
                                onSuccess = {
                                    showSuccessBanner = true
                                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.padding(bottom = 6.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = AppTheme.colors.textMuted,
                )
            },
            singleLine = true,
            visualTransformation = if (!isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off,
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
                focusedBorderColor = Color(0xFFA855F7),
                unfocusedBorderColor = AppTheme.colors.inputBorder,
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChangePasswordScreenPreview() {
    Diawell123Theme {
        ChangePasswordScreen()
    }
}
