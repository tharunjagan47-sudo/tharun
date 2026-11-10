package com.simats.diawell123.ui.settings

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.ExportReportOptions
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ExportRecordsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
) {
    val exportState by viewModel.exportState.collectAsState()
    val context = LocalContext.current

    var selectedTimeRange by remember { mutableStateOf("Last 30 Days") }
    var selectedFormat by remember { mutableStateOf("PDF") }

    var includeGlucose by remember { mutableStateOf(true) }
    var includeMeals by remember { mutableStateOf(true) }
    var includeMedications by remember { mutableStateOf(true) }
    var includeActivity by remember { mutableStateOf(true) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(exportState.errorMessage) {
        exportState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(500)
            showSuccessBanner = false
            onExportClick()
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(4.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Export Clinical Records",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Generate hospital-grade PDF or CSV health summaries.",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Success Confirmation Banner
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clinical Report Ready!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Opening live document preview...",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            if (exportState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(14.dp))
                        .clickable { viewModel.clearExportError() }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ ${exportState.errorMessage}",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F),
                        lineHeight = 16.sp,
                    )
                }
            }

            // Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏥", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Compiles your real MySQL glucose logs, medications, meals, and workouts into a doctor-ready clinical summary.",
                        fontSize = 12.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Range Section (7 Days, 30 Days, 3 Months, All Time)
            Text(
                text = "Time Range",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Last 7 Days", "Last 30 Days").forEach { range ->
                        TimeRangeChip(
                            label = range,
                            isSelected = selectedTimeRange == range,
                            onClick = { selectedTimeRange = range },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Last 3 Months", "All Time").forEach { range ->
                        TimeRangeChip(
                            label = range,
                            isSelected = selectedTimeRange == range,
                            onClick = { selectedTimeRange = range },
                            modifier = Modifier.weight(1f),
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
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ExportFormatCard(
                    title = "PDF Report",
                    subtitle = "📄 Hospital-grade multi-page report",
                    isSelected = selectedFormat == "PDF",
                    onClick = { selectedFormat = "PDF" },
                    modifier = Modifier.weight(1f),
                )

                ExportFormatCard(
                    title = "CSV Data",
                    subtitle = "📊 Spreadsheet-ready raw telemetry",
                    isSelected = selectedFormat == "CSV",
                    onClick = { selectedFormat = "CSV" },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Options Section
            Text(
                text = "Include Sections",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "🩸  Blood Glucose Readings History",
                checked = includeGlucose,
                onCheckedChange = { includeGlucose = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "💊  Medication Regimen & Adherence",
                checked = includeMedications,
                onCheckedChange = { includeMedications = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "🥗  Diet & Nutrition Logs",
                checked = includeMeals,
                onCheckedChange = { includeMeals = it },
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExportCheckOption(
                label = "🏃  Physical Activity & Exercise Logs",
                checked = includeActivity,
                onCheckedChange = { includeActivity = it },
            )

            Spacer(modifier = Modifier.height(26.dp))

            val daysParam = when (selectedTimeRange) {
                "Last 7 Days" -> "7"
                "Last 30 Days" -> "30"
                "Last 3 Months" -> "90"
                else -> "all"
            }

            IosLiquidGlassButton(
                text = if (exportState.isLoading) "Generating $selectedFormat Report..." else "Generate & Preview Report",
                onClick = {
                    if (exportState.isLoading) return@IosLiquidGlassButton
                    if (!includeGlucose && !includeMeals && !includeMedications && !includeActivity) {
                        Toast.makeText(context, "Select at least one section to export.", Toast.LENGTH_SHORT).show()
                        return@IosLiquidGlassButton
                    }

                    val options = ExportReportOptions(
                        timeRangeLabel = selectedTimeRange,
                        daysParam = daysParam,
                        format = selectedFormat,
                        includeGlucose = includeGlucose,
                        includeMeals = includeMeals,
                        includeMedications = includeMedications,
                        includeActivity = includeActivity
                    )
                    viewModel.generateAndExportReport(
                        context = context,
                        options = options,
                        onSuccess = {
                            showSuccessBanner = true
                        }
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Generated clinical reports stay private on your device unless you choose to share them.",
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
private fun TimeRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)
                } else {
                    AppTheme.colors.secondaryBackground
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
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
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) {
                    if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)
                } else {
                    AppTheme.colors.cardBackground
                }
            )
            .border(
                width = 1.2.dp,
                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = AppTheme.colors.textSecondary,
                lineHeight = 14.sp,
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
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFB81D85),
                    uncheckedColor = AppTheme.colors.textMuted,
                    checkmarkColor = Color.White,
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
