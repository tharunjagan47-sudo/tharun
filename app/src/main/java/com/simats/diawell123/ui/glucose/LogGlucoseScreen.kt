package com.simats.diawell123.ui.glucose
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogGlucoseScreen(
    modifier: Modifier = Modifier,
    viewModel: GlucoseViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaveReadingClick: () -> Unit = {},
) {
    val uiState by viewModel.logState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(1000)
            viewModel.resetLogState()
            onSaveReadingClick()
        }
    }

    val readingTypes = listOf("Fasting", "After meal", "Before meal", "Bedtime", "Random")
    val glucosePresets = listOf("65" to "Low", "110" to "Target", "155" to "Elevated", "195" to "High")

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
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            ElementPopUpContainer(delayMillis = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Log Glucose",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )
                        Text(
                            text = "Keep an active record of your blood sugar.",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Success Confirmation Banner
            AnimatedVisibility(
                visible = uiState.isSuccess,
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
                                text = "Glucose Reading Saved!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your reading has been recorded and synced.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Error Banner
            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        fontSize = 12.sp,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Blood sugar reading Card (Dynamic Value Card)
            ElementPopUpContainer(delayMillis = 80) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Blood sugar reading",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = uiState.readingValue.ifEmpty { "0" },
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7A1C7D),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "mg/dL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7A1C7D),
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick presets row
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            glucosePresets.forEach { (presetVal, label) ->
                                val isSelected = uiState.readingValue == presetVal
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFBD1E85) else Color.White)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFFBD1E85) else AppTheme.colors.border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.onReadingValueChanged(presetVal) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "$presetVal ($label)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reading Type & Inputs
            ElementPopUpContainer(delayMillis = 160) {
                Column {
                    Text(
                        text = "Enter Value (mg/dL)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.readingValue,
                        onValueChange = { viewModel.onReadingValueChanged(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.inputBackground,
                            unfocusedContainerColor = AppTheme.colors.inputBackground,
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = AppTheme.colors.inputBorder,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Reading type",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        readingTypes.forEach { type ->
                            val isSelected = uiState.readingType.equals(type, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                colors = listOf(AppTheme.colors.secondaryBackground, AppTheme.colors.secondaryBackground),
                                            )
                                        },
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFBD1E85) else AppTheme.colors.border,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.onReadingTypeChanged(type) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date (YYYY-MM-DD)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.textPrimary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            OutlinedTextField(
                                value = uiState.readingDate,
                                onValueChange = { viewModel.onReadingDateChanged(it) },
                                singleLine = true,
                                trailingIcon = { Text(text = "📅", fontSize = 14.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = AppTheme.colors.inputBackground,
                                    unfocusedContainerColor = AppTheme.colors.inputBackground,
                                    focusedBorderColor = Color(0xFFBD1E85),
                                    unfocusedBorderColor = AppTheme.colors.inputBorder,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.textPrimary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            OutlinedTextField(
                                value = uiState.readingTime,
                                onValueChange = { viewModel.onReadingTimeChanged(it) },
                                singleLine = true,
                                trailingIcon = { Text(text = "⏰", fontSize = 14.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = AppTheme.colors.inputBackground,
                                    unfocusedContainerColor = AppTheme.colors.inputBackground,
                                    focusedBorderColor = Color(0xFFBD1E85),
                                    unfocusedBorderColor = AppTheme.colors.inputBorder,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes (optional)
                    Text(
                        text = "Notes (optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        placeholder = {
                            Text(
                                text = "e.g., Before breakfast, after 20m light walk...",
                                fontSize = 12.sp,
                                color = AppTheme.colors.textMuted,
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.inputBackground,
                            unfocusedContainerColor = AppTheme.colors.inputBackground,
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = AppTheme.colors.inputBorder,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Reading Button
            ElementPopUpContainer(delayMillis = 240) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFBD1E85)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    IosLiquidGlassButton(
                        text = "✓  Save Reading",
                        onClick = {
                            val vFloat = uiState.readingValue.toFloatOrNull() ?: 0f
                            viewModel.logGlucose(
                                readingValue = vFloat,
                                readingType = uiState.readingType,
                                readingDate = uiState.readingDate,
                                readingTime = uiState.readingTime,
                                notes = uiState.notes,
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Readings are for tracking, not a diagnosis.\nAlways review sustained high or low readings with your healthcare provider.",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}
