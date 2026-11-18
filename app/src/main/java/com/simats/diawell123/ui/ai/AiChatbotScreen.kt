package com.simats.diawell123.ui.ai

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Live Animated Talking DIVA Robot Avatar.
 * - Pre-loads the muted DIVA talking-mouth animation (R.raw.diva_talking) on a hardware TextureView
 *   with center-crop scaling so playback starts instantaneously when TTS talkback begins.
 * - Crossfades smoothly between the live talking animation (mouth moving, blinking, hovering, sparkles)
 *   during SPEAKING and the gently floating DIVA profile avatar when idle/listening/thinking.
 */
@Composable
fun DivaTalkingAvatar(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Int = 152,
) {
    val context = LocalContext.current
    val latestIsSpeaking by rememberUpdatedState(isSpeaking)
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isVideoReady by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "DivaFloatTransition")
    val idleFloatY by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "DivaIdleFloat",
    )

    val videoAlpha by animateFloatAsState(
        targetValue = if (isSpeaking && isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "DivaVideoCrossfade",
    )

    // Control muted talking video playback strictly from live TTS speaking state
    LaunchedEffect(isSpeaking, mediaPlayer, isVideoReady) {
        val mp = mediaPlayer ?: return@LaunchedEffect
        if (!isVideoReady) return@LaunchedEffect
        try {
            if (isSpeaking) {
                if (!mp.isPlaying) {
                    mp.seekTo(1200) // Start right where DIVA's talking mouth & sparkles are active
                    mp.start()
                }
            } else {
                if (mp.isPlaying) {
                    mp.pause()
                }
            }
        } catch (_: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
            } catch (_: Exception) {}
            mediaPlayer = null
        }
    }

    val rimBrush = remember(isSpeaking) {
        Brush.linearGradient(
            colors = if (isSpeaking) {
                listOf(Color(0xFF00E5FF), Color(0xFFA855F7), Color(0xFFF0ABFC))
            } else {
                listOf(Color(0xFFA855F7), Color(0xFF6D28D9), Color(0xFF00E5FF))
            },
        )
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(0xFF0B0D18))
            .border(width = 2.dp, brush = rimBrush, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Static high-res DIVA robot avatar underneath (with gentle floating hover when idle/listening)
        Image(
            painter = painterResource(id = R.drawable.diva_avatar),
            contentDescription = "DIVA Assistant",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = if (isSpeaking) 0f else idleFloatY.dp.toPx()
                },
        )

        // Muted hardware-accelerated looping talking-mouth video overlay (visible while DIVA is speaking!)
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        private fun applyCenterCrop(viewWidth: Int, viewHeight: Int, videoW: Int, videoH: Int) {
                            if (viewWidth <= 0 || viewHeight <= 0 || videoW <= 0 || videoH <= 0) return
                            val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()
                            val videoRatio = videoW.toFloat() / videoH.toFloat()
                            // Extra 1.16x zoom centers cleanly on DIVA robot and crops out outer edges
                            val zoom = 1.16f
                            val scaleX: Float
                            val scaleY: Float
                            if (videoRatio > viewRatio) {
                                scaleX = (videoRatio / viewRatio) * zoom
                                scaleY = zoom
                            } else {
                                scaleX = zoom
                                scaleY = (viewRatio / videoRatio) * zoom
                            }
                            val matrix = Matrix()
                            matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                            setTransform(matrix)
                        }

                        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                            try {
                                val mp = MediaPlayer().apply {
                                    context.resources.openRawResourceFd(R.raw.diva_talking).use { afd ->
                                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                    }
                                    setSurface(Surface(surfaceTexture))
                                    isLooping = true
                                    setVolume(0f, 0f) // Always muted so DIVA's live TTS voice speaks cleanly
                                    setOnVideoSizeChangedListener { _, vw, vh ->
                                        applyCenterCrop(width, height, vw, vh)
                                    }
                                    setOnPreparedListener { preparedMp ->
                                        applyCenterCrop(width, height, preparedMp.videoWidth, preparedMp.videoHeight)
                                        isVideoReady = true
                                        if (latestIsSpeaking) {
                                            try {
                                                preparedMp.seekTo(1200)
                                                preparedMp.start()
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    prepareAsync()
                                }
                                mediaPlayer = mp
                            } catch (_: Exception) {}
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                            val mp = mediaPlayer ?: return
                            applyCenterCrop(width, height, mp.videoWidth, mp.videoHeight)
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            isVideoReady = false
                            try {
                                mediaPlayer?.let {
                                    if (it.isPlaying) it.stop()
                                    it.release()
                                }
                            } catch (_: Exception) {}
                            mediaPlayer = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = videoAlpha },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiChatbotScreenPreview() {
    Diawell123Theme {
        AiChatbotScreen()
    }
}
