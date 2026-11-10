package com.simats.diawell123.ui.ai

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Siri-Style Glowing Voice Assistant Overlay for Dr. Jarvis.
 * Supports live RMS waveform reactivity, partial STT transcripts, smart action hints,
 * and tap-to-interrupt TTS speaking.
 */
@Composable
fun SiriVoiceOverlay(
    visible: Boolean,
    voiceState: JarvisVoiceState,
    rmsLevel: Float,
    partialTranscript: String,
    latestAiResponse: String,
    voiceError: String?,
    localeLabel: String,
    isAutoSpeakEnabled: Boolean,
    onOrbTap: () -> Unit,
    onQuickVoiceCommand: (String) -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onStopAudio: () -> Unit,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) + scaleIn(initialScale = 0.96f, animationSpec = tween(260)),
        exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF050816).copy(alpha = 0.97f),
                            Color(0xFF150A28).copy(alpha = 0.98f),
                            Color(0xFF050816).copy(alpha = 0.99f)
                        )
                    )
                )
                .systemBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dr. Jarvis Voice ⚡",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Voice Mode ($localeLabel) • Hands-Free Care",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isAutoSpeakEnabled) "🔊 Voice On" else "🔇 Muted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoSpeakEnabled) Color(0xFF00E5FF) else Color(0xFFCBD5E1)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                .clickable { onClose() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✕ Close",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
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

                    val stateBadgeText = when (voiceState) {
                        JarvisVoiceState.LISTENING -> "● LISTENING..."
                        JarvisVoiceState.THINKING -> "⚡ THINKING..."
                        JarvisVoiceState.SPEAKING -> "🔊 DR. JARVIS SPEAKING"
                        JarvisVoiceState.ERROR -> "⚠️ TRY AGAIN"
                        JarvisVoiceState.IDLE -> "🎙️ TAP ORB TO SPEAK"
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
                        Text(
                            text = stateBadgeText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = stateBadgeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Central Glowing Siri Orb
                    GlowingSiriOrb(
                        voiceState = voiceState,
                        rmsLevel = rmsLevel,
                        onClick = onOrbTap
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Live User Speech Transcript Box
                    if (partialTranscript.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "YOU SAID",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$partialTranscript\"",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
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

                    // Latest Dr. Jarvis Spoken Response Preview
                    if (latestAiResponse.isNotBlank() && voiceState != JarvisVoiceState.LISTENING) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1B4B).copy(alpha = 0.72f))
                                .border(1.dp, Color(0xFFFF2E93).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column {
                                Text(
                                    text = "DR. JARVIS",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF472B6),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                MarkdownFormattedText(
                                    text = latestAiResponse,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
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
                            "🩸 Log fasting sugar 115" to "Log fasting sugar 115",
                            "💊 Remind Metformin at 8 PM" to "Remind me to take Metformin at 8 PM",
                            "🍛 Low-GI Indian lunch" to "Suggest a low-GI Indian lunch",
                            "🗣️ En sarkarai 145 after lunch" to "En sarkarai 145 after lunch"
                        )
                        quickCommands.forEach { (chipLabel, cmd) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.09f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                    .clickable { onQuickVoiceCommand(cmd) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = chipLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val helperHint = when (voiceState) {
                        JarvisVoiceState.LISTENING -> "Tap Orb when finished speaking"
                        JarvisVoiceState.SPEAKING -> "Tap Orb to interrupt and speak"
                        JarvisVoiceState.THINKING -> "Executing & analyzing..."
                        else -> "Tap the Glowing Orb to speak"
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
                                Text(
                                    text = "⏹ Stop Audio",
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

@Composable
fun GlowingSiriOrb(
    voiceState: JarvisVoiceState,
    rmsLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SiriOrbAnim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    JarvisVoiceState.THINKING -> 1600
                    JarvisVoiceState.SPEAKING -> 2400
                    else -> 5200
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRotation"
    )

    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbBreath"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val smoothRms by animateFloatAsState(
        targetValue = when (voiceState) {
            JarvisVoiceState.LISTENING -> rmsLevel.coerceIn(0.08f, 1f)
            JarvisVoiceState.SPEAKING -> 0.55f
            JarvisVoiceState.THINKING -> 0.35f
            else -> 0.05f
        },
        animationSpec = tween(durationMillis = 110),
        label = "smoothRms"
    )

    Box(
        modifier = modifier
            .size(210.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val baseRadius = size.minDimension * 0.30f * breathing + (smoothRms * 18f)

            // Outer ambient glow
            val auraColors = when (voiceState) {
                JarvisVoiceState.SPEAKING -> listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.40f),
                    Color(0xFF4ADE80).copy(alpha = 0.18f),
                    Color.Transparent
                )
                JarvisVoiceState.THINKING -> listOf(
                    Color(0xFFFF2E93).copy(alpha = 0.42f),
                    Color(0xFF8B5CF6).copy(alpha = 0.20f),
                    Color.Transparent
                )
                else -> listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.35f + smoothRms * 0.25f),
                    Color(0xFFFF2E93).copy(alpha = 0.22f),
                    Color.Transparent
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = auraColors,
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.49f
                )
            )

            // Dual counter-rotating Siri plasma ribbons
            rotate(degrees = rotationAngle, pivot = Offset(cx, cy)) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF8B5CF6),
                            Color(0xFFFF2E93),
                            Color(0xFF38BDF8),
                            Color(0xFF00E5FF)
                        ),
                        center = Offset(cx, cy)
                    ),
                    radius = baseRadius + 14f,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            rotate(degrees = -rotationAngle * 1.3f, pivot = Offset(cx, cy)) {
                val points = 72
                val ringPath = Path()
                val amp = if (voiceState == JarvisVoiceState.LISTENING || voiceState == JarvisVoiceState.SPEAKING) {
                    6f + (smoothRms * 14f)
                } else {
                    2.5f
                }
                for (i in 0..points) {
                    val theta = (i.toFloat() / points) * 2f * PI.toFloat()
                    val r = baseRadius + 5f + sin(theta * 5f + wavePhase) * amp
                    val px = cx + r * cos(theta)
                    val py = cy + r * sin(theta)
                    if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
                }
                ringPath.close()
                drawPath(
                    path = ringPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4081), Color(0xFF00E5FF), Color(0xFFA855F7))
                    ),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner Core Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 0.88f
                ),
                radius = baseRadius * 0.88f
            )

            // Reactive Audio Waveform Bars inside Orb during Listening/Speaking
            if (voiceState == JarvisVoiceState.LISTENING || voiceState == JarvisVoiceState.SPEAKING) {
                val barCount = 7
                val spacing = 9.dp.toPx()
                val startX = cx - ((barCount - 1) * spacing) / 2f
                for (b in 0 until barCount) {
                    val weight = 1f - (kotlin.math.abs(b - 3) * 0.18f)
                    val oscillation = kotlin.math.abs(sin(wavePhase + b * 0.7f))
                    val barHeight = (10f + (smoothRms * 34f * oscillation * weight)).dp.toPx()
                        .coerceAtMost(baseRadius * 0.9f)
                    val bx = startX + b * spacing
                    drawLine(
                        color = if (b % 2 == 0) Color(0xFF00E5FF) else Color(0xFFFF80AB),
                        start = Offset(bx, cy - barHeight / 2f),
                        end = Offset(bx, cy + barHeight / 2f),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Center avatar or icon when Idle / Thinking / Error
        if (voiceState != JarvisVoiceState.LISTENING && voiceState != JarvisVoiceState.SPEAKING) {
            IronmanDoctorAvatar(sizeDp = 56)
        }
    }
}

