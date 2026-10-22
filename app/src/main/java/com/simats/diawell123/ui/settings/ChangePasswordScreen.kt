package com.simats.diawell123.ui.settings

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSavePasswordClick: () -> Unit = {},
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isCurrentPasswordVisible by remember { mutableStateOf(value = false) }
    var isNewPasswordVisible by remember { mutableStateOf(value = false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(value = false) }

    var showSuccessBanner by remember { mutableStateOf(value = false) }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onSavePasswordClick()
        }
    }

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
                        text = "Change Password",
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

            Spacer(modifier = Modifier.height(12.dp))

            // 1-Second Success Confirmation Banner
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
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✓", fontSize = 22.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Password Changed Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your security credentials have been updated.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔑", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Keep your DiaWell account safe with a strong, unique password.",
                        fontSize = 12.sp,
                        color = Color(0xFF811978),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Password
            PasswordInputField(
                label = "Current Password",
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = "Enter current password",
                isPasswordVisible = isCurrentPasswordVisible,
                onToggleVisibility = { isCurrentPasswordVisible = !isCurrentPasswordVisible },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            PasswordInputField(
                label = "New Password",
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "Enter new password",
                isPasswordVisible = isNewPasswordVisible,
                onToggleVisibility = { isNewPasswordVisible = !isNewPasswordVisible },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm New Password
            PasswordInputField(
                label = "Confirm New Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Re-enter new password",
                isPasswordVisible = isConfirmPasswordVisible,
                onToggleVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Security Guidelines Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "Password Requirements",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ At least 8 characters\n✓ Include numbers or symbols for better security",
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Update Password Button
            IosLiquidGlassButton(
                text = "Update Password",
                onClick = { showSuccessBanner = true },
            )

            Spacer(modifier = Modifier.height(24.dp))
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
            color = Color(0xFF232226),
            modifier = Modifier.padding(bottom = 6.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFFA09BAA),
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
                focusedContainerColor = Color(0xFFF8F7FA),
                unfocusedContainerColor = Color(0xFFF8F7FA),
                focusedBorderColor = Color(0xFFBD1E85),
                unfocusedBorderColor = Color(0xFFEFECEF),
                focusedTextColor = Color(0xFF1E1E22),
                unfocusedTextColor = Color(0xFF1E1E22),
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
