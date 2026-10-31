package com.simats.diawell123.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.HomeRoutineItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.ai.IronmanDoctorAvatar
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.profile.ProfileAvatarView
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import com.simats.diawell123.ui.theme.ThemeManager
import kotlin.math.abs
import kotlin.math.exp

@Composable
fun FloatingBubbleContainer(
    modifier: Modifier = Modifier,
    phaseOffsetMs: Int = 0,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BubbleFloat_$phaseOffsetMs")

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800 + (phaseOffsetMs % 400),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatOffsetY",
    )

    val floatScale by infiniteTransition.animateFloat(
        initialValue = 0.992f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500 + (phaseOffsetMs % 300),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatScale",
    )

    val wobbleAngle by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200 + (phaseOffsetMs % 500),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wobbleAngle",
    )

    Box(
        modifier = modifier.graphicsLayer {
            translationY = floatOffsetY.dp.toPx()
            scaleX = floatScale
            scaleY = floatScale
            rotationZ = wobbleAngle
        },
    ) {
        content()
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onLogGlucoseClick: () -> Unit = {},
    onGlucoseHistoryClick: () -> Unit = {},
    onTrackFoodClick: () -> Unit = {},
    onActivityTrackingClick: () -> Unit = {},
    onDietChatbotClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Home") }
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.dashboardData

    LaunchedEffect(Unit) {
        viewModel.loadDashboard(isPullToRefresh = false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Premium Hero Header Banner Card (Floating Bubble Effect)
                ElementPopUpContainer(delayMillis = 0) {
                    FloatingBubbleContainer(phaseOffsetMs = 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6B156E),
                                            Color(0xFFB81D85),
                                            Color(0xFFFF5722),
                                        ),
                                    ),
                                )
                                .padding(18.dp),
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(
                                            text = data?.greeting?.takeIf { it.isNotBlank() } ?: "Good morning, ${ProfileState.userName} 👋",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                        Text(
                                            text = data?.wellnessSubtitle?.takeIf { it.isNotBlank() } ?: "Daily Wellness • 94% On Track",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                        )
                                    }

                                    // Top Right Actions: Bell & Profile Avatar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        // Quick Theme Toggle Button (☀️ / 🌙)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(21.dp))
                                                .background(Color.White.copy(alpha = 0.25f))
                                                .clickable { ThemeManager.toggleTheme() }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = if (AppTheme.colors.isDark) "🌙" else "☀️",
                                                fontSize = 16.sp,
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(21.dp))
                                                .background(Color.White.copy(alpha = 0.25f))
                                                .clickable { onNotificationsClick() }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(text = "🔔", fontSize = 16.sp)
                                        }

                                        Box(
                                            modifier = Modifier.clickable { onProfileClick() },
                                        ) {
                                            ProfileAvatarView(
                                                sizeDp = 42.dp,
                                                fontSizeSp = 15,
                                                bgCircleColor = Color.White.copy(alpha = 0.25f),
                                                textColor = Color.White,
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        Text(
                                            text = data?.streakBadge?.takeIf { it.isNotBlank() } ?: "🔥 7 Day Logging Streak",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    ) {
                                        Text(
                                            text = data?.targetBadge?.takeIf { it.isNotBlank() } ?: "🎯 Fasting Target Met",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                                if (uiState.errorMessage != null && data == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                            .clickable { viewModel.loadDashboard(isPullToRefresh = true) }
                            .padding(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${uiState.errorMessage}",
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Retry ↻",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Title: Key Health Metrics
                Text(
                    text = "Key Health Metrics",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Colorful Metrics Grid (Floating Bubble Effect)
                ElementPopUpContainer(delayMillis = 80) {
                    Column {
                        val gMetric = data?.metrics?.glucose
                        val nMetric = data?.metrics?.nutrition
                        val mMetric = data?.metrics?.medication
                        val wMetric = data?.metrics?.weeklyProgress

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Glucose Metric Card
                            FloatingBubbleContainer(
                                phaseOffsetMs = 200,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    icon = "🩸",
                                    title = gMetric?.title?.ifBlank { "Glucose" } ?: "Glucose",
                                    value = gMetric?.value?.ifBlank { "128 mg/dL" } ?: "128 mg/dL",
                                    subtitle = gMetric?.subtitle?.ifBlank { "Fasting • 7:15 AM" } ?: "Fasting • 7:15 AM",
                                    statusText = gMetric?.statusText?.ifBlank { "In Target Range" } ?: "In Target Range",
                                    statusBg = parseHexColor(gMetric?.statusBg, Color(0xFFE8F5E9)),
                                    statusColor = parseHexColor(gMetric?.statusColor, Color(0xFF2E7D32)),
                                    cardBg = Color(0xFFFDEEF7),
                                    borderColor = AppTheme.colors.border,
                                    actionText = "History ›",
                                    onActionClick = onGlucoseHistoryClick,
                                )
                            }

                            // Today's Diet Card
                            FloatingBubbleContainer(
                                phaseOffsetMs = 400,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    icon = "🥗",
                                    title = nMetric?.title?.ifBlank { "Nutrition" } ?: "Nutrition",
                                    value = nMetric?.value?.ifBlank { "1,450 kcal" } ?: "1,450 kcal",
                                    subtitle = nMetric?.subtitle?.ifBlank { "3 of 4 meals logged" } ?: "3 of 4 meals logged",
                                    statusText = nMetric?.statusText?.ifBlank { "C:160g • P:75g" } ?: "C:160g • P:75g",
                                    statusBg = parseHexColor(nMetric?.statusBg, Color(0xFFE8F5E9)),
                                    statusColor = parseHexColor(nMetric?.statusColor, Color(0xFF2E7D32)),
                                    cardBg = Color(0xFFEBF7F0),
                                    borderColor = Color(0xFFD4EFE0),
                                    actionText = "Explore",
                                    onActionClick = onTrackFoodClick,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Medication Card
                            FloatingBubbleContainer(
                                phaseOffsetMs = 600,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    icon = "💊",
                                    title = mMetric?.title?.ifBlank { "Medication" } ?: "Medication",
                                    value = mMetric?.value?.ifBlank { "Metformin" } ?: "Metformin",
                                    subtitle = mMetric?.subtitle?.ifBlank { "500mg • 8:00 PM" } ?: "500mg • 8:00 PM",
                                    statusText = mMetric?.statusText?.ifBlank { "Upcoming Dose" } ?: "Upcoming Dose",
                                    statusBg = parseHexColor(mMetric?.statusBg, Color(0xFFFFF3E0)),
                                    statusColor = parseHexColor(mMetric?.statusColor, Color(0xFFE65100)),
                                    cardBg = Color(0xFFFFF8EE),
                                    borderColor = Color(0xFFFFECC8),
                                    actionText = "View",
                                    onActionClick = onMedsTabClick,
                                )
                            }

                            // Weekly Progress Card
                            FloatingBubbleContainer(
                                phaseOffsetMs = 800,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    icon = "📊",
                                    title = wMetric?.title?.ifBlank { "Weekly Report" } ?: "Weekly Report",
                                    value = wMetric?.value?.ifBlank { "7/7 Days" } ?: "7/7 Days",
                                    subtitle = wMetric?.subtitle?.ifBlank { "Glucose & Diet Logged" } ?: "Glucose & Diet Logged",
                                    statusText = wMetric?.statusText?.ifBlank { "Excellent Routine" } ?: "Excellent Routine",
                                    statusBg = parseHexColor(wMetric?.statusBg, Color(0xFFE3F2FD)),
                                    statusColor = parseHexColor(wMetric?.statusColor, Color(0xFF1565C0)),
                                    cardBg = Color(0xFFF0F7FF),
                                    borderColor = Color(0xFFD0E4FC),
                                    actionText = "Reports",
                                    onActionClick = onReportsTabClick,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Featured Dr. JARVIS AI Daily Tip Card (Floating Bubble Effect)
                ElementPopUpContainer(delayMillis = 140) {
                    FloatingBubbleContainer(phaseOffsetMs = 300) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    brush = if (AppTheme.colors.isDark) {
                                        Brush.linearGradient(listOf(Color(0xFF202C40), Color(0xFF182235)))
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xFFFDEEF7), Color(0xFFFFF0F5)))
                                    },
                                )
                                .border(1.5.dp, if (AppTheme.colors.isDark) AppTheme.colors.border else AppTheme.colors.border, RoundedCornerShape(18.dp))
                                .clickable { onDietChatbotClick() }
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IronmanDoctorAvatar(sizeDp = 44)

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = data?.aiTip?.title ?: "Dr. JARVIS Tip Of The Day ⚡",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB81D85),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = data?.aiTip?.tip ?: "A 10-minute light walk after meals helps smooth out blood sugar levels effectively!",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textSecondary,
                                        lineHeight = 17.sp,
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Ask →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB81D85),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Today's Routine Timeline Check-In (Floating Bubble Effect)
                ElementPopUpContainer(delayMillis = 200) {
                    FloatingBubbleContainer(phaseOffsetMs = 500) {
                        Column {
                            Text(
                                text = "Today's Check-In Routine",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AppTheme.colors.cardBackground)
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val routineItems = data?.routine?.takeIf { it.isNotEmpty() } ?: listOf(
                                        HomeRoutineItem(icon = "🥣", label = "Breakfast", isDone = false),
                                        HomeRoutineItem(icon = "🩸", label = "Glucose", isDone = true),
                                        HomeRoutineItem(icon = "🥗", label = "Lunch", isDone = false),
                                        HomeRoutineItem(icon = "💊", label = "Dinner/Meds", isDone = false)
                                    )
                                    routineItems.forEach { item ->
                                        RoutineCheckItem(
                                            icon = item.icon.ifBlank { "✓" },
                                            label = item.label,
                                            isDone = item.isDone
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Vibrant Quick Actions (Floating Bubble Effect)
                ElementPopUpContainer(delayMillis = 260) {
                    Column {
                        Text(
                            text = "Quick Actions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            FloatingBubbleContainer(
                                phaseOffsetMs = 150,
                                modifier = Modifier.weight(1f),
                            ) {
                                VibrantQuickActionButton(
                                    title = "Log Glucose",
                                    icon = "🩸",
                                    gradientColors = listOf(Color(0xFFE53935), Color(0xFFFF5722)),
                                    onClick = onLogGlucoseClick,
                                )
                            }
                            FloatingBubbleContainer(
                                phaseOffsetMs = 350,
                                modifier = Modifier.weight(1f),
                            ) {
                                VibrantQuickActionButton(
                                    title = "Activity & Workout",
                                    icon = "🏃‍♂️",
                                    gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF00838F)),
                                    onClick = onActivityTrackingClick,
                                )
                            }
                            FloatingBubbleContainer(
                                phaseOffsetMs = 550,
                                modifier = Modifier.weight(1f),
                            ) {
                                VibrantQuickActionButton(
                                    title = "JARVIS AI",
                                    icon = "💬",
                                    gradientColors = listOf(Color(0xFF7A1C7D), Color(0xFFBD1E85)),
                                    onClick = onDietChatbotClick,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock (Pops Up)
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = { selectedTab = "Home" },
                onDietTabClick = onDietTabClick,
                onMedsTabClick = onMedsTabClick,
                onReportsTabClick = onReportsTabClick,
                onSettingsTabClick = onSettingsTabClick,
            )
        }
    }
}

@Composable
fun IosLiquidGlassBottomNav(
    selectedTab: String,
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val tabs = remember {
        listOf(
            "Home" to "🏠",
            "Diet" to "🥗",
            "Meds" to "💊",
            "Reports" to "📊",
            "Settings" to "⚙️",
        )
    }

    val selectedIndex = remember(selectedTab) {
        tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    }

    var dockWidthPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragXOffsetPx by remember { mutableFloatStateOf(0f) }

    fun triggerTabSelection(index: Int) {
        val tabName = tabs.getOrNull(index)?.first ?: return
        if (tabName != selectedTab) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
            when (tabName) {
                "Home" -> onHomeTabClick()
                "Diet" -> onDietTabClick()
                "Meds" -> onMedsTabClick()
                "Reports" -> onReportsTabClick()
                "Settings" -> onSettingsTabClick()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val itemWidthPx = if (dockWidthPx > 0f) dockWidthPx / tabs.size else 1f

        // Target Center X of the Active Liquid Bubble
        val targetActiveXPx = if (isDragging && dockWidthPx > 0f) {
            dragXOffsetPx.coerceIn(itemWidthPx / 2f, dockWidthPx - itemWidthPx / 2f)
        } else {
            (selectedIndex + 0.5f) * itemWidthPx
        }

        // Animated Fluid Active Center X Position
        val animatedActiveXPx by animateFloatAsState(
            targetValue = targetActiveXPx,
            animationSpec = spring(
                dampingRatio = 0.68f, // Bouncy Liquid Bubble Motion
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "fluidActiveXAnimation",
        )

        // Floating Organic Fluid Liquid Glass Dock Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .onGloballyPositioned { coordinates ->
                    dockWidthPx = coordinates.size.width.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragXOffsetPx = offset.x
                            val targetIndex = (offset.x / (dockWidthPx / tabs.size)).toInt().coerceIn(0, tabs.size - 1)
                            triggerTabSelection(targetIndex)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragXOffsetPx = change.position.x
                            val targetIndex = (change.position.x / (dockWidthPx / tabs.size)).toInt().coerceIn(0, tabs.size - 1)
                            triggerTabSelection(targetIndex)
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            // 1. ORGANIC FLUID LIQUID GLASS CANVAS DOCK (Bulges dynamically around active tab!)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerY = h / 2f

                val baseHeight = 46.dp.toPx()
                val bulgeHeight = 60.dp.toPx()
                val halfBase = baseHeight / 2f
                val halfBulge = bulgeHeight / 2f
                val bulgeRadiusPx = itemWidthPx * 0.70f

                // Draw Organic Morphing Liquid Glass Outer Boundary Path
                val fluidPath = Path().apply {
                    val steps = 60
                    val dx = w / steps

                    // Top Organic Curve
                    val topPoints = ArrayList<Offset>()
                    for (i in 0..steps) {
                        val x = i * dx
                        val distFromActive = abs(x - animatedActiveXPx)
                        val influence = exp(-((distFromActive / bulgeRadiusPx) * (distFromActive / bulgeRadiusPx)).toDouble()).toFloat()
                        val currentHalfH = halfBase + (halfBulge - halfBase) * influence
                        topPoints.add(Offset(x, centerY - currentHalfH))
                    }

                    // Bottom Organic Curve
                    val bottomPoints = ArrayList<Offset>()
                    for (i in 0..steps) {
                        val x = i * dx
                        val distFromActive = abs(x - animatedActiveXPx)
                        val influence = exp(-((distFromActive / bulgeRadiusPx) * (distFromActive / bulgeRadiusPx)).toDouble()).toFloat()
                        val currentHalfH = halfBase + (halfBulge - halfBase) * influence
                        bottomPoints.add(Offset(x, centerY + currentHalfH))
                    }

                    // Build Closed Organic Path
                    val firstPt = topPoints.first()
                    moveTo(firstPt.x, firstPt.y)
                    for (i in 1 until topPoints.size) {
                        lineTo(topPoints[i].x, topPoints[i].y)
                    }

                    // Right Cap
                    arcTo(
                        rect = Rect(
                            left = w - baseHeight,
                            top = centerY - halfBase,
                            right = w,
                            bottom = centerY + halfBase,
                        ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false,
                    )

                    // Bottom Curve (Right to Left)
                    for (i in bottomPoints.size - 1 downTo 0) {
                        lineTo(bottomPoints[i].x, bottomPoints[i].y)
                    }

                    // Left Cap
                    arcTo(
                        rect = Rect(
                            left = 0f,
                            top = centerY - halfBase,
                            right = baseHeight,
                            bottom = centerY + halfBase,
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false,
                    )

                    close()
                }

                // Glass Ambient Shadow Glow
                val isDarkNav = AppTheme.colors.isDark
                drawPath(
                    path = fluidPath,
                    brush = Brush.radialGradient(
                        colors = if (isDarkNav) {
                            listOf(
                                Color(0x6600E5FF),
                                Color(0x33B81D85),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                Color(0x668E24AA),
                                Color(0x225363FF),
                                Color.Transparent,
                            )
                        },
                        center = Offset(animatedActiveXPx, centerY),
                        radius = bulgeRadiusPx * 1.5f,
                    ),
                )

                // Translucent Liquid Glass Background Fill
                drawPath(
                    path = fluidPath,
                    brush = Brush.verticalGradient(
                        colors = if (isDarkNav) {
                            listOf(
                                Color(0xFF1E293B).copy(alpha = 0.95f),
                                Color(0xFF0F172A).copy(alpha = 0.92f),
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.92f),
                                Color(0xFFF2E6F5).copy(alpha = 0.88f),
                            )
                        },
                    ),
                )

                // Luminous Specular Glass Outline Rim
                drawPath(
                    path = fluidPath,
                    brush = Brush.horizontalGradient(
                        colors = if (isDarkNav) {
                            listOf(
                                Color(0xFF334155),
                                Color(0xFF00E5FF).copy(alpha = 0.50f),
                                Color(0xFFF472B6).copy(alpha = 0.50f),
                                Color(0xFF334155),
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color(0xFF00E5FF).copy(alpha = 0.60f),
                                Color(0xFFB81D85).copy(alpha = 0.60f),
                                Color.White.copy(alpha = 0.95f),
                            )
                        },
                    ),
                    style = Stroke(width = 1.8.dp.toPx()),
                )
            }

            // 2. INNER FLOATING ACTIVE LIQUID GLASS BUTTON PILL (Matches Image EXACTLY!)
            if (dockWidthPx > 0f) {
                val pillWidthDp = with(density) { (itemWidthPx - 10.dp.toPx()).toDp() }
                val pillOffsetDp = with(density) { (animatedActiveXPx - (itemWidthPx - 10.dp.toPx()) / 2f).toDp() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = pillOffsetDp)
                            .width(pillWidthDp)
                            .height(50.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0xFF5363FF).copy(alpha = 0.45f),
                                spotColor = Color(0xFF8E24AA).copy(alpha = 0.55f),
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF5363FF),
                                        Color(0xFF8E24AA),
                                    ),
                                ),
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color.White.copy(alpha = 0.30f),
                                    ),
                                ),
                                shape = RoundedCornerShape(20.dp),
                            ),
                    ) {
                        // Glass Top Gloss Highlight Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.42f),
                                            Color.White.copy(alpha = 0f),
                                        ),
                                    ),
                                ),
                        )
                    }
                }
            }

            // 3. TAB ICONS & LABELS LAYER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, (name, icon) ->
                    val isSelected = selectedTab == name

                    val iconPopScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessLow,
                        ),
                        label = "tabPopScale",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { triggerTabSelection(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconPopScale
                                scaleY = iconPopScale
                            },
                        ) {
                            Text(
                                text = icon,
                                fontSize = if (isSelected) 18.sp else 16.sp,
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = name,
                                fontSize = if (isSelected) 10.sp else 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    statusText: String,
    statusBg: Color,
    statusColor: Color,
    cardBg: Color,
    borderColor: Color,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actualCardBg = if (AppTheme.colors.isDark) AppTheme.colors.cardBackground else cardBg
    val actualBorder = if (AppTheme.colors.isDark) AppTheme.colors.border else borderColor
    val actualStatusBg = if (AppTheme.colors.isDark) statusColor.copy(alpha = 0.22f) else statusBg
    val actualActionColor = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFFB81D85)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(actualCardBg)
            .border(1.dp, actualBorder, RoundedCornerShape(16.dp))
            .clickable { onActionClick() }
            .padding(14.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = icon, fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textSecondary,
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = AppTheme.colors.textMuted,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "$actionText →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = actualActionColor,
                )
            }
        }
    }
}

@Composable
fun RoutineCheckItem(
    icon: String,
    label: String,
    isDone: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isDone) AppTheme.colors.successBg else AppTheme.colors.secondaryBackground)
                .border(
                    width = 1.5.dp,
                    color = if (isDone) AppTheme.colors.success else AppTheme.colors.border,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = if (isDone) "✓" else icon, fontSize = 16.sp, color = if (isDone) AppTheme.colors.successText else AppTheme.colors.textPrimary)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
            color = if (isDone) AppTheme.colors.successText else AppTheme.colors.textSecondary,
        )
    }
}

@Composable
fun VibrantQuickActionButton(
    title: String,
    icon: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(colors = gradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
                textAlign = TextAlign.Center,
            )
        }
    }
}


fun parseHexColor(hexString: String?, fallback: Color): Color {
    if (hexString.isNullOrBlank()) return fallback
    return try {
        val cleanHex = hexString.removePrefix("#").trim()
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(0xFF000000 or colorInt)
        } else if (cleanHex.length == 8) {
            Color(colorInt)
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Diawell123Theme {
        HomeScreen()
    }
}
