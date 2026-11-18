package com.simats.diawell123.ui.home

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import com.simats.diawell123.data.local.SessionManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.HomeRoutineItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.ai.IronmanDoctorAvatar
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.profile.ProfileAvatarView
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import com.simats.diawell123.ui.theme.ThemeManager
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.roundToInt
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
    onExportReportClick: () -> Unit = {},
    onTrackFoodCategoryClick: (category: String) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf("Home") }
    var showStreakDialog by remember { mutableStateOf(false) }
    var showTargetEditDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.dashboardData
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (data == null) {
            viewModel.loadDashboard(isPullToRefresh = false)
        }
    }

    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboard(isPullToRefresh = true)
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
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
                .padding(bottom = 100.dp)
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
                                            Color(0xFF1E1B4B), // Deep Indigo
                                            Color(0xFF7E22CE), // Royal Purple
                                            Color(0xFFA855F7), // Electric Purple
                                            Color(0xFFF0ABFC), // Neon Lilac / Orchid
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
                                    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                                    val timeGreeting = remember(currentHour) {
                                        when {
                                            currentHour < 12 -> "Good morning"
                                            currentHour < 17 -> "Good afternoon"
                                            else -> "Good evening"
                                        }
                                    }
                                    val displayName: String = data?.profile?.name?.takeIf { it.isNotBlank() }
                                        ?: ProfileState.userName.takeIf { it.isNotBlank() }
                                        ?: (try { SessionManager.getInstance().getFullName() } catch (_: Exception) { null })?.takeIf { it.isNotBlank() }
                                        ?: "Patient"

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 12.dp),
                                    ) {
                                        Text(
                                            text = "$timeGreeting,",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.90f),
                                            letterSpacing = 0.5.sp,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$displayName 👋",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = data?.wellnessSubtitle?.takeIf { it.isNotBlank() } ?: "Daily Wellness • Start your day",
                                            fontSize = 11.5.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    // Top Right Actions: Theme Toggle & Profile Avatar (Bell button removed)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        // Quick Theme Toggle Button (☀️ / 🌙) - Perfectly Circular
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.20f))
                                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                                .clickable { ThemeManager.toggleTheme() },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = if (AppTheme.colors.isDark) "🌙" else "☀️",
                                                fontSize = 16.sp,
                                            )
                                        }

                                        // Profile Avatar - Crisp, distinct, perfectly circular with specular border
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .clickable { onProfileClick() },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            ProfileAvatarView(
                                                sizeDp = 44.dp,
                                                fontSizeSp = 16,
                                                bgCircleColor = Color.White.copy(alpha = 0.25f),
                                                borderColor = Color.White.copy(alpha = 0.90f),
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
                                    // Interactive Streak Badge (e.g. 2-Day Streak)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.22f))
                                            .border(1.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
                                            .clickable { showStreakDialog = true }
                                            .padding(horizontal = 11.dp, vertical = 6.dp),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = data?.streakBadge?.takeIf { it.isNotBlank() } ?: "⚡ Start your streak today",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "🔥", fontSize = 10.sp)
                                        }
                                    }

                                    // Interactive Target Editing Badge / Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.22f))
                                            .border(1.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
                                            .clickable { showTargetEditDialog = true }
                                            .padding(horizontal = 11.dp, vertical = 6.dp),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = data?.targetBadge?.takeIf { it.isNotBlank() } ?: "🎯 Target: 100 mg/dL",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(text = "✏️", fontSize = 11.sp)
                                        }
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

                // Section Title
                Text(
                    text = "Key Health Metrics",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.fillMaxWidth(),
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
                                    value = gMetric?.value?.ifBlank { "-- mg/dL" } ?: "-- mg/dL",
                                    subtitle = gMetric?.subtitle?.ifBlank { "Tap to log your first reading" } ?: "Tap to log your first reading",
                                    statusText = gMetric?.statusText?.ifBlank { "No Readings Logged" } ?: "No Readings Logged",
                                    statusBg = parseHexColor(gMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = parseHexColor(gMetric?.statusColor, Color(0xFF6B7280)),
                                    cardBg = if (AppTheme.colors.isDark) Color(0xFF2E1065) else Color(0xFFF5F3FF),
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
                                    value = nMetric?.value?.ifBlank { "0 kcal" } ?: "0 kcal",
                                    subtitle = nMetric?.subtitle?.ifBlank { "0 meals logged today" } ?: "0 meals logged today",
                                    statusText = nMetric?.statusText?.ifBlank { "No meals logged" } ?: "No meals logged",
                                    statusBg = parseHexColor(nMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = parseHexColor(nMetric?.statusColor, Color(0xFF6B7280)),
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
                                    value = mMetric?.value?.ifBlank { "0 Active Medications" } ?: "0 Active Medications",
                                    subtitle = mMetric?.subtitle?.ifBlank { "No medications scheduled" } ?: "No medications scheduled",
                                    statusText = mMetric?.statusText?.ifBlank { "No Reminders" } ?: "No Reminders",
                                    statusBg = parseHexColor(mMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = parseHexColor(mMetric?.statusColor, Color(0xFF6B7280)),
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
                                    value = wMetric?.value?.ifBlank { "0/7 Days" } ?: "0/7 Days",
                                    subtitle = wMetric?.subtitle?.ifBlank { "No logs this week" } ?: "No logs this week",
                                    statusText = wMetric?.statusText?.ifBlank { "Start logging today" } ?: "Start logging today",
                                    statusBg = parseHexColor(wMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = parseHexColor(wMetric?.statusColor, Color(0xFF6B7280)),
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
                                        Brush.linearGradient(listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF)))
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
                                            color = Color(0xFFA855F7),
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
                                    color = Color(0xFFA855F7),
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
                                    .padding(horizontal = 10.dp, vertical = 14.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val defaultRoutine = remember {
                                        listOf(
                                            HomeRoutineItem(icon = "🥣", label = "Breakfast", isDone = false),
                                            HomeRoutineItem(icon = "🩸", label = "Glucose", isDone = false),
                                            HomeRoutineItem(icon = "🥗", label = "Lunch", isDone = false),
                                            HomeRoutineItem(icon = "💊", label = "Dinner/Meds", isDone = false),
                                        )
                                    }
                                    val routineItems = data?.routine?.takeIf { it.isNotEmpty() } ?: defaultRoutine
                                    routineItems.forEach { item ->
                                        val onRoutineClick: () -> Unit = when {
                                            item.label.contains("Glucose", ignoreCase = true) -> onLogGlucoseClick
                                            item.label.contains("Breakfast", ignoreCase = true) -> {
                                                { onTrackFoodCategoryClick("breakfast") }
                                            }
                                            item.label.contains("Lunch", ignoreCase = true) -> {
                                                { onTrackFoodCategoryClick("lunch") }
                                            }
                                            item.label.contains("Dinner", ignoreCase = true) -> {
                                                { onTrackFoodCategoryClick("dinner") }
                                            }
                                            item.label.contains("Med", ignoreCase = true) -> onMedsTabClick
                                            else -> onTrackFoodClick
                                        }
                                        RoutineCheckItem(
                                            icon = item.icon.ifBlank { "✓" },
                                            label = item.label,
                                            isDone = item.isDone,
                                            onClick = onRoutineClick,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // 5. Vibrant Quick Actions
                ElementPopUpContainer(delayMillis = 240) {
                    Column {
                        Text(
                            text = "Quick Actions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FloatingBubbleContainer(phaseOffsetMs = 250) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                VibrantQuickActionButton(
                                    title = "Log Glucose",
                                    icon = "🩸",
                                    gradientColors = listOf(Color(0xFFE53935), Color(0xFFFF5722)),
                                    onClick = onLogGlucoseClick,
                                    modifier = Modifier.weight(1f),
                                )
                                VibrantQuickActionButton(
                                    title = "Log Meal",
                                    icon = "🥗",
                                    gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)),
                                    onClick = onTrackFoodClick,
                                    modifier = Modifier.weight(1f),
                                )
                                VibrantQuickActionButton(
                                    title = "Activity",
                                    icon = "🏃‍♂️",
                                    gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF00838F)),
                                    onClick = onActivityTrackingClick,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        if (showStreakDialog) {
            StreakDetailsDialog(
                streakDays = data?.streakDays ?: 2,
                streakBadge = data?.streakBadge ?: "🔥 2 Day Logging Streak",
                onDismissRequest = { showStreakDialog = false },
                onLogGlucoseClick = {
                    showStreakDialog = false
                    onLogGlucoseClick()
                },
                onTrackFoodClick = {
                    showStreakDialog = false
                    onTrackFoodClick()
                }
            )
        }

        if (showTargetEditDialog) {
            TargetEditDialog(
                initialFasting = data?.targetFastingGlucose ?: 100,
                initialPostmeal = data?.targetPostmealGlucose ?: 140,
                onDismissRequest = { showTargetEditDialog = false },
                onSaveTarget = { fasting, postmeal ->
                    viewModel.updateTargetGlucose(
                        fastingTarget = fasting,
                        postmealTarget = postmeal,
                        onSuccess = {
                            showTargetEditDialog = false
                            Toast.makeText(context, "Target blood sugar updated to $fasting mg/dL!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
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
    val density = LocalDensity.current
    val isDark = AppTheme.colors.isDark

    val tabs = remember {
        listOf(
            "Home" to Icons.Rounded.Home,
            "Diet" to Icons.Rounded.Restaurant,
            "Meds" to Icons.Rounded.Medication,
            "Reports" to Icons.Rounded.Assessment,
            "Settings" to Icons.Rounded.Settings,
        )
    }

    // Single source of truth: strictly driven by selectedTab index (zero conflicting drag state)
    val selectedTabIndex = remember(selectedTab) {
        tabs.indexOfFirst { it.first == selectedTab }.coerceIn(0, tabs.lastIndex)
    }

    var rowWidthPx by remember { mutableFloatStateOf(0f) }
    val tabSlotWidthPx = if (tabs.isNotEmpty()) rowWidthPx / tabs.size.toFloat() else 0f
    val pillWidthDp = 64.dp
    val pillHeightDp = 48.dp
    val pillWidthPx = with(density) { pillWidthDp.toPx() }

    val animatedIndexState = animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.82f, // Crisp, zero-overshoot hi-tech snap
            stiffness = 680f,     // Ultra-fast response (never gets stuck)
        ),
        label = "HiTechPillSlide",
    )

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

    // Pre-allocated hardware-accelerated shapes & brushes
    val dockShape = remember { RoundedCornerShape(34.dp) }
    val pillShape = remember { RoundedCornerShape(24.dp) }
    val laserShape = remember { RoundedCornerShape(50) }

    val dockFillColor = if (isDark) Color(0xF013111C) else Color(0xF5FFFFFF)
    val dockBorderBrush = remember(isDark) {
        Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(Color(0x66A855F7), Color(0x22FFFFFF), Color(0x667C3AED))
            } else {
                listOf(Color(0x55A855F7), Color(0x227C3AED), Color(0x557C3AED))
            },
        )
    }
    val activeCapsuleBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF9333EA), Color(0xFF5B21B6)),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer Hi-Tech Cyber-Medical Floating Glass Capsule Dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = if (isDark) 20.dp else 12.dp,
                    shape = dockShape,
                    ambientColor = Color(0xFF09061A).copy(alpha = if (isDark) 0.75f else 0.16f),
                    spotColor = Color(0xFF9333EA).copy(alpha = if (isDark) 0.42f else 0.22f),
                )
                .clip(dockShape)
                .background(dockFillColor)
                .border(width = 1.dp, brush = dockBorderBrush, shape = dockShape)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // 1. EXACT PIXEL-LOCKED SLIDING ACTIVE CAPSULE (64.dp x 48.dp)
            if (rowWidthPx > 0f) {
                Box(
                    modifier = Modifier
                        .offset {
                            val xPx = (animatedIndexState.value * tabSlotWidthPx) +
                                ((tabSlotWidthPx - pillWidthPx) / 2f)
                            IntOffset(xPx.roundToInt(), 0)
                        }
                        .width(pillWidthDp)
                        .height(pillHeightDp)
                        .shadow(
                            elevation = 12.dp,
                            shape = pillShape,
                            ambientColor = Color(0xFF9333EA).copy(alpha = 0.65f),
                            spotColor = Color(0xFFA855F7).copy(alpha = 0.85f),
                        )
                        .clip(pillShape)
                        .background(activeCapsuleBrush)
                        .border(
                            width = 0.8.dp,
                            color = Color(0x66E9D5FF),
                            shape = pillShape,
                        ),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    // Bright top neon laser indicator line
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .width(24.dp)
                            .height(3.dp)
                            .clip(laserShape)
                            .background(Color(0xFFE9D5FF)),
                    )
                }
            }

            // 2. EQUAL-WEIGHT TAB ROW WITH CRISP MATERIAL 3 VECTOR ICONS
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        rowWidthPx = coordinates.size.width.toFloat()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, (name, iconVector) ->
                    val isSelected = index == selectedTabIndex
                    val interactionSource = remember { MutableInteractionSource() }

                    val iconScaleState = animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                        label = "HiTechIconScale_$name",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) {
                                triggerTabSelection(index)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isSelected) {
                                    Color.White
                                } else {
                                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                },
                                modifier = Modifier
                                    .size(21.dp)
                                    .graphicsLayer {
                                        val s = iconScaleState.value
                                        scaleX = s
                                        scaleY = s
                                    },
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = name,
                                fontSize = if (isSelected) 11.sp else 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    Color(0xFFE9D5FF)
                                } else {
                                    Color(0xFF64748B)
                                },
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
    val actualActionColor = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFFA855F7)
    Box(
        modifier = modifier
            .heightIn(min = 140.dp)
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
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
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
            Text(
                text = if (isDone) "✓" else icon,
                fontSize = 16.sp,
                color = if (isDone) AppTheme.colors.successText else AppTheme.colors.textPrimary,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
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
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
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
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                softWrap = true,
            )
        }
    }
}


@Composable
fun StreakDetailsDialog(
    streakDays: Int,
    streakBadge: String,
    onDismissRequest: () -> Unit,
    onLogGlucoseClick: () -> Unit,
    onTrackFoodClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with fire icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔥", fontSize = 34.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (streakDays > 0) "$streakDays-Day Active Streak!" else "Start Your Streak!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (streakDays > 0)
                        "You've logged your health data $streakDays consecutive days in a row! Consistent daily logging keeps blood sugar in target."
                    else
                        "Log your blood sugar or meals today to kick off your healthy diabetes care streak!",
                    fontSize = 13.sp,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Streak Calendar Ribbon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Today")
                    days.forEachIndexed { index, day ->
                        val isAchieved = index < streakDays.coerceAtMost(5)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isAchieved) Color(0xFFA855F7) else AppTheme.colors.elevatedSurface)
                                    .border(1.dp, if (isAchieved) Color(0xFFA855F7) else AppTheme.colors.border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAchieved) "✓" else "○",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAchieved) Color.White else AppTheme.colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = day, fontSize = 10.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFA855F7))
                            .clickable {
                                onDismissRequest()
                                onLogGlucoseClick()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🩸 Log Glucose",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (AppTheme.colors.isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF5F3FF))
                            .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                            .clickable {
                                onDismissRequest()
                                onTrackFoodClick()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🥗 Log Meal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA855F7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Close",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier
                        .clickable { onDismissRequest() }
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun TargetEditDialog(
    initialFasting: Int,
    initialPostmeal: Int,
    onDismissRequest: () -> Unit,
    onSaveTarget: (fasting: Int, postmeal: Int) -> Unit
) {
    var fastingText by remember { mutableStateOf(initialFasting.toString()) }
    var postmealText by remember { mutableStateOf(initialPostmeal.toString()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(AppTheme.colors.cardBackground)
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 Edit Target Glucose",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF3F4F6))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Personalize your target fasting & post-meal blood sugar levels.",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fasting Target Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fasting Target (mg/dL)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.elevatedSurface)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = fastingText.toIntOrNull() ?: 100
                                    if (current > 70) fastingText = (current - 5).toString()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }

                        OutlinedTextField(
                            value = fastingText,
                            onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) fastingText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = AppTheme.colors.border,
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.elevatedSurface)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = fastingText.toIntOrNull() ?: 100
                                    if (current < 200) fastingText = (current + 5).toString()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Post-Meal Target Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Post-Meal Target (mg/dL)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.elevatedSurface)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = postmealText.toIntOrNull() ?: 140
                                    if (current > 100) postmealText = (current - 5).toString()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }

                        OutlinedTextField(
                            value = postmealText,
                            onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) postmealText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = AppTheme.colors.border,
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.elevatedSurface)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = postmealText.toIntOrNull() ?: 140
                                    if (current < 300) postmealText = (current + 5).toString()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recommended Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf("Standard (100)" to (100 to 140), "Strict (90)" to (90 to 130), "Gentle (120)" to (120 to 160))
                    presets.forEach { (label, targets) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (fastingText == targets.first.toString()) Color(0xFFA855F7).copy(alpha = 0.2f) else AppTheme.colors.elevatedSurface)
                                .border(1.dp, if (fastingText == targets.first.toString()) Color(0xFFA855F7) else AppTheme.colors.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    fastingText = targets.first.toString()
                                    postmealText = targets.second.toString()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (fastingText == targets.first.toString()) Color(0xFFA855F7) else AppTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Target Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFA855F7))
                        .clickable {
                            val f = fastingText.toIntOrNull() ?: 100
                            val p = postmealText.toIntOrNull() ?: 140
                            onSaveTarget(f, p)
                        }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save Target",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
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
