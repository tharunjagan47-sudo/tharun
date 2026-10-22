package com.simats.diawell123.ui.glucose

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun LogGlucoseScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveReadingClick: () -> Unit = {},
) {
    var readingValue by remember { mutableStateOf("128") }
    var readingType by remember { mutableStateOf("Fasting") }
    var readingDate by remember { mutableStateOf("15 Oct 2026") }
    var readingTime by remember { mutableStateOf("7:30 AM") }
    var notes by remember { mutableStateOf("Before breakfast, after an overnight fast.") }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onSaveReadingClick()
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
                        text = "Log glucose",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Keep a record of your daily readings.",
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
                                text = "Glucose Reading Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your reading has been added to your log.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Demo profile • Sample data",
                    fontSize = 11.sp,
                    color = Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Blood sugar reading Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Blood sugar reading",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF811978),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = readingValue,
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reading Type
            Text(
                text = "Reading type",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf("Fasting", "After meal").forEach { type ->
                    val isSelected = readingType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFF8F7FA), Color(0xFFF8F7FA)),
                                    )
                                },
                            )
                            .clickable { readingType = type },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = type,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF6E6877),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF232226),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    OutlinedTextField(
                        value = readingDate,
                        onValueChange = { readingDate = it },
                        singleLine = true,
                        trailingIcon = { Text(text = "📅", fontSize = 14.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8F7FA),
                            unfocusedContainerColor = Color(0xFFF8F7FA),
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = Color(0xFFEFECEF),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF232226),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    OutlinedTextField(
                        value = readingTime,
                        onValueChange = { readingTime = it },
                        singleLine = true,
                        trailingIcon = { Text(text = "⏰", fontSize = 14.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8F7FA),
                            unfocusedContainerColor = Color(0xFFF8F7FA),
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = Color(0xFFEFECEF),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes (optional)
            Text(
                text = "Notes (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF232226),
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Reading Button
            IosLiquidGlassButton(
                text = "✓  Save Reading",
                onClick = { showSuccessBanner = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Readings are for tracking, not a diagnosis.\nChoose your personal targets with your care team.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LogGlucoseScreenPreview() {
    Diawell123Theme {
        LogGlucoseScreen()
    }
}
