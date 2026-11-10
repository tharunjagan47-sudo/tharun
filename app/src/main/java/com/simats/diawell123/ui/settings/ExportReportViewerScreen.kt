package com.simats.diawell123.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ArcReactorCanvas
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ExportReportViewerScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDoneClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val exportState by viewModel.exportState.collectAsState()
    val data = exportState.exportedData
    val options = exportState.options

    val patientName = data?.patient?.name?.ifBlank { ProfileState.userName } ?: ProfileState.userName
    val diabetesType = data?.patient?.diabetesType?.ifBlank { "Type 2" } ?: "Type 2"
    val ageText = data?.patient?.age?.takeIf { it > 0 }?.let { "Age $it" } ?: "Adult Patient"
    val periodLabel = options.timeRangeLabel
    val dateRangeSpan = if (data?.summary?.startDate?.isNotBlank() == true && data.summary.endDate.isNotBlank()) {
        "${data.summary.startDate} – ${data.summary.endDate}"
    } else {
        "Up to today"
    }
    val formatTag = if (options.format.equals("CSV", ignoreCase = true)) "CSV" else "PDF"
    val fileNameDisplay = exportState.generatedFileName ?: "${patientName.replace(" ", "_")}_Clinical_Report.${formatTag.lowercase()}"

    var actionBannerTitle by remember { mutableStateOf("") }
    var actionBannerDetail by remember { mutableStateOf("") }
    var actionBannerIsError by remember { mutableStateOf(false) }
    var showActionBanner by remember { mutableStateOf(false) }

    LaunchedEffect(showActionBanner) {
        if (showActionBanner) {
            delay(2400)
            showActionBanner = false
        }
    }

    fun handleActionFeedback(ok: Boolean, msg: String, titleOnSuccess: String) {
        actionBannerIsError = !ok
        actionBannerTitle = if (ok) titleOnSuccess else "Action Notice"
        actionBannerDetail = msg
        showActionBanner = true
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                        text = "Generated Health Report",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = fileNameDisplay,
                        fontSize = 11.5.sp,
                        color = AppTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Confirmation Banner
            AnimatedVisibility(
                visible = showActionBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                val bg = if (actionBannerIsError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                val fg = if (actionBannerIsError) Color(0xFFC62828) else Color(0xFF2E7D32)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .border(1.5.dp, fg, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (actionBannerIsError) "⚠️" else "✓",
                            fontSize = 20.sp,
                            color = fg,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = actionBannerTitle,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = fg,
                            )
                            Text(
                                text = actionBannerDetail,
                                fontSize = 11.sp,
                                color = fg,
                                lineHeight = 15.sp,
                            )
                        }
                    }
                }
            }

            // Clinical Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.cardBackground)
                    .border(1.5.dp, AppTheme.colors.border, RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            ArcReactorCanvas(
                                rotationAngle = 0f,
                                modifier = Modifier.size(34.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DiaWell Clinical Summary",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF7A1C7D),
                                )
                                Text(
                                    text = "$periodLabel • ${if (formatTag == "PDF") "Hospital PDF" else "CSV Telemetry"}",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textMuted,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Ready • $formatTag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PATIENT DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$patientName • $ageText • $diabetesType\nPeriod: $dateRangeSpan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                        lineHeight = 17.sp,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "LIVE CLINICAL SUMMARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.secondaryBackground)
                            .padding(12.dp),
                    ) {
                        val s = data?.summary
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (options.includeGlucose) {
                                val avgText = if (s != null && s.averageGlucose > 0f) {
                                    if (s.totalGlucoseReadings > 0) {
                                        "${s.averageGlucose.toInt()} mg/dL (${s.inTargetPercent}% In Target)"
                                    } else {
                                        "${s.averageGlucose.toInt()} mg/dL (Baseline)"
                                    }
                                } else {
                                    "No logs recorded"
                                }
                                ReportSummaryRow(label = "Average Blood Glucose", value = avgText)
                                if (s != null && s.estimatedHba1c > 0f) {
                                    ReportSummaryRow(label = "Estimated eHbA1c", value = "${s.estimatedHba1c}%")
                                }
                            }
                            if (options.includeMedications) {
                                val medText = if (s != null && s.totalMedications > 0) {
                                    "${s.takenMedications} of ${s.totalMedications} doses (${s.adherencePercent}%)"
                                } else {
                                    "No medications scheduled"
                                }
                                ReportSummaryRow(label = "Medication Adherence", value = medText)
                            }
                            if (options.includeMeals) {
                                val mealText = if (s != null && s.totalMealsLogged > 0) {
                                    "${s.totalMealsLogged} meals (${s.averageDailyCalories} kcal/day avg)"
                                } else {
                                    "No meals logged"
                                }
                                ReportSummaryRow(label = "Meals Logged", value = mealText)
                            }
                            if (options.includeActivity) {
                                val actText = if (s != null && s.totalWorkouts > 0) {
                                    "${s.totalWorkouts} sessions (${s.totalActivityMins} mins)"
                                } else {
                                    "No workouts logged"
                                }
                                ReportSummaryRow(label = "Physical Activity", value = actText)
                            }
                            ReportSummaryRow(
                                label = "Clinical Status",
                                value = s?.careStatus?.ifBlank { "Ready for clinical review" } ?: "Ready for clinical review"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (2x2 Grid so buttons never truncate on narrow phones)
            Text(
                text = "Report Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ReportActionButton(
                        icon = "📂",
                        label = "Open $formatTag",
                        onClick = {
                            viewModel.openGeneratedReport(context) { ok, msg ->
                                handleActionFeedback(ok, msg, "Opening Document")
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )

                    ReportActionButton(
                        icon = "💾",
                        label = "Save to Downloads",
                        onClick = {
                            viewModel.saveGeneratedReportToDownloads(context) { ok, msg ->
                                handleActionFeedback(ok, msg, "$formatTag Saved to Downloads!")
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ReportActionButton(
                        icon = "📤",
                        label = "Share / Email",
                        onClick = {
                            viewModel.shareGeneratedReport(context) { ok, msg ->
                                handleActionFeedback(ok, msg, "Share Options Ready")
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )

                    ReportActionButton(
                        icon = "🖨️",
                        label = if (formatTag == "PDF") "Print PDF" else "Send CSV",
                        onClick = {
                            viewModel.printGeneratedReport(context) { ok, msg ->
                                handleActionFeedback(ok, msg, "Sent to Print Service!")
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Live Document Page Preview
            if (exportState.previewBitmaps.isNotEmpty() || !exportState.csvPreviewText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (formatTag == "PDF") "Rendered PDF Document Preview" else "CSV Telemetry Preview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    if (exportState.previewBitmaps.isNotEmpty()) {
                        Text(
                            text = "${exportState.previewBitmaps.size} page(s)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB81D85),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (exportState.previewBitmaps.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        exportState.previewBitmaps.forEachIndexed { idx, bmp ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.openGeneratedReport(context) { ok, msg ->
                                            if (!ok) handleActionFeedback(false, msg, "")
                                        }
                                    }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Report Page ${idx + 1}",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(bmp.width.toFloat() / bmp.height.coerceAtLeast(1).toFloat())
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Page ${idx + 1} of ${exportState.previewBitmaps.size} • Tap to Open Full Viewer",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B),
                                )
                            }
                        }
                    }
                } else if (!exportState.csvPreviewText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = exportState.csvPreviewText ?: "",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AppTheme.colors.textPrimary,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Done Button
            IosLiquidGlassButton(
                text = "✓  Done",
                onClick = {
                    onDoneClick()
                },
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ReportSummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun ReportActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExportReportViewerScreenPreview() {
    Diawell123Theme {
        ExportReportViewerScreen()
    }
}
