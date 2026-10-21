package com.simats.diawell123.ui.ai

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            ChatMessage("ai", "Hi Maya! I can help with balanced meal ideas and everyday food questions. What's on your mind?"),
            ChatMessage("user", "What can I eat for breakfast?"),
            ChatMessage("ai", "Try unsweetened oats with plain yogurt, berries and a few almonds. This combines whole grains, protein and fiber. Choose a portion that fits your care plan."),
        )
    }

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

                // Top Bar
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

                        Column {
                            Text(
                                text = "DiaWell AI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E22),
                            )
                            Text(
                                text = "Your general wellness companion",
                                fontSize = 12.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }

                    Text(text = "✨", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Disclaimer Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "Advice is general, not medical. Follow your clinician's advice.",
                        fontSize = 11.sp,
                        color = Color(0xFF811978),
                        lineHeight = 16.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chat Messages
                messages.forEach { msg ->
                    if (msg.sender == "ai") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8F7FA))
                                .padding(14.dp),
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = Color(0xFF1E1E22),
                                lineHeight = 18.sp,
                            )
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
                    text = "YOU COULD ASK",
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
                            text = "Ask about food or meal ideas...",
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AiChatbotScreenPreview() {
    Diawell123Theme {
        AiChatbotScreen()
    }
}
