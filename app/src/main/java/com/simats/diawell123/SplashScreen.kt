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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate as drawRotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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

// Rounded geometric sans-serif font matching the sleek DiaWell wordmark
val DiaWellWordmarkFont = FontFamily(
    Font(R.font.quicksand_bold, FontWeight.Bold),
)

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // 1. Rotation animation: Spins, slows down, and STOPS completely
    val spinRotation = remember { Animatable(0f) }

    // Synchronized Smooth Cinematic Intro Theme & Gentle Haptic Swell
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
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                context.resources.openRawResourceFd(R.raw.cinematic_intro).use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                prepare()
                setVolume(1.0f, 1.0f)
            }
        } catch (_: Exception) {
            null
        }

        try {
            // Start cinematic Netflix-style intro theme right with the splash reveal
            try {
                mediaPlayer?.start()
            } catch (_: Exception) {}

            // Launch smooth logo spin to stop
            val spinJob = launch {
                spinRotation.animateTo(
                    targetValue = 720f, // 2 full spins
                    animationSpec = tween(
                        durationMillis = 2200,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }

            // Single gentle smooth haptic swell matching the smooth cinematic bloom
            delay(220)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0L, 120L, 180L, 140L),
                            intArrayOf(0, 65, 115, 45),
                            -1,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(60L)
                }
            } catch (_: Exception) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            spinJob.join()
            // Allow cinematic reverb tail (2.55s) to complete cleanly
            delay(360)
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
            .systemBarsPadding(),
    ) {
        // Soft pulsing center halo behind the DW emblem without any hard circular clip edge
        val isSplashDark = AppTheme.colors.isDark
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = if (isSplashDark) 0.16f else 0.10f),
                        Color(0xFF7C3AED).copy(alpha = if (isSplashDark) 0.08f else 0.04f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = (size.minDimension * 0.56f) * pulseScale,
                ),
            )
        }

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

                Spacer(modifier = Modifier.height(28.dp))

                // App Title in glowing cursive script with a small heart replacing the dot on 'i'
                DiaWellScriptWordmark()

                Spacer(modifier = Modifier.height(10.dp))

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
fun DiaWellScriptWordmark(
    modifier: Modifier = Modifier,
) {
    val isDark = AppTheme.colors.isDark
    var iBox by remember { mutableStateOf<Rect?>(null) }

    val wordmarkBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color(0xFF6D28D9),
                Color(0xFF9333EA),
                Color(0xFFC084FC),
                Color(0xFFF0ABFC),
                Color(0xFFF5D0FE),
            )
        } else {
            listOf(
                Color(0xFF5B21B6),
                Color(0xFF7C3AED),
                Color(0xFFA855F7),
                Color(0xFFD946EF),
            )
        },
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Unicode U+0131 (dotless 'ı') removes the default dot cleanly so the heart replaces it
        Text(
            text = "D\u0131aWell",
            style = TextStyle(
                fontFamily = DiaWellWordmarkFont,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                brush = wordmarkBrush,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
                shadow = Shadow(
                    color = Color(0xFFA855F7).copy(alpha = if (isDark) 0.32f else 0.20f),
                    offset = Offset(0f, 0f),
                    blurRadius = if (isDark) 10f else 6f,
                ),
            ),
            onTextLayout = { layoutResult ->
                iBox = layoutResult.getBoundingBox(1)
            },
        )

        // Small glowing heart replacing the dot above the letter 'i'
        Canvas(modifier = Modifier.matchParentSize()) {
            val box = iBox ?: return@Canvas
            val cx = box.left + box.width * 0.50f
            val cy = box.top + box.height * 0.235f
            val hSize = box.height * 0.135f

            // Subtle purple-pink glow halo behind the heart
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE879F9).copy(alpha = if (isDark) 0.35f else 0.18f),
                        Color(0xFFA855F7).copy(alpha = if (isDark) 0.14f else 0.06f),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = hSize * 1.5f,
                ),
                radius = hSize * 1.5f,
                center = Offset(cx, cy),
            )

            // Crisp upright heart matching the rounded geometric font
            drawRotate(degrees = 0f, pivot = Offset(cx, cy)) {
                val heartPath = Path().apply {
                    val topY = cy - hSize * 0.22f
                    val bottomY = cy + hSize * 0.62f
                    moveTo(cx, bottomY)
                    cubicTo(
                        cx - hSize * 0.95f, cy + hSize * 0.08f,
                        cx - hSize * 0.82f, topY - hSize * 0.55f,
                        cx, topY + hSize * 0.08f,
                    )
                    cubicTo(
                        cx + hSize * 0.82f, topY - hSize * 0.55f,
                        cx + hSize * 0.95f, cy + hSize * 0.08f,
                        cx, bottomY,
                    )
                    close()
                }
                drawPath(
                    path = heartPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9333EA),
                            Color(0xFFC084FC),
                            Color(0xFFF0ABFC),
                        ),
                        start = Offset(cx - hSize, cy - hSize),
                        end = Offset(cx + hSize, cy + hSize),
                    ),
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
