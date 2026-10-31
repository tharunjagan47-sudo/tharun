package com.simats.diawell123.ui.auth
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ArcReactorCanvas
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.profile.AiSpeechOrbLoadingView
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onLogInClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Handle SignUp Success Navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignUpSuccess()
            viewModel.resetState()
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
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Top Header Row with Title on Left & Arc Reactor Logo on Top Right Corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Create your account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your wellbeing starts with a simple step.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = AppTheme.colors.textSecondary,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Arc Reactor Logo in Top Right Corner
                ArcReactorCanvas(
                    rotationAngle = 0f,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info Banner Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArcReactorCanvas(
                    rotationAngle = 0f,
                    modifier = Modifier.size(36.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "A little support for your meals, medication and daily habits.",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Error Banner
            AnimatedVisibility(
                visible = !uiState.errorMessage.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                uiState.errorMessage?.let { errorText ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE8E8))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shield_check),
                                contentDescription = "Alert",
                                tint = Color(0xFFC53030),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9B1C1C),
                                lineHeight = 18.sp,
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9B1C1C),
                            )
                        }
                    }
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Input: Full Name
            SignUpInputField(
                label = "Full Name",
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                placeholder = "Enter your full name",
                trailingIconRes = R.drawable.ic_person,
                keyboardType = KeyboardType.Text,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Email Address
            SignUpInputField(
                label = "Email Address",
                value = email,
                onValueChange = {
                    email = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                placeholder = "you@example.com",
                trailingIconRes = R.drawable.ic_email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Password
            SignUpInputField(
                label = "Password",
                value = password,
                onValueChange = {
                    password = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                placeholder = "Create a password (min. 8 characters)",
                isPassword = true,
                isPasswordVisible = isPasswordVisible,
                onPasswordToggleClick = { isPasswordVisible = !isPasswordVisible },
                keyboardType = KeyboardType.Password,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Confirm Password
            SignUpInputField(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                placeholder = "Re-enter your password",
                isPassword = true,
                isPasswordVisible = isConfirmPasswordVisible,
                onPasswordToggleClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                keyboardType = KeyboardType.Password,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Create Account Button
            IosLiquidGlassButton(
                text = if (uiState.isLoading) "Creating Account..." else "Create Account",
                onClick = {
                    if (!uiState.isLoading) {
                        viewModel.onRegister(
                            name = fullName,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms & Privacy Note
            Text(
                text = "By creating an account, you agree to our\nTerms & Conditions and Privacy Policy.",
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login Footer Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = AppTheme.colors.textSecondary,
                )
                Text(
                    text = "Log In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                    modifier = Modifier.clickable { onLogInClick() },
                )
            }
        }

        // ROTATING 3D SATURN AI SPEECH ORB LOADING OVERLAY FOR SIGNUP
        AnimatedVisibility(
            visible = uiState.isLoading,
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
                    statusText = "Creating your account & preparing profile...",
                )
            }
        }
    }
}

@Composable
fun SignUpInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIconRes: Int? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordToggleClick: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
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
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = {
                when {
                    isPassword -> {
                        IconButton(onClick = onPasswordToggleClick) {
                            Icon(
                                painter = painterResource(
                                    id = if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off,
                                ),
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF9A96A4),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    trailingIconRes != null -> {
                        Icon(
                            painter = painterResource(id = trailingIconRes),
                            contentDescription = null,
                            tint = Color(0xFF9A96A4),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppTheme.colors.inputBackground,
                unfocusedContainerColor = AppTheme.colors.inputBackground,
                disabledContainerColor = AppTheme.colors.secondaryBackground,
                focusedBorderColor = Color(0xFFBD1E85),
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
fun SignUpScreenPreview() {
    Diawell123Theme {
        SignUpScreen()
    }
}
