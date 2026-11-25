package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun DeleteAccountScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onConfirmDeleteClick: () -> Unit = {},
) {
    val deleteState by viewModel.deleteState.collectAsState()
    val context = LocalContext.current

    var confirmText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("I no longer need the app") }

    LaunchedEffect(deleteState.errorMessage) {
        deleteState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    text = "Delete Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trash Icon Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Are you sure you want to delete your account?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Warning Callout Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.errorBg)
                    .border(1.dp, AppTheme.colors.errorBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = AppTheme.colors.errorText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This action is permanent and cannot be undone. All your logged glucose readings, meal history, medication schedules, and account preferences will be permanently erased.",
                        fontSize = 12.sp,
                        color = AppTheme.colors.errorText,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (deleteState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.errorBg)
                        .border(1.dp, AppTheme.colors.errorBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = deleteState.errorMessage ?: "Deletion failed.",
                        fontSize = 12.sp,
                        color = AppTheme.colors.errorText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reason Selection
            Text(
                text = "Please tell us why you're leaving:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
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
                        .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF5F3FF)) else AppTheme.colors.cardBackground)
                        .border(1.dp, if (isSelected) Color(0xFFA855F7) else AppTheme.colors.border, RoundedCornerShape(10.dp))
                        .clickable {
                            selectedReason = reason
                            Toast.makeText(context, "Reason: $reason", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFA855F7) else AppTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFA855F7) else AppTheme.colors.textPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Confirm with your password:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    placeholder = {
                        Text(text = "Enter current password", fontSize = 13.sp, color = AppTheme.colors.textMuted)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary,
                        cursorColor = Color(0xFFD32F2F),
                        focusedContainerColor = AppTheme.colors.inputBackground,
                        unfocusedContainerColor = AppTheme.colors.inputBackground,
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = AppTheme.colors.inputBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirmation Input Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Type 'DELETE' to confirm:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    placeholder = {
                        Text(text = "Type DELETE here", fontSize = 13.sp, color = AppTheme.colors.textMuted)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary,
                        cursorColor = Color(0xFFD32F2F),
                        focusedContainerColor = AppTheme.colors.inputBackground,
                        unfocusedContainerColor = AppTheme.colors.inputBackground,
                        focusedBorderColor = Color(0xFFD32F2F),
                        unfocusedBorderColor = AppTheme.colors.inputBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isConfirmed = confirmText.trim().equals("DELETE", ignoreCase = true) && passwordText.isNotEmpty()

            // Delete Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isConfirmed && !deleteState.isDeleting) Color(0xFFD32F2F) else Color(0xFFE57373).copy(alpha = 0.5f))
                    .clickable(enabled = !deleteState.isDeleting) {
                        if (!isConfirmed) {
                            Toast.makeText(context, "Please enter your password and type 'DELETE' to confirm.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.deleteAccount(
                                password = passwordText,
                                reason = selectedReason,
                                onSuccess = {
                                    Toast.makeText(context, "Account permanently deleted. We're sorry to see you go.", Toast.LENGTH_LONG).show()
                                    onConfirmDeleteClick()
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (deleteState.isDeleting) "Deleting..." else "Delete Account",
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
                    .background(AppTheme.colors.cardBackground)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textSecondary,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
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
