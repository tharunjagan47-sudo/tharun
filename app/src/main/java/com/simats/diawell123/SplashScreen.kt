package com.simats.diawell123

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Timeline progress from 0ms to 6500ms
    val animTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // High-volume system tone generator for audible spin clicks & lock
        val toneGen = try {
            ToneGenerator(AudioManager.STREAM_SYSTEM, 70)
        } catch (_: Exception) {
            try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (_: Exception) {
                null
            }
        }

        launch {
            animTime.animateTo(
                targetValue = 6500f,
                animationSpec = tween(
                    durationMillis = 6500,
                    easing = LinearEasing,
                ),
            )
        }

        // Haptic pulses during spin (0ms to 2400ms)
        var lastTick = 0L
        while (animTime.value < 2600f) {
            val t = animTime.value
            if (t in 200f..2600f) {
                val interval = 60 + ((t / 2600f) * (t / 2600f) * 220).toLong()
                if (System.currentTimeMillis() - lastTick > interval) {
                    lastTick = System.currentTimeMillis()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 20)
                    try {
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(15L)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            delay(30)
        }

        // Wait until lock-in at 5400ms for heavy haptic confirmation
        while (animTime.value < 5400f) {
            delay(50)
        }
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 60)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50L)
                }
            }
        } catch (_: Exception) {}
        toneGen?.release()
    }

    val tMs = animTime.value

    // SCENE 1 & 2: Ring Rotation
    // 0ms - 1800ms: Accelerates spin
    // 1800ms - 2800ms: Decelerates and stops dead at 1080f (upright)
    // 2800ms+: STAYS COMPLETELY STATIONARY AT 0° / 1080°!
    val ringRotation = remember(tMs) {
        when {
            tMs < 200f -> 0f
            tMs < 1800f -> {
                val p = (tMs - 200f) / 1600f
                (p * p * 720f)
            }
            tMs < 2800f -> {
                val p = (tMs - 1800f) / 1000f
                val decel = 1f - (1f - p) * (1f - p) * (1f - p)
                720f + (decel * 360f)
            }
            else -> 1080f // 3 full cycles, exactly equivalent to 0° upright and 100% stationary!
        }
    }

    // SCENE 3: DW Drawing Progress (2800ms to 5400ms)
    val dwDrawProgress = remember(tMs) {
        when {
            tMs < 2800f -> 0f
            tMs < 5200f -> (tMs - 2800f) / 2400f
            else -> 1f
        }
    }

    // SCENE 4: Final Pulse & Glow
    val isRevealed = tMs >= 5200f
    val finalPulseAlpha = remember(tMs) {
        if (tMs >= 5400f && tMs < 6200f) {
            1f - (tMs - 5400f) / 800f
        } else 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), // Deep black background (Strict Rule)
        contentAlignment = Alignment.Center,
    ) {
        // Shockwave pulse in Scene 4
        if (finalPulseAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size((280 + (1f - finalPulseAlpha) * 200).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0ABFC).copy(alpha = finalPulseAlpha * 0.35f))
            )
        }

        // Stationary / Spinning Ring Container
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 1. Outer Circular Ring: Spins, stops at 2.8s, remains stationary thereafter!
            Image(
                painter = painterResource(id = R.drawable.ring_only),
                contentDescription = "DiaWell Outer Ring",
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(ringRotation),
            )

            // 2. DW Monogram Reveal (Scene 3 & 4)
            if (tMs >= 2800f) {
                if (isRevealed) {
                    // Fully solid 3D DW Monogram settled inside stationary ring
                    Image(
                        painter = painterResource(id = R.drawable.dw_only),
                        contentDescription = "DiaWell Monogram",
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFFA855F7).copy(alpha = 0.55f),
                                spotColor = Color(0xFFF0ABFC).copy(alpha = 0.55f)
                            )
                    )
                } else {
                    // Progressive reveal with neon glow pen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dw_only),
                            contentDescription = "DiaWell Monogram Reveal",
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(dwDrawProgress)
                                .scale(0.92f + dwDrawProgress * 0.08f)
                        )

                        // Dynamic glowing laser tracer
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f

                            // Neon laser pen coordinates
                            val penX: Float
                            val penY: Float
                            if (dwDrawProgress < 0.48f) {
                                val pD = dwDrawProgress / 0.48f
                                val angle = pD * (Math.PI * 2).toFloat()
                                penX = cx - (w * 0.20f) + (Math.sin(angle.toDouble()) * (w * 0.12f)).toFloat()
                                penY = cy - (h * 0.14f) + (pD * (h * 0.28f))
                            } else {
                                val pW = (dwDrawProgress - 0.48f) / 0.52f
                                val wave = Math.sin((pW * Math.PI * 2.5).toDouble()).toFloat()
                                penX = cx - (w * 0.04f) + (pW * (w * 0.32f))
                                penY = cy + (wave * (h * 0.14f))
                            }

                            // Glowing laser pen bloom
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF0ABFC),
                                        Color(0xFFA855F7).copy(alpha = 0.4f),
                                        Color.Transparent
                                    ),
                                    center = Offset(penX, penY),
                                    radius = 28.dp.toPx()
                                ),
                                center = Offset(penX, penY),
                                radius = 28.dp.toPx()
                            )

                            // Laser hot spot
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(penX, penY)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArcReactorCanvas(
    rotationAngle: Float = 0f,
    modifier: Modifier = Modifier,
    isSplashTransform: Boolean = false,
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
