package com.simats.diawell123.ui.ai

import androidx.activity.compose.BackHandler
import com.simats.diawell123.ui.components.DiaWellAmbientBackground
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.simats.diawell123.ui.components.FluidGlassBackButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Siri-Style Glowing Voice Assistant Overlay for DIVA.
 * Supports live RMS waveform reactivity, real-time speech-synced DIVA mouth & body animation,
 * partial STT transcripts, smart action hints, and tap-to-interrupt TTS speaking.
 */
@Composable
fun SiriVoiceOverlay(
    visible: Boolean,
    voiceState: JarvisVoiceState,
    rmsLevel: Float,
    mouthOpenness: Float = 0f,
    mouthShape: DivaMouthShape = DivaMouthShape.SMILE,
    partialTranscript: String,
    latestAiResponse: String,
    voiceError: String?,
    localeLabel: String,
    isAutoSpeakEnabled: Boolean,
    onOrbTap: () -> Unit,
    onQuickVoiceCommand: (String) -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onStopAudio: () -> Unit,
    onSwitchToChat: () -> Unit = {},
    onClose: () -> Unit
) {
    BackHandler(enabled = visible) {
        onClose()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) + scaleIn(initialScale = 0.96f, animationSpec = tween(260)),
        exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220))
    ) {
        DiaWellAmbientBackground(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FluidGlassBackButton(onClick = onClose)

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "DIVA Voice",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$localeLabel • Hands-Free Care",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFA855F7).copy(alpha = 0.24f))
                                .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(14.dp))
                                .clickable { onSwitchToChat() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = "Switch to Chat Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Chat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isAutoSpeakEnabled) Color(0xFF00E5FF).copy(alpha = 0.16f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .border(
                                    1.dp,
                                    if (isAutoSpeakEnabled) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onToggleAutoSpeak() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAutoSpeakEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                    contentDescription = null,
                                    tint = if (isAutoSpeakEnabled) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAutoSpeakEnabled) "On" else "Muted",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAutoSpeakEnabled) Color(0xFF00E5FF) else Color(0xFFCBD5E1),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                .clickable { onClose() }
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Close",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Middle Area: Interactive Glowing Siri Orb + Live Transcript
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val (stateBadgeIcon, stateBadgeText) = when (voiceState) {
                        JarvisVoiceState.LISTENING -> Icons.Rounded.FiberManualRecord to "LISTENING..."
                        JarvisVoiceState.THINKING -> Icons.Rounded.AutoAwesome to "THINKING..."
                        JarvisVoiceState.SPEAKING -> Icons.Rounded.VolumeUp to "DIVA SPEAKING"
                        JarvisVoiceState.ERROR -> Icons.Rounded.Warning to "TRY AGAIN"
                        JarvisVoiceState.IDLE -> Icons.Rounded.Mic to "TAP DW LOGO TO SPEAK"
                    }
                    val stateBadgeColor = when (voiceState) {
                        JarvisVoiceState.LISTENING -> Color(0xFF00E5FF)
                        JarvisVoiceState.THINKING -> Color(0xFFF472B6)
                        JarvisVoiceState.SPEAKING -> Color(0xFF4ADE80)
                        JarvisVoiceState.ERROR -> Color(0xFFF87171)
                        JarvisVoiceState.IDLE -> Color(0xFFE2E8F0)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(stateBadgeColor.copy(alpha = 0.14f))
                            .border(1.dp, stateBadgeColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = stateBadgeIcon,
                                contentDescription = null,
                                tint = stateBadgeColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stateBadgeText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = stateBadgeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Central Full-Body Animated DIVA with Synchronized DW Chest Voice Orb
                    GlowingSiriOrb(
                        voiceState = voiceState,
                        rmsLevel = rmsLevel,
                        mouthOpenness = mouthOpenness,
                        mouthShape = mouthShape,
                        onClick = onOrbTap
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Clean Virtual User Speech Transcript (Unboxed, directly on dark canvas)
                    if (partialTranscript.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "\"$partialTranscript\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 23.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Voice Error Message
                    if (!voiceError.isNullOrBlank()) {
                        Text(
                            text = voiceError,
                            fontSize = 12.5.sp,
                            color = Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Clean Virtual DIVA Spoken Response (Unboxed clean typography directly on canvas)
                    if (latestAiResponse.isNotBlank() && voiceState != JarvisVoiceState.LISTENING) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MarkdownFormattedText(
                                text = latestAiResponse,
                                color = Color(0xFFF1F5F9),
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bottom Voice Command Examples & Action Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Try saying or tap a voice action:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickCommands = listOf(
                            Triple(Icons.Rounded.WaterDrop, "Log fasting sugar 115", "Log fasting sugar 115"),
                            Triple(Icons.Rounded.Medication, "Remind Metformin at 8 PM", "Remind me to take Metformin at 8 PM"),
                            Triple(Icons.Rounded.Restaurant, "Low-GI Indian lunch", "Suggest a low-GI Indian lunch"),
                            Triple(Icons.Rounded.RecordVoiceOver, "En sarkarai 145 after lunch", "En sarkarai 145 after lunch")
                        )
                        quickCommands.forEach { (icon, chipLabel, cmd) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.09f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                    .clickable { onQuickVoiceCommand(cmd) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color(0xFFA855F7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = chipLabel,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val helperHint = when (voiceState) {
                        JarvisVoiceState.LISTENING -> "Tap the DW chest orb when finished speaking"
                        JarvisVoiceState.SPEAKING -> "Tap the DW chest orb to interrupt and speak"
                        JarvisVoiceState.THINKING -> "DIVA is analyzing..."
                        else -> "Tap the DW logo on DIVA's chest to speak"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = helperHint,
                            fontSize = 11.5.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.weight(1f)
                        )

                        if (voiceState == JarvisVoiceState.SPEAKING) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.22f))
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(14.dp))
                                    .clickable { onStopAudio() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Stop,
                                        contentDescription = "Stop Audio",
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Stop Audio",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFCA5A5)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlowingSiriOrb(
    voiceState: JarvisVoiceState,
    rmsLevel: Float,
    mouthOpenness: Float = 0f,
    mouthShape: DivaMouthShape = DivaMouthShape.SMILE,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DivaChestCoreAnim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    JarvisVoiceState.THINKING -> 1400
                    JarvisVoiceState.SPEAKING -> 2200
                    JarvisVoiceState.LISTENING -> 2600
                    else -> 5400
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "chestOrbRotation"
    )

    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chestOrbBreath"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chestWavePhase"
    )

    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chestRipple"
    )

    val smoothRms by animateFloatAsState(
        targetValue = when (voiceState) {
            JarvisVoiceState.LISTENING -> rmsLevel.coerceIn(0.12f, 1f)
            JarvisVoiceState.SPEAKING -> (0.34f + mouthOpenness.coerceIn(0f, 1f) * 0.66f)
            JarvisVoiceState.THINKING -> 0.42f
            else -> 0.06f
        },
        animationSpec = tween(durationMillis = 65),
        label = "smoothRms"
    )

    val isOrbActive = voiceState == JarvisVoiceState.LISTENING ||
        voiceState == JarvisVoiceState.THINKING ||
        voiceState == JarvisVoiceState.SPEAKING

    val orbActivationAlpha by animateFloatAsState(
        targetValue = if (isOrbActive) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "orbActivationAlpha"
    )

    val stageSizeDp = 304
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(stageSizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        // LARGE FULL-BODY VIRTUAL ASSISTANT DIVA ROBOT + SYNCHRONIZED DW CHEST VOICE ORB
        DivaTalkingAvatar(
            isSpeaking = voiceState == JarvisVoiceState.SPEAKING,
            mouthOpenness = mouthOpenness,
            mouthShape = mouthShape,
            sizeDp = stageSizeDp,
            fullBody = true,
            modifier = Modifier.fillMaxSize(),
            chestOverlay = {
                // INTERACTIVE VOICE ORB CENTERED EXACTLY ON DIVA'S DW CHEST LOGO (x=50%, y=61.5%)
                // Moves, floats, and tilts in 100% sync with DIVA's body and speech!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.50f
                    val cy = size.height * 0.615f
                    val dwCoreRadius = size.minDimension * 0.148f

                    // Subtle inviting glow halo on the DW chest logo even when IDLE
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFA855F7).copy(alpha = 0.22f * breathing),
                                Color(0xFF00E5FF).copy(alpha = 0.10f * breathing),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = dwCoreRadius * 1.45f
                        ),
                        radius = dwCoreRadius * 1.45f,
                        center = Offset(cx, cy)
                    )

                    // Active Glowing Siri Voice Orb erupting right inside/around the DW chest emblem!
                    if (orbActivationAlpha > 0.01f) {
                        val activeRadius = dwCoreRadius * (0.98f + smoothRms * 0.14f) * breathing

                        // Expanding voice energy ripple wave radiating out from DW chest core
                        val rippleRadius = dwCoreRadius * (1.0f + rippleProgress * (0.85f + smoothRms * 0.65f))
                        val rippleAlpha = (1f - rippleProgress) * 0.55f * orbActivationAlpha
                        drawCircle(
                            color = when (voiceState) {
                                JarvisVoiceState.SPEAKING -> Color(0xFF00E5FF).copy(alpha = rippleAlpha)
                                JarvisVoiceState.THINKING -> Color(0xFFF472B6).copy(alpha = rippleAlpha)
                                else -> Color(0xFF00E5FF).copy(alpha = rippleAlpha)
                            },
                            radius = rippleRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.2.dp.toPx())
                        )

                        // Soft glowing plasma aura centered right on the DW logo
                        val auraColors = when (voiceState) {
                            JarvisVoiceState.SPEAKING -> listOf(
                                Color(0xFF00E5FF).copy(alpha = (0.32f + smoothRms * 0.26f) * orbActivationAlpha),
                                Color(0xFFA855F7).copy(alpha = 0.28f * orbActivationAlpha),
                                Color.Transparent
                            )
                            JarvisVoiceState.THINKING -> listOf(
                                Color(0xFFFF2E93).copy(alpha = 0.46f * orbActivationAlpha),
                                Color(0xFF8B5CF6).copy(alpha = 0.26f * orbActivationAlpha),
                                Color.Transparent
                            )
                            else -> listOf(
                                Color(0xFF00E5FF).copy(alpha = (0.40f + smoothRms * 0.25f) * orbActivationAlpha),
                                Color(0xFFA855F7).copy(alpha = 0.28f * orbActivationAlpha),
                                Color.Transparent
                            )
                        }
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = auraColors,
                                center = Offset(cx, cy),
                                radius = dwCoreRadius * 1.95f
                            ),
                            radius = dwCoreRadius * 1.95f,
                            center = Offset(cx, cy)
                        )

                        // Primary rotating neon Siri plasma ring hugging the DW chest emblem
                        rotate(degrees = rotationAngle, pivot = Offset(cx, cy)) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = orbActivationAlpha),
                                        Color(0xFF8B5CF6).copy(alpha = orbActivationAlpha),
                                        Color(0xFFFF2E93).copy(alpha = orbActivationAlpha),
                                        Color(0xFFF0ABFC).copy(alpha = orbActivationAlpha),
                                        Color(0xFF00E5FF).copy(alpha = orbActivationAlpha)
                                    ),
                                    center = Offset(cx, cy)
                                ),
                                radius = activeRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 3.2.dp.toPx())
                            )
                        }

                        // Counter-rotating voice-synced liquid plasma waveform ring right on the DW logo
                        rotate(degrees = -rotationAngle * 1.35f, pivot = Offset(cx, cy)) {
                            val points = 60
                            val ringPath = Path()
                            val amp = (3.0f + smoothRms * 8.5f) * orbActivationAlpha
                            for (i in 0..points) {
                                val theta = (i.toFloat() / points) * 2f * PI.toFloat()
                                val r = activeRadius * 0.92f + sin(theta * 5f + wavePhase) * amp
                                val px = cx + r * cos(theta)
                                val py = cy + r * sin(theta)
                                if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
                            }
                            ringPath.close()
                            drawPath(
                                path = ringPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.90f * orbActivationAlpha),
                                        Color(0xFFA855F7).copy(alpha = 0.90f * orbActivationAlpha),
                                        Color(0xFFF0ABFC).copy(alpha = 0.90f * orbActivationAlpha)
                                    )
                                ),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Mini live audio equalizer bars pulsing inside DW chest core during LISTENING
                        if (voiceState == JarvisVoiceState.LISTENING) {
                            val barCount = 5
                            val spacing = 7.dp.toPx()
                            val startX = cx - ((barCount - 1) * spacing) / 2f
                            for (b in 0 until barCount) {
                                val weight = 1f - (kotlin.math.abs(b - 2) * 0.22f)
                                val osc = kotlin.math.abs(sin(wavePhase + b * 0.85f))
                                val barH = (6f + smoothRms * 22f * osc * weight).dp.toPx()
                                    .coerceAtMost(dwCoreRadius * 1.15f)
                                val bx = startX + b * spacing
                                drawLine(
                                    color = if (b % 2 == 0) Color(0xFF00E5FF) else Color(0xFFF0ABFC),
                                    start = Offset(bx, cy - barH / 2f),
                                    end = Offset(bx, cy + barH / 2f),
                                    strokeWidth = 2.8.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // DEDICATED CLICKABLE HOTSPOT LOCKED RIGHT OVER DIVA'S DW CHEST LOGO
                Box(
                    modifier = Modifier
                        .offset(y = 35.dp)
                        .size(106.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onClick()
                        }
                )
            }
        )
    }
}

