package com.simats.diawell123.ui.home

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.R
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.components.FluidGlassCircleButton
import com.simats.diawell123.ui.components.lightShimmerSheen
import com.simats.diawell123.ui.profile.ProfileAvatarView
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.ui.theme.Diawell123Theme
import com.simats.diawell123.ui.theme.ThemeManager
import android.os.Build
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
    var showStreakDialog by rememberSaveable { mutableStateOf(false) }
    var showTargetEditDialog by rememberSaveable { mutableStateOf(false) }
    var zoomedMetric by remember { mutableStateOf<HealthMetricZoomData?>(null) }
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
                                .lightShimmerSheen(durationMillis = 4000, peakAlpha = 0.08f)
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
                                    val displayName: String = remember(data?.profile?.name, ProfileState.userName) {
                                        data?.profile?.name?.takeIf { it.isNotBlank() }
                                            ?: ProfileState.userName.takeIf { it.isNotBlank() }
                                            ?: (try { SessionManager.getInstance().getFullName() } catch (_: Exception) { null })?.takeIf { it.isNotBlank() }
                                            ?: "Patient"
                                    }

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

                                    // Top Right Actions: Theme Toggle, Settings & Profile Avatar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                                        // Settings Button - Fluid Glass Circular Bubble between Theme & Profile
                                        FluidGlassCircleButton(
                                            imageVector = Icons.Rounded.Settings,
                                            onClick = onSettingsTabClick,
                                            contentDescription = "Settings",
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
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // Interactive Streak Badge (e.g. 2-Day Streak)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
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
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }

                                    // Interactive Target Editing Badge / Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
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
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
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
                            val gTitle = gMetric?.title?.ifBlank { "Glucose" } ?: "Glucose"
                            val gValue = gMetric?.value?.ifBlank { "-- mg/dL" } ?: "-- mg/dL"
                            val gSub = gMetric?.subtitle?.ifBlank { "Tap to log your first reading" } ?: "Tap to log your first reading"
                            val gStatus = gMetric?.statusText?.ifBlank { "No Readings Logged" } ?: "No Readings Logged"
                            val gStatusColor = parseHexColor(gMetric?.statusColor, Color(0xFF6B7280))
                            FloatingBubbleContainer(
                                phaseOffsetMs = 200,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    vectorIcon = Icons.Rounded.WaterDrop,
                                    title = gTitle,
                                    value = gValue,
                                    subtitle = gSub,
                                    statusText = gStatus,
                                    statusBg = parseHexColor(gMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = gStatusColor,
                                    cardBg = if (AppTheme.colors.isDark) Color(0xFF2E1065) else Color(0xFFF5F3FF),
                                    borderColor = AppTheme.colors.border,
                                    actionText = "History ›",
                                    onActionClick = {
                                        zoomedMetric = HealthMetricZoomData(
                                            type = HealthMetricZoomType.GLUCOSE,
                                            title = gTitle,
                                            value = gValue,
                                            subtitle = gSub,
                                            statusText = gStatus,
                                            statusColor = gStatusColor,
                                            accentColor = Color(0xFFA855F7),
                                            secondaryAccent = Color(0xFF38BDF8),
                                            icon = Icons.Rounded.WaterDrop,
                                            actionLabel = "+ Log Glucose",
                                            progressFraction = 0.72f,
                                            primaryDetailLabel = "Target Fasting",
                                            primaryDetailValue = "${data?.targetFastingGlucose ?: 100} mg/dL",
                                            secondaryDetailLabel = "Post-Meal Goal",
                                            secondaryDetailValue = "${data?.targetPostmealGlucose ?: 140} mg/dL",
                                            onOpenScreen = onLogGlucoseClick,
                                            secondaryActionLabel = "Open Glucose History  ›",
                                            onSecondaryAction = onGlucoseHistoryClick,
                                        )
                                    },
                                )
                            }

                            // Today's Diet Card
                            val nTitle = nMetric?.title?.ifBlank { "Nutrition" } ?: "Nutrition"
                            val nValue = nMetric?.value?.ifBlank { "0 kcal" } ?: "0 kcal"
                            val nSub = nMetric?.subtitle?.ifBlank { "0 meals logged today" } ?: "0 meals logged today"
                            val nStatus = nMetric?.statusText?.ifBlank { "No meals logged" } ?: "No meals logged"
                            val nStatusColor = parseHexColor(nMetric?.statusColor, Color(0xFF6B7280))
                            FloatingBubbleContainer(
                                phaseOffsetMs = 400,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    vectorIcon = Icons.Rounded.Restaurant,
                                    title = nTitle,
                                    value = nValue,
                                    subtitle = nSub,
                                    statusText = nStatus,
                                    statusBg = parseHexColor(nMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = nStatusColor,
                                    cardBg = Color(0xFFEBF7F0),
                                    borderColor = Color(0xFFD4EFE0),
                                    actionText = "Explore",
                                    onActionClick = {
                                        zoomedMetric = HealthMetricZoomData(
                                            type = HealthMetricZoomType.NUTRITION,
                                            title = nTitle,
                                            value = nValue,
                                            subtitle = nSub,
                                            statusText = nStatus,
                                            statusColor = nStatusColor,
                                            accentColor = Color(0xFF10B981),
                                            secondaryAccent = Color(0xFF34D399),
                                            icon = Icons.Rounded.Restaurant,
                                            actionLabel = "Explore Daily Diet Plan  ›",
                                            progressFraction = 0.65f,
                                            primaryDetailLabel = "Daily Summary",
                                            primaryDetailValue = nSub,
                                            secondaryDetailLabel = "Diet Status",
                                            secondaryDetailValue = nStatus,
                                            onOpenScreen = onTrackFoodClick,
                                        )
                                    },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Medication Card
                            val mTitle = mMetric?.title?.ifBlank { "Medication" } ?: "Medication"
                            val mValue = mMetric?.value?.ifBlank { "0 Active Medications" } ?: "0 Active Medications"
                            val mSub = mMetric?.subtitle?.ifBlank { "No medications scheduled" } ?: "No medications scheduled"
                            val mStatus = mMetric?.statusText?.ifBlank { "No Reminders" } ?: "No Reminders"
                            val mStatusColor = parseHexColor(mMetric?.statusColor, Color(0xFF6B7280))
                            FloatingBubbleContainer(
                                phaseOffsetMs = 600,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    vectorIcon = Icons.Rounded.Medication,
                                    title = mTitle,
                                    value = mValue,
                                    subtitle = mSub,
                                    statusText = mStatus,
                                    statusBg = parseHexColor(mMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = mStatusColor,
                                    cardBg = Color(0xFFFFF8EE),
                                    borderColor = Color(0xFFFFECC8),
                                    actionText = "View",
                                    onActionClick = {
                                        zoomedMetric = HealthMetricZoomData(
                                            type = HealthMetricZoomType.MEDICATION,
                                            title = mTitle,
                                            value = mValue,
                                            subtitle = mSub,
                                            statusText = mStatus,
                                            statusColor = mStatusColor,
                                            accentColor = Color(0xFFF59E0B),
                                            secondaryAccent = Color(0xFFFBBF24),
                                            icon = Icons.Rounded.Medication,
                                            actionLabel = "View Medications  ›",
                                            progressFraction = 0.80f,
                                            primaryDetailLabel = "Schedule",
                                            primaryDetailValue = mSub,
                                            secondaryDetailLabel = "Adherence",
                                            secondaryDetailValue = mStatus,
                                            onOpenScreen = onMedsTabClick,
                                        )
                                    },
                                )
                            }

                            // Weekly Progress Card
                            val wTitle = wMetric?.title?.ifBlank { "Weekly Report" } ?: "Weekly Report"
                            val wValue = wMetric?.value?.ifBlank { "0/7 Days" } ?: "0/7 Days"
                            val wSub = wMetric?.subtitle?.ifBlank { "No logs this week" } ?: "No logs this week"
                            val wStatus = wMetric?.statusText?.ifBlank { "Start logging today" } ?: "Start logging today"
                            val wStatusColor = parseHexColor(wMetric?.statusColor, Color(0xFF6B7280))
                            FloatingBubbleContainer(
                                phaseOffsetMs = 800,
                                modifier = Modifier.weight(1f),
                            ) {
                                MetricCard(
                                    vectorIcon = Icons.Rounded.InsertChart,
                                    title = wTitle,
                                    value = wValue,
                                    subtitle = wSub,
                                    statusText = wStatus,
                                    statusBg = parseHexColor(wMetric?.statusBg, Color(0xFFF3F4F6)),
                                    statusColor = wStatusColor,
                                    cardBg = Color(0xFFF0F7FF),
                                    borderColor = Color(0xFFD0E4FC),
                                    actionText = "Reports",
                                    onActionClick = {
                                        zoomedMetric = HealthMetricZoomData(
                                            type = HealthMetricZoomType.WEEKLY_REPORT,
                                            title = wTitle,
                                            value = wValue,
                                            subtitle = wSub,
                                            statusText = wStatus,
                                            statusColor = wStatusColor,
                                            accentColor = Color(0xFF38BDF8),
                                            secondaryAccent = Color(0xFF8B5CF6),
                                            icon = Icons.Rounded.InsertChart,
                                            actionLabel = "Open Weekly Report  ›",
                                            progressFraction = 0.75f,
                                            primaryDetailLabel = "Weekly Activity",
                                            primaryDetailValue = wSub,
                                            secondaryDetailLabel = "Consistency",
                                            secondaryDetailValue = wStatus,
                                            onOpenScreen = onReportsTabClick,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Today's Routine Timeline Check-In (Floating Bubble Effect)
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
                                            HomeRoutineItem(icon = "", label = "Dinner", isDone = false),
                                            HomeRoutineItem(icon = "", label = "Meds", isDone = false),
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

        HealthMetricZoomOverlay(
            metric = zoomedMetric,
            onDismiss = { zoomedMetric = null },
        )
    }
}

/**
 * Shared live-blurred frosted liquid-glass surface clipped cleanly to [shape].
 * Samples the underlying Compose [backdropLayer] on Android 12+ (`RenderEffect` GPU blur)
 * with graceful high-frost translucency fallback on older API levels.
 */
@Composable
private fun FrostedGlassCapsuleSurface(
    shape: Shape,
    backdropLayer: GraphicsLayer?,
    tintBrush: Brush,
    borderBrush: Brush,
    topSheenBrush: Brush,
    borderWidth: Dp,
    shadowElevation: Dp,
    isDark: Boolean,
    blurRadius: Dp = 26.dp,
    modifier: Modifier = Modifier,
) {
    val supportsLiveBlur = backdropLayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var blurOriginInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color(0xFF05040D).copy(alpha = if (isDark) 0.72f else 0.16f),
                spotColor = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.14f),
            )
            .clip(shape),
    ) {
        if (supportsLiveBlur && backdropLayer != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .onGloballyPositioned { coords ->
                        blurOriginInRoot = coords.positionInRoot()
                    }
                    .blur(
                        radius = blurRadius,
                        edgeTreatment = BlurredEdgeTreatment.Rectangle,
                    )
                    .drawBehind {
                        clipRect(0f, 0f, size.width, size.height) {
                            translate(
                                left = -blurOriginInRoot.x,
                                top = -blurOriginInRoot.y,
                            ) {
                                drawLayer(backdropLayer)
                            }
                        }
                    },
            )
        }

        // Frosted milk / obsidian glass tint layer on top of live blurred backdrop
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(tintBrush),
        )

        // Subtle curved top meniscus highlight reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .align(Alignment.TopCenter)
                .background(topSheenBrush),
        )

        // Gleaming outer glass rim border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(width = borderWidth, brush = borderBrush, shape = shape),
        )
    }
}

private data class NavTabItem(
    val name: String,
    val iconVector: ImageVector? = null,
    val iconResId: Int? = null,
)

private val diaWellNavTabs = listOf(
    NavTabItem(name = "Home", iconVector = Icons.Rounded.Home),
    NavTabItem(name = "Diet", iconVector = Icons.Rounded.Restaurant),
    NavTabItem(name = "AI", iconResId = R.drawable.ic_nav_ai),
    NavTabItem(name = "Meds", iconVector = Icons.Rounded.Medication),
    NavTabItem(name = "Reports", iconVector = Icons.Rounded.Assessment),
)

@Composable
fun IosLiquidGlassBottomNav(
    selectedTab: String,
    backdropLayer: GraphicsLayer? = null,
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onAiTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val isDark = AppTheme.colors.isDark
    val coroutineScope = rememberCoroutineScope()
    val hasLiveBlur = backdropLayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val tabs = diaWellNavTabs

    val selectedTabIndex = remember(selectedTab) {
        tabs.indexOfFirst { it.name == selectedTab }.coerceIn(0, tabs.lastIndex)
    }

    // Keep latest callbacks & state accessible inside pointerInput(Unit) without ever cancelling active drags
    val currentSelectedIndex by rememberUpdatedState(selectedTabIndex)
    val currentOnSelectIndex by rememberUpdatedState<(Int) -> Unit> { index ->
        val tabName = tabs.getOrNull(index)?.name ?: return@rememberUpdatedState
        if (tabName != selectedTab) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
            when (tabName) {
                "Home" -> onHomeTabClick()
                "Diet" -> onDietTabClick()
                "AI" -> onAiTabClick()
                "Meds" -> onMedsTabClick()
                "Reports" -> onReportsTabClick()
            }
        }
    }

    var rowWidthPx by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }
    var isActivelyDragging by remember { mutableStateOf(false) }
    var activeDragVelocitySlots by remember { mutableFloatStateOf(0f) }
    var settledTabIndex by remember { mutableIntStateOf(selectedTabIndex) }
    var hoveredIndex by remember { mutableIntStateOf(selectedTabIndex) }

    // Continuous Animatable preserves release velocity and tracks finger drags without restarting Compose state animations
    val lensPos = remember { Animatable(selectedTabIndex.toFloat()) }

    // Sync lens when tab changes externally (e.g., dashboard card clicks or system back)
    LaunchedEffect(selectedTabIndex) {
        settledTabIndex = selectedTabIndex
        if (!isPressed) {
            hoveredIndex = selectedTabIndex
            lensPos.animateTo(
                targetValue = selectedTabIndex.toFloat(),
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
            )
        }
    }

    // Gentle liquid buoyancy scale when finger presses & slides the nested stadium bubble
    val pressScaleState = animateFloatAsState(
        targetValue = if (isPressed) 1.07f else 1.0f,
        animationSpec = spring(dampingRatio = 0.64f, stiffness = 460f),
        label = "LiquidLensPressScale",
    )

    val dockHeightDp = 64.dp
    val innerBubbleHeightDp = 56.dp
    val touchSlopPx = with(density) { 8.dp.toPx() }
    val tabSlotWidthPx = if (tabs.isNotEmpty() && rowWidthPx > 0f) rowWidthPx / tabs.size.toFloat() else 0f
    val minBubbleWidthPx = with(density) { 68.dp.toPx() }
    val maxBubbleWidthPx = with(density) { 92.dp.toPx() }
    val bubbleExtraPx = with(density) { 8.dp.toPx() }
    val bubbleWidthPx = if (tabSlotWidthPx > 0f) {
        (tabSlotWidthPx + bubbleExtraPx).coerceIn(minBubbleWidthPx, maxBubbleWidthPx)
    } else {
        with(density) { 74.dp.toPx() }
    }
    val bubbleWidthDp = with(density) { bubbleWidthPx.toDp() }
    val maxBubbleTravelPx = (rowWidthPx - bubbleWidthPx).coerceAtLeast(0f)

    // Full capsule shapes: nested stadium oval bubble hugs the outer stadium capsule concentrically
    val dockShape = CircleShape
    val bubbleShape = CircleShape

    // Outer frosted acrylic dock tint over live GPU blur
    val dockTintBrush = remember(isDark, hasLiveBlur) {
        if (isDark) {
            val topAlpha = if (hasLiveBlur) 0.50f else 0.88f
            val midAlpha = if (hasLiveBlur) 0.62f else 0.94f
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFF231D3A).copy(alpha = topAlpha),
                    0.50f to Color(0xFF141124).copy(alpha = midAlpha),
                    1.00f to Color(0xFF1B1530).copy(alpha = topAlpha + 0.04f),
                ),
            )
        } else {
            val topAlpha = if (hasLiveBlur) 0.54f else 0.92f
            val midAlpha = if (hasLiveBlur) 0.44f else 0.88f
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color.White.copy(alpha = topAlpha),
                    0.50f to Color(0xFFF6F4FA).copy(alpha = midAlpha),
                    1.00f to Color.White.copy(alpha = topAlpha),
                ),
            )
        }
    }

    val dockBorderBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) {
                listOf(
                    Color.White.copy(alpha = 0.45f),
                    Color(0xFFA855F7).copy(alpha = 0.28f),
                    Color(0xFFE9D5FF).copy(alpha = 0.34f),
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.94f),
                    Color(0xFFCBD5E1).copy(alpha = 0.42f),
                    Color.White.copy(alpha = 0.86f),
                )
            },
        )
    }

    val dockTopSheenBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.14f else 0.44f),
                Color.Transparent,
            ),
        )
    }

    // Inner nested frosted bubble fill (bright pearlescent pill in Light, luminous smoked crystal in Dark)
    val bubbleFillBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFFE9D5FF).copy(alpha = 0.30f),
                    0.45f to Color(0xFF33235C).copy(alpha = 0.72f),
                    1.00f to Color(0xFF4C1D95).copy(alpha = 0.56f),
                ),
            )
        } else {
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color.White.copy(alpha = 0.86f),
                    0.52f to Color(0xFFE8E6F2).copy(alpha = 0.72f),
                    1.00f to Color(0xFFF3F1FA).copy(alpha = 0.82f),
                ),
            )
        }
    }

    val bubbleRimBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) {
                listOf(
                    Color.White.copy(alpha = 0.72f),
                    Color(0xFFC4B5FD).copy(alpha = 0.28f),
                    Color(0xFFF0ABFC).copy(alpha = 0.54f),
                )
            } else {
                listOf(
                    Color.White,
                    Color.Black.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.88f),
                )
            },
        )
    }

    val bubbleTopSheenBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.28f else 0.55f),
                Color.Transparent,
            ),
        )
    }

    // Crisp iOS 26 contrast palette: vivid violet active tab + crisp near-black/silver inactive tabs
    val activeIconColor = if (isDark) Color.White else Color(0xFF6D28D9)
    val activeLabelColor = if (isDark) Color(0xFFF3E8FF) else Color(0xFF6D28D9)
    val inactiveIconColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF111827)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(dockHeightDp),
        contentAlignment = Alignment.Center,
    ) {
        // 1. LIVE-BLURRED FLOATING CAPSULE DOCK BASE (64.dp full pill)
        FrostedGlassCapsuleSurface(
            shape = dockShape,
            backdropLayer = backdropLayer,
            tintBrush = dockTintBrush,
            borderBrush = dockBorderBrush,
            topSheenBrush = dockTopSheenBrush,
            borderWidth = 1.2.dp,
            shadowElevation = if (isDark) 18.dp else 14.dp,
            isDark = isDark,
            blurRadius = 26.dp,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. NESTED STADIUM BUBBLE & INTERACTIVE TAB CONTENT CONTAINER (4.dp concentric inset)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .onGloballyPositioned { coordinates ->
                    rowWidthPx = coordinates.size.width.toFloat()
                }
                // Keyed on Unit so gesture coroutine NEVER cancels mid-slide when switching screens
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val width = size.width.toFloat()
                        if (width <= 0f) return@awaitEachGesture
                        val slotW = width / tabs.size.toFloat()

                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)

                        val downX = down.position.x
                        var lastX = downX
                        var hasDragged = false
                        var targetTabIdx = (downX / slotW).toInt().coerceIn(0, tabs.lastIndex)

                        isPressed = true
                        isActivelyDragging = false
                        hoveredIndex = targetTabIdx

                        // On initial press down, glide nested bubble smoothly to tapped tab
                        val downJob: Job = coroutineScope.launch {
                            lensPos.animateTo(
                                targetValue = targetTabIdx.toFloat(),
                                animationSpec = spring(dampingRatio = 0.78f, stiffness = 780f),
                            )
                        }

                        var activeMoveJob: Job? = null
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
                                lastX = currentX
                                velocityTracker.addPosition(change.uptimeMillis, change.position)

                                if (!hasDragged && abs(currentX - downX) > touchSlopPx) {
                                    hasDragged = true
                                    isActivelyDragging = true
                                    downJob.cancel()
                                }

                                if (hasDragged) {
                                    change.consume()

                                    // Continuous sub-pixel position with smooth asymptotic iOS rubber-banding at edges
                                    val rawIdx = (currentX / slotW) - 0.5f
                                    val continuousIdx = when {
                                        rawIdx < 0f -> -0.22f * (1f - 1f / (1f + abs(rawIdx) * 2.4f))
                                        rawIdx > tabs.lastIndex -> tabs.lastIndex + 0.22f * (1f - 1f / (1f + (rawIdx - tabs.lastIndex) * 2.4f))
                                        else -> rawIdx
                                    }

                                    val instantVel = (velocityTracker.calculateVelocity().x / slotW).coerceIn(-14f, 14f)
                                    activeDragVelocitySlots = (activeDragVelocitySlots * 0.55f) + (instantVel * 0.45f)
                                    val smoothedPos = (lensPos.value * 0.18f) + (continuousIdx * 0.82f)
                                    activeMoveJob?.cancel()
                                    activeMoveJob = coroutineScope.launch {
                                        lensPos.snapTo(smoothedPos)
                                    }

                                    val newTabIdx = (currentX / slotW).toInt().coerceIn(0, tabs.lastIndex)
                                    if (newTabIdx != targetTabIdx) {
                                        targetTabIdx = newTabIdx
                                        hoveredIndex = newTabIdx
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        } finally {
                            downJob.cancel()
                            activeMoveJob?.cancel()

                            val velPxPerSec = velocityTracker.calculateVelocity().x
                            val velSlotsPerSec = (velPxPerSec / slotW).coerceIn(-14f, 14f)

                            val finalTab = if (hasDragged) {
                                val projectedX = (lastX + velPxPerSec * 0.075f).coerceIn(0f, width)
                                (projectedX / slotW).toInt().coerceIn(0, tabs.lastIndex)
                            } else {
                                (downX / slotW).toInt().coerceIn(0, tabs.lastIndex)
                            }

                            settledTabIndex = finalTab
                            hoveredIndex = finalTab
                            activeDragVelocitySlots = 0f
                            isActivelyDragging = false
                            isPressed = false

                            coroutineScope.launch {
                                lensPos.animateTo(
                                    targetValue = finalTab.toFloat(),
                                    initialVelocity = if (hasDragged) velSlotsPerSec else 0f,
                                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
                                )
                            }

                            if (finalTab != currentSelectedIndex) {
                                currentOnSelectIndex(finalTab)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            // 2A. NESTED STADIUM CAPSULE SELECTION BUBBLE (Concentric CircleShape pill!)
            if (rowWidthPx > 0f && tabs.size > 1) {
                Box(
                    modifier = Modifier
                        .width(bubbleWidthDp)
                        .height(innerBubbleHeightDp)
                        .graphicsLayer {
                            val currentPos = lensPos.value
                            val xPx = (currentPos / tabs.lastIndex.toFloat()) * maxBubbleTravelPx
                            val targetPos = (if (isPressed) hoveredIndex else settledTabIndex).toFloat()
                            val motionSpeed = if (isActivelyDragging) {
                                (abs(activeDragVelocitySlots) * 0.020f)
                            } else {
                                abs(targetPos - currentPos) * 0.16f
                            }
                            val stretch = motionSpeed.coerceIn(0f, 0.18f)
                            val directionalTilt = if (isActivelyDragging) {
                                (activeDragVelocitySlots * 0.18f).coerceIn(-2.0f, 2.0f)
                            } else {
                                ((targetPos - currentPos) * 1.1f).coerceIn(-1.6f, 1.6f)
                            }
                            val baseScale = pressScaleState.value
                            translationX = xPx
                            scaleX = baseScale + stretch
                            scaleY = baseScale - (stretch * 0.36f)
                            rotationZ = directionalTilt
                        }
                        .shadow(
                            elevation = if (isDark) 10.dp else 5.dp,
                            shape = bubbleShape,
                            ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.14f),
                            spotColor = if (isDark) Color(0xFFA855F7).copy(alpha = 0.48f) else Color.Black.copy(alpha = 0.14f),
                        )
                        .clip(bubbleShape)
                        .background(bubbleFillBrush)
                        .border(width = 1.1.dp, brush = bubbleRimBrush, shape = bubbleShape),
                ) {
                    // Subtle glossy curved upper highlight inside the stadium pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .align(Alignment.TopCenter)
                            .background(bubbleTopSheenBrush),
                    )
                }
            }

            // 2B. EQUAL-WEIGHT TAB ITEMS WITH PROXIMITY LENS MAGNIFICATION & VECTOR MICRO-ANIMATIONS
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val name = tab.name
                    val activeIndex = if (isPressed) hoveredIndex else settledTabIndex
                    val isActive = (index == activeIndex)

                    val animatedIconTint by animateColorAsState(
                        targetValue = if (isActive) activeIconColor else inactiveIconColor,
                        animationSpec = tween(durationMillis = 160),
                        label = "BottomNavIconTint_$name",
                    )
                    val animatedLabelTint by animateColorAsState(
                        targetValue = if (isActive) activeLabelColor else inactiveIconColor,
                        animationSpec = tween(durationMillis = 160),
                        label = "BottomNavLabelTint_$name",
                    )
                    val labelAlphaState = animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.85f,
                        animationSpec = tween(durationMillis = 160),
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
                                // Smooth GPU optical lift synced directly to the sub-pixel lens Animatable
                                val currentLens = lensPos.value
                                val proximity = (1f - abs(index.toFloat() - currentLens)).coerceIn(0f, 1f)
                                val magScale = 1f + (proximity * 0.10f)
                                scaleX = magScale
                                scaleY = magScale
                                translationY = (-1.5f.dp.toPx()) * proximity
                            },
                        ) {
                            AnimatedNavVectorIcon(
                                tabName = name,
                                iconVector = tab.iconVector,
                                iconResId = tab.iconResId,
                                isActive = isActive,
                                tint = animatedIconTint,
                                isDark = isDark,
                                sizeDp = if (name == "AI") 23 else 22,
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = name,
                                fontSize = if (isActive) 10.5.sp else 10.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = animatedLabelTint,
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
 * Per-tab Material 3 vector icon micro-choreography shared by both portrait dock & landscape rail:
 * - Home: upward bounce & scale pop
 * - Diet (Restaurant): playful double-nudge fork/knife tilt
 * - Meds (Medication): rhythmic pill shake
 * - Reports (Assessment): bottom-anchored bar-chart rise pop
 * - AI (Custom Robot Vector): friendly wave rock and spring pop
 */
@Composable
private fun AnimatedNavVectorIcon(
    tabName: String,
    iconVector: ImageVector? = null,
    iconResId: Int? = null,
    isActive: Boolean,
    tint: Color,
    isDark: Boolean,
    sizeDp: Int = 21,
) {
    val iconScaleX = remember { Animatable(1f) }
    val iconScaleY = remember { Animatable(1f) }
    val iconRotation = remember { Animatable(0f) }
    val iconOffsetY = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(if (isActive) 1f else 0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            launch {
                glowAlpha.animateTo(1f, tween(220))
            }
            when (tabName) {
                "AI" -> {
                    // Friendly robot wave rock & spring pop
                    launch {
                        iconRotation.snapTo(-10f)
                        iconRotation.animateTo(8f, spring(dampingRatio = 0.52f, stiffness = 720f))
                        iconRotation.animateTo(-4f, spring(dampingRatio = 0.56f, stiffness = 720f))
                        iconRotation.animateTo(0f, spring(dampingRatio = 0.60f, stiffness = 560f))
                    }
                    launch {
                        iconScaleX.snapTo(1.20f)
                        iconScaleY.snapTo(1.20f)
                        iconScaleX.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 520f))
                    }
                    launch {
                        iconScaleY.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 520f))
                    }
                }

                "Reports" -> {
                    // Chart bars dip briefly then rise dynamically from the baseline
                    launch {
                        iconScaleX.snapTo(1.06f)
                        iconScaleY.snapTo(0.76f)
                        iconScaleX.animateTo(1f, spring(dampingRatio = 0.50f, stiffness = 520f))
                    }
                    launch {
                        iconScaleY.animateTo(1.16f, spring(dampingRatio = 0.58f, stiffness = 700f))
                        iconScaleY.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 480f))
                    }
                }

                "Home" -> {
                    // Warm upward hop and settle
                    launch {
                        iconOffsetY.snapTo(-4.5f)
                        iconOffsetY.animateTo(0f, spring(dampingRatio = 0.46f, stiffness = 520f))
                    }
                    launch {
                        iconScaleX.snapTo(1.20f)
                        iconScaleY.snapTo(1.20f)
                        iconScaleX.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 540f))
                    }
                    launch {
                        iconScaleY.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 540f))
                    }
                }

                "Meds" -> {
                    // Playful pill shake
                    launch {
                        iconRotation.snapTo(-15f)
                        iconRotation.animateTo(10f, spring(dampingRatio = 0.55f, stiffness = 920f))
                        iconRotation.animateTo(-5f, spring(dampingRatio = 0.55f, stiffness = 920f))
                        iconRotation.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = 680f))
                    }
                    launch {
                        iconScaleX.snapTo(1.18f)
                        iconScaleY.snapTo(1.18f)
                        iconScaleX.animateTo(1f, spring(dampingRatio = 0.50f, stiffness = 540f))
                    }
                    launch {
                        iconScaleY.animateTo(1f, spring(dampingRatio = 0.50f, stiffness = 540f))
                    }
                }

                else -> {
                    // Diet / Restaurant fork-and-knife nod
                    launch {
                        iconRotation.snapTo(-12f)
                        iconRotation.animateTo(7f, spring(dampingRatio = 0.52f, stiffness = 820f))
                        iconRotation.animateTo(0f, spring(dampingRatio = 0.52f, stiffness = 640f))
                    }
                    launch {
                        iconScaleX.snapTo(1.16f)
                        iconScaleY.snapTo(1.16f)
                        iconScaleX.animateTo(1f, spring(dampingRatio = 0.50f, stiffness = 540f))
                    }
                    launch {
                        iconScaleY.animateTo(1f, spring(dampingRatio = 0.50f, stiffness = 540f))
                    }
                }
            }
        } else {
            launch { glowAlpha.animateTo(0f, tween(160)) }
            launch { iconRotation.animateTo(0f, tween(140)) }
            launch { iconOffsetY.animateTo(0f, tween(140)) }
            launch { iconScaleX.animateTo(1f, tween(140)) }
            launch { iconScaleY.animateTo(1f, tween(140)) }
        }
    }

    val haloColor = remember(isDark) {
        if (isDark) Color(0x55C084FC) else Color(0x309333EA)
    }

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size((sizeDp + 10).dp)
                .graphicsLayer {
                    val a = glowAlpha.value
                    alpha = a
                    val s = 0.72f + (a * 0.28f)
                    scaleX = s
                    scaleY = s
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(haloColor, Color.Transparent),
                    ),
                ),
        )

        val iconModifier = Modifier
            .size(sizeDp.dp)
            .graphicsLayer {
                scaleX = iconScaleX.value
                scaleY = iconScaleY.value
                rotationZ = iconRotation.value
                translationY = iconOffsetY.value.dp.toPx()
                transformOrigin = if (tabName == "Reports") {
                    TransformOrigin(0.5f, 0.85f)
                } else {
                    TransformOrigin.Center
                }
            }

        if (iconResId != null) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = tabName,
                tint = tint,
                modifier = iconModifier,
            )
        } else if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = tabName,
                tint = tint,
                modifier = iconModifier,
            )
        }
    }
}

/**
 * Slim left-side NavigationRail for landscape orientation and tablet screen widths.
 * Supports smooth vertical drag-and-slide selection, sliding glass pill indicator,
 * and coordinated Material 3 vector icon animations matching the portrait dock.
 */
@Composable
fun DiaWellLiquidGlassNavRail(
    selectedTab: String,
    backdropLayer: GraphicsLayer? = null,
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onAiTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val isDark = AppTheme.colors.isDark
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hasLiveBlur = backdropLayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val tabs = diaWellNavTabs

    val selectedTabIndex = remember(selectedTab) {
        tabs.indexOfFirst { it.name == selectedTab }.coerceIn(0, tabs.lastIndex)
    }
    val currentSelectedIndex by rememberUpdatedState(selectedTabIndex)
    val currentOnSelectIndex by rememberUpdatedState<(Int) -> Unit> { index ->
        val tabName = tabs.getOrNull(index)?.name ?: return@rememberUpdatedState
        if (tabName != selectedTab) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
            when (tabName) {
                "Home" -> onHomeTabClick()
                "Diet" -> onDietTabClick()
                "AI" -> onAiTabClick()
                "Meds" -> onMedsTabClick()
                "Reports" -> onReportsTabClick()
            }
        }
    }

    var columnHeightPx by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }
    var isActivelyDragging by remember { mutableStateOf(false) }
    var activeRailDragVel by remember { mutableFloatStateOf(0f) }
    var settledTabIndex by remember { mutableIntStateOf(selectedTabIndex) }
    var hoveredIndex by remember { mutableIntStateOf(selectedTabIndex) }
    val railPos = remember { Animatable(selectedTabIndex.toFloat()) }

    LaunchedEffect(selectedTabIndex) {
        settledTabIndex = selectedTabIndex
        if (!isPressed) {
            hoveredIndex = selectedTabIndex
            railPos.animateTo(
                targetValue = selectedTabIndex.toFloat(),
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
            )
        }
    }

    val pressScaleState = animateFloatAsState(
        targetValue = if (isPressed) 1.07f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 460f),
        label = "RailLensPressScale",
    )

    val pillSizeDp = 56.dp
    val pillSizePx = with(density) { pillSizeDp.toPx() }
    val touchSlopPx = with(density) { 8.dp.toPx() }
    val maxRailTravelPx = (columnHeightPx - pillSizePx).coerceAtLeast(0f)

    val activeIconColor = if (isDark) Color.White else Color(0xFF6D28D9)
    val activeLabelColor = if (isDark) Color(0xFFF3E8FF) else Color(0xFF6D28D9)
    val inactiveIconColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF111827)

    val railTintBrush = remember(isDark, hasLiveBlur) {
        if (isDark) {
            val topAlpha = if (hasLiveBlur) 0.50f else 0.88f
            val midAlpha = if (hasLiveBlur) 0.62f else 0.94f
            Brush.verticalGradient(
                listOf(
                    Color(0xFF231D3A).copy(alpha = topAlpha),
                    Color(0xFF141124).copy(alpha = midAlpha),
                    Color(0xFF1B1530).copy(alpha = topAlpha + 0.04f),
                )
            )
        } else {
            val topAlpha = if (hasLiveBlur) 0.54f else 0.92f
            val midAlpha = if (hasLiveBlur) 0.44f else 0.88f
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = topAlpha),
                    Color(0xFFF6F4FA).copy(alpha = midAlpha),
                    Color.White.copy(alpha = topAlpha),
                )
            )
        }
    }
    val railBorderBrush = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) listOf(Color.White.copy(alpha = 0.45f), Color(0xFFA855F7).copy(alpha = 0.28f), Color(0xFFE9D5FF).copy(alpha = 0.34f))
            else listOf(Color.White.copy(alpha = 0.94f), Color(0xFFCBD5E1).copy(alpha = 0.42f), Color.White.copy(alpha = 0.86f))
        )
    }
    val railTopSheenBrush = remember(isDark) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = if (isDark) 0.14f else 0.44f), Color.Transparent))
    }
    val railBubbleFill = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFE9D5FF).copy(alpha = 0.30f),
                    Color(0xFF33235C).copy(alpha = 0.72f),
                    Color(0xFF4C1D95).copy(alpha = 0.56f),
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.86f),
                    Color(0xFFE8E6F2).copy(alpha = 0.72f),
                    Color(0xFFF3F1FA).copy(alpha = 0.82f),
                )
            )
        }
    }
    val railBubbleRim = remember(isDark) {
        Brush.verticalGradient(
            if (isDark) listOf(Color.White.copy(alpha = 0.72f), Color(0xFFC4B5FD).copy(alpha = 0.28f), Color(0xFFF0ABFC).copy(alpha = 0.54f))
            else listOf(Color.White, Color.Black.copy(alpha = 0.08f), Color.White.copy(alpha = 0.88f))
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
                .width(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            FrostedGlassCapsuleSurface(
                shape = CircleShape,
                backdropLayer = backdropLayer,
                tintBrush = railTintBrush,
                borderBrush = railBorderBrush,
                topSheenBrush = railTopSheenBrush,
                borderWidth = 1.2.dp,
                shadowElevation = if (isDark) 16.dp else 12.dp,
                isDark = isDark,
                blurRadius = 26.dp,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .onGloballyPositioned { coords ->
                        columnHeightPx = coords.size.height.toFloat()
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val height = size.height.toFloat()
                            if (height <= 0f) return@awaitEachGesture
                            val slotH = height / tabs.size.toFloat()

                            val velocityTracker = VelocityTracker()
                            velocityTracker.addPosition(down.uptimeMillis, down.position)

                            val downY = down.position.y
                            var lastY = downY
                            var hasDragged = false
                            var targetIdx = (downY / slotH).toInt().coerceIn(0, tabs.lastIndex)

                            isPressed = true
                            hoveredIndex = targetIdx

                            val downJob: Job = coroutineScope.launch {
                                railPos.animateTo(
                                    targetValue = targetIdx.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 780f),
                                )
                            }

                            var moveJob: Job? = null
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                        ?: break
                                    if (change.changedToUpIgnoreConsumed() || !change.pressed) break

                                    val currentY = change.position.y
                                    lastY = currentY
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)

                                    if (!hasDragged && abs(currentY - downY) > touchSlopPx) {
                                        hasDragged = true
                                        isActivelyDragging = true
                                        downJob.cancel()
                                    }

                                    if (hasDragged) {
                                        change.consume()
                                        val rawIdx = (currentY / slotH) - 0.5f
                                        val continuousIdx = when {
                                            rawIdx < 0f -> -0.22f * (1f - 1f / (1f + abs(rawIdx) * 2.4f))
                                            rawIdx > tabs.lastIndex -> tabs.lastIndex + 0.22f * (1f - 1f / (1f + (rawIdx - tabs.lastIndex) * 2.4f))
                                            else -> rawIdx
                                        }
                                        val instantVel = (velocityTracker.calculateVelocity().y / slotH).coerceIn(-12f, 12f)
                                        activeRailDragVel = (activeRailDragVel * 0.55f) + (instantVel * 0.45f)
                                        val smoothedY = (railPos.value * 0.18f) + (continuousIdx * 0.82f)
                                        moveJob?.cancel()
                                        moveJob = coroutineScope.launch {
                                            railPos.snapTo(smoothedY)
                                        }
                                        val newIdx = (currentY / slotH).toInt().coerceIn(0, tabs.lastIndex)
                                        if (newIdx != targetIdx) {
                                            targetIdx = newIdx
                                            hoveredIndex = newIdx
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                            } finally {
                                downJob.cancel()
                                moveJob?.cancel()
                                val velY = velocityTracker.calculateVelocity().y
                                val velSlots = (velY / slotH).coerceIn(-12f, 12f)
                                val finalTab = if (hasDragged) {
                                    val projectedY = (lastY + velY * 0.075f).coerceIn(0f, height)
                                    (projectedY / slotH).toInt().coerceIn(0, tabs.lastIndex)
                                } else {
                                    (downY / slotH).toInt().coerceIn(0, tabs.lastIndex)
                                }
                                settledTabIndex = finalTab
                                hoveredIndex = finalTab
                                activeRailDragVel = 0f
                                isActivelyDragging = false
                                isPressed = false
                                coroutineScope.launch {
                                    railPos.animateTo(
                                        targetValue = finalTab.toFloat(),
                                        initialVelocity = if (hasDragged) velSlots else 0f,
                                        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
                                    )
                                }
                                if (finalTab != currentSelectedIndex) {
                                    currentOnSelectIndex(finalTab)
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.TopCenter,
            ) {
                if (columnHeightPx > 0f && tabs.size > 1) {
                    Box(
                        modifier = Modifier
                            .size(pillSizeDp)
                            .graphicsLayer {
                                val currentPos = railPos.value
                                val yPx = (currentPos / tabs.lastIndex.toFloat()) * maxRailTravelPx
                                val stretch = (if (isActivelyDragging) abs(activeRailDragVel) * 0.018f else abs(railPos.velocity) * 0.018f).coerceIn(0f, 0.14f)
                                translationY = yPx
                                scaleY = pressScaleState.value + stretch
                                scaleX = pressScaleState.value - (stretch * 0.32f)
                            }
                            .shadow(
                                elevation = if (isDark) 10.dp else 5.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.14f),
                                spotColor = if (isDark) Color(0xFFA855F7).copy(alpha = 0.48f) else Color.Black.copy(alpha = 0.14f),
                            )
                            .clip(CircleShape)
                            .background(railBubbleFill)
                            .border(1.1.dp, railBubbleRim, CircleShape),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val name = tab.name
                        val activeIndex = if (isPressed) hoveredIndex else settledTabIndex
                        val isActive = (index == activeIndex)
                        val animatedIconTint by animateColorAsState(
                            targetValue = if (isActive) activeIconColor else inactiveIconColor,
                            animationSpec = tween(durationMillis = 160),
                            label = "RailIconTint_$name",
                        )
                        val animatedLabelTint by animateColorAsState(
                            targetValue = if (isActive) activeLabelColor else inactiveIconColor,
                            animationSpec = tween(durationMillis = 160),
                            label = "RailLabelTint_$name",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.graphicsLayer {
                                    val proximity = (1f - abs(index.toFloat() - railPos.value)).coerceIn(0f, 1f)
                                    val scale = 1f + (proximity * 0.10f)
                                    scaleX = scale
                                    scaleY = scale
                                },
                            ) {
                                AnimatedNavVectorIcon(
                                    tabName = name,
                                    iconVector = tab.iconVector,
                                    iconResId = tab.iconResId,
                                    isActive = isActive,
                                    tint = animatedIconTint,
                                    isDark = isDark,
                                    sizeDp = if (name == "AI") 22 else 21,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name,
                                    fontSize = 9.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                    color = animatedLabelTint,
                                    maxLines = 1,
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
    val isDark = AppTheme.colors.isDark
    val actualCardBg = if (isDark) AppTheme.colors.cardBackground else cardBg
    val actualBorder = if (isDark) AppTheme.colors.border else borderColor
    val actualStatusColor = if (isDark) brightenDarkStatusColor(statusColor) else statusColor
    val actualStatusBg = if (isDark) actualStatusColor.copy(alpha = 0.18f) else statusBg
    val actualActionColor = if (isDark) Color(0xFFF0ABFC) else Color(0xFFA855F7)
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
                            .weight(1f, fill = false)
                            .padding(start = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(actualStatusBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = actualStatusColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    var fastingText by rememberSaveable(initialFasting) { mutableStateOf(initialFasting.toString()) }
    var postmealText by rememberSaveable(initialPostmeal) { mutableStateOf(initialPostmeal.toString()) }

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

private fun brightenDarkStatusColor(color: Color): Color {
    return when (color) {
        Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF047857) -> Color(0xFF6EE7B7)
        Color(0xFFE65100), Color(0xFFD97706) -> Color(0xFFFCD34D)
        Color(0xFFC62828), Color(0xFFD32F2F) -> Color(0xFFFCA5A5)
        Color(0xFF1565C0), Color(0xFF0284C7) -> Color(0xFF7DD3FC)
        Color(0xFF6B7280) -> Color(0xFFCBD5E1)
        else -> color
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
