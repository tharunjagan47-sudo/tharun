package com.simats.diawell123.ui.profile
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun PatientProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onContinueClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var age by remember { mutableStateOf("30 years") }
    var selectedDiabetesType by remember { mutableStateOf("Type 2") }
    var height by remember { mutableStateOf("165 cm") }
    var weight by remember { mutableStateOf("68 kg") }
    var currentGlucoseReading by remember { mutableStateOf("128 mg/dL") }
    var targetFastingGlucose by remember { mutableStateOf("120 mg/dL") }

    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { p ->
            p.diabetesType?.let { selectedDiabetesType = it }
            p.age?.let { age = "$it years" }
            p.heightCm?.let { height = "${it.toInt()} cm" }
            p.weightKg?.let { weight = "${it.toInt()} kg" }
        }
    }

    val ageRange = remember { (18..80).map { "$it years" } }
    val heightRange = remember { (140..210).filter { it % 5 == 0 }.map { "$it cm" } }
    val weightRange = remember { (40..140).filter { it % 5 == 0 || it == 68 }.map { "$it kg" } }
    val glucoseRange = remember { (70..220).filter { it % 10 == 0 || it == 128 }.map { "$it mg/dL" } }
    val targetGlucoseRange = remember { (70..180).filter { it % 10 == 0 }.map { "$it mg/dL" } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Make it personal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Text(
                text = "Scroll and select your patient profile details below.",
                fontSize = 13.sp,
                color = AppTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Progress
            Text(
                text = "PATIENT PROFILE • STEP 1 OF 2",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
                letterSpacing = 0.5.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { 0.5f },
                color = Color(0xFFB81D85),
                trackColor = AppTheme.colors.border,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Profile • ${ProfileState.userName}",
                    fontSize = 12.sp,
                    color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to save details.",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Diabetes Type iOS Liquid Glass Slider
            IosLiquidGlassSlider(
                label = "Diabetes Type",
                items = listOf("Type 1", "Type 2", "Gestational", "Pre-diabetes", "LADA / Other"),
                selectedItem = selectedDiabetesType,
                onItemSelected = { selectedDiabetesType = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Current Glucose Level iOS Liquid Glass Slider
            IosLiquidGlassSlider(
                label = "Current Glucose Reading",
                items = glucoseRange,
                selectedItem = if (currentGlucoseReading.contains("mg/dL")) currentGlucoseReading else "$currentGlucoseReading mg/dL",
                onItemSelected = { currentGlucoseReading = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Target Fasting Glucose iOS Liquid Glass Slider
            IosLiquidGlassSlider(
                label = "Target Fasting Glucose",
                items = targetGlucoseRange,
                selectedItem = if (targetFastingGlucose.contains("mg/dL")) targetFastingGlucose else "$targetFastingGlucose mg/dL",
                onItemSelected = { targetFastingGlucose = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Age iOS Liquid Glass Slider
            IosLiquidGlassSlider(
                label = "Age",
                items = ageRange,
                selectedItem = if (age.contains("years")) age else "$age years",
                onItemSelected = { age = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Height iOS Liquid Glass Slider
            IosLiquidGlassSlider(
                label = "Height",
                items = heightRange,
                selectedItem = if (height.contains("cm")) height else "$height cm",
                onItemSelected = { height = it },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Weight iOS Liquid Glass Slider
            IosLiquidGlassSlider(
                label = "Weight",
                items = weightRange,
                selectedItem = if (weight.contains("kg")) weight else "$weight kg",
                onItemSelected = { weight = it },
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Continue Button
            IosLiquidGlassButton(
                text = if (uiState.isSaving) "Saving Details..." else "→  Continue to Step 2 (Health Preferences)",
                onClick = {
                    val ageNum = age.replace(Regex("[^0-9]"), "").toIntOrNull()
                    val heightNum = height.replace(Regex("[^0-9.]"), "").toFloatOrNull()
                    val weightNum = weight.replace(Regex("[^0-9.]"), "").toFloatOrNull()
                    val currentGlucoseNum = currentGlucoseReading.replace(Regex("[^0-9.]"), "").toFloatOrNull()
                    val targetGlucoseNum = targetFastingGlucose.replace(Regex("[^0-9.]"), "").toFloatOrNull()

                    viewModel.savePatientDetails(
                        diabetesType = selectedDiabetesType,
                        age = ageNum,
                        heightCm = heightNum,
                        weightKg = weightNum,
                        currentGlucose = currentGlucoseNum,
                        targetGlucose = targetGlucoseNum,
                        onSuccess = onContinueClick
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Our suggestions support your outline, not a diagnosis.\nFollow your healthcare professional's advice.",
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

@Composable
fun IosLiquidGlassSlider(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = selectedItem,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Outer iOS Liquid Glass Capsule Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0xFF811978).copy(alpha = 0.15f),
                    spotColor = Color(0xFF811978).copy(alpha = 0.20f),
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.90f),
                            Color(0xFFF3EAF4).copy(alpha = 0.85f),
                        ),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.4f),
                        ),
                    ),
                    shape = RoundedCornerShape(32.dp),
                )
                .padding(vertical = 8.dp, horizontal = 6.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // iOS Handle Bar Pill at Top Center
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFC7BDC9)),
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(items) { item ->
                        val isSelected = item == selectedItem

                        if (isSelected) {
                            // Selected Liquid Glass Pill
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = Color(0xFFB81D85).copy(alpha = 0.3f),
                                        spotColor = Color(0xFFB81D85).copy(alpha = 0.4f),
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFD81B60),
                                                Color(0xFF8E24AA),
                                            ),
                                        ),
                                    )
                                    .border(
                                        width = 1.2.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.8f),
                                                Color.White.copy(alpha = 0.2f),
                                            ),
                                        ),
                                        shape = RoundedCornerShape(24.dp),
                                    )
                                    .clickable { onItemSelected(item) }
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        } else {
                            // Unselected Frosted Glass Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(24.dp),
                                    )
                                    .clickable { onItemSelected(item) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PatientProfileScreenPreview() {
    Diawell123Theme {
        PatientProfileScreen()
    }
}
