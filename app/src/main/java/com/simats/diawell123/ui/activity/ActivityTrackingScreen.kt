package com.simats.diawell123.ui.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import com.simats.diawell123.util.DiaWellNotificationHelper
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun ActivityTrackingScreen(
    modifier: Modifier = Modifier,
    initialActivity: String? = null,
    glucoseLevel: Int = 128,
    weightKg: Float = 68f,
    viewModel: ActivityViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFinishActivityClick: () -> Unit = {},
) {
    val trackingState by viewModel.trackingState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(initialActivity) {
        if (!initialActivity.isNullOrBlank()) {
            viewModel.selectActivity(initialActivity)
        }
    }

    var isTrackingActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var liveSteps by remember { mutableIntStateOf(0) }
    var showCompletedBanner by remember { mutableStateOf(false) }

    // Request Activity Recognition Permission safely on Android 10+ (API 29+) using 16-bit compliant requestCode
    LaunchedEffect(Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    (context as? android.app.Activity)?.let { act ->
                        androidx.core.app.ActivityCompat.requestPermissions(
                            act,
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            1002
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Silently tolerate
        }
    }

    // Dynamic Activity Recommendations & Clinical Settings
    val activeRec = trackingState.activeRecommendation
    val currentActivity = trackingState.selectedActivity
    val isWalking = currentActivity.equals("Walking", ignoreCase = true)
    val isRunning = currentActivity.equals("Running", ignoreCase = true)
    val isCycling = currentActivity.equals("Cycling", ignoreCase = true)

    val recommendedMins = activeRec?.durationMinutes ?: 30
    val targetDistanceKm = activeRec?.distanceKm ?: 2.5f
    val clinicalReason = activeRec?.reason
        ?: "Evidence-informed exercise activates muscle GLUT4 transporters to improve glycemic control."
    val safetyNote = activeRec?.safetyNote ?: trackingState.safetyGuidance
        ?: "Stay hydrated, monitor symptoms, and avoid exercising through discomfort."

    val effectiveWeight = trackingState.userContext?.weightKg ?: weightKg
    val effectiveGlucose = trackingState.userContext?.currentGlucoseMgDl?.toInt() ?: glucoseLevel

    // Hardware Motion Sensor Step Tracker (Only increments when physically moving with phone)
    val sensorManager = remember {
        try {
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        } catch (_: Exception) {
            null
        }
    }
    val stepDetectorSensor = remember {
        try {
            sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        } catch (_: Exception) {
            null
        }
    }
    val accelerometerSensor = remember {
        try {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(isTrackingActive, currentActivity) {
        if (!isTrackingActive || isCycling) {
            return@DisposableEffect onDispose {}
        }

        var lastStepTime = 0L
        var prevMagnitude = 9.8f
        var peakArmed = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || !isTrackingActive) return
                val now = System.currentTimeMillis()

                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    // Physical hardware footstep detected
                    if (now - lastStepTime > 240L) {
                        lastStepTime = now
                        liveSteps += 1
                    }
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    // Physical accelerometer motion peak detection
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val delta = magnitude - prevMagnitude
                    prevMagnitude = magnitude

                    val threshold = if (isRunning) 2.5f else 1.6f
                    val minInterval = if (isRunning) 250L else 310L

                    if (delta > threshold && !peakArmed) {
                        peakArmed = true
                        if (now - lastStepTime > minInterval) {
                            lastStepTime = now
                            liveSteps += 1
                        }
                    } else if (delta < -0.8f) {
                        peakArmed = false
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        try {
            if (stepDetectorSensor != null) {
                sensorManager?.registerListener(listener, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
            }
            if (accelerometerSensor != null) {
                sensorManager?.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            }
        } catch (_: Exception) {}

        onDispose {
            try {
                sensorManager?.unregisterListener(listener)
            } catch (_: Exception) {}
        }
    }

    // Live Activity Timer Effect
    LaunchedEffect(isTrackingActive) {
        while (isTrackingActive) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    // Auto-dismiss completed banner after 1.5 seconds and trigger navigation
    LaunchedEffect(showCompletedBanner) {
        if (showCompletedBanner) {
            delay(1500L)
            showCompletedBanner = false
            onFinishActivityClick()
        }
    }

    // Exact MET and Biometric Formulas (Part 1)
    val elapsedMins = elapsedSeconds / 60
    val durationHours = elapsedSeconds / 3600.0

    // Distance Calculation (Walking: 0.78m stride, Running: 0.95m stride, Cycling: 16 km/h benchmark)
    val distanceCoveredKm: Float = when {
        isWalking -> (liveSteps * 0.00078f)
        isRunning -> (liveSteps * 0.00095f)
        isCycling -> ((16.0f * elapsedSeconds) / 3600.0f)
        else -> (targetDistanceKm * (elapsedSeconds.toFloat() / (recommendedMins * 60f))).coerceAtLeast(0f)
    }

    // Calories Burned via MET formula: MET * weightKg * (durationMins / 60.0)
    val metValue = when {
        isWalking -> 3.5f
        isRunning -> 8.0f
        isCycling -> 7.5f
        else -> activeRec?.metValue ?: 3.8f
    }
    val caloriesBurned = (metValue * effectiveWeight * durationHours).toInt()

    // Heart Rate for Cycling (Simulated live estimated heart rate between 120-145 bpm)
    val liveCyclingHeartRate = if (isTrackingActive) {
        "${120 + (elapsedSeconds % 26)} bpm"
    } else {
        "--"
    }

    // Track Progress: Walking & Running advance ONLY when physical steps are taken!
    val targetSteps = when {
        isWalking -> (targetDistanceKm / 0.00078f).toInt().coerceAtLeast(100)
        isRunning -> (targetDistanceKm / 0.00095f).toInt().coerceAtLeast(100)
        else -> 1000
    }

    val progressFraction = if (isCycling) {
        (elapsedSeconds.toFloat() / (maxOf(1, recommendedMins) * 60f)).coerceIn(0f, 1f)
    } else {
        (liveSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
    }

    val intensity = when {
        metValue < 4.0f -> "Light"
        metValue <= 7.0f -> "Moderate"
        else -> "Vigorous"
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Header Bar (Pops Up)
            ElementPopUpContainer(delayMillis = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StyledBackButton(onBackClick = onBackClick)

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Activity",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                                if (trackingState.isLoading) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CircularProgressIndicator(
                                        color = Color(0xFF0D9488),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // History Logs Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.elevatedSurface)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                            .clickable { onHistoryClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "History",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D9488),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity Completed Banner
            AnimatedVisibility(
                visible = showCompletedBanner,
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
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Workout Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            val extraDetail = if (isCycling) {
                                "${String.format("%.2f", distanceCoveredKm)} km"
                            } else {
                                "${String.format("%,d", liveSteps)} steps"
                            }
                            Text(
                                text = "Logged $caloriesBurned kcal • $extraDetail.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Error / Safety Contraindication Banner if any
            if (trackingState.errorMessage != null && !showCompletedBanner) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "⚠️ ${trackingState.errorMessage}",
                        fontSize = 12.sp,
                        color = Color(0xFFC62828),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Activity Mode Selection Pills (Pops Up)
            val activityOptions = if (trackingState.recommendations.isNotEmpty()) {
                trackingState.recommendations.map { it.activity to it.icon }
            } else {
                listOf("Walking" to "🚶", "Cycling" to "🚴", "Running" to "🏃")
            }

            ElementPopUpContainer(delayMillis = 60) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    activityOptions.forEach { (type, icon) ->
                        val isSelected = trackingState.selectedActivity.equals(type, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF0F766E), Color(0xFF0D9488)),
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(AppTheme.colors.cardBackground, AppTheme.colors.cardBackground),
                                        )
                                    },
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF14B8A6) else AppTheme.colors.border,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable {
                                    if (!isTrackingActive) {
                                        viewModel.selectActivity(type)
                                        elapsedSeconds = 0
                                        liveSteps = 0
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = type,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else AppTheme.colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Central Interactive Progress Dial & Animated Progress Track Card (Pops Up)
            ElementPopUpContainer(delayMillis = 120) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Intensity & MET Reference Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (intensity) {
                                            "Light" -> Color(0xFFE0F2F1)
                                            "Moderate" -> Color(0xFFE8F5E9)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Intensity: $intensity • $metValue MET",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (intensity) {
                                        "Light" -> Color(0xFF00695C)
                                        "Moderate" -> Color(0xFF2E7D32)
                                        else -> Color(0xFFE65100)
                                    }
                                )
                            }
                        }

                        // Circular Progress Ring Arc Canvas
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                // Background Track Arc
                                drawArc(
                                    color = if (AppTheme.colors.isDark) AppTheme.colors.border else Color(0xFFE2E8F0),
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )

                                // Animated Progress Arc in Dark Neon / Teal Theme (#0D9488)
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF14B8A6),
                                            Color(0xFF0D9488),
                                            Color(0xFF065F46),
                                        ),
                                    ),
                                    startAngle = 135f,
                                    sweepAngle = 270f * progressFraction,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )
                            }

                            // Center Timer Text Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val minsStr = String.format("%02d", elapsedSeconds / 60)
                                val secsStr = String.format("%02d", elapsedSeconds % 60)

                                Text(
                                    text = "$minsStr:$secsStr",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                )
                                Text(
                                    text = "Goal: $recommendedMins mins",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textMuted,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Animated Progress Track with Dashed Baseline & Forward-Facing Moving Figure
                        WorkoutProgressTrack(
                            progressFraction = progressFraction,
                            activityType = currentActivity,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Distance Status (Speed completely removed)
                        if (isCycling) {
                            Text(
                                text = "Target Distance: ${String.format("%.2f", targetDistanceKm)} km",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9488),
                            )
                        } else {
                            Text(
                                text = "Target: ${String.format("%.2f", targetDistanceKm)} km • Step tracker",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Real-time Live Workout Statistics Grid (Speed removed from Cycling; Steps on Walking/Running)
            ElementPopUpContainer(delayMillis = 180) {
                if (isCycling) {
                    // Cycling: 3 cards (Distance, Calories, Heart Rate - Speed removed completely)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ActivityStatBox(
                            icon = "📍",
                            title = "Distance",
                            value = "${String.format("%.2f", distanceCoveredKm)} km",
                            modifier = Modifier.weight(1f),
                        )

                        ActivityStatBox(
                            icon = "🔥",
                            title = "Calories",
                            value = "$caloriesBurned kcal",
                            modifier = Modifier.weight(1f),
                        )

                        ActivityStatBox(
                            icon = "❤️",
                            title = "Heart Rate",
                            value = liveCyclingHeartRate,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    // Walking & Running: 3 cards (Distance, Calories, real hardware Steps)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ActivityStatBox(
                            icon = "📍",
                            title = "Distance",
                            value = "${String.format("%.2f", distanceCoveredKm)} km",
                            modifier = Modifier.weight(1f),
                        )

                        ActivityStatBox(
                            icon = "🔥",
                            title = "Calories",
                            value = "$caloriesBurned kcal",
                            modifier = Modifier.weight(1f),
                        )

                        ActivityStatBox(
                            icon = if (isRunning) "👟" else "👣",
                            title = "Steps",
                            value = String.format("%,d", liveSteps),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. AI Recommendation Rationale Box (Pops Up)
            ElementPopUpContainer(delayMillis = 240) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.elevatedSurface)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "⚡", fontSize = 20.sp, modifier = Modifier.padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Recommendation Rationale",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9488),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = clinicalReason,
                                fontSize = 11.5.sp,
                                color = AppTheme.colors.textSecondary,
                                lineHeight = 16.sp,
                            )

                            if (safetyNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (AppTheme.colors.isDark) Color(0xFF134E4A).copy(alpha = 0.5f) else Color(0xFFE8F5E9))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🛡", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = safetyNote,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2E7D32),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Primary Action Button: Start / Pause / Complete Workout (Pops Up)
            ElementPopUpContainer(delayMillis = 280) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (isTrackingActive) {
                        IosLiquidGlassButton(
                            text = "⏸ Pause Activity",
                            onClick = { isTrackingActive = false },
                            modifier = Modifier.weight(1f),
                        )

                        IosLiquidGlassButton(
                            text = if (trackingState.isLoggingWorkout) "Saving..." else "✓ Complete",
                            onClick = {
                                isTrackingActive = false
                                val actName = trackingState.selectedActivity
                                val dur = maxOf(1, elapsedMins)
                                val cal = caloriesBurned
                                val dist = distanceCoveredKm
                                val workoutNote = if (isCycling) {
                                    "Completed $intensity cycling ($dur mins, $metValue MET)"
                                } else {
                                    "Completed $intensity $actName (${String.format("%,d", liveSteps)} steps, $metValue MET)"
                                }

                                viewModel.logCompletedWorkout(
                                    activityType = actName,
                                    durationMins = dur,
                                    distanceKm = dist,
                                    caloriesKcal = cal,
                                    glucoseBefore = effectiveGlucose.toFloat(),
                                    notes = workoutNote
                                ) {
                                    val summaryMsg = if (isCycling) {
                                        "Completed $dur mins of Cycling ($cal kcal burned, ${String.format("%.2f", dist)} km)."
                                    } else {
                                        "Completed $dur mins of $actName ($cal kcal burned, ${String.format("%,d", liveSteps)} steps)."
                                    }
                                    DiaWellNotificationHelper.showGeneralNotification(
                                        context = context,
                                        title = "🏃 Activity Logged!",
                                        message = summaryMsg
                                    )
                                    showCompletedBanner = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        IosLiquidGlassButton(
                            text = if (elapsedSeconds > 0) "▶ Resume Activity" else "▶ Start ${trackingState.selectedActivity}",
                            onClick = { isTrackingActive = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Custom Canvas & Vector Animated Progress Track
 * Horizontal dashed baseline track with moving figure icon (🚶, 🏃, 🚴) smoothly
 * advancing from fraction = 0f to fraction = 1.0f as real steps/workout progress.
 * The icon is flipped horizontally (scaleX = -1f) so it moves forward towards the right.
 * Styled in DiaWell's dark neon/teal theme (#0D9488).
 */
@Composable
fun WorkoutProgressTrack(
    progressFraction: Float,
    activityType: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = AppTheme.colors.isDark
) {
    val figureIcon = when {
        activityType.equals("Cycling", ignoreCase = true) -> "🚴"
        activityType.equals("Running", ignoreCase = true) -> "🏃"
        else -> "🚶"
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "WorkoutFigureTrackProgress"
    )

    val trackAccent = if (isDark) Color(0xFF38BDF8) else Color(0xFF7C3AED)
    val trackBgColor = if (isDark) Color(0xFF1E1B2E) else Color(0xFFF5F3FF)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(trackBgColor)
            .border(1.dp, trackAccent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Progress Track",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = trackAccent
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = trackAccent
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                // Dashed baseline track & solid active progress path
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    val trackY = size.height / 2f
                    val strokeW = 3.dp.toPx()
                    val paddingX = 12.dp.toPx()
                    val availableWidth = size.width - (paddingX * 2f)

                    // 1. Horizontal dashed baseline track
                    drawLine(
                        color = trackAccent.copy(alpha = 0.35f),
                        start = Offset(paddingX, trackY),
                        end = Offset(size.width - paddingX, trackY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // 2. Active completed progress segment
                    val currentProgressX = paddingX + (availableWidth * animatedProgress)
                    if (currentProgressX > paddingX) {
                        drawLine(
                            color = trackAccent,
                            start = Offset(paddingX, trackY),
                            end = Offset(currentProgressX, trackY),
                            strokeWidth = strokeW + 0.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 3. Start Point Dot
                    drawCircle(
                        color = trackAccent,
                        radius = 4.dp.toPx(),
                        center = Offset(paddingX, trackY)
                    )

                    // 4. Finish Goal Dot
                    drawCircle(
                        color = trackAccent,
                        radius = 4.dp.toPx(),
                        center = Offset(size.width - paddingX, trackY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Moving figure icon (🚶, 🏃, 🚴) smoothly advancing forward
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val paddingXDp = 12.dp
                    val iconWidthDp = 22.dp
                    val availableWidthDp = maxWidth - (paddingXDp * 2) - iconWidthDp
                    val iconOffsetXDp = paddingXDp + (availableWidthDp * animatedProgress)

                    Box(
                        modifier = Modifier
                            .offset(x = iconOffsetXDp)
                            .align(Alignment.CenterStart)
                            .size(iconWidthDp)
                            .scale(scaleX = -1f, scaleY = 1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = figureIcon,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Walking Screen Composable
 */
@Composable
fun WalkingScreen(
    modifier: Modifier = Modifier,
    glucoseLevel: Int = 128,
    weightKg: Float = 68f,
    viewModel: ActivityViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFinishActivityClick: () -> Unit = {},
) {
    ActivityTrackingScreen(
        modifier = modifier,
        initialActivity = "Walking",
        glucoseLevel = glucoseLevel,
        weightKg = weightKg,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onHistoryClick = onHistoryClick,
        onFinishActivityClick = onFinishActivityClick,
    )
}

/**
 * Dedicated Running Screen Composable
 */
@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    glucoseLevel: Int = 128,
    weightKg: Float = 68f,
    viewModel: ActivityViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFinishActivityClick: () -> Unit = {},
) {
    ActivityTrackingScreen(
        modifier = modifier,
        initialActivity = "Running",
        glucoseLevel = glucoseLevel,
        weightKg = weightKg,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onHistoryClick = onHistoryClick,
        onFinishActivityClick = onFinishActivityClick,
    )
}

/**
 * Dedicated Cycling Screen Composable
 */
@Composable
fun CyclingScreen(
    modifier: Modifier = Modifier,
    glucoseLevel: Int = 128,
    weightKg: Float = 68f,
    viewModel: ActivityViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFinishActivityClick: () -> Unit = {},
) {
    ActivityTrackingScreen(
        modifier = modifier,
        initialActivity = "Cycling",
        glucoseLevel = glucoseLevel,
        weightKg = weightKg,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onHistoryClick = onHistoryClick,
        onFinishActivityClick = onFinishActivityClick,
    )
}

@Composable
fun ActivityStatBox(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textMuted,
            )
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivityTrackingScreenPreview() {
    Diawell123Theme {
        ActivityTrackingScreen()
    }
}
