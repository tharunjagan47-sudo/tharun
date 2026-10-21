package com.simats.diawell123.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun LogoutConfirmationDialog(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit = {},
    onConfirmLogoutClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Exit Icon Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDEEF7)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "➔", fontSize = 28.sp, color = Color(0xFFB81D85))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Are you sure you want to log out?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                Text(
                    text = "Your saved records will stay safe. You can log back in whenever you're ready.",
                    fontSize = 13.sp,
                    color = Color(0xFF6E6877),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Logout Gradient Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85), Color(0xFFFF5722)),
                            ),
                        )
                        .clickable { onConfirmLogoutClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Log Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cancel Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                        .clickable { onCancelClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6E6877),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A little support, whenever you need it.",
                    fontSize = 11.sp,
                    color = Color(0xFF8A8595),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LogoutConfirmationDialogPreview() {
    Diawell123Theme {
        LogoutConfirmationDialog()
    }
}
