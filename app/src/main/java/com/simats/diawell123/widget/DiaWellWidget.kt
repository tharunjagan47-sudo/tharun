package com.simats.diawell123.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.simats.diawell123.MainActivity
import com.simats.diawell123.R

/**
 * Sleek, dark-themed Home Screen Widget for DiaWell built with Jetpack Glance.
 * Matches the "Liquid Glass" Obsidian Purple / Medical Violet aesthetic.
 *
 * Displays:
 * 1. Latest Glucose Reading with color-coded status badge & relative timestamp.
 * 2. Next Upcoming Medication Reminder or "All caught up" state.
 * 3. One-tap "+ Log" quick action to open glucose logging screen directly.
 * 4. Full widget clickability to open DiaWell home dashboard.
 */
class DiaWellWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = DiaWellWidgetDataManager.getWidgetData(context)

        provideContent {
            DiaWellWidgetContent(data = data)
        }
    }
}

private val TargetScreenKey = ActionParameters.Key<String>("target_screen")

@Composable
fun DiaWellWidgetContent(data: DiaWellWidgetData) {
    val openHomeAction = actionStartActivity<MainActivity>(
        actionParametersOf(TargetScreenKey to "home")
    )
    val openLogGlucoseAction = actionStartActivity<MainActivity>(
        actionParametersOf(TargetScreenKey to "log_glucose")
    )
    val openMedicationsAction = actionStartActivity<MainActivity>(
        actionParametersOf(TargetScreenKey to "medications")
    )

    // Outer Liquid Glass Container (Obsidian Purple surface with 20.dp rounded corners)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0F0B1A))
            .cornerRadius(20.dp)
            .padding(12.dp)
            .clickable(openHomeAction)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header Bar: DiaWell Branding + "+ Log" Quick-Action Button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo / Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_blood_drop),
                        contentDescription = "DiaWell",
                        modifier = GlanceModifier.size(15.dp)
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = "DIAWELL",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFC084FC)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // "+ Log" Action Button
                Box(
                    modifier = GlanceModifier
                        .background(Color(0xFF7E22CE))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable(openLogGlucoseAction),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_add),
                            contentDescription = "Log Glucose",
                            modifier = GlanceModifier.size(11.dp)
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "Log",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // Body Area: Two Cards Side-by-Side (Glucose Reading & Next Medication)
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Card: Latest Glucose Reading
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .background(Color(0xFF1B132E))
                        .cornerRadius(14.dp)
                        .padding(8.dp)
                        .clickable(openLogGlucoseAction)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Section Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_blood_drop),
                                contentDescription = null,
                                modifier = GlanceModifier.size(12.dp)
                            )
                            Spacer(GlanceModifier.width(4.dp))
                            Text(
                                text = "Blood Sugar",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFA1A1AA)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(GlanceModifier.height(4.dp))

                        if (data.hasGlucoseReading) {
                            // Value & unit
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = data.glucoseValue,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(GlanceModifier.width(3.dp))
                                Text(
                                    text = data.glucoseUnit,
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFFA1A1AA)),
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Spacer(GlanceModifier.height(4.dp))

                            // Status pill & relative time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (statusBg, statusFg) = when (data.glucoseStatus.lowercase()) {
                                    "low", "hypoglycemia" -> Color(0xFF7F1D1D) to Color(0xFFF87171)
                                    "high", "hyperglycemia", "elevated" -> Color(0xFF78350F) to Color(0xFFFBBF24)
                                    else -> Color(0xFF064E3B) to Color(0xFF34D399)
                                }
                                Box(
                                    modifier = GlanceModifier
                                        .background(statusBg)
                                        .cornerRadius(6.dp)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = data.glucoseStatus.ifBlank { "Normal" },
                                        style = TextStyle(
                                            color = ColorProvider(statusFg),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                if (data.glucoseTime.isNotBlank()) {
                                    Spacer(GlanceModifier.width(4.dp))
                                    Text(
                                        text = data.glucoseTime,
                                        style = TextStyle(
                                            color = ColorProvider(Color(0xFF71717A)),
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        } else {
                            // Empty state
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = "No logs yet",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFE4E4E7)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = "Tap + to record",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF71717A)),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.width(8.dp))

                // Right Card: Next Medication Reminder
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .background(Color(0xFF1B132E))
                        .cornerRadius(14.dp)
                        .padding(8.dp)
                        .clickable(openMedicationsAction)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Section Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_medication),
                                contentDescription = null,
                                modifier = GlanceModifier.size(12.dp)
                            )
                            Spacer(GlanceModifier.width(4.dp))
                            Text(
                                text = "Next Medicine",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFA1A1AA)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(GlanceModifier.height(4.dp))

                        if (data.hasUpcomingMed) {
                            Text(
                                text = data.nextMedName,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )

                            Spacer(GlanceModifier.height(2.dp))

                            val doseLabel = if (data.nextMedDose.isNotBlank()) "${data.nextMedDose} • " else ""
                            Text(
                                text = "$doseLabel${data.nextMedTime}",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFC084FC)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )

                            Spacer(GlanceModifier.height(4.dp))

                            Box(
                                modifier = GlanceModifier
                                    .background(Color(0xFF3B0764))
                                    .cornerRadius(6.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Scheduled",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFFF0ABFC)),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } else {
                            // All caught up state
                            Spacer(GlanceModifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_widget_check_circle),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(12.dp)
                                )
                                Spacer(GlanceModifier.width(4.dp))
                                Text(
                                    text = "All caught up",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF34D399)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = "All doses taken today",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF71717A)),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
