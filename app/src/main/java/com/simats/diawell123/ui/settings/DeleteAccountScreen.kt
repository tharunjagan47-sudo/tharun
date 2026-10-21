package com.simats.diawell123.ui.settings

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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun DeleteAccountScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onConfirmDeleteClick: () -> Unit = {},
) {
    var confirmText by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("I no longer need the app") }

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
                        text = "Delete Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                    )
                    Text(
                        text = "Settings",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trash Icon Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🗑️", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Are you sure you want to delete your account?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Warning Callout Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFEBEE))
                    .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    text = "⚠️ Warning: This action is permanent and cannot be undone. All your logged glucose readings, meal history, medication schedules, and account preferences will be permanently erased.",
                    fontSize = 12.sp,
                    color = Color(0xFFC62828),
                    lineHeight = 17.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reason Selection
            Text(
                text = "Please tell us why you're leaving:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                "I no longer need the app",
                "Privacy concerns",
                "Starting fresh with a new account",
                "Other reason",
            ).forEach { reason ->
                val isSelected = selectedReason == reason
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFFFDEEF7) else Color.White)
                        .border(1.dp, if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF), RoundedCornerShape(10.dp))
                        .clickable { selectedReason = reason }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = if (isSelected) "●  $reason" else "○  $reason",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFFB81D85) else Color(0xFF1E1E22),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirmation Input Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Type 'DELETE' to confirm:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    placeholder = {
                        Text(text = "Type DELETE here", fontSize = 13.sp, color = Color(0xFFA09BAA))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F7FA),
                        unfocusedContainerColor = Color(0xFFF8F7FA),
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = Color(0xFFEFECEF),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isConfirmed = confirmText.trim().equals("DELETE", ignoreCase = true)

            // Delete Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isConfirmed) Color(0xFFD32F2F) else Color(0xFFE57373).copy(alpha = 0.5f))
                    .clickable(enabled = isConfirmed) { onConfirmDeleteClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Permanently Delete Account",
                    fontSize = 14.sp,
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
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6E6877),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DeleteAccountScreenPreview() {
    Diawell123Theme {
        DeleteAccountScreen()
    }
}
