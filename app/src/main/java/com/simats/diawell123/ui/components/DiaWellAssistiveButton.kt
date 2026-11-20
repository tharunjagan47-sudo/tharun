package com.simats.diawell123.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.R
import com.simats.diawell123.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * iOS AssistiveTouch-style Floating Quick-Action Button for DiaWell.
 * - 56.dp circular floating button featuring the DiaWell app logo.
 * - Idle Auto-Dim (dims down to 0.45f after 3s of inactivity, returns to 1.0f on touch).
 * - Ultra-smooth 60fps drag with magnetic edge snapping (springs to Left/Right edge on release).
 * - Tap to expand a centered dark-glass AssistiveTouch menu for 5 quick actions:
 *   1. DIVA AI Health Assistant
 *   2. Snap Food AI Analyzer
 *   3. Log Glucose
 *   4. Log Meal
 *   5. Activity / Workout Tracker
 */
@Composable
fun DiaWellAssistiveButton(
    visible: Boolean,
    onDivaAiClick: () -> Unit,
    onSnapFoodClick: () -> Unit,
    onLogGlucoseClick: () -> Unit,
    onLogMealClick: () -> Unit,
    onActivityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    val isDark = AppTheme.colors.isDark
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val buttonSizeDp = 56.dp
        val buttonSizePx = with(density) { buttonSizeDp.toPx() }
        val edgePaddingPx = with(density) { 12.dp.toPx() }

        val minYPx = with(density) { 80.dp.toPx() }
        val maxYPx = (screenHeightPx - with(density) { 150.dp.toPx() }).coerceAtLeast(minYPx)

        // Initial docked position: Right edge, middle of screen
        val initialXPx = (screenWidthPx - buttonSizePx - edgePaddingPx).coerceAtLeast(edgePaddingPx)
        val initialYPx = (screenHeightPx * 0.52f).coerceIn(minYPx, maxYPx)

        val offsetX = remember { Animatable(initialXPx) }
        val offsetY = remember { Animatable(initialYPx) }

        var isDragging by remember { mutableStateOf(false) }
        var isExpanded by remember { mutableStateOf(false) }
        var isIdle by remember { mutableStateOf(false) }
        var lastInteractionMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

        // Keep offsetX anchored within screen on orientation/size updates
        LaunchedEffect(screenWidthPx) {
            val isNearRight = offsetX.value > screenWidthPx / 2f
            val targetX = if (isNearRight) {
                screenWidthPx - buttonSizePx - edgePaddingPx
            } else {
                edgePaddingPx
            }
            offsetX.snapTo(targetX.coerceAtLeast(edgePaddingPx))
        }

        // Idle Auto-Dim Timer (dims to 0.45f after 3 seconds of no interaction)
        LaunchedEffect(lastInteractionMs, isDragging, isExpanded) {
            if (isDragging || isExpanded) {
                isIdle = false
            } else {
                isIdle = false
                delay(3000)
                isIdle = true
            }
        }

        val buttonAlpha by animateFloatAsState(
            targetValue = if (isIdle && !isExpanded) 0.45f else 1.0f,
            animationSpec = tween(durationMillis = 500),
            label = "assistiveButtonAlpha"
        )

        // -------------------------------------------------------------
        // 1. FULLSCREEN TRANSPARENT SCRIM (Dismiss on outside tap)
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (isDark) 0.52f else 0.38f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isExpanded = false
                        lastInteractionMs = System.currentTimeMillis()
                    }
            )
        }

        // -------------------------------------------------------------
        // 2. EXPANDABLE ASSISTIVETOUCH GLASS PANEL (Centered Overlay)
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isExpanded,
            enter = scaleIn(
                initialScale = 0.82f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            ) + fadeIn(tween(180)),
            exit = scaleOut(
                targetScale = 0.85f,
                animationSpec = tween(140)
            ) + fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            AssistiveTouchGlassMenu(
                isDark = isDark,
                onDivaAiClick = {
                    isExpanded = false
                    lastInteractionMs = System.currentTimeMillis()
                    onDivaAiClick()
                },
                onSnapFoodClick = {
                    isExpanded = false
                    lastInteractionMs = System.currentTimeMillis()
                    onSnapFoodClick()
                },
                onLogGlucoseClick = {
                    isExpanded = false
                    lastInteractionMs = System.currentTimeMillis()
                    onLogGlucoseClick()
                },
                onLogMealClick = {
                    isExpanded = false
                    lastInteractionMs = System.currentTimeMillis()
                    onLogMealClick()
                },
                onActivityClick = {
                    isExpanded = false
                    lastInteractionMs = System.currentTimeMillis()
                    onActivityClick()
                },
                onCloseClick = {
                    isExpanded = false
                    lastInteractionMs = System.currentTimeMillis()
                }
            )
        }

        // -------------------------------------------------------------
        // 3. DRAGGABLE FLOATING BUTTON (DiaWell Logo + Glass Ring)
        // -------------------------------------------------------------
        var totalDragDistance by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .size(buttonSizeDp)
                .alpha(if (isExpanded) 0f else buttonAlpha)
                .shadow(
                    elevation = if (isDragging) 18.dp else 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF0A0618).copy(alpha = 0.7f),
                    spotColor = Color(0xFFA855F7).copy(alpha = 0.65f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF2E1955), Color(0xFF140E26), Color(0xFF0D0A18))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0xFFF3E8FF), Color(0xFFE9D5FF))
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFA855F7),
                            Color(0xFF00E5FF),
                            Color(0xFFEC4899),
                            Color(0xFF6366F1),
                            Color(0xFFA855F7)
                        )
                    ),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            lastInteractionMs = System.currentTimeMillis()
                            totalDragDistance = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            lastInteractionMs = System.currentTimeMillis()
                            totalDragDistance += abs(dragAmount.x) + abs(dragAmount.y)

                            val newX = (offsetX.value + dragAmount.x)
                                .coerceIn(edgePaddingPx, screenWidthPx - buttonSizePx - edgePaddingPx)
                            val newY = (offsetY.value + dragAmount.y)
                                .coerceIn(minYPx, maxYPx)

                            coroutineScope.launch {
                                offsetX.snapTo(newX)
                                offsetY.snapTo(newY)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            lastInteractionMs = System.currentTimeMillis()

                            // If drag movement is tiny, treat as a Tap!
                            if (totalDragDistance < 14f) {
                                isExpanded = true
                            } else {
                                // Magnetic Edge Snapping: spring to Left or Right edge
                                val snapToLeft = (offsetX.value + buttonSizePx / 2f) < (screenWidthPx / 2f)
                                val targetX = if (snapToLeft) {
                                    edgePaddingPx
                                } else {
                                    screenWidthPx - buttonSizePx - edgePaddingPx
                                }

                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = targetX,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // DiaWell App Logo centered inside floating button
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "DiaWell AssistiveTouch Menu",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
        }
    }
}

/**
 * iOS AssistiveTouch-style Glassmorphic Quick Action Panel.
 * Shows 5 core actions + a dismiss button in a 3x2 grid.
 */
@Composable
private fun AssistiveTouchGlassMenu(
    isDark: Boolean,
    onDivaAiClick: () -> Unit,
    onSnapFoodClick: () -> Unit,
    onLogGlucoseClick: () -> Unit,
    onLogMealClick: () -> Unit,
    onActivityClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val panelBgBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color(0xF51B1233),
                    Color(0xF5120C24),
                    Color(0xF518102E)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xF8FFFFFF),
                    Color(0xF8F5F3FF),
                    Color(0xF8FAF5FF)
                )
            )
        }
    }

    val panelBorderBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFFA855F7),
                Color(0xFF00E5FF),
                Color(0xFFEC4899),
                Color(0xFFA855F7)
            )
        )
    }

    Box(
        modifier = Modifier
            .width(286.dp)
            .shadow(
                elevation = if (isDark) 24.dp else 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFF0A0618).copy(alpha = 0.8f),
                spotColor = Color(0xFFA855F7).copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(panelBgBrush)
            .border(width = 1.6.dp, brush = panelBorderBrush, shape = RoundedCornerShape(32.dp))
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title with DiaWell badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ DiaWell Quick Actions",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF3E8FF) else Color(0xFF4C1D95)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Row 1: DIVA AI, Snap Food, Log Glucose
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistiveMenuItem(
                    icon = "🤖",
                    label = "DIVA AI",
                    badgeBrush = Brush.linearGradient(listOf(Color(0xFF7E22CE), Color(0xFFA855F7))),
                    isDark = isDark,
                    onClick = onDivaAiClick
                )

                AssistiveMenuItem(
                    icon = "📸",
                    label = "Snap Food",
                    badgeBrush = Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF00E5FF))),
                    isDark = isDark,
                    onClick = onSnapFoodClick
                )

                AssistiveMenuItem(
                    icon = "🩸",
                    label = "Glucose",
                    badgeBrush = Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFFF43F5E))),
                    isDark = isDark,
                    onClick = onLogGlucoseClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Log Meal, Activity, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistiveMenuItem(
                    icon = "🥗",
                    label = "Log Meal",
                    badgeBrush = Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
                    isDark = isDark,
                    onClick = onLogMealClick
                )

                AssistiveMenuItem(
                    icon = "🏃",
                    label = "Activity",
                    badgeBrush = Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B))),
                    isDark = isDark,
                    onClick = onActivityClick
                )

                AssistiveMenuItem(
                    icon = "✕",
                    label = "Close",
                    badgeBrush = Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF2E2248), Color(0xFF1D1433))
                        else listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    ),
                    isDark = isDark,
                    onClick = onCloseClick
                )
            }
        }
    }
}

@Composable
private fun AssistiveMenuItem(
    icon: String,
    label: String,
    badgeBrush: Brush,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular Icon Badge
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFA855F7).copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .background(badgeBrush)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Concise Label
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
