package com.simats.diawell123
import com.simats.diawell123.ui.theme.AppTheme

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // 1. Rotation animation: Spins, slows down, and STOPS completely!
    val spinRotation = remember { Animatable(0f) }

    // Synchronized Sound Effects & Vibration Feedback Effect
    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // High-volume system tone generator for audible spin clicks
        val toneGen = try {
            ToneGenerator(AudioManager.STREAM_SYSTEM, 80)
        } catch (_: Exception) {
            try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            } catch (_: Exception) {
                null
            }
        }

        // Launch Arc Reactor Rotation
        launch {
            spinRotation.animateTo(
                targetValue = 720f, // 2 full spins
                animationSpec = tween(
                    durationMillis = 2200,
                    easing = FastOutSlowInEasing, // Spins, slows down, and comes to a complete STOP
                ),
            )
        }

        // Synchronized Pulsing Sound & Haptic Ticks that slow down and stop
        val startTime = System.currentTimeMillis()
        val duration = 2200L

        while (spinRotation.isRunning) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

            if (progress >= 0.94f) break

            // Interval slows down gradually as easing slows down the rotation (45ms -> 280ms)
            val currentInterval = (45 + (progress * progress * 235)).toLong()

            // 1. Trigger Audible Audio Beep/Click
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)

            // 2. Trigger Compose System Haptic Feedback
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {
                // Ignore
            }

            // 3. Trigger Hardware Vibrator Feedback
            try {
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(25L)
                    }
                }
            } catch (_: Exception) {
                // Ignore
            }

            delay(currentInterval)
        }

        toneGen?.release()
    }

    // 2. Infinite transition for Arc Reactor Glow + Running Horizontal ECG Pulse Wave OUTSIDE
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorAnimations")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neonGlowAlpha",
    )

    // Continuous running ECG pulse line animation (0f to 1f) running OUTSIDE the Arc Reactor
    val ecgSweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ecgSweepProgress",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        // Ambient background shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val ambientPink = if (AppTheme.colors.isDark) Color(0xFF2E1065).copy(alpha = 0.20f) else Color(0xFFF3E8FF)
            val ambientCyan = if (AppTheme.colors.isDark) Color(0xFF0E7490).copy(alpha = 0.20f) else Color(0xFFE0F7FA).copy(alpha = 0.6f)

            drawCircle(
                color = ambientPink,
                radius = width * 0.45f,
                center = Offset(x = width * 0.1f, y = height * 0.08f),
            )

            drawCircle(
                color = ambientCyan,
                radius = width * 0.55f,
                center = Offset(x = width * 0.85f, y = height * 0.12f),
            )

            drawCircle(
                color = ambientPink,
                radius = width * 0.48f,
                center = Offset(x = width * 0.92f, y = height * 0.85f),
                style = Stroke(width = 44.dp.toPx()),
            )
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center section: Arc Reactor + Running Horizontal Pulse Line OUTSIDE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Arc Reactor Logo Container with Horizontal ECG Line Running Outside
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp, 180.dp),
                ) {
                    // Running ECG Pulse Line Canvas OUTSIDE the Arc Reactor
                    OutsideEcgPulseCanvas(
                        ecgProgress = ecgSweepProgress,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Outer Purple Energy Aura Ring
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFFA855F7).copy(alpha = neonGlowAlpha * 0.35f)),
                    )

                    // Inner Lilac Energy Aura Ring
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .scale(pulseScale * 0.96f)
                            .clip(CircleShape)
                            .background(Color(0xFFF0ABFC).copy(alpha = neonGlowAlpha * 0.25f)),
                    )

                    // Arc Reactor Canvas Logo (Transforms from Bird to DW Logo as it spins, slows down, and STOPS!)
                    ArcReactorCanvas(
                        rotationAngle = spinRotation.value,
                        isSplashTransform = true,
                        modifier = Modifier
                            .size(128.dp)
                            .scale(pulseScale),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Title
                Text(
                    text = "DiaWell",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA855F7),
                    letterSpacing = (-0.5).sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tagline / Headline
                Text(
                    text = "Eat Smart. Track Better. Live Well.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Text
                Text(
                    text = "Powered by high-precision glucose intelligence\nand smart health routines.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }

            // Bottom Footer Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Designed around your wellbeing",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFFA855F7),
                )
            }
        }
    }
}

@Composable
fun OutsideEcgPulseCanvas(
    ecgProgress: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f

        // Baseline Line across the screen
        drawLine(
            color = Color(0xFFA855F7).copy(alpha = 0.30f),
            start = Offset(0f, centerY),
            end = Offset(w, centerY),
            strokeWidth = 2.dp.toPx(),
        )

        // Continuous Moving Pulse Wave running OUTSIDE
        val pulseWidth = w * 0.45f
        val pulseStartX = (ecgProgress * (w + pulseWidth)) - pulseWidth

        val ecgPath = Path().apply {
            moveTo(0f, centerY)
            if (pulseStartX > 0f) {
                lineTo(pulseStartX, centerY)
            }
            lineTo(pulseStartX + pulseWidth * 0.25f, centerY)
            lineTo(pulseStartX + pulseWidth * 0.38f, centerY - 28.dp.toPx())
            lineTo(pulseStartX + pulseWidth * 0.50f, centerY + 32.dp.toPx())
            lineTo(pulseStartX + pulseWidth * 0.62f, centerY - 38.dp.toPx())
            lineTo(pulseStartX + pulseWidth * 0.75f, centerY + 18.dp.toPx())
            lineTo(pulseStartX + pulseWidth, centerY)
            lineTo(w, centerY)
        }

        drawPath(
            path = ecgPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFA855F7).copy(alpha = 0.2f),
                    Color(0xFFA855F7),
                    Color(0xFFF0ABFC),
                    Color(0xFFA855F7).copy(alpha = 0.2f),
                ),
            ),
            style = Stroke(width = 3.5.dp.toPx()),
        )

        // Glowing Lead Dot
        val leadDotX = (pulseStartX + pulseWidth * 0.62f).coerceIn(0f, w)
        val leadDotY = centerY - 38.dp.toPx()

        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(leadDotX, leadDotY),
        )

        drawCircle(
            color = Color(0xFFF0ABFC),
            radius = 7.dp.toPx(),
            center = Offset(leadDotX, leadDotY),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
fun ArcReactorCanvas(
    rotationAngle: Float = 0f,
    modifier: Modifier = Modifier,
    isSplashTransform: Boolean = false,
) {
    val showDW = !isSplashTransform || rotationAngle >= 360f
    val currentDrawable = if (showDW) R.drawable.app_logo else R.drawable.bird_logo

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color(0xFFA855F7).copy(alpha = 0.55f),
                spotColor = Color(0xFFF0ABFC).copy(alpha = 0.55f),
            )
            .clip(CircleShape)
            .background(Color(0xFF0A0C19)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = currentDrawable),
            contentDescription = "DiaWell Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationAngle),
        )
    }
}

@Composable
fun DiaWellLogo(
    modifier: Modifier = Modifier,
    rotationAngle: Float = 0f,
) {
    ArcReactorCanvas(rotationAngle = rotationAngle, modifier = modifier)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    Diawell123Theme {
        SplashScreen()
    }
}
