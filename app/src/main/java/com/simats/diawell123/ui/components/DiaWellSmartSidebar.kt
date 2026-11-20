package com.simats.diawell123.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.AppTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Smart Sidebar (Floating Quick-Action Edge Dock) for DiaWell.
 * Features an unobtrusive edge handle docked to the right edge with vertical sliding,
 * and an expandable Obsidian/Royal Purple glassmorphic dock for instant access to:
 * 1. DIVA AI Chat
 * 2. Snap Food AI Analyzer
 * 3. Log Glucose
 * 4. Log Meal
 * 5. Activity Tracking
 */
@Composable
fun DiaWellSmartSidebar(
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
    var isExpanded by remember { mutableStateOf(false) }

    // Vertical drag offset in pixels along the right edge
    var dragOffsetYPx by remember { mutableFloatStateOf(0f) }
    val maxDragOffsetPx = with(density) { 240.dp.toPx() }

    // Subtle edge handle opacity animation: 0.65 idle -> 1.0 active
    val handleAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 0.72f,
        animationSpec = tween(durationMillis = 200),
        label = "handleAlpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
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
                    .background(Color.Black.copy(alpha = if (isDark) 0.42f else 0.28f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isExpanded = false
                    }
            )
        }

        // -------------------------------------------------------------
        // 2. COLLAPSED SLIM EDGE HANDLE (Docked on Right Screen Edge)
        // -------------------------------------------------------------
        if (!isExpanded) {
            var handleDragX by remember { mutableFloatStateOf(0f) }
            var handleDragY by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = dragOffsetYPx.roundToInt()) }
                    .width(18.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 0.dp, bottomEnd = 0.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (isDark) Color(0xCC1E1338) else Color(0xEEEDE9FE),
                                if (isDark) Color(0xDD2A1B4E) else Color(0xEEFFFFFF)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFA855F7), Color(0xFF00E5FF), Color(0xFFA855F7))
                        ),
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                handleDragX = 0f
                                handleDragY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetYPx = (dragOffsetYPx + dragAmount.y).coerceIn(-maxDragOffsetPx, maxDragOffsetPx)
                                handleDragX += dragAmount.x
                                handleDragY += dragAmount.y
                                // Inward horizontal swipe (dragging leftwards by > 20px)
                                if (handleDragX < -22f) {
                                    isExpanded = true
                                }
                            },
                            onDragEnd = {
                                // If user tapped without dragging, expand!
                                if (abs(handleDragX) < 14f && abs(handleDragY) < 14f) {
                                    isExpanded = true
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Subtle Glowing Indicator Bar inside handle
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFFA855F7), Color(0xFFEC4899))
                            )
                        )
                )
            }
        }

        // -------------------------------------------------------------
        // 3. EXPANDED GLASSMORHPIC VERTICAL DOCK
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInHorizontally(
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ) { it } + fadeIn(tween(180)),
            exit = slideOutHorizontally(
                animationSpec = tween(180, easing = FastOutSlowInEasing)
            ) { it } + fadeOut(tween(140)),
            modifier = Modifier.padding(end = 10.dp)
        ) {
            var dockDragX by remember { mutableFloatStateOf(0f) }

            val dockBgBrush = remember(isDark) {
                if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF218112C),
                            Color(0xF2120D22),
                            Color(0xF21B1336)
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

            val dockBorderBrush = remember(isDark) {
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
                    .offset { IntOffset(x = 0, y = dragOffsetYPx.roundToInt()) }
                    .width(88.dp)
                    .shadow(
                        elevation = if (isDark) 20.dp else 14.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color(0xFF0A0618).copy(alpha = 0.7f),
                        spotColor = Color(0xFFA855F7).copy(alpha = 0.55f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(dockBgBrush)
                    .border(width = 1.4.dp, brush = dockBorderBrush, shape = RoundedCornerShape(26.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dockDragX = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dockDragX += dragAmount.x
                                // Swiping rightward (outward) closes the dock
                                if (dockDragX > 25f) {
                                    isExpanded = false
                                }
                            }
                        )
                    }
                    .padding(vertical = 12.dp, horizontal = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Close Chevron Pill
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33A855F7) else Color(0x33EDE9FE))
                            .clickable { isExpanded = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA855F7)
                        )
                    }

                    // 1. DIVA AI Health Assistant
                    SidebarQuickActionItem(
                        icon = "🤖",
                        label = "DIVA AI",
                        badgeBrush = Brush.linearGradient(
                            listOf(Color(0xFF7E22CE), Color(0xFFA855F7))
                        ),
                        isDark = isDark,
                        onClick = {
                            isExpanded = false
                            onDivaAiClick()
                        }
                    )

                    // 2. Snap Food AI Analyzer
                    SidebarQuickActionItem(
                        icon = "📸",
                        label = "Snap Food",
                        badgeBrush = Brush.linearGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF00E5FF))
                        ),
                        isDark = isDark,
                        onClick = {
                            isExpanded = false
                            onSnapFoodClick()
                        }
                    )

                    // 3. Log Blood Glucose
                    SidebarQuickActionItem(
                        icon = "🩸",
                        label = "Glucose",
                        badgeBrush = Brush.linearGradient(
                            listOf(Color(0xFFDC2626), Color(0xFFF43F5E))
                        ),
                        isDark = isDark,
                        onClick = {
                            isExpanded = false
                            onLogGlucoseClick()
                        }
                    )

                    // 4. Log Meal & Nutrition
                    SidebarQuickActionItem(
                        icon = "🥗",
                        label = "Log Meal",
                        badgeBrush = Brush.linearGradient(
                            listOf(Color(0xFF059669), Color(0xFF10B981))
                        ),
                        isDark = isDark,
                        onClick = {
                            isExpanded = false
                            onLogMealClick()
                        }
                    )

                    // 5. Activity / Workout Tracker
                    SidebarQuickActionItem(
                        icon = "🏃",
                        label = "Activity",
                        badgeBrush = Brush.linearGradient(
                            listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                        ),
                        isDark = isDark,
                        onClick = {
                            isExpanded = false
                            onActivityClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarQuickActionItem(
    icon: String,
    label: String,
    badgeBrush: Brush,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular Icon Badge
        Box(
            modifier = Modifier
                .size(42.dp)
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
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Concise Label
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
