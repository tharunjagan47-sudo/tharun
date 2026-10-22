package com.simats.diawell123.ui.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

data class ChatMessage(val sender: String, val text: String)

@Composable
fun AiChatbotScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("ai", "Hi Maya! I'm JARVIS, your AI Health & Glucose Assistant! ⚡🩺 I can suggest balanced meal plans, answer food questions, and track your daily routine. What's on your mind?"),
            ChatMessage("user", "What can I eat for breakfast?"),
            ChatMessage("ai", "Try unsweetened oats with plain yogurt, berries and a few almonds. This combines whole grains, protein and fiber. Choose a portion that fits your care plan."),
        )
    }

    // Infinite transition for Arc Reactor pulsing aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "JarvisArcGlow")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "avatarScale",
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "auraAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar with Iron Man JARVIS Avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "←",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBackClick() }
                                .padding(4.dp),
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // JARVIS Avatar Container
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(46.dp),
                        ) {
                            // Pulsing Cyan Arc Reactor Glow Ring
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(avatarScale)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF).copy(alpha = auraAlpha)),
                            )

                            // Iron Man Doctor Avatar
                            IronmanDoctorAvatar(sizeDp = 40)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "JARVIS AI ⚡",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                )
                            }
                            Text(
                                text = "Arc Reactor Health Intelligence",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }

                    Text(text = "🩺", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Disclaimer Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .border(1.dp, Color(0xFFF3E2F0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "⚡ JARVIS AI gives personalized wellness guidance, not medical diagnosis. Follow your clinician's advice.",
                        fontSize = 11.sp,
                        color = Color(0xFF811978),
                        lineHeight = 16.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chat Messages
                messages.forEach { msg ->
                    if (msg.sender == "ai") {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.92f),
                            verticalAlignment = Alignment.Top,
                        ) {
                            // Iron Man Doctor Avatar next to response
                            Box(
                                modifier = Modifier.scale(avatarScale),
                            ) {
                                IronmanDoctorAvatar(sizeDp = 34)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF8F7FA))
                                    .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                            ) {
                                Column {
                                    Text(
                                        text = "JARVIS AI ⚡🩺",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB81D85),
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E1E22),
                                        lineHeight = 18.sp,
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                        ),
                                    )
                                    .padding(14.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Prompt Chips
                Text(
                    text = "ASK JARVIS AI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A8595),
                    letterSpacing = 0.5.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                listOf("What can I eat for breakfast?", "Suggest a healthy snack").forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDEEF7))
                            .border(1.dp, Color(0xFFF3E2F0), RoundedCornerShape(12.dp))
                            .clickable {
                                messages.add(ChatMessage("user", prompt))
                                messages.add(ChatMessage("ai", "I suggest a balanced combination of fiber, protein, and healthy fats tailored to your daily goals."))
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Bottom Chat Input Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFEFECEF))
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            text = "Ask JARVIS about food or meal ideas...",
                            fontSize = 13.sp,
                            color = Color(0xFFA09BAA),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F7FA),
                        unfocusedContainerColor = Color(0xFFF8F7FA),
                        focusedBorderColor = Color(0xFFBD1E85),
                        unfocusedBorderColor = Color(0xFFEFECEF),
                    ),
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF7A1C7D), Color(0xFFFF5722)),
                            ),
                        )
                        .clickable {
                            if (messageText.isNotBlank()) {
                                messages.add(ChatMessage("user", messageText))
                                messages.add(ChatMessage("ai", "That's a great question! For a balanced diet, prioritize whole foods, portion control, and staying consistent with your daily routine."))
                                messageText = ""
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "↑", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun IronmanDoctorAvatar(
    modifier: Modifier = Modifier,
    sizeDp: Int = 40,
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Color(0xFF8E0000)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Red Metallic Base Head
            drawCircle(color = Color(0xFFC62828), radius = w / 2)

            // 2. Iron Man Gold Faceplate Path
            val goldPlatePath = Path().apply {
                moveTo(w * 0.28f, h * 0.22f)
                lineTo(w * 0.72f, h * 0.22f)
                lineTo(w * 0.80f, h * 0.42f)
                lineTo(w * 0.65f, h * 0.68f)
                lineTo(w * 0.35f, h * 0.68f)
                lineTo(w * 0.20f, h * 0.42f)
                close()
            }

            drawPath(path = goldPlatePath, color = Color(0xFFFFD54F))
            drawPath(path = goldPlatePath, color = Color(0xFFFFA000), style = Stroke(width = 1.2.dp.toPx()))

            // 3. Iron Man Glowing Cyan Slanted Eye Slits
            val leftEye = Path().apply {
                moveTo(w * 0.30f, h * 0.38f)
                lineTo(w * 0.46f, h * 0.40f)
                lineTo(w * 0.45f, h * 0.44f)
                lineTo(w * 0.32f, h * 0.43f)
                close()
            }

            val rightEye = Path().apply {
                moveTo(w * 0.70f, h * 0.38f)
                lineTo(w * 0.54f, h * 0.40f)
                lineTo(w * 0.55f, h * 0.44f)
                lineTo(w * 0.68f, h * 0.43f)
                close()
            }

            drawPath(path = leftEye, color = Color(0xFF00E5FF))
            drawPath(path = rightEye, color = Color(0xFF00E5FF))

            // 4. White Doctor Lab Coat Shoulders
            val coatPath = Path().apply {
                moveTo(0f, h)
                lineTo(w * 0.22f, h * 0.62f)
                lineTo(w * 0.50f, h * 0.76f)
                lineTo(w * 0.78f, h * 0.62f)
                lineTo(w, h)
                close()
            }
            drawPath(coatPath, color = Color.White)
            drawPath(coatPath, color = Color(0xFFB0BEC5), style = Stroke(width = 1.dp.toPx()))

            // 5. Stethoscope Tubing across coat with Mini Arc Reactor Node
            val stethoPath = Path().apply {
                moveTo(w * 0.32f, h * 0.68f)
                cubicTo(w * 0.35f, h * 0.88f, w * 0.65f, h * 0.88f, w * 0.68f, h * 0.68f)
            }
            drawPath(stethoPath, color = Color(0xFF37474F), style = Stroke(width = 2.5.dp.toPx()))
            drawCircle(color = Color(0xFF00E5FF), radius = 3.dp.toPx(), center = Offset(w * 0.5f, h * 0.82f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiChatbotScreenPreview() {
    Diawell123Theme {
        AiChatbotScreen()
    }
}
