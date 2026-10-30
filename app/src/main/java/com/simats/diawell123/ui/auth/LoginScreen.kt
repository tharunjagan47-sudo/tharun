package com.simats.diawell123.ui.auth

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
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Check for auto-logout / session expiry notice on composition
    LaunchedEffect(Unit) {
        viewModel.checkExpiredReason()
    }

    // Handle Login Success Navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
            viewModel.resetState()
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Title Header
                Text(
                    text = "Welcome back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )

                Text(
                    text = "Let's make today a little healthier.",
                    fontSize = 13.sp,
                    color = Color(0xFF6E6877),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Arc Reactor Logo
                ArcReactorCanvas(
                    rotationAngle = 0f,
                    modifier = Modifier.size(76.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "DiaWell",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A1C7D),
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Email Field
                LoginInputField(
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

                // Password Field
                LoginInputField(
                    label = "Password",
                    value = password,
                    onValueChange = {
                        password = it
                        if (uiState.errorMessage != null) viewModel.clearError()
                    },
                    placeholder = "Enter your password",
                    isPassword = true,
                    isPasswordVisible = isPasswordVisible,
                    onPasswordToggleClick = { isPasswordVisible = !isPasswordVisible },
                    keyboardType = KeyboardType.Password,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Forgot Password link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "Forgot Password?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB81D85),
                        modifier = Modifier.clickable { onForgotPasswordClick() },
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Login Gradient Button
                IosLiquidGlassButton(
                    text = if (uiState.isLoading) "Logging in..." else "Login",
                    onClick = {
                        if (!uiState.isLoading) {
                            viewModel.onLogin(email, password)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                // New to DiaWell? Create Account link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "New to DiaWell? ",
                        fontSize = 13.sp,
                        color = Color(0xFF6E6877),
                    )
                    Text(
                        text = "Create Account",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        modifier = Modifier.clickable { onCreateAccountClick() },
                    )
                }
            }

            // Footer Disclaimer
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
                    color = Color(0xFF8A8595),
                )
            }
        }

        // ROTATING 3D SATURN AI SPEECH ORB LOADING OVERLAY FOR LOGIN
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
                    statusText = "Logging in & Setting up your Dashboard...",
                )
            }
        }
    }
}

@Composable
fun LoginInputField(
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
fun LoginScreenPreview() {
    Diawell123Theme {
        LoginScreen()
    }
}
