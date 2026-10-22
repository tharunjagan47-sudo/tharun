package com.simats.diawell123.ui.settings

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
fun ExportRecordsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
) {
    var selectedTimeRange by remember { mutableStateOf("Last 30 Days") }
    var selectedFormat by remember { mutableStateOf("PDF") }

    var includeGlucose by remember { mutableStateOf(value = true) }
    var includeMeals by remember { mutableStateOf(value = true) }
    var includeMedications by remember { mutableStateOf(value = true) }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onExportClick()
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
                        text = "Export Records",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Download a copy of your health and log data.",
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
                                text = "Health Records Exported Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your report file has been generated.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Export your glucose readings, meal history, and medication records into accessible file formats.",
                        fontSize = 12.sp,
                        color = Color(0xFF811978),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Range Section
            Text(
                text = "Time Range",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Last 30 Days", "Last 3 Months", "All Time").forEach { range ->
                    val isSelected = selectedTimeRange == range
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { selectedTimeRange = range }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = range,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // File Format Section
            Text(
                text = "File Format",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ExportFormatCard(
                    title = "PDF Report",
                    subtitle = "📄 Printable summary for doctor",
                    isSelected = selectedFormat == "PDF",
                    onClick = { selectedFormat = "PDF" },
                    modifier = Modifier.weight(1f),
                )

                ExportFormatCard(
                    title = "CSV Data",
                    subtitle = "📊 Raw spreadsheet format",
                    isSelected = selectedFormat == "CSV",
                    onClick = { selectedFormat = "CSV" },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Options Section
            Text(
                text = "Include Data",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "Glucose Readings History",
                checked = includeGlucose,
                onCheckedChange = { includeGlucose = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "Meal & Nutrition Logs",
                checked = includeMeals,
                onCheckedChange = { includeMeals = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "Medication Schedules & Reminders",
                checked = includeMedications,
                onCheckedChange = { includeMedications = it },
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Export Button
            IosLiquidGlassButton(
                text = "Export & Share Report",
                onClick = { showSuccessBanner = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Exported files contain personal health data. Store or share them securely.",
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

@Composable
fun ExportFormatCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFFFDEEF7) else Color.White)
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFFB81D85) else Color(0xFF1E1E22),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF6E6877),
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
fun ExportCheckOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E22),
            )

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFB81D85),
                    uncheckedColor = Color(0xFF8A8595),
                ),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExportRecordsScreenPreview() {
    Diawell123Theme {
        ExportRecordsScreen()
    }
}
