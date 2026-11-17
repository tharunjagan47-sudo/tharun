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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
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

    // 1. Rotation animation: Dotted ring spins from 0° to 720° and comes to a complete STOP
    val spinRotation = remember { Animatable(0f) }
    // 2. DW Monogram writing animation: Begins writing letter-by-letter as soon as the dotted ring stops!
    val dwWriteProgress = remember { Animatable(0f) }
    val isRingStopped = remember { mutableStateOf(false) }

    // Synchronized Sound Effects & Vibration Feedback Effect for Rotation & Netflix-Style Writing
    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // High-volume system tone generator for audible spin clicks & writing shimmer
        val toneGen = try {
            ToneGenerator(AudioManager.STREAM_SYSTEM, 75)
        } catch (_: Exception) {
            try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 75)
            } catch (_: Exception) {
                null
            }
        }

        // Step 1: Launch Dotted Ring Rotation (0ms to 1300ms)
        launch {
            spinRotation.animateTo(
                targetValue = 720f, // 2 full spins, decelerates and comes to a complete STOP
                animationSpec = tween(
                    durationMillis = 1300,
                    easing = FastOutSlowInEasing,
                ),
            )
            isRingStopped.value = true

            // Step 2: Once dotted ring stops, write the DW monogram from start to finish! (1300ms to 2900ms)
            dwWriteProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1600,
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        // Sound for Rotation (0ms to 1300ms): Mechanical clicking ticks that decelerate and stop
        val startTime = System.currentTimeMillis()
        val duration = 1300L
        while (spinRotation.isRunning) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            if (progress >= 0.94f) break

            val currentInterval = (40 + (progress * progress * 190)).toLong()
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}

            delay(currentInterval)
        }

        // Wait for dotted ring to stop completely
        while (!isRingStopped.value) {
            delay(20)
        }

        // Sound for Writing the DW Logo (Netflix-style arpeggio sequence as D and W are being written)
        val writingTones = intArrayOf(
            ToneGenerator.TONE_DTMF_1,
            ToneGenerator.TONE_DTMF_4,
            ToneGenerator.TONE_DTMF_5,
            ToneGenerator.TONE_DTMF_8,
            ToneGenerator.TONE_DTMF_9,
            ToneGenerator.TONE_DTMF_C,
            ToneGenerator.TONE_DTMF_D
        )
        for (tone in writingTones) {
            toneGen?.startTone(tone, 35)
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
            delay(150)
        }

        // Final Lock-in confirmation chime when DW writing completes!
        toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45L)
                }
            }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}
        toneGen?.release()
    }

    // Infinite transition for Energy Aura Glow + Running Horizontal ECG Pulse Wave
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

    // Continuous running ECG pulse line animation (0f to 1f) running OUTSIDE the logo
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

            // Center section: Dotted Ring + DW Stroke Handwriting Animation + Running ECG Line
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp, 180.dp),
                ) {
                    // Running ECG Pulse Line Canvas OUTSIDE the Logo
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

                    // Cinematic Dotted Ring & Stroke-Written DW Logo
                    CinematicDiaWellLogo(
                        ringRotation = spinRotation.value,
                        writeProgress = dwWriteProgress.value,
                        modifier = Modifier
                            .size(136.dp)
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
fun CinematicDiaWellLogo(
    ringRotation: Float,
    writeProgress: Float,
    modifier: Modifier = Modifier,
) {
    // Exact handwriting paths for 'D' and 'W' in 512x512 coordinate space
    val pathD = remember {
        Path().apply {
            // Start at bottom of D spine
            moveTo(118f, 325f)
            // Up the spine to top
            lineTo(118f, 185f)
            // Top curve of D
            cubicTo(118f, 172f, 155f, 168f, 195f, 180f)
            // Outer belly curve of D
            cubicTo(245f, 195f, 258f, 235f, 252f, 270f)
            // Bottom curve closing D
            cubicTo(246f, 310f, 215f, 325f, 165f, 325f)
            lineTo(118f, 325f)
        }
    }

    val pathW = remember {
        Path().apply {
            // Bridge from D into W valley 1
            moveTo(238f, 255f)
            cubicTo(255f, 275f, 270f, 305f, 280f, 305f)
            // Valley 1 up to center apex peak
            cubicTo(295f, 305f, 315f, 235f, 335f, 180f)
            cubicTo(342f, 172f, 350f, 175f, 355f, 190f)
            // Apex down to valley 2
            cubicTo(365f, 235f, 375f, 305f, 385f, 305f)
            // Valley 2 up to right wing terminus
            cubicTo(395f, 305f, 405f, 260f, 405f, 200f)
        }
    }

    val pathMeasureD = remember { PathMeasure() }
    val pathMeasureW = remember { PathMeasure() }

    remember(pathD) {
        pathMeasureD.setPath(pathD, false)
        true
    }
    remember(pathW) {
        pathMeasureW.setPath(pathW, false)
        true
    }

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
        // 1. Dotted Segmented Ring: Spins 2 full cycles and STOPS completely at 0°!
        Image(
            painter = painterResource(id = R.drawable.ring_only),
            contentDescription = "DiaWell Dotted Ring",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .rotate(ringRotation),
        )

        // 2. DW Monogram: When the dotted ring stops, DW is written from start to finish!
        if (writeProgress > 0f) {
            val isComplete = writeProgress >= 1f

            if (isComplete) {
                // Completed solid 3D DW logo settled inside stationary ring
                Image(
                    painter = painterResource(id = R.drawable.dw_only),
                    contentDescription = "DiaWell DW Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFA855F7).copy(alpha = 0.45f),
                            spotColor = Color(0xFFF0ABFC).copy(alpha = 0.45f),
                        ),
                )
            } else {
                // Progressive stroke-writing reveal
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    // Underneath: 3D logo texture progressively fading in as strokes are written
                    Image(
                        painter = painterResource(id = R.drawable.dw_only),
                        contentDescription = "DiaWell DW Writing",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.15f + writeProgress * 0.85f)
                            .scale(0.92f + writeProgress * 0.08f),
                    )

                    // Top: Dynamic stroke-writing vector path & glowing laser tracer
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val s = w / 512f

                        scale(s, s, pivot = Offset(0f, 0f)) {
                            var penPos: Offset? = null

                            // Phase 1: Draw Letter 'D' (0f to 0.48f)
                            if (writeProgress <= 0.48f) {
                                val pD = (writeProgress / 0.48f).coerceIn(0f, 1f)
                                val lenD = pathMeasureD.length
                                val dstD = Path()
                                pathMeasureD.getSegment(0f, lenD * pD, dstD, true)

                                drawPath(
                                    path = dstD,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFF0ABFC)),
                                        start = Offset(118f, 185f),
                                        end = Offset(250f, 325f),
                                    ),
                                    style = Stroke(
                                        width = 24.dp.toPx() / s,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round,
                                    ),
                                )

                                penPos = pathMeasureD.getPosition(lenD * pD)
                            } else {
                                // 'D' is fully completed!
                                drawPath(
                                    path = pathD,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFF0ABFC)),
                                        start = Offset(118f, 185f),
                                        end = Offset(250f, 325f),
                                    ),
                                    style = Stroke(
                                        width = 24.dp.toPx() / s,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round,
                                    ),
                                )

                                // Phase 2: Draw Letter 'W' (0.48f to 1.0f)
                                val pW = ((writeProgress - 0.48f) / 0.52f).coerceIn(0f, 1f)
                                val lenW = pathMeasureW.length
                                val dstW = Path()
                                pathMeasureW.getSegment(0f, lenW * pW, dstW, true)

                                drawPath(
                                    path = dstW,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFA855F7), Color(0xFFF0ABFC), Color.White),
                                        start = Offset(240f, 260f),
                                        end = Offset(405f, 200f),
                                    ),
                                    style = Stroke(
                                        width = 24.dp.toPx() / s,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round,
                                    ),
                                )

                                penPos = pathMeasureW.getPosition(lenW * pW)
                            }

                            // Glowing Neon Laser Pen Head at current handwriting coordinate
                            penPos?.let { pos ->
                                // Outer bloom
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFFF0ABFC),
                                            Color(0xFFA855F7).copy(alpha = 0.5f),
                                            Color.Transparent,
                                        ),
                                        center = pos,
                                        radius = 28.dp.toPx() / s,
                                    ),
                                    center = pos,
                                    radius = 28.dp.toPx() / s,
                                )

                                // Bright spark core
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx() / s,
                                    center = pos,
                                )
                            }
                        }
                    }
                }
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
