package com.simats.diawell123.ui.ai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.R
import com.simats.diawell123.ui.components.FluidGlassBackButton
import com.simats.diawell123.ui.components.FluidGlassPillContainer
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.data.model.ChatMessage
import com.simats.diawell123.data.model.ChatSender
import com.simats.diawell123.data.model.NutritionMetric
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import com.simats.diawell123.ui.profile.ProfileState

/**
 * Ultra-minimal modern dark-mode DIVA AI Chat Screen.
 * Styled in DiaWell's Obsidian Black & Royal Purple / Deep Violet ambient theme.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.isImeVisible
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voiceManager = remember { JarvisVoiceManager(context) }
    val voiceState by voiceManager.voiceState.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()
    val mouthOpenness by voiceManager.mouthOpenness.collectAsState()
    val mouthShape by voiceManager.mouthShape.collectAsState()
    val partialTranscript by voiceManager.partialTranscript.collectAsState()
    val voiceError by voiceManager.voiceError.collectAsState()
    val localeLabel by voiceManager.activeLocaleLabel.collectAsState()

    val sessionManager = remember {
        try { com.simats.diawell123.data.local.SessionManager.getInstance() }
        catch (_: Exception) { com.simats.diawell123.data.local.SessionManager.init(context) }
    }
    var isVoiceOverlayOpen by remember { mutableStateOf(false) }
    var autoSpeakEnabled by remember { mutableStateOf(sessionManager.isDrJarvisVoiceEnabled()) }

    val latestAiResponse = remember(messages) {
        messages.lastOrNull { it.sender == ChatSender.AI }?.text ?: ""
    }

    LaunchedEffect(Unit) {
        viewModel.refreshWelcomeMessage(context)
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

    // Auto-scroll to bottom on new messages, loading change, or keyboard open
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && messages.isNotEmpty()) {
            delay(120)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // -------------------------------------------------------------------------
    // Voice Input Speech Recognizer Integration
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

    // Dynamic Ambient Fluid Gradient (Gemini-inspired top half bloom & sinusoidal motion)
    val ambientTransition = rememberInfiniteTransition(label = "ChatAmbientGradientMotion")
    val phase1 by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    val pulseScale by ambientTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Edge-to-edge layout content with integrated moving ambient gradient
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Fluid Top Ambient Gradient Canvas (Emerald & Teal to Royal Purple fading to pitch black)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val offsetX1 = w * (0.18f + 0.08f * sin(phase1))
            val offsetY1 = h * (0.08f + 0.04f * cos(phase1))
            val radius1 = w * (0.95f * pulseScale)

            val offsetX2 = w * (0.82f - 0.08f * cos(phase2))
            val offsetY2 = h * (0.12f + 0.05f * sin(phase2))
            val radius2 = w * (0.92f * (2f - pulseScale))

            // 1. Broad Top-Half Ambient Horizontal Glow with lateral drift
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF047857).copy(alpha = 0.65f), // Rich Emerald / Teal on left (matches screenshot)
                        Color(0xFF0E7490).copy(alpha = 0.48f), // Deep Cyan in middle
                        Color(0xFF1E3A8A).copy(alpha = 0.45f), // Deep Midnight Blue / Indigo
                        Color(0xFF581C87).copy(alpha = 0.48f), // Deep Violet / Purple on right
                    ),
                    startX = -w * 0.10f + w * 0.10f * sin(phase1),
                    endX = w * 1.10f + w * 0.10f * sin(phase1)
                ),
                size = androidx.compose.ui.geometry.Size(w, h * 0.50f)
            )

            // 2. High-saturation Top-Left Radial Bloom (Emerald & Vibrant Teal)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.72f),
                        Color(0xFF0D9488).copy(alpha = 0.52f),
                        Color(0xFF064E3B).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(offsetX1, offsetY1),
                    radius = radius1
                )
            )

            // 3. High-saturation Top-Right Radial Bloom (Indigo & Royal Purple)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.35f),
                        Color(0xFF6366F1).copy(alpha = 0.50f),
                        Color(0xFF7C3AED).copy(alpha = 0.55f),
                        Color(0xFF3B0764).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(offsetX2, offsetY2),
                    radius = radius2
                )
            )

            // 4. Subtle central floating aura node
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF065F46).copy(alpha = 0.35f),
                        Color(0xFF1E1B4B).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(w * (0.50f + 0.05f * sin(phase2)), h * (0.16f + 0.03f * cos(phase1))),
                    radius = w * 0.75f
                )
            )

            // 5. Smooth atmospheric diffusion fading to pure obsidian pitch black (#000000) by ~48-52%
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.20f to Color.Transparent,
                        0.32f to Color(0xFF000000).copy(alpha = 0.35f),
                        0.44f to Color(0xFF000000).copy(alpha = 0.85f),
                        0.50f to Color(0xFF000000),
                        1.0f to Color(0xFF000000)
                    )
                )
            )
        }

        // Edge-to-edge layout content centered for landscape/tablet screens
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
                .imePadding()
        ) {

            // =================================================================
            // PART 2: MINIMAL FLOATING GLASS TOP BAR
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Apple VisionOS / iOS Fluid Glass Bubble Back Button
                FluidGlassBackButton(onClick = onBackClick)

                // Right: Single Sleek Translucent Fluid Glass Pill (New Chat & More Options)
                var showOptionsMenu by remember { mutableStateOf(false) }

                FluidGlassPillContainer(
                    horizontalPadding = 8.dp,
                    verticalPadding = 4.dp,
                ) {
                    // New Chat (Compose Icon)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable {
                                voiceManager.stopSpeaking()
                                viewModel.resetChat()
                                Toast.makeText(context, "New chat started", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val squarePath = Path().apply {
                                    moveTo(w * 0.72f, h * 0.44f)
                                    lineTo(w * 0.72f, h * 0.82f)
                                    lineTo(w * 0.18f, h * 0.82f)
                                    lineTo(w * 0.18f, h * 0.28f)
                                    lineTo(w * 0.56f, h * 0.28f)
                                }
                                drawPath(
                                    path = squarePath,
                                    color = Color(0xFFF1F5F9),
                                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                val pencilPath = Path().apply {
                                    moveTo(w * 0.42f, h * 0.58f)
                                    lineTo(w * 0.84f, h * 0.16f)
                                }
                                drawPath(
                                    path = pencilPath,
                                    color = Color(0xFFA78BFA),
                                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Subtle Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(Color(0x26FFFFFF))
                        )

                        // More / Options (...)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { showOptionsMenu = !showOptionsMenu },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val r = 1.6.dp.toPx()
                                drawCircle(color = Color(0xFFF1F5F9), radius = r, center = Offset(w * 0.22f, h * 0.5f))
                                drawCircle(color = Color(0xFFF1F5F9), radius = r, center = Offset(w * 0.50f, h * 0.5f))
                                drawCircle(color = Color(0xFFF1F5F9), radius = r, center = Offset(w * 0.78f, h * 0.5f))
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false },
                                modifier = Modifier
                                    .background(Color(0xFF161524))
                                    .border(1.dp, Color(0x40A855F7), RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (autoSpeakEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = if (autoSpeakEnabled) "Auto-Speak Enabled" else "Auto-Speak Disabled",
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        autoSpeakEnabled = !autoSpeakEnabled
                                        sessionManager.setDrJarvisVoiceEnabled(autoSpeakEnabled)
                                        if (!autoSpeakEnabled) voiceManager.stopSpeaking()
                                        Toast.makeText(context, if (autoSpeakEnabled) "DIVA will speak responses" else "DIVA voice muted", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Mic,
                                            contentDescription = null,
                                            tint = Color(0xFFF0ABFC),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Open Voice Mode",
                                            color = Color(0xFFF0ABFC),
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        startVoiceInput()
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteSweep,
                                            contentDescription = null,
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Clear Conversation",
                                            color = Color(0xFFF87171),
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        viewModel.resetChat()
                                    }
                                )
                            }
                        }
                    }
            }

            // =================================================================
            // PART 3: MINIMAL CHAT MESSAGES & ANIMATED THINKING STYLE
            // =================================================================
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onSpeakClick = { textToSpeak -> voiceManager.speak(textToSpeak) }
                    )
                }

                if (isLoading) {
                    item(key = "typing_indicator") {
                        DivaThinkingIndicator()
                    }
                }
            }

            // Error Banner with Retry
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A1518))
                            .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                fontSize = 12.sp,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171),
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

            // Sleek floating pill above input bar (matching reference screenshot: 'Answer now')
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E1F24))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .clickable { viewModel.stopGenerating() }
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Answer now",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }

            // Quick Prompt Suggestion Chips (collapses when keyboard is open or while loading)
            AnimatedVisibility(
                visible = !isKeyboardOpen && !isLoading && messages.size <= 2,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val suggestions = listOf(
                        Icons.Rounded.Grain to "High-fibre Indian foods",
                        Icons.Rounded.DirectionsWalk to "Walking MET for 30 mins",
                        Icons.Rounded.Warning to "What to do if sugar < 70?",
                        Icons.Rounded.Restaurant to "Low-carb dinner ideas"
                    )
                    suggestions.forEach { (icon, prompt) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x331F1B2E))
                                .border(1.dp, Color(0x33A855F7), RoundedCornerShape(16.dp))
                                .clickable { submitQuery(prompt, fromVoice = false) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
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
                                    text = prompt,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE9D5FF),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // PART 4: FLOATING GLASS BOTTOM INPUT PILL (`Ask DIVA`)
            // =================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E1F24))
                    .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Minimal '+' Action Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFFF))
                            .clickable { startVoiceInput() },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(14.dp)) {
                            val w = size.width
                            val h = size.height
                            drawLine(
                                color = Color(0xFFF1F5F9),
                                start = Offset(w * 0.5f, h * 0.18f),
                                end = Offset(w * 0.5f, h * 0.82f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFFF1F5F9),
                                start = Offset(w * 0.18f, h * 0.5f),
                                end = Offset(w * 0.82f, h * 0.5f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Center: Borderless BasicTextField with placeholder 'Ask DIVA'
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp),
                        textStyle = TextStyle(
                            color = Color(0xFFF8FAFC),
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFFA855F7)),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Ask DIVA",
                                        fontSize = 15.sp,
                                        color = Color(0x80FFFFFF)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: Action Button (Send Arrow vs Stop Square vs Minimal Rounded Square)
                    val canSend = inputText.isNotBlank() && !isLoading
                    val actionButtonBrush = when {
                        isLoading -> Brush.linearGradient(listOf(Color(0xFF3B1547), Color(0xFF3B1547)))
                        canSend -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0D9488)))
                        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(actionButtonBrush)
                            .clickable {
                                if (isLoading) {
                                    viewModel.stopGenerating()
                                } else if (canSend) {
                                    val query = inputText.trim()
                                    inputText = ""
                                    submitQuery(query, fromVoice = false)
                                } else {
                                    startVoiceInput()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            // Stop Square Icon (■)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFF5D0FE))
                            )
                        } else if (canSend) {
                            // Crisp Vector Send Arrow
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val path = Path().apply {
                                    moveTo(w * 0.5f, h * 0.82f)
                                    lineTo(w * 0.5f, h * 0.18f)
                                    moveTo(w * 0.22f, h * 0.44f)
                                    lineTo(w * 0.5f, h * 0.16f)
                                    lineTo(w * 0.78f, h * 0.44f)
                                }
                                drawPath(
                                    path = path,
                                    color = Color.White,
                                    style = Stroke(
                                        width = 2.2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        } else {
                            // Minimal Rounded Square Icon matching reference UI
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val w = size.width
                                val h = size.height
                                drawRoundRect(
                                    color = Color(0xFFCBD5E1),
                                    size = androidx.compose.ui.geometry.Size(w * 0.78f, h * 0.78f),
                                    topLeft = Offset(w * 0.11f, h * 0.11f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // Siri-Style Voice Assistant Overlay (activated hands-free or via voice button)
        SiriVoiceOverlay(
            visible = isVoiceOverlayOpen,
            voiceState = voiceState,
            rmsLevel = rmsLevel,
            mouthOpenness = mouthOpenness,
            mouthShape = mouthShape,
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
                sessionManager.setDrJarvisVoiceEnabled(autoSpeakEnabled)
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
 * Message items rendered according to the new sleek minimal typography design:
 * - User Messages: Right-aligned dark-charcoal translucent bubble (#1A1A22 at ~85% alpha).
 * - DIVA AI Messages: Full-width, bubble-less clean typography directly on obsidian canvas with tiny purple sparkle badge.
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
        // User Message: Sleek wide dark rounded card with user avatar thumbnail in top-right (matches Gemini reference!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E2024))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Attached Image preview if present
                if (!message.attachedImageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2A2B32))
                            .border(1.dp, Color(0x33A855F7), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoCamera,
                            contentDescription = "Attached photo",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // User's Prompt Text (wraps and expands)
                    Text(
                        text = message.text,
                        fontSize = 15.5.sp,
                        color = Color(0xFFF1F5F9),
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 14.dp)
                    )

                    // Top-Right User Avatar / Photo Thumbnail (matches reference screenshot!)
                    val profileBitmap = ProfileState.profileImageBitmap
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 66.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2A2B32)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap,
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = ProfileState.userName
                                    .split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .ifEmpty { "ME" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // DIVA AI Response: Full-width, bubble-less clean typography directly on dark obsidian canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Clinical Emergency Alert Card (if any)
                if (message.alertType != AlertType.NONE) {
                    ClinicalAlertBanner(alertType = message.alertType)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bubble-less clean Markdown / bold typography directly on dark canvas
                MarkdownFormattedText(
                    text = message.text,
                    color = if (message.isError) Color(0xFFF87171) else Color(0xFFF1F5F9),
                    fontSize = 15.sp,
                    lineHeight = 23.sp
                )

                // Nutrition Badges (if any)
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

                // Subtle speak & timestamp footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1AFFFFFF))
                            .clickable { onSpeakClick(message.text) }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = "Listen",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Listen",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = Color(0x66FFFFFF)
                    )
                }
            }
        }
    }
}

/**
 * Animated DIVA Thinking Indicator:
 * Sleek minimal 4-dot constellation (matching '.:' from reference UI),
 * followed by clean unboxed status text. No backend jargon.
 */
@Composable
fun DivaThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "ThinkingDots")
    val dotPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotPhase"
    )

    var statusIndex by remember { mutableIntStateOf(0) }
    val statusMessages = remember {
        listOf(
            "Searching the web",
            "Checking health logs...",
            "Analyzing nutrition metrics...",
            "Formulating guidance..."
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2400)
            statusIndex = (statusIndex + 1) % statusMessages.size
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minimal 4-dot cluster arranged as '.:' matching screenshot
        Canvas(modifier = Modifier.size(width = 18.dp, height = 18.dp)) {
            val w = size.width
            val h = size.height
            val points = listOf(
                Offset(w * 0.22f, h * 0.68f),
                Offset(w * 0.38f, h * 0.28f),
                Offset(w * 0.78f, h * 0.38f),
                Offset(w * 0.64f, h * 0.72f)
            )
            points.forEachIndexed { i, pt ->
                val localPhase = (dotPhase + i * 0.25f) % 1f
                val bounce = sin(localPhase * Math.PI).toFloat().coerceIn(0f, 1f)
                val r = 2.0.dp.toPx() * (0.85f + bounce * 0.35f)
                val alpha = 0.40f + bounce * 0.60f
                val color = if (i % 2 == 0) Color(0xFF34D399) else Color(0xFFFFFFFF)
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = r,
                    center = pt
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = statusMessages[statusIndex],
            fontSize = 15.sp,
            color = Color(0xFFF1F5F9),
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Clinical High-Contrast Alert Card for Hypoglycemia, Hyperglycemia, and Ketone Warnings.
 */
@Composable
fun ClinicalAlertBanner(alertType: AlertType) {
    val (icon, title, borderColor, bgColor, textColor) = when (alertType) {
        AlertType.HYPOGLYCEMIA_WARNING -> Tuple5<ImageVector?, String, Color, Color, Color>(
            Icons.Rounded.Warning,
            "CRITICAL: Hypoglycemia Guidance (<70 mg/dL)",
            Color(0xFFEF4444),
            Color(0xFF381418),
            Color(0xFFFCA5A5)
        )
        AlertType.HYPERGLYCEMIA_WARNING -> Tuple5<ImageVector?, String, Color, Color, Color>(
            Icons.Rounded.Warning,
            "ELEVATED: Hyperglycemia Caution (>250 mg/dL)",
            Color(0xFFF59E0B),
            Color(0xFF352010),
            Color(0xFFFDE68A)
        )
        AlertType.KETONE_WARNING -> Tuple5<ImageVector?, String, Color, Color, Color>(
            Icons.Rounded.Warning,
            "ALERT: Ketone Protocol & Clinical Advisory",
            Color(0xFFF97316),
            Color(0xFF381912),
            Color(0xFFFDBA74)
        )
        AlertType.GENERAL_CLINICAL_ALERT -> Tuple5<ImageVector?, String, Color, Color, Color>(
            Icons.Rounded.Info,
            "Clinical Safety & Medication Advisory",
            Color(0xFF38BDF8),
            Color(0xFF10233D),
            Color(0xFFBAE6FD)
        )
        AlertType.NONE -> Tuple5<ImageVector?, String, Color, Color, Color>(null, "", Color.Transparent, Color.Transparent, Color.Transparent)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = 11.5.sp,
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
                Triple(Color(0xFF0D331A), Color(0xFF10B981), Color(0xFF6EE7B7))
            } else {
                Triple(Color(0xFF0F2B1D), Color(0xFF059669), Color(0xFF34D399))
            }
        }
        BadgeType.CALORIES -> Triple(Color(0xFF33200D), Color(0xFFF59E0B), Color(0xFFFCD34D))
        BadgeType.CARBOHYDRATES -> Triple(Color(0xFF0F263E), Color(0xFF38BDF8), Color(0xFF7DD3FC))
        BadgeType.PROTEIN -> Triple(Color(0xFF2C103D), Color(0xFFA855F7), Color(0xFFE9D5FF))
        BadgeType.MET_ACTIVITY -> Triple(Color(0xFF1E143D), Color(0xFF818CF8), Color(0xFFC7D2FE))
        BadgeType.GENERAL -> Triple(Color(0xFF1E1E28), Color(0xFF64748B), Color(0xFFCBD5E1))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = metric.label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = chipText
        )
    }
}

/**
 * Renders bold Markdown and bullet points cleanly without raw asterisks leaking.
 */
@Composable
fun MarkdownFormattedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFF1F5F9),
    fontSize: TextUnit = 14.5.sp,
    lineHeight: TextUnit = 22.sp
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
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        ),
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
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to DIVA (English / Tanglish)...")
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
