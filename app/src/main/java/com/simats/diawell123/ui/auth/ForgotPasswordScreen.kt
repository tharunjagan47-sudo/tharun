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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ArcReactorCanvas
import com.simats.diawell123.R
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSendResetLinkClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }

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
                Spacer(modifier = Modifier.height(16.dp))

                // Top Back Arrow Bar
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
                            text = "Forgot Password?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "Let's get you back to your daily routine.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Arc Reactor Logo
                ArcReactorCanvas(
                    rotationAngle = 0f,
                    modifier = Modifier.size(72.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "DiaWell",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A1C7D),
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Mail Envelope Card Graphic
                Box(
                    modifier = Modifier
                        .size(100.dp, 70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDEEF7)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = null,
                        tint = Color(0xFFB81D85),
                        modifier = Modifier.size(40.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Enter the email linked to your account. We'll send you a link to reset your password.",
                    fontSize = 13.sp,
                    color = Color(0xFF6E6877),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Email Input Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email Address",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF232226),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = {
                            Text(
                                text = "you@example.com",
                                fontSize = 14.sp,
                                color = Color(0xFFA09BAA),
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

                Spacer(modifier = Modifier.height(24.dp))

                // Send Reset Link Button
                IosLiquidGlassButton(
                    text = "Send Reset Link",
                    onClick = onSendResetLinkClick,
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
                    color = Color(0xFF8A8595),
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
