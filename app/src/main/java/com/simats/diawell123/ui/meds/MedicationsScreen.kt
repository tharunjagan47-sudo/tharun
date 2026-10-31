package com.simats.diawell123.ui.meds
import com.simats.diawell123.ui.theme.AppTheme

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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.MedicationItem
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun MedicationsScreen(
    modifier: Modifier = Modifier,
    viewModel: MedsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddMedicationClick: () -> Unit = {},
    onReminderClick: (MedicationItem) -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Meds") }
    val uiState by viewModel.listState.collectAsState()
    val medsData = uiState.data

    LaunchedEffect(Unit) {
        viewModel.loadMedications()
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
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar & Add Button (Pops Up)
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StyledBackButton(onBackClick = onBackClick)

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Your medications",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(
                                    text = medsData?.dateLabel?.takeIf { it.isNotBlank() }
                                        ?: "Reminders set according to your care plan.",
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textSecondary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add Medication Bar Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                                .border(1.dp, Color(0xFFB81D85), RoundedCornerShape(12.dp))
                                .clickable { onAddMedicationClick() }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+ Add medication or set reminder",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB81D85),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message banner
                if (uiState.errorMessage != null && medsData == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                            .clickable { viewModel.loadMedications(isPullToRefresh = true) }
                            .padding(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${uiState.errorMessage}",
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Retry ↻",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Adherence Quick Stats Card
                medsData?.stats?.let { stats ->
                    if (stats.total > 0) {
                        ElementPopUpContainer(delayMillis = 40) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppTheme.colors.cardBackground)
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "${stats.total}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                        Text(text = "Total Scheduled", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "${stats.taken}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        Text(text = "Taken", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "${stats.adherencePercent}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81D85))
                                        Text(text = "Adherence", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Loading Spinner
                if (uiState.isLoading && medsData == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFB81D85),
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Medication Cards (Pop Up Staggered)
                val medications = medsData?.medications ?: emptyList()

                if (!uiState.isLoading && medications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppTheme.colors.cardBackground)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "💊", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No medication reminders set",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add medication' above to add your daily tablets and configure schedules.",
                                fontSize = 12.sp,
                                color = AppTheme.colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    medications.forEachIndexed { index, item ->
                        ElementPopUpContainer(delayMillis = 80 * (index + 1)) {
                            MedicationScheduleCard(
                                time = item.reminderTime,
                                name = item.name,
                                dose = item.dose,
                                status = item.status,
                                statusColor = parseMedsHexColor(item.statusColor, Color(0xFFB81D85)),
                                statusBg = parseMedsHexColor(item.statusBg, Color(0xFFFDEEF7)),
                                dosageForm = item.dosageForm,
                                category = item.category.ifBlank { item.therapeuticClass },
                                primaryIngredient = item.primaryIngredient,
                                timing = item.timing,
                                onClick = {
                                    viewModel.setSelectedMedication(item)
                                    onReminderClick(item)
                                },
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Always consult your physician before changing doses.\nNotifications keep your daily routine consistent.",
                    fontSize = 11.sp,
                    color = AppTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 88.dp),
                )
            }
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock (Pops Up)
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = onHomeTabClick,
                onDietTabClick = onDietTabClick,
                onMedsTabClick = { selectedTab = "Meds" },
                onReportsTabClick = onReportsTabClick,
                onSettingsTabClick = onSettingsTabClick,
            )
        }
    }
}

@Composable
fun MedicationScheduleCard(
    time: String,
    name: String,
    dose: String,
    status: String,
    statusColor: Color,
    statusBg: Color,
    dosageForm: String = "Tablet",
    category: String = "",
    primaryIngredient: String = "",
    timing: String = "",
    onClick: () -> Unit = {},
) {
    val iconChar = when {
        dosageForm.contains("inject", ignoreCase = true) || name.contains("insulin", ignoreCase = true) -> "💉"
        dosageForm.contains("inhal", ignoreCase = true) -> "💨"
        dosageForm.contains("syrup", ignoreCase = true) || dosageForm.contains("liquid", ignoreCase = true) -> "💧"
        dosageForm.contains("cream", ignoreCase = true) || dosageForm.contains("gel", ignoreCase = true) -> "🧴"
        else -> "💊"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = iconChar, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = time,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                        if (timing.isNotBlank()) {
                            Text(
                                text = " • $timing",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textMuted,
                            )
                        }
                    }
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = dose,
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
                    )
                    if (primaryIngredient.isNotBlank() || category.isNotBlank()) {
                        val subInfo = listOf(
                            if (primaryIngredient.isNotBlank()) "Active: $primaryIngredient" else "",
                            category
                        ).filter { it.isNotBlank() }.joinToString(" • ")
                        Text(
                            text = subInfo,
                            fontSize = 10.5.sp,
                            color = Color(0xFF7A1C7D),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }
        }
    }
}

fun parseMedsHexColor(hexString: String?, fallback: Color): Color {
    if (hexString.isNullOrBlank()) return fallback
    return try {
        val clean = hexString.removePrefix("#").trim()
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else if (clean.length == 8) {
            Color(colorInt)
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicationsScreenPreview() {
    Diawell123Theme {
        MedicationsScreen()
    }
}
