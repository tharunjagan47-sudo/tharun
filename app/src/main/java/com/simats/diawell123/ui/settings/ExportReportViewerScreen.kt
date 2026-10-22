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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.simats.diawell123.ArcReactorCanvas
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@Composable
fun ExportReportViewerScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDoneClick: () -> Unit = {},
) {
    var shareActionStatus by remember { mutableStateOf("") }
    var showShareBanner by remember { mutableStateOf(value = false) }

    LaunchedEffect(showShareBanner) {
        if (showShareBanner) {
            delay(1500)
            showShareBanner = false
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
                        text = "Generated Health Report",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Maya_Johnson_DiaWell_Report.pdf",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share Action Confirmation Banner
            AnimatedVisibility(
                visible = showShareBanner,
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
                                text = shareActionStatus,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Action completed for Maya_Johnson_DiaWell_Report.pdf",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // PDF Document Card Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFEFECEF), RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ArcReactorCanvas(
                                rotationAngle = 0f,
                                modifier = Modifier.size(36.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DiaWell Health Summary",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7A1C7D),
                                )
                                Text(
                                    text = "Clinical PDF Report",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8A8595),
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
                                text = "Ready • PDF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "PATIENT DETAILS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Maya Johnson • Age 30 • Type 2 Diabetes\nPeriod: 8 Oct 2026 – 15 Oct 2026",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E22),
                        lineHeight = 17.sp,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "EXECUTIVE SUMMARY",
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
                            .background(Color(0xFFF8F7FA))
                            .padding(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReportSummaryRow(label = "Average Fasting Glucose", value = "120 mg/dL (In Target)")
                            ReportSummaryRow(label = "Meals Logged", value = "25 of 28 (89%)")
                            ReportSummaryRow(label = "Medication Adherence", value = "19 of 21 Doses (90%)")
                            ReportSummaryRow(label = "Care Status", value = "Stable • Consistent Routine")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Care Note: Logged regularly with high adherence. Shared for clinical review with doctor.",
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Grid
            Text(
                text = "Share or Save Report",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReportActionButton(
                    icon = "📤",
                    label = "Share / Email",
                    onClick = {
                        shareActionStatus = "Report Shared via Email!"
                        showShareBanner = true
                    },
                    modifier = Modifier.weight(1f),
                )

                ReportActionButton(
                    icon = "💾",
                    label = "Download PDF",
                    onClick = {
                        shareActionStatus = "PDF Saved to Downloads!"
                        showShareBanner = true
                    },
                    modifier = Modifier.weight(1f),
                )

                ReportActionButton(
                    icon = "🖨️",
                    label = "Print Report",
                    onClick = {
                        shareActionStatus = "Sent to Printer!"
                        showShareBanner = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Done Button
            IosLiquidGlassButton(
                text = "✓  Done",
                onClick = onDoneClick,
            )

            Spacer(modifier = Modifier.height(24.dp))
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
        Text(text = label, fontSize = 11.sp, color = Color(0xFF6E6877))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E22))
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
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
                textAlign = TextAlign.Center,
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
