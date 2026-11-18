package com.simats.diawell123
import com.simats.diawell123.ui.theme.AppTheme

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Physical constant-friction deceleration matching wheel_spin.wav peg cadence
private val WheelDecelerationEasing = Easing { u -> 1f - (1f - u) * (1f - u) }

// Instagram Sans Headline font family matching exact branding
val InstagramSansHeadline = FontFamily(
    Font(R.font.instagram_sans_headline_bold, FontWeight.Bold),
    Font(R.font.instagram_sans_headline_regular, FontWeight.Normal),
)

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // 1. Rotation animation: Spins fast immediately, slows down smoothly, and STOPS completely
    val spinRotation = remember { Animatable(0f) }

    // Synchronized Wheel Spinning Sound (R.raw.wheel_spin) & Tactile Haptic Feedback
    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        val mediaPlayer = try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                context.resources.openRawResourceFd(R.raw.wheel_spin).use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                prepare()
                setVolume(1.0f, 1.0f)
            }
        } catch (_: Exception) {
            null
        }

        try {
            val spinDurationMs = 2200
            val numPegs = 36

            // Start realistic wheel spinning sound simultaneously with logo rotation
            try {
                mediaPlayer?.start()
            } catch (_: Exception) {}

            // Launch Logo Wheel Rotation using identical quadratic friction deceleration
            val spinJob = launch {
                spinRotation.animateTo(
                    targetValue = 720f, // 2 full spins, decelerating smoothly to a complete stop
                    animationSpec = tween(
                        durationMillis = spinDurationMs,
                        easing = WheelDecelerationEasing,
                    ),
                )
            }

            // Light tactile haptic ticks aligned with the exact wheel peg timestamps
            val startTime = System.currentTimeMillis()
            for (k in 0 until numPegs) {
                val yPos = (k + 0.92) / numPegs.toDouble()
                val u = 1.0 - sqrt((1.0 - yPos).coerceAtLeast(0.0))
                val targetMs = (u * spinDurationMs).toLong()
                val waitMs = targetMs - (System.currentTimeMillis() - startTime)
                if (waitMs > 0L) delay(waitMs)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val vel = (1.0 - u).toFloat().coerceIn(0.15f, 1.0f)
                        val amplitude = (45 + (vel * 120)).toInt().coerceIn(35, 180)
                        vibrator?.vibrate(VibrationEffect.createOneShot(10L, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(10L)
                    }
                } catch (_: Exception) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }

            spinJob.join()
            // Allow audio tail (2.35s) to decay cleanly to silence
            delay(180)
        } finally {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (_: Exception) {}
        }
    }

    // 2. Subtle ambient breathing pulse for the outer glow rings
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    // 3. Continuous ECG wave progression (runs seamlessly outside the logo)
    val ecgProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ecg_progress",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        // Ambient Background Lighting
        Box(
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.Center)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA855F7).copy(alpha = if (AppTheme.colors.isDark) 0.16f else 0.10f),
                            Color(0xFF7C3AED).copy(alpha = if (AppTheme.colors.isDark) 0.08f else 0.04f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Center Logo & Core Title Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f),
            ) {
                // Spinning DiaWell Central Logo with Pulse Wave running BEHIND it
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutsideEcgPulseCanvas(
                        ecgProgress = ecgProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                    )

                    DiaWellLogo(
                        rotationAngle = spinRotation.value,
                        modifier = Modifier.size(175.dp),
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // App Title with Exact Instagram Sans Headline Font
                Text(
                    text = "DiaWell",
                    fontFamily = InstagramSansHeadline,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Health Tagline
                Text(
                    text = "Eat Smart. Track Better. Live Well.",
                    fontSize = 18.sp,
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
) {
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
            painter = painterResource(id = R.drawable.app_logo),
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
