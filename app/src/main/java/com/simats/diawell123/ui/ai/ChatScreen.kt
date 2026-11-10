package com.simats.diawell123.ui.ai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.data.model.ChatMessage
import com.simats.diawell123.data.model.ChatSender
import com.simats.diawell123.data.model.NutritionMetric
import com.simats.diawell123.ui.diet.StyledBackButton
import com.simats.diawell123.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Dr. Jarvis AI Chat Screen with Diabetes Visual Styling and Speech-to-Text.
 */
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voiceManager = remember { JarvisVoiceManager(context) }
    val voiceState by voiceManager.voiceState.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()
    val partialTranscript by voiceManager.partialTranscript.collectAsState()
    val voiceError by voiceManager.voiceError.collectAsState()
    val localeLabel by voiceManager.activeLocaleLabel.collectAsState()

    var isVoiceOverlayOpen by remember { mutableStateOf(false) }
    var autoSpeakEnabled by remember { mutableStateOf(true) }

    val latestAiResponse = remember(messages) {
        messages.lastOrNull { it.sender == ChatSender.AI }?.text ?: ""
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.release()
        }
    }

    fun submitQuery(query: String, fromVoice: Boolean = false) {
        if (fromVoice || isVoiceOverlayOpen) {
            voiceManager.setThinkingState()
        }
        viewModel.sendMessage(
            query = query,
            context = context,
            onSpokenResponseReady = { reply ->
                if (autoSpeakEnabled || fromVoice) {
                    voiceManager.speak(reply)
                } else {
                    voiceManager.setIdleState()
                }
            },
            onErrorCallback = {
                voiceManager.setIdleState()
            }
        )
    }

    // Auto-scroll to bottom on new messages or loading change
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // -------------------------------------------------------------------------
    // Voice Input Speech Recognizer & Siri-Style Live Voice Session Integration
    // -------------------------------------------------------------------------
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()?.trim()
            if (!spokenText.isNullOrEmpty()) {
                submitQuery(spokenText, fromVoice = true)
            } else {
                voiceManager.setIdleState()
            }
        } else {
            voiceManager.setIdleState()
        }
    }

    fun beginLiveVoiceSession() {
        voiceManager.stopSpeaking()
        isVoiceOverlayOpen = true
        voiceManager.startListening(
            onFinalResult = { spokenText ->
                submitQuery(spokenText, fromVoice = true)
            },
            onFallbackIntentNeeded = {
                launchSpeechInput(context, speechLauncher)
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            beginLiveVoiceSession()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for voice input.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun startVoiceInput() {
        val permission = android.Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            beginLiveVoiceSession()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    // Arc Reactor Pulsing Animation for Dr. Jarvis Avatar
    val infiniteTransition = rememberInfiniteTransition(label = "JarvisPulse")
    val avatarPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {

            // -----------------------------------------------------------------
            // Top Bar: Dr. Jarvis Iron Man Medical Avatar & Compact Actions
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(8.dp))

                    // Arc Reactor Avatar with pulsing glow
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .scale(avatarPulseScale)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = auraAlpha))
                        )
                        IronmanDoctorAvatar(sizeDp = 36)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dr. Jarvis AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                        Text(
                            text = "Diabetes Care • Qwen 2.5",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = AppTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Controls: Uniform Circular Auto-Speak & Reset Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.elevatedSurface)
                            .border(1.dp, AppTheme.colors.border, CircleShape)
                            .clickable {
                                autoSpeakEnabled = !autoSpeakEnabled
                                if (!autoSpeakEnabled) {
                                    voiceManager.stopSpeaking()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (autoSpeakEnabled) "🔊" else "🔇",
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.elevatedSurface)
                            .border(1.dp, AppTheme.colors.border, CircleShape)
                            .clickable {
                                voiceManager.stopSpeaking()
                                viewModel.resetChat()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔄", fontSize = 15.sp)
                    }
                }
            }

            // -----------------------------------------------------------------
            // Clinical Safety Disclaimer + Siri Voice Mode Action Strip
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.elevatedSurface)
                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🩺", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wellness & diet guidance. Consult your doctor for med edits.",
                        fontSize = 11.sp,
                        color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF7A1C7D), Color(0xFFFF5722))
                                )
                            )
                            .clickable { startVoiceInput() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎙️ Voice",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // -----------------------------------------------------------------
            // Messages LazyColumn
            // -----------------------------------------------------------------
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onSpeakClick = { textToSpeak -> voiceManager.speak(textToSpeak) }
                    )
                }

                if (isLoading) {
                    item(key = "typing_indicator") {
                        DrJarvisThinkingIndicator(avatarPulseScale)
                    }
                }
            }

            // -----------------------------------------------------------------
            // Error Banner with Retry
            // -----------------------------------------------------------------
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFEF5350), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                fontSize = 11.sp,
                                color = Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.retryLastMessage(
                                            context = context,
                                            onSpokenResponseReady = { reply ->
                                                if (autoSpeakEnabled || isVoiceOverlayOpen) {
                                                    voiceManager.speak(reply)
                                                } else {
                                                    voiceManager.setIdleState()
                                                }
                                            }
                                        )
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // Quick Prompt Suggestion Chips
            // -----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val suggestions = listOf(
                    "🌾 3 high-fibre Indian foods",
                    "🚶 Walking MET value for 30 mins",
                    "🚨 What to do if glucose < 70 mg/dL?",
                    "🥗 Low carbohydrate snacks"
                )
                suggestions.forEach { prompt ->
                    val cleanPrompt = prompt.substringAfter(" ")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.elevatedSurface)
                            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                            .clickable { submitQuery(cleanPrompt, fromVoice = false) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFFB81D85),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // Bottom Chat Input Bar with Microphone Voice Action
            // -----------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.cardBackground)
            ) {
                HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice Input Microphone Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.elevatedSurface)
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85))
                                ),
                                shape = CircleShape
                            )
                            .clickable { startVoiceInput() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎙️", fontSize = 18.sp)
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask Dr. Jarvis or tap mic...",
                                fontSize = 13.sp,
                                color = AppTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary,
                            focusedContainerColor = AppTheme.colors.inputBackground,
                            unfocusedContainerColor = AppTheme.colors.inputBackground,
                            focusedBorderColor = Color(0xFFBD1E85),
                            unfocusedBorderColor = AppTheme.colors.inputBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp)
                    )

                    // Send Button with Crisp Centered Vector Arrow
                    val canSend = inputText.isNotBlank() && !isLoading
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                brush = if (canSend) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF7A1C7D), Color(0xFFFF5722))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f))
                                    )
                                }
                            )
                            .clickable(enabled = canSend) {
                                if (inputText.isNotBlank()) {
                                    val query = inputText.trim()
                                    inputText = ""
                                    submitQuery(query, fromVoice = false)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val w = size.width
                            val h = size.height
                            val arrowPath = Path().apply {
                                moveTo(w * 0.5f, h * 0.85f)
                                lineTo(w * 0.5f, h * 0.18f)
                                moveTo(w * 0.22f, h * 0.44f)
                                lineTo(w * 0.5f, h * 0.16f)
                                lineTo(w * 0.78f, h * 0.44f)
                            }
                            drawPath(
                                path = arrowPath,
                                color = Color.White,
                                style = Stroke(
                                    width = 2.4.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }

        // Interactive Siri-Style Voice Assistant Overlay
        SiriVoiceOverlay(
            visible = isVoiceOverlayOpen,
            voiceState = voiceState,
            rmsLevel = rmsLevel,
            partialTranscript = partialTranscript,
            latestAiResponse = latestAiResponse,
            voiceError = voiceError,
            localeLabel = localeLabel,
            isAutoSpeakEnabled = autoSpeakEnabled,
            onOrbTap = {
                when (voiceState) {
                    JarvisVoiceState.LISTENING -> voiceManager.stopListening()
                    JarvisVoiceState.SPEAKING -> beginLiveVoiceSession()
                    else -> beginLiveVoiceSession()
                }
            },
            onQuickVoiceCommand = { cmd ->
                voiceManager.stopSpeaking()
                submitQuery(cmd, fromVoice = true)
            },
            onToggleAutoSpeak = {
                autoSpeakEnabled = !autoSpeakEnabled
                if (!autoSpeakEnabled) {
                    voiceManager.stopSpeaking()
                }
            },
            onStopAudio = {
                voiceManager.stopSpeaking()
            },
            onClose = {
                isVoiceOverlayOpen = false
                voiceManager.cancelListening()
                voiceManager.stopSpeaking()
            }
        )
    }
}

/**
 * Single Message Bubble with Sender Alignment, Clinical Cards, and Nutrition Chips.
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeakClick: (String) -> Unit = {}
) {
    val isUser = message.sender == ChatSender.USER
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    if (isUser) {
        // User Message (Right Aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .padding(start = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp,
                        softWrap = true,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = AppTheme.colors.textMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    } else {
        // Dr. Jarvis AI Message (Left Aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            IronmanDoctorAvatar(sizeDp = 34)

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                        .background(
                            if (message.isError) Color(0xFFFFEBEE) else AppTheme.colors.cardElevated
                        )
                        .border(
                            1.dp,
                            if (message.isError) Color(0xFFEF5350) else AppTheme.colors.border,
                            RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        // Header Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Dr. Jarvis AI ⚡🩺",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFFB81D85),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.colors.elevatedSurface)
                                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                                        .clickable { onSpeakClick(message.text) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🔊",
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = timeFormatted,
                                    fontSize = 10.sp,
                                    color = AppTheme.colors.textMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        // Clinical Emergency Alert Card
                        if (message.alertType != AlertType.NONE) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ClinicalAlertBanner(alertType = message.alertType)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Formatted Markdown Content
                        MarkdownFormattedText(
                            text = message.text,
                            color = if (message.isError) Color(0xFFC62828) else AppTheme.colors.textPrimary
                        )

                        // Nutrition / MET Badges
                        if (message.metrics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                message.metrics.forEach { metric ->
                                    NutritionBadgeChip(metric = metric)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clinical High-Contrast Alert Card for Hypoglycemia, Hyperglycemia, and Ketone Warnings.
 */
@Composable
fun ClinicalAlertBanner(alertType: AlertType) {
    val (icon, title, borderColor, bgColor, textColor) = when (alertType) {
        AlertType.HYPOGLYCEMIA_WARNING -> Tuple5(
            "🚨",
            "CRITICAL: Hypoglycemia Guidance (<70 mg/dL)",
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            Color(0xFFB71C1C)
        )
        AlertType.HYPERGLYCEMIA_WARNING -> Tuple5(
            "⚠️",
            "ELEVATED: Hyperglycemia Caution (>250 mg/dL)",
            Color(0xFFF57C00),
            Color(0xFFFFF3E0),
            Color(0xFFE65100)
        )
        AlertType.KETONE_WARNING -> Tuple5(
            "⚠️",
            "ALERT: Ketone Protocol & Clinical Advisory",
            Color(0xFFE64A19),
            Color(0xFFFBE9E7),
            Color(0xFFBF360C)
        )
        AlertType.GENERAL_CLINICAL_ALERT -> Tuple5(
            "ℹ️",
            "Clinical Safety & Medication Advisory",
            Color(0xFF1976D2),
            Color(0xFFE3F2FD),
            Color(0xFF0D47A1)
        )
        AlertType.NONE -> Tuple5("", "", Color.Transparent, Color.Transparent, Color.Transparent)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Structured Metric Chip for High-Fibre, Calories, Carbohydrates, and METs.
 */
@Composable
fun NutritionBadgeChip(metric: NutritionMetric) {
    val (chipBg, chipBorder, chipText) = when (metric.badgeType) {
        BadgeType.HIGH_FIBRE -> {
            if (metric.isHighlight) {
                Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), Color(0xFF1B5E20))
            } else {
                Triple(Color(0xFFF1F8E9), Color(0xFF8BC34A), Color(0xFF33691E))
            }
        }
        BadgeType.CALORIES -> Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), Color(0xFFE65100))
        BadgeType.CARBOHYDRATES -> Triple(Color(0xFFE1F5FE), Color(0xFF03A9F4), Color(0xFF01579B))
        BadgeType.PROTEIN -> Triple(Color(0xFFF3E5F5), Color(0xFFAB47BC), Color(0xFF4A148C))
        BadgeType.MET_ACTIVITY -> Triple(Color(0xFFEDE7F6), Color(0xFF7E57C2), Color(0xFF311B92))
        BadgeType.GENERAL -> Triple(Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFF263238))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = metric.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = chipText
        )
    }
}

/**
 * Animated Dr. Jarvis Thinking Indicator.
 */
@Composable
fun DrJarvisThinkingIndicator(pulseScale: Float) {
    var statusText by remember { mutableStateOf("Dr. Jarvis is reviewing your query...") }

    LaunchedEffect(Unit) {
        delay(3500)
        statusText = "Consulting verified diabetes datasets..."
        delay(5500)
        statusText = "Synthesizing clinical & nutritional facts..."
        delay(7000)
        statusText = "Formulating Dr. Jarvis AI response..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 14.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.scale(pulseScale)) {
            IronmanDoctorAvatar(sizeDp = 34)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.cardElevated)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
    }
}

/**
 * Renders bold Markdown and bullet points cleanly without raw asterisks leaking.
 */
@Composable
fun MarkdownFormattedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.textPrimary,
    fontSize: TextUnit = 13.sp,
    lineHeight: TextUnit = 18.sp
) {
    val annotated = remember(text) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                // Clean bullet markers
                val cleanLine = when {
                    line.trimStart().startsWith("- ") -> "• " + line.trimStart().removePrefix("- ")
                    line.trimStart().startsWith("* ") -> "• " + line.trimStart().removePrefix("* ")
                    else -> line
                }

                // Parse **bold** markdown segments
                var cursor = 0
                val pattern = java.util.regex.Pattern.compile("\\*\\*(.*?)\\*\\*")
                val matcher = pattern.matcher(cleanLine)

                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    val boldText = matcher.group(1) ?: ""

                    // Append regular text before bold
                    if (start > cursor) {
                        append(cleanLine.substring(cursor, start))
                    }

                    // Append bold text
                    val boldStart = length
                    append(boldText)
                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                        start = boldStart,
                        end = length
                    )
                    cursor = end
                }

                // Append remainder of line
                if (cursor < cleanLine.length) {
                    append(cleanLine.substring(cursor))
                }

                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight
    )
}

// Helper data holder
private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

/**
 * Trigger Android Native SpeechRecognizer Intent.
 */
private fun launchSpeechInput(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Dr. Jarvis (English / Tanglish)...")
    }
    try {
        launcher.launch(intent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Speech recognition is not supported on this device.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

