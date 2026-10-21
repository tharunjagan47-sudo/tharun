package com.simats.diawell123.ui.auth

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
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignUpSuccess: () -> Unit = {},
    onLogInClick: () -> Unit = {},
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(value = false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(value = false) }

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
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Create your account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Your wellbeing starts with a simple step.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6E6877),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Info Banner Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Gradient Icon Box
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7A1C7D),
                                    Color(0xFFBD1E85),
                                    Color(0xFFFF5722),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart_pulse),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "A little support for your meals, medication and daily habits.",
                    fontSize = 12.sp,
                    color = Color(0xFF6E6877),
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input: Full Name
            SignUpInputField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Enter your full name",
                trailingIconRes = R.drawable.ic_person,
                keyboardType = KeyboardType.Text,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Email Address
            SignUpInputField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "you@example.com",
                trailingIconRes = R.drawable.ic_email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Password
            SignUpInputField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Create a password",
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
                onValueChange = { confirmPassword = it },
                placeholder = "Re-enter your password",
                isPassword = true,
                isPasswordVisible = isConfirmPasswordVisible,
                onPasswordToggleClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                keyboardType = KeyboardType.Password,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Create Account Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7A1C7D),
                                Color(0xFFBD1E85),
                                Color(0xFFFF5722),
                            ),
                        ),
                    )
                    .clickable { onSignUpSuccess() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms & Privacy Note
            Text(
                text = "By creating an account, you agree to our\nTerms & Conditions and Privacy Policy.",
                fontSize = 12.sp,
                color = Color(0xFF6E6877),
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
                    color = Color(0xFF6E6877),
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
                disabledContainerColor = Color(0xFFF8F7FA),
                focusedBorderColor = Color(0xFFBD1E85),
                unfocusedBorderColor = Color(0xFFEFECEF),
                focusedTextColor = Color(0xFF1E1E22),
                unfocusedTextColor = Color(0xFF1E1E22),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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
