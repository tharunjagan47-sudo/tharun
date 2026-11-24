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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.simats.diawell123.ui.components.FluidGlassCircleButton
import com.simats.diawell123.ui.components.aiRotatingBorder
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
            .statusBarsPadding()
            .displayCutoutPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1100.dp)
                .align(Alignment.TopCenter)
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
                                            color = Color.White.copy(alpha = 0.85f),
                                            letterSpacing = 0.5.sp,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = displayName,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    // Top Right Actions: Theme Toggle & Profile Avatar (Bell button removed)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        // Quick Theme Toggle Button - Fluid Glass Circular Bubble
                                        FluidGlassCircleButton(
                                            imageVector = if (AppTheme.colors.isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                            onClick = { ThemeManager.toggleTheme(context) },
                                            contentDescription = "Toggle Theme",
                                            sizeDp = 40.dp,
                                            iconSizeDp = 18.dp,
                                            tint = Color.White,
                                        )

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
                                            Icon(
                                                imageVector = Icons.Rounded.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = Color(0xFFFF9800),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = data?.streakBadge?.replace(Regex("[^\\x00-\\x7F]"), "")?.trim()?.takeIf { it.isNotBlank() } ?: "Start streak",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
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
                                            Icon(
                                                imageVector = Icons.Rounded.TrackChanges,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = data?.targetBadge?.replace(Regex("[^\\x00-\\x7F]"), "")?.trim()?.takeIf { it.isNotBlank() } ?: "Target: 100 mg/dL",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier.size(12.dp)
                                            )
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC62828),
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Retry",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Title (16.sp, SemiBold, 12.dp gap below)
                Text(
                    text = "Health Metrics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
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
                                    vectorIcon = Icons.Rounded.WaterDrop,
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
                                    vectorIcon = Icons.Rounded.Restaurant,
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
                                    vectorIcon = Icons.Rounded.Medication,
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
                                    vectorIcon = Icons.Rounded.InsertChart,
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
                                .aiRotatingBorder(strokeWidth = 1.6.dp, shape = RoundedCornerShape(18.dp), durationMillis = 3500)
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
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFA855F7),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Daily Tip",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA855F7),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = data?.aiTip?.tip ?: "A 10-minute light walk after meals helps smooth blood sugar levels.",
                                        fontSize = 12.5.sp,
                                        color = AppTheme.colors.textSecondary,
                                        lineHeight = 17.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Ask ›",
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
                                text = "Today's Routine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AppTheme.colors.cardBackground)
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 18.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val defaultRoutine = remember {
                                        listOf(
                                            HomeRoutineItem(icon = "", label = "Breakfast", isDone = false),
                                            HomeRoutineItem(icon = "", label = "Glucose", isDone = false),
                                            HomeRoutineItem(icon = "", label = "Lunch", isDone = false),
                                            HomeRoutineItem(icon = "", label = "Dinner/Meds", isDone = false),
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
                                            icon = item.icon.ifBlank { "" },
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

                Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding())
            }
        }

        if (showStreakDialog) {
            StreakDetailsDialog(
                streakDays = data?.streakDays ?: 2,
                streakBadge = data?.streakBadge?.replace(Regex("[^\\x00-\\x7F]"), "")?.trim() ?: "2 Day Logging Streak",
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

    val selectedTabIndex = remember(selectedTab) {
        tabs.indexOfFirst { it.first == selectedTab }.coerceIn(0, tabs.lastIndex)
    }

    // Keep latest callbacks & state accessible inside pointerInput(Unit) without ever cancelling active drags!
    val currentSelectedIndex by rememberUpdatedState(selectedTabIndex)
    val currentOnSelectIndex by rememberUpdatedState<(Int) -> Unit> { index ->
        val tabName = tabs.getOrNull(index)?.first ?: return@rememberUpdatedState
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

    var rowWidthPx by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }
    var settledTabIndex by remember { mutableIntStateOf(selectedTabIndex) }
    var hoveredIndex by remember { mutableIntStateOf(selectedTabIndex) }
    var activeDragPos by remember { mutableStateOf<Float?>(null) }

    // Sync settled & hovered indices when tab changes externally (e.g. from screen buttons/back press)
    LaunchedEffect(selectedTabIndex) {
        settledTabIndex = selectedTabIndex
        if (!isPressed) {
            hoveredIndex = selectedTabIndex
        }
    }

    // Target continuous position [0f .. 4f]: follows finger seamlessly during slide, locks to settled tab on release
    val targetLensPos = activeDragPos ?: settledTabIndex.toFloat()

    // Silky critically-damped GPU spring: filters touch digitizer micro-jitter while staying glued to thumb (~8ms lag),
    // then glides smoothly with juicy elastic liquid settle on release — read strictly inside graphicsLayer!
    val smoothLensPosState = animateFloatAsState(
        targetValue = targetLensPos,
        animationSpec = if (isPressed && activeDragPos != null) {
            spring(dampingRatio = 0.90f, stiffness = 920f)
        } else {
            spring(dampingRatio = 0.78f, stiffness = 440f)
        },
        label = "SilkyLiquidLensPos",
    )

    // Smooth liquid inflation when finger presses & slides the glass lens
    val pressScaleState = animateFloatAsState(
        targetValue = if (isPressed) 1.10f else 1.0f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 480f),
        label = "LiquidLensPressScale",
    )

    val pillWidthDp = 76.dp
    val pillHeightDp = 70.dp
    val dockHeightDp = 56.dp
    val pillWidthPx = with(density) { pillWidthDp.toPx() }
    val tabSlotWidthPx = if (tabs.isNotEmpty() && rowWidthPx > 0f) rowWidthPx / tabs.size.toFloat() else 0f

    // Pre-allocated shapes & crystal liquid-glass brushes (zero per-frame allocations)
    val dockShape = remember { RoundedCornerShape(28.dp) }
    val lensShape = remember { RoundedCornerShape(24.dp) }

    val dockFillColor = if (isDark) Color(0xBA141124) else Color(0xDCFFFFFF)
    val dockBorderBrush = remember(isDark) {
        Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(Color(0x45FFFFFF), Color(0x38A855F7), Color(0x45FFFFFF))
            } else {
                listOf(Color(0x337C3AED), Color(0x18000000), Color(0x337C3AED))
            },
        )
    }

    // iOS 26 Refractive Liquid Glass Lens Fill:
    // Bright translucent crystal edges protruding top & bottom + deep smoked-purple glass center
    val lensFillBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color(0x58E9D5FF),
                    0.16f to Color(0xB8181428),
                    0.50f to Color(0xD8151226),
                    0.84f to Color(0xB82E1065),
                    1.00f to Color(0x66F0ABFC),
                ),
            )
        } else {
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color(0x99FFFFFF),
                    0.18f to Color(0xCCEDE9FE),
                    0.50f to Color(0xE0DDD6FE),
                    0.84f to Color(0xCCF5D0FE),
                    1.00f to Color(0x99FFFFFF),
                ),
            )
        }
    }

    // Crisp gleaming liquid-glass specular rim border
    val lensRimBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) {
                listOf(
                    Color(0xE6FFFFFF),
                    Color(0x55C4B5FD),
                    Color(0x337C3AED),
                    Color(0xCCF0ABFC),
                )
            } else {
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0x889333EA),
                    Color(0x667C3AED),
                    Color(0xFFFFFFFF),
                )
            },
        )
    }

    val topRefractionBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.36f),
                Color.Transparent,
            ),
        )
    }
    val bottomRefractionBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFF0ABFC).copy(alpha = 0.28f),
            ),
        )
    }

    val bgColor = if (AppTheme.colors.isDark) Color(0xFF000000) else AppTheme.colors.background
    val bottomEdgeFade = remember(bgColor) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.55f to bgColor.copy(alpha = 0.55f),
                1.00f to bgColor.copy(alpha = 0.90f),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bottomEdgeFade)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(78.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 1. SLEEK FLOATING PILL DOCK BASE (56.dp tall — glass lens protrudes gracefully above & below!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp)
                .shadow(
                    elevation = if (isDark) 18.dp else 12.dp,
                    shape = dockShape,
                    ambientColor = Color(0xFF05040D).copy(alpha = if (isDark) 0.80f else 0.16f),
                    spotColor = Color(0xFF7C3AED).copy(alpha = if (isDark) 0.30f else 0.20f),
                )
                .clip(dockShape)
                .background(dockFillColor)
                .border(width = 1.dp, brush = dockBorderBrush, shape = dockShape),
        )

        // 2. INTERACTIVE CONTENT & OVERSIZED LIQUID GLASS LENS CONTAINER
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
                .onGloballyPositioned { coordinates ->
                    rowWidthPx = coordinates.size.width.toFloat()
                }
                // Keyed on Unit so gesture coroutine NEVER cancels mid-slide when switching screens!
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val width = size.width.toFloat()
                        if (width <= 0f) return@awaitEachGesture
                        val slotW = width / tabs.size.toFloat()

                        isPressed = true
                        var targetTabIdx = (down.position.x / slotW).toInt().coerceIn(0, tabs.lastIndex)
                        hoveredIndex = targetTabIdx

                        // Immediately glide lens toward finger touch point
                        val initialContinuous = ((down.position.x / slotW) - 0.5f).coerceIn(
                            -0.08f,
                            (tabs.lastIndex + 0.08f),
                        )
                        activeDragPos = initialContinuous

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: break

                                if (change.changedToUpIgnoreConsumed() || !change.pressed) {
                                    break
                                }

                                val currentX = change.position.x
                                if (abs(currentX - down.position.x) > 6f) {
                                    change.consume()
                                }

                                // Pure continuous sub-pixel tracking with soft rubber-band edge resistance
                                val rawIdx = (currentX / slotW) - 0.5f
                                val continuousIdx = when {
                                    rawIdx < 0f -> rawIdx * 0.25f
                                    rawIdx > tabs.lastIndex -> tabs.lastIndex + (rawIdx - tabs.lastIndex) * 0.25f
                                    else -> rawIdx
                                }
                                activeDragPos = continuousIdx

                                val newTabIdx = (currentX / slotW).toInt().coerceIn(0, tabs.lastIndex)
                                if (newTabIdx != targetTabIdx) {
                                    targetTabIdx = newTabIdx
                                    hoveredIndex = newTabIdx
                                    // Subtle micro-haptic tick as crystal lens crosses each tab boundary
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                }
                            }
                        } finally {
                            // Guaranteed cleanup on release: spring-lock lens to final tab and commit screen change ONLY now!
                            val finalTab = targetTabIdx.coerceIn(0, tabs.lastIndex)
                            settledTabIndex = finalTab
                            hoveredIndex = finalTab
                            activeDragPos = null
                            isPressed = false
                            if (finalTab != currentSelectedIndex) {
                                currentOnSelectIndex(finalTab)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            // 2A. OVERSIZED iOS 26 FLOATING LIQUID GLASS MAGNIFYING LENS (100% GPU sub-pixel translationX!)
            if (rowWidthPx > 0f) {
                Box(
                    modifier = Modifier
                        .width(pillWidthDp)
                        .height(pillHeightDp)
                        .graphicsLayer {
                            val currentPos = smoothLensPosState.value
                            val xPx = (currentPos * tabSlotWidthPx) + ((tabSlotWidthPx - pillWidthPx) / 2f)
                            val delta = abs(targetLensPos - currentPos)
                            val stretch = (delta * 0.20f).coerceIn(0f, 0.18f)
                            val baseScale = pressScaleState.value
                            translationX = xPx
                            scaleX = baseScale + stretch
                            scaleY = baseScale - (stretch * 0.42f)
                        }
                        .shadow(
                            elevation = if (isDark) 16.dp else 10.dp,
                            shape = lensShape,
                            ambientColor = Color(0xFF0A0618).copy(alpha = 0.65f),
                            spotColor = Color(0xFFA855F7).copy(alpha = 0.65f),
                        )
                        .clip(lensShape)
                        .background(lensFillBrush)
                        .border(width = 1.4.dp, brush = lensRimBrush, shape = lensShape),
                ) {
                    // Top crystal meniscus highlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .align(Alignment.TopCenter)
                            .background(topRefractionBrush),
                    )
                    // Bottom purple-magenta refractive glow crescent
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .align(Alignment.BottomCenter)
                            .background(bottomRefractionBrush),
                    )
                }
            }

            // 2B. EQUAL-WEIGHT TAB ITEMS WITH REAL-TIME LIQUID LENS MAGNIFICATION & MICRO-ANIMATIONS
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, (name, iconVector) ->
                    val activeIndex = if (isPressed) hoveredIndex else settledTabIndex
                    val isActive = (index == activeIndex)

                    val iconTilt = remember { Animatable(0f) }
                    LaunchedEffect(isActive) {
                        if (isActive) {
                            iconTilt.snapTo(-8f)
                            iconTilt.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.45f, stiffness = 680f),
                            )
                        } else {
                            iconTilt.snapTo(0f)
                        }
                    }

                    val labelAlphaState = animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.68f,
                        animationSpec = tween(durationMillis = 140),
                        label = "LensLabelAlpha_$name",
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.graphicsLayer {
                                // Real-time silky optical magnification synced to sub-pixel GPU lens position!
                                val currentLens = smoothLensPosState.value
                                val proximity = (1f - abs(index.toFloat() - currentLens)).coerceIn(0f, 1f)
                                val magScale = 1f + (proximity * 0.18f)
                                scaleX = magScale
                                scaleY = magScale
                                translationY = (-2.dp.toPx()) * proximity
                            },
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isActive) {
                                    if (isDark) Color.White else Color(0xFF4C1D95)
                                } else {
                                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                },
                                modifier = Modifier
                                    .size(21.dp)
                                    .graphicsLayer {
                                        rotationZ = iconTilt.value
                                    },
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = name,
                                fontSize = if (isActive) 10.5.sp else 9.5.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) {
                                    if (isDark) Color(0xFFF3E8FF) else Color(0xFF4C1D95)
                                } else {
                                    Color(0xFF64748B)
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                modifier = Modifier.graphicsLayer {
                                    alpha = labelAlphaState.value
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slim left-side NavigationRail for landscape orientation and tablet screen widths.
 * Matches DiaWell's Obsidian & Royal Purple glass aesthetic without wasting vertical screen space.
 */
@Composable
fun DiaWellLiquidGlassNavRail(
    selectedTab: String,
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
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

    Box(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding()
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            .width(68.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(64.dp)
                .shadow(
                    elevation = if (isDark) 16.dp else 10.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0xFF05040D).copy(alpha = if (isDark) 0.80f else 0.16f),
                    spotColor = Color(0xFF7C3AED).copy(alpha = if (isDark) 0.30f else 0.20f),
                )
                .clip(RoundedCornerShape(28.dp))
                .background(if (isDark) Color(0xDE141124) else Color(0xDCFFFFFF))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(Color(0x45FFFFFF), Color(0x38A855F7), Color(0x45FFFFFF))
                        else listOf(Color(0x337C3AED), Color(0x18000000), Color(0x337C3AED))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                tabs.forEach { (name, iconVector) ->
                    val isActive = (name == selectedTab)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isActive) {
                                    if (isDark) Color(0x40A855F7) else Color(0x297C3AED)
                                } else Color.Transparent
                            )
                            .border(
                                width = if (isActive) 1.2.dp else 0.dp,
                                color = if (isActive) Color(0xFFA855F7) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                when (name) {
                                    "Home" -> onHomeTabClick()
                                    "Diet" -> onDietTabClick()
                                    "Meds" -> onMedsTabClick()
                                    "Reports" -> onReportsTabClick()
                                    "Settings" -> onSettingsTabClick()
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isActive) {
                                    if (isDark) Color.White else Color(0xFF4C1D95)
                                } else {
                                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                },
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = name,
                                fontSize = 9.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) {
                                    if (isDark) Color(0xFFF3E8FF) else Color(0xFF4C1D95)
                                } else Color(0xFF64748B),
                                maxLines = 1,
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
    vectorIcon: ImageVector? = null,
    icon: String = "",
    title: String,
    value: String,
    subtitle: String = "",
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
    val actualStatusBg = if (AppTheme.colors.isDark) statusColor.copy(alpha = 0.18f) else statusBg
    val actualActionColor = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFFA855F7)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(actualCardBg)
            .border(1.dp, actualBorder, RoundedCornerShape(20.dp))
            .clickable { onActionClick() }
            .padding(18.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (vectorIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) Color(0xFF1E1035) else Color(0xFFF3E8FF))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = title,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (icon.isNotBlank()) {
                    Text(text = icon, fontSize = 22.sp)
                }
                if (statusText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(actualStatusBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big bold metric value (24.sp-26.sp Bold)
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            // 1-2 word muted label (12.sp-13.sp, alpha 0.65f)
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textSecondary.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "$actionText ›",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
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
    val routineVector: ImageVector = when {
        isDone -> Icons.Rounded.Check
        label.contains("Glucose", ignoreCase = true) -> Icons.Rounded.WaterDrop
        label.contains("Med", ignoreCase = true) -> Icons.Rounded.Medication
        else -> Icons.Rounded.Restaurant
    }

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
            Icon(
                imageVector = routineVector,
                contentDescription = label,
                tint = if (isDone) AppTheme.colors.successText else Color(0xFFA855F7),
                modifier = Modifier.size(20.dp)
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
    vectorIcon: ImageVector? = null,
    icon: String = "",
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
                if (vectorIcon != null) {
                    Icon(imageVector = vectorIcon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = icon, fontSize = 18.sp)
                }
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
                // Header with fire icon inside circular glass
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (AppTheme.colors.isDark) Color(0xFF451A03) else Color(0xFFFFEDD5))
                        .border(1.dp, Color(0xFFF97316).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(34.dp)
                    )
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
                                if (isAchieved) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AppTheme.colors.textSecondary.copy(alpha = 0.4f))
                                    )
                                }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Log Glucose",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Log Meal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Edit Target Glucose",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF3F4F6))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = AppTheme.colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
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
