package com.simats.diawell123.ui.ai

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.R
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Backwards-compatible composable forwarding to DIVA (DiaWell Intelligent Virtual Assistant) ChatScreen.
 * Seamlessly integrates local Ollama Qwen 2.5 backend and speech-to-text.
 */
@Composable
fun AiChatbotScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    ChatScreen(
        modifier = modifier,
        viewModel = viewModel,
        onBackClick = onBackClick,
    )
}

fun lerpFloat(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp = start + (stop - start) * fraction

/**
 * DIVA (DiaWell Intelligent Virtual Assistant) Profile Avatar.
 * Displays the official purple DIVA robot with DW chest emblem inside a glowing neon rim.
 */
@Composable
fun IronmanDoctorAvatar(
    modifier: Modifier = Modifier,
    sizeDp: Int = 40,
) {
    val rimBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFA855F7),
                Color(0xFF00E5FF),
                Color(0xFFF0ABFC),
            ),
        )
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(0xFF0B0D18))
            .border(
                width = if (sizeDp >= 44) 1.5.dp else 1.2.dp,
                brush = rimBrush,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.diva_avatar),
            contentDescription = "DIVA AI Assistant",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Live Speech-Synced Animated DIVA Robot Avatar (100% Procedural GPU Compose Animation — Zero Video).
 * - Real-time lip-sync mouth visemes (SMILE, HALF_OPEN, WIDE_OPEN, ROUND_O) driven directly by live TTS words.
 * - Synchronized robot body hover, expressive speech-reactive head/body tilt, natural periodic eye blinks,
 *   glowing cyan eye luminance, and ambient purple energy waves + floating sparkles.
 * - Optional [chestOverlay] lives inside the exact same animated container so the DW chest voice orb
 *   stays 100% locked onto DIVA's DW emblem as she floats and speaks.
 */
@Composable
fun DivaTalkingAvatar(
    isSpeaking: Boolean,
    mouthOpenness: Float = 0f,
    mouthShape: DivaMouthShape = DivaMouthShape.SMILE,
    modifier: Modifier = Modifier,
    sizeDp: Int = 152,
    fullBody: Boolean = false,
    chestOverlay: (@Composable BoxScope.() -> Unit)? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DivaLivingAnim")

    val floatPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "DivaFloatPhase",
    )

    val swayPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "DivaSwayPhase",
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "DivaAmbientWavePhase",
    )

    // Ultra-responsive speech mouth interpolation (52ms) for crisp syllable sync
    val smoothOpen by animateFloatAsState(
        targetValue = if (isSpeaking) mouthOpenness.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 52, easing = FastOutSlowInEasing),
        label = "DivaMouthOpen",
    )

    val speakingFactor by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "DivaSpeakingFactor",
    )

    // Natural organic eye-blinking cycle
    val blinkAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val intervals = longArrayOf(2800L, 3600L, 2100L, 4100L, 2600L)
        var idx = 0
        while (true) {
            delay(intervals[idx % intervals.size])
            idx++
            blinkAnim.animateTo(1f, animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing))
            blinkAnim.animateTo(0f, animationSpec = tween(durationMillis = 95, easing = FastOutSlowInEasing))
        }
    }
    val blinkProgress = blinkAnim.value

    val rimBrush = remember(isSpeaking) {
        Brush.linearGradient(
            colors = if (isSpeaking) {
                listOf(Color(0xFF00E5FF), Color(0xFFA855F7), Color(0xFFF0ABFC))
            } else {
                listOf(Color(0xFFA855F7), Color(0xFF6D28D9), Color(0xFF00E5FF))
            },
        )
    }

    val containerModifier = if (fullBody) {
        modifier
            .fillMaxWidth()
            .height(sizeDp.dp)
    } else {
        modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(0xFF0B0D18))
            .border(width = 2.dp, brush = rimBrush, shape = CircleShape)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        // 1. Ambient purple energy waves & drifting sparkles behind DIVA (react to speech energy!)
        if (fullBody) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val energy = 0.28f + speakingFactor * 0.45f + smoothOpen * 0.35f

                // Flowing luminous purple-cyan energy ribbons across mid-stage
                for (ribbon in 0..2) {
                    val ribbonPath = Path()
                    val baseY = h * (0.44f + ribbon * 0.08f)
                    val amp = h * (0.025f + energy * 0.032f) * (1f - ribbon * 0.16f)
                    val freq = 2.2f + ribbon * 0.65f
                    val phaseOffset = wavePhase + ribbon * 1.45f
                    val steps = 36
                    for (s in 0..steps) {
                        val t = s.toFloat() / steps
                        val x = t * w
                        val edgeFade = sin(t * PI).toFloat()
                        val y = baseY + sin(t * freq * PI.toFloat() + phaseOffset) * amp * edgeFade
                        if (s == 0) ribbonPath.moveTo(x, y) else ribbonPath.lineTo(x, y)
                    }
                    val ribbonColor = when (ribbon) {
                        0 -> Color(0xFFA855F7)
                        1 -> Color(0xFF00E5FF)
                        else -> Color(0xFFF0ABFC)
                    }
                    drawPath(
                        path = ribbonPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ribbonColor.copy(alpha = (0.14f + energy * 0.24f).coerceAtMost(0.45f)),
                                Color.Transparent,
                            ),
                        ),
                        style = Stroke(width = (1.6f + ribbon * 0.5f).dp.toPx(), cap = StrokeCap.Round),
                    )
                }

                // Soft drifting golden & cyan 4-point sparkles around DIVA
                val sparkleSeeds = listOf(
                    Triple(0.14f, 0.24f, 0.0f),
                    Triple(0.86f, 0.21f, 1.3f),
                    Triple(0.11f, 0.56f, 2.5f),
                    Triple(0.89f, 0.52f, 3.8f),
                    Triple(0.18f, 0.78f, 4.9f),
                    Triple(0.84f, 0.76f, 5.7f),
                )
                sparkleSeeds.forEachIndexed { idx, (sx, sy, phaseSeed) ->
                    val pulse = (0.45f + 0.55f * sin(wavePhase * 1.4f + phaseSeed)).coerceIn(0f, 1f)
                    val driftY = cos(floatPhase + phaseSeed) * h * 0.018f
                    val cx = w * sx
                    val cy = h * sy + driftY
                    val starR = (2.8f + pulse * 3.0f * energy).dp.toPx()
                    val starColor = if (idx % 2 == 0) Color(0xFFFDE68A) else Color(0xFF7DF9FF)
                    val alpha = (0.16f + pulse * 0.52f * energy).coerceAtMost(0.82f)
                    drawSparkleStar(center = Offset(cx, cy), radius = starR, color = starColor.copy(alpha = alpha))
                }
            }
        }

        var isVideoReady by remember { mutableStateOf(false) }
        val videoAlpha by animateFloatAsState(
            targetValue = if (fullBody && isSpeaking && isVideoReady) 1f else 0f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            label = "DivaSpeakingVideoAlpha",
        )

        // 2. UNIFIED ANIMATED ROBOT STAGE (Idle Cutout <-> Synced 3D Gesture Video + DW Chest Orb)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val baseFloat = sin(floatPhase) * (if (fullBody) 5.0f else 2.8f)
                    translationY = baseFloat.dp.toPx()

                    val idleSway = sin(swayPhase) * 0.45f
                    val talkSway = sin(swayPhase) * (speakingFactor * 0.85f)
                    rotationZ = idleSway + talkSway

                    val breath = sin(floatPhase + 0.7f) * 0.006f
                    scaleY = 1f + breath
                    scaleX = 1f - breath * 0.35f
                    transformOrigin = TransformOrigin(0.5f, 0.58f)
                },
            contentAlignment = Alignment.Center,
        ) {
            if (fullBody) {
                DivaSpeakingVideoLayer(
                    isSpeaking = isSpeaking,
                    alpha = videoAlpha,
                    onVideoReady = { isVideoReady = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Base High-Res DIVA Robot Illustration (crossfades smoothly when 3D gesture video speaks)
            Image(
                painter = painterResource(id = if (fullBody) R.drawable.diva_robot_cutout else R.drawable.diva_avatar),
                contentDescription = "DIVA Assistant",
                contentScale = if (fullBody) ContentScale.Fit else ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - videoAlpha
                    },
            )

            // Live Procedural Face Visor Animation (Blinking Eyes + Speech-Synced Glowing Cyan Mouth)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - videoAlpha },
            ) {
                val w = size.width
                val h = size.height
                val visorDark = Color(0xFF0A111D)

                // Exact measured normalized facial feature positions on diva_avatar.png (512x512)
                val leftEyeCenter = Offset(w * 0.387f, h * 0.254f)
                val rightEyeCenter = Offset(w * 0.611f, h * 0.254f)
                val eyeW = w * 0.084f
                val eyeH = h * 0.058f

                // --- A. Expressive Speech Eye Glow + Natural Blinking ---
                if (smoothOpen > 0.06f) {
                    val glowAlpha = (smoothOpen * 0.22f).coerceIn(0f, 0.26f)
                    listOf(leftEyeCenter, rightEyeCenter).forEach { eyeC ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4BF6FF).copy(alpha = glowAlpha),
                                    Color.Transparent,
                                ),
                                center = eyeC,
                                radius = eyeW * 0.95f,
                            ),
                            radius = eyeW * 0.95f,
                            center = eyeC,
                        )
                    }
                }

                if (blinkProgress > 0.02f) {
                    listOf(leftEyeCenter, rightEyeCenter).forEach { eyeC ->
                        // Smooth visor eyelid wipe covering the glowing cyan eye
                        val coverH = eyeH * (0.25f + blinkProgress * 0.95f)
                        drawOval(
                            color = visorDark.copy(alpha = (0.4f + blinkProgress * 0.6f).coerceAtMost(1f)),
                            topLeft = Offset(eyeC.x - eyeW * 0.56f, eyeC.y - eyeH * 0.56f),
                            size = Size(eyeW * 1.12f, coverH),
                        )
                        // Crisp glowing cyan closed-eye happy blink arc at peak blink
                        if (blinkProgress > 0.45f) {
                            val arcAlpha = ((blinkProgress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                            val arcPath = Path().apply {
                                moveTo(eyeC.x - eyeW * 0.38f, eyeC.y + eyeH * 0.06f)
                                quadraticTo(
                                    eyeC.x,
                                    eyeC.y - eyeH * 0.24f,
                                    eyeC.x + eyeW * 0.38f,
                                    eyeC.y + eyeH * 0.06f,
                                )
                            }
                            drawPath(
                                path = arcPath,
                                color = Color(0xFF5CF6FF).copy(alpha = arcAlpha),
                                style = Stroke(
                                    width = (w * 0.011f).coerceAtLeast(1.8f),
                                    cap = StrokeCap.Round,
                                ),
                            )
                        }
                    }
                }

                // --- B. Real-Time Speech-Synced Animated Cyan Mouth ---
                if (speakingFactor > 0.02f || smoothOpen > 0.02f) {
                    val mouthCenter = Offset(w * 0.499f, h * 0.338f)
                    val maskAlpha = (speakingFactor.coerceAtLeast(smoothOpen * 2f)).coerceIn(0f, 1f)

                    // 1. Cleanly mask out the static mouth on the dark #0A111D OLED visor
                    drawOval(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to visorDark.copy(alpha = maskAlpha),
                                0.72f to visorDark.copy(alpha = maskAlpha),
                                1.00f to Color.Transparent,
                            ),
                            center = mouthCenter,
                            radius = w * 0.062f,
                        ),
                        topLeft = Offset(mouthCenter.x - w * 0.062f, mouthCenter.y - h * 0.044f),
                        size = Size(w * 0.124f, h * 0.088f),
                    )

                    // 2. Soft neon cyan mouth glow bloom on visor glass
                    val bloomRadius = w * (0.045f + smoothOpen * 0.032f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = (0.16f + smoothOpen * 0.26f) * maskAlpha),
                                Color.Transparent,
                            ),
                            center = mouthCenter,
                            radius = bloomRadius,
                        ),
                        radius = bloomRadius,
                        center = mouthCenter,
                    )

                    // 3. Render active viseme shape synced to live TTS syllable
                    val mouthFillBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8CFBFF).copy(alpha = maskAlpha),
                            Color(0xFF2AE8FF).copy(alpha = maskAlpha),
                            Color(0xFF00C2E8).copy(alpha = maskAlpha),
                        ),
                        startY = mouthCenter.y - h * 0.03f,
                        endY = mouthCenter.y + h * 0.045f,
                    )
                    val innerCavityColor = Color(0xFF072B3F).copy(alpha = 0.85f * maskAlpha)

                    when {
                        smoothOpen < 0.14f || mouthShape == DivaMouthShape.SMILE -> {
                            // Cute glowing cyan crescent smile between words
                            val mw = w * 0.052f
                            val mh = h * (0.018f + smoothOpen * 0.026f)
                            val smilePath = Path().apply {
                                moveTo(mouthCenter.x - mw * 0.5f, mouthCenter.y - mh * 0.25f)
                                quadraticTo(
                                    mouthCenter.x,
                                    mouthCenter.y - mh * 0.05f,
                                    mouthCenter.x + mw * 0.5f,
                                    mouthCenter.y - mh * 0.25f,
                                )
                                quadraticTo(
                                    mouthCenter.x,
                                    mouthCenter.y + mh * 1.15f,
                                    mouthCenter.x - mw * 0.5f,
                                    mouthCenter.y - mh * 0.25f,
                                )
                                close()
                            }
                            drawPath(path = smilePath, brush = mouthFillBrush)
                        }

                        mouthShape == DivaMouthShape.ROUND_O -> {
                            // Expressive rounded cyan "O" mouth (for O/U/W syllables)
                            val rx = w * (0.020f + smoothOpen * 0.012f)
                            val ry = h * (0.017f + smoothOpen * 0.021f)
                            val cy = mouthCenter.y + h * 0.004f
                            drawOval(
                                brush = mouthFillBrush,
                                topLeft = Offset(mouthCenter.x - rx, cy - ry),
                                size = Size(rx * 2f, ry * 2f),
                            )
                            // Dark inner vocal core for crisp 3D OLED look
                            drawOval(
                                color = innerCavityColor,
                                topLeft = Offset(mouthCenter.x - rx * 0.54f, cy - ry * 0.54f),
                                size = Size(rx * 1.08f, ry * 1.08f),
                            )
                        }

                        mouthShape == DivaMouthShape.WIDE_OPEN -> {
                            // Expressive wide-open smiling D-mouth (for A/E/I syllables)
                            val halfW = w * (0.030f + smoothOpen * 0.014f)
                            val topY = mouthCenter.y - h * 0.010f
                            val botY = mouthCenter.y + h * (0.016f + smoothOpen * 0.030f)

                            val openPath = Path().apply {
                                moveTo(mouthCenter.x - halfW, topY)
                                quadraticTo(mouthCenter.x, topY + h * 0.005f, mouthCenter.x + halfW, topY)
                                cubicTo(
                                    mouthCenter.x + halfW * 0.92f,
                                    botY,
                                    mouthCenter.x - halfW * 0.92f,
                                    botY,
                                    mouthCenter.x - halfW,
                                    topY,
                                )
                                close()
                            }
                            drawPath(path = openPath, brush = mouthFillBrush)

                            // Subtle inner vocal shadow when mouth opens wide
                            if (smoothOpen > 0.42f) {
                                val innerW = halfW * 0.56f
                                val innerTop = topY + (botY - topY) * 0.22f
                                val innerBot = botY - (botY - topY) * 0.24f
                                drawOval(
                                    color = innerCavityColor,
                                    topLeft = Offset(mouthCenter.x - innerW, innerTop),
                                    size = Size(innerW * 2f, (innerBot - innerTop).coerceAtLeast(1f)),
                                )
                            }

                            // Crisp luminous cyan lip outline
                            drawPath(
                                path = openPath,
                                color = Color(0xFFB4FDFF).copy(alpha = 0.72f * maskAlpha),
                                style = Stroke(
                                    width = (w * 0.005f).coerceAtLeast(1.1f),
                                    join = StrokeJoin.Round,
                                ),
                            )
                        }

                        else -> {
                            // HALF_OPEN: sleek glowing cyan speaking capsule
                            val halfW = w * (0.025f + smoothOpen * 0.011f)
                            val halfH = h * (0.011f + smoothOpen * 0.014f)
                            val cy = mouthCenter.y + h * 0.003f
                            drawRoundRect(
                                brush = mouthFillBrush,
                                topLeft = Offset(mouthCenter.x - halfW, cy - halfH),
                                size = Size(halfW * 2f, halfH * 2f),
                                cornerRadius = CornerRadius(halfH, halfH),
                            )
                        }
                    }
                }
            }

            // 3. Synchronized DW Chest Overlay (moves & tilts identically with DIVA!)
            chestOverlay?.invoke(this)
        }
    }
}

/**
 * Hardware-accelerated looping video layer for DIVA's speaking gesture clip.
 * Pre-prepares MediaPlayer on mount, mutes embedded audio, crops out bottom subtitles
 * and corner watermark, and crossfades smoothly with the idle cutout.
 */
@Composable
private fun DivaSpeakingVideoLayer(
    isSpeaking: Boolean,
    alpha: Float,
    onVideoReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val latestIsSpeaking by rememberUpdatedState(isSpeaking)
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }
    var videoWidth by remember { mutableIntStateOf(16) }
    var videoHeight by remember { mutableIntStateOf(9) }

    LaunchedEffect(isSpeaking, isPrepared) {
        val player = mediaPlayer ?: return@LaunchedEffect
        if (!isPrepared) return@LaunchedEffect
        try {
            if (isSpeaking) {
                if (!player.isPlaying) {
                    player.start()
                }
            } else {
                if (player.isPlaying) {
                    player.pause()
                }
                player.seekTo(0)
            }
        } catch (_: IllegalStateException) {
            // Ignore transient state during lifecycle teardown
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
            } catch (_: IllegalStateException) {
            }
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                this.alpha = alpha
            }
            .drawWithContent {
                drawContent()
                val radius = size.maxDimension * 0.56f
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black,
                            0.58f to Color.Black,
                            0.80f to Color.Black.copy(alpha = 0.62f),
                            0.98f to Color.Transparent,
                            1.00f to Color.Transparent,
                        ),
                        center = Offset(size.width * 0.50f, size.height * 0.46f),
                        radius = radius,
                    ),
                    blendMode = BlendMode.DstIn,
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black,
                            0.76f to Color.Black,
                            0.92f to Color.Transparent,
                            1.00f to Color.Transparent,
                        ),
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    val textureView = this
                    isOpaque = false
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int,
                        ) {
                            val player = MediaPlayer().apply {
                                val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.diva_voice_motion}")
                                setDataSource(ctx, videoUri)
                                setSurface(Surface(surfaceTexture))
                                isLooping = true
                                setVolume(0f, 0f)
                                setOnVideoSizeChangedListener { _, vw, vh ->
                                    if (vw > 0 && vh > 0) {
                                        videoWidth = vw
                                        videoHeight = vh
                                        applyVideoCenterCropMatrix(
                                            textureView = textureView,
                                            viewWidth = width,
                                            viewHeight = height,
                                            videoWidth = vw,
                                            videoHeight = vh,
                                        )
                                    }
                                }
                                setOnPreparedListener { mp ->
                                    isPrepared = true
                                    onVideoReady()
                                    applyVideoCenterCropMatrix(
                                        textureView = textureView,
                                        viewWidth = width,
                                        viewHeight = height,
                                        videoWidth = videoWidth,
                                        videoHeight = videoHeight,
                                    )
                                    if (latestIsSpeaking) {
                                        mp.start()
                                    } else {
                                        mp.seekTo(0)
                                    }
                                }
                                prepareAsync()
                            }
                            mediaPlayer = player
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int,
                        ) {
                            applyVideoCenterCropMatrix(
                                textureView = textureView,
                                viewWidth = width,
                                viewHeight = height,
                                videoWidth = videoWidth,
                                videoHeight = videoHeight,
                            )
                        }

                        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                            try {
                                mediaPlayer?.stop()
                            } catch (_: IllegalStateException) {
                            }
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPrepared = false
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun applyVideoCenterCropMatrix(
    textureView: TextureView,
    viewWidth: Int,
    viewHeight: Int,
    videoWidth: Int,
    videoHeight: Int,
) {
    if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return

    // Fit the full robot + both outstretched hands (middle ~82% width, top ~84% height)
    // while keeping bottom Tamil subtitles and corner watermark cropped out.
    val scale = minOf(
        viewWidth.toFloat() / (videoWidth.toFloat() * 0.82f),
        viewHeight.toFloat() / (videoHeight.toFloat() * 0.84f),
    )

    val scaledWidth = videoWidth * scale
    val scaledHeight = videoHeight * scale

    val dx = (viewWidth - scaledWidth) * 0.5f
    val dy = viewHeight * 0.49f - scaledHeight * 0.43f

    val matrix = Matrix().apply {
        setScale(scaledWidth / viewWidth.toFloat(), scaledHeight / viewHeight.toFloat())
        postTranslate(dx, dy)
    }
    textureView.setTransform(matrix)
}

private fun DrawScope.drawSparkleStar(center: Offset, radius: Float, color: Color) {
    val inner = radius * 0.28f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path = path, color = color)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiChatbotScreenPreview() {
    Diawell123Theme {
        AiChatbotScreen()
    }
}
