package com.simats.diawell123.ui.meds

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.simats.diawell123.ui.theme.Diawell123Theme
import com.simats.diawell123.util.DiaWellNotificationHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-Screen Interactive Medication Alarm Activity.
 * Displays over the lock screen and wakes up the display when scheduled medication time arrives.
 */
class MedAlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn on screen and show over Android Lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val medId = intent.getIntExtra("med_id", 1001)
        val medName = intent.getStringExtra("med_name") ?: "Medication"
        val medDosage = intent.getStringExtra("med_dosage") ?: "500 mg • 1 Tablet"
        val scheduledTime = intent.getStringExtra("scheduled_time") ?: SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        setContent {
            Diawell123Theme(darkTheme = true) {
                MedAlarmScreen(
                    medId = medId,
                    medName = medName,
                    medDosage = medDosage,
                    scheduledTime = scheduledTime,
                    onTaken = {
                        DiaWellNotificationHelper.stopAlarmSound(this)
                        NotificationManagerCompat.from(this).cancel(medId)
                        DiaWellNotificationHelper.showGeneralNotification(
                            context = this,
                            title = "✅ Dose Completed",
                            message = "Great job taking $medName on time!"
                        )
                        Toast.makeText(this, "Marked as Taken ✅", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onSnooze = {
                        DiaWellNotificationHelper.stopAlarmSound(this)
                        NotificationManagerCompat.from(this).cancel(medId)
                        DiaWellNotificationHelper.snoozeMedicationAlarm(this, medId, medName, medDosage, 5)
                        DiaWellNotificationHelper.showGeneralNotification(
                            context = this,
                            title = "⏰ Medication Snoozed",
                            message = "We will remind you to take $medName again in 5 minutes."
                        )
                        Toast.makeText(this, "Snoozed for 5 minutes ⏰", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onSkip = {
                        DiaWellNotificationHelper.stopAlarmSound(this)
                        NotificationManagerCompat.from(this).cancel(medId)
                        Toast.makeText(this, "Medication Skipped", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DiaWellNotificationHelper.stopAlarmSound(this)
        try {
            val medId = intent.getIntExtra("med_id", 1001)
            NotificationManagerCompat.from(this).cancel(medId)
        } catch (_: Exception) {}
    }
}

@Composable
fun MedAlarmScreen(
    medId: Int,
    medName: String,
    medDosage: String,
    scheduledTime: String,
    onTaken: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()))
    }
    var currentDateString by remember {
        mutableStateOf(SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()))
    }

    // Keep clock current every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            currentDateString = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
        }
    }

    // Pulsing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "AlarmPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Animated Icon & Alarm Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF3366).copy(alpha = 0.4f),
                                    Color(0xFFBD1E85).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFFF3366), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💊",
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentTimeString,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = currentDateString,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TIME FOR YOUR MEDICATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFF3366)
                )
            }

            // Middle Section: Medicine Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = medName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                        softWrap = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFBD1E85).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFFBD1E85), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = medDosage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF85D0)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Scheduled: $scheduledTime\nTake with water to keep your blood glucose in target range.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // Bottom Section: 3 Action Buttons (TAKEN, SNOOZE, SKIP)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TAKEN (Green Primary Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF16A34A),
                                    Color(0xFF22C55E)
                                )
                            )
                        )
                        .clickable { onTaken() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✅", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAKEN NOW",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // 2. SNOOZE 5 MINS (Amber / Blue Secondary Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2563EB).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(16.dp))
                        .clickable { onSnooze() }
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "⏰", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snooze (5 Minutes)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 3. SKIP (Red Outlined Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Transparent)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .clickable { onSkip() }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Skip This Dose",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

