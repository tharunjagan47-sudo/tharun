package com.simats.diawell123.ui.profile
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthPreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSavePreferencesClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedHealthFocus by remember { mutableStateOf("Glucose Control") }
    var selectedActivityLevel by remember { mutableStateOf("Moderate (3-5 days/week)") }
    var isSavingAndLoading by remember { mutableStateOf(false) }

    val selectedDiets = remember {
        mutableStateListOf(
            "Non-Vegetarian" to true,
            "Low-Carb / Keto" to true,
            "Vegetarian" to false,
            "Vegan" to false,
            "Pescatarian" to false,
        )
    }

    val allergiesList = remember {
        mutableStateListOf(
            "Nuts" to true,
            "Lactose / Dairy" to false,
            "Gluten" to false,
            "Seafood" to false,
            "Soy" to false,
            "Shellfish" to false,
        )
    }

    // Smooth 3D Saturn AI Orb Loading Transition into Home Screen
    LaunchedEffect(isSavingAndLoading) {
        if (isSavingAndLoading) {
            delay(2200) // 2.2 Seconds 3D Rotating Saturn AI Orb Loading
            isSavingAndLoading = false
            onSavePreferencesClick()
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(4.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Health Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Step 2 of 2 • Health focus & dietary preferences",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to save preferences.",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // 1. Primary Health Focus Selection
            Text(
                text = "Primary Health Focus",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Glucose Control" to "Keep daily sugar levels balanced and steady.",
                    "Weight Management" to "Calorie-conscious meal suggestions for steady goals.",
                    "Energy & Stamina" to "Nutritious routines to prevent spikes and crashes.",
                ).forEach { (title, subtitle) ->
                    val isSelected = selectedHealthFocus == title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { selectedHealthFocus = title }
                            .padding(12.dp),
                    ) {
                        Column {
                            Text(
                                text = if (isSelected) "●  $title" else "○  $title",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textPrimary,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Dietary Preferences Chips
            Text(
                text = "Dietary Style",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selectedDiets.forEachIndexed { index, (dietName, isChecked) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isChecked) Color(0xFFB81D85) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isChecked) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable {
                                selectedDiets[index] = dietName to !isChecked
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = if (isChecked) "✓ $dietName" else "+ $dietName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isChecked) Color.White else AppTheme.colors.textPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Allergies & Food Sensitivities
            Text(
                text = "Food Allergies & Sensitivities",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allergiesList.forEachIndexed { index, (allergyName, isChecked) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isChecked) Color(0xFFE53935) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isChecked) Color(0xFFE53935) else AppTheme.colors.border,
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable {
                                allergiesList[index] = allergyName to !isChecked
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = if (isChecked) "⚠️ $allergyName" else "+ $allergyName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isChecked) Color.White else AppTheme.colors.textPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Activity & Exercise Level
            Text(
                text = "Activity & Exercise Routine",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Sedentary (Little or no exercise)",
                    "Light Exercise (1-2 days/week)",
                    "Moderate (3-5 days/week)",
                    "Active (Daily workout)",
                ).forEach { level ->
                    val isSelected = selectedActivityLevel.contains(level.substringBefore(" "))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { selectedActivityLevel = level }
                            .padding(12.dp),
                    ) {
                        Text(
                            text = if (isSelected) "●  $level" else "○  $level",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info Banner Callout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(12.dp),
            ) {
                Text(
                    text = "Your health preferences guide AI recommendation models to suggest balanced, personalized meal and exercise plans tailored for your diabetes management.",
                    fontSize = 12.sp,
                    color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                    lineHeight = 17.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Preferences Button
            IosLiquidGlassButton(
                text = if (uiState.isSaving) "Saving Preferences..." else "Save Health Preferences",
                onClick = {
                    val diets = selectedDiets.filter { it.second }.map { it.first }
                    val allergies = allergiesList.filter { it.second }.map { it.first }

                    viewModel.saveHealthPreferences(
                        healthFocus = selectedHealthFocus,
                        dietaryPreferences = diets,
                        allergies = allergies,
                        activityLevel = selectedActivityLevel,
                        onSuccess = {
                            isSavingAndLoading = true
                        }
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "General meal and lifestyle ideas, not a medical diet prescription.",
                fontSize = 11.sp,
                color = AppTheme.colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }

        // ROTATING 3D SATURN AI SPEECH ORB LOADING OVERLAY
        AnimatedVisibility(
            visible = isSavingAndLoading,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.96f))
                    .clickable(enabled = false) {}, // Intercept clicks during loading
                contentAlignment = Alignment.Center,
            ) {
                AiSpeechOrbLoadingView(
                    statusText = "Setting up your DiaWell Health AI...",
                )
            }
        }
    }
}

@Composable
fun AiSpeechOrbLoadingView(
    modifier: Modifier = Modifier,
    statusText: String = "Setting up your DiaWell Health AI...",
) {
    val infiniteTransition = rememberInfiniteTransition(label = "3DSaturnOrbTransition")

    // Rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "saturnRotation",
    )

    // Pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "saturnPulse",
    )

    // Glowing aura alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "saturnGlowAlpha",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(24.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp),
        ) {
            // Ambient Outer Glow Aura Ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.45f),
                                Color(0xFFB81D85).copy(alpha = glowAlpha * 0.32f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            // Canvas for 3D SATURN PLANETARY RING ROTATING GLOWING AI ORB
            Canvas(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale),
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                val planetRadius = w * 0.28f
                val ringRx = w * 0.46f // Horizontal radius of 3D Saturn Ring
                val ringRy = w * 0.16f // Vertical radius (flattened 3D tilt ellipse)

                // 1. BACK HALF OF 3D SATURN RING (Passes BEHIND the top half of Saturn)
                rotate(degrees = rotationAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFFB81D85).copy(alpha = 0.5f),
                                Color(0xFF00E5FF).copy(alpha = 0.2f),
                                Color(0xFF00E5FF),
                            ),
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f, // Top half (180deg to 360deg - behind)
                        useCenter = false,
                        topLeft = Offset(center.x - ringRx, center.y - ringRy),
                        size = Size(ringRx * 2f, ringRy * 2f),
                        style = Stroke(width = 3.5.dp.toPx()),
                    )
                }

                // 2. SATURN CORE LIQUID GLASS SPHERE
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF5363FF),
                            Color(0xFF8E24AA),
                            Color(0xFF1B0033),
                        ),
                        center = Offset(center.x - planetRadius * 0.35f, center.y - planetRadius * 0.35f),
                        radius = planetRadius * 1.3f,
                    ),
                    radius = planetRadius,
                    center = center,
                )

                // Glass Planet Specular Top Arc Highlight Reflection
                drawArc(
                    color = Color.White.copy(alpha = 0.70f),
                    startAngle = 200f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(center.x - planetRadius * 0.85f, center.y - planetRadius * 0.85f),
                    size = Size(planetRadius * 1.7f, planetRadius * 1.7f),
                    style = Stroke(width = 2.5.dp.toPx()),
                )

                // 3. FRONT HALF OF 3D SATURN RING (Passes IN FRONT OF the lower half of Saturn!)
                rotate(degrees = rotationAngle, pivot = center) {
                    // Outer Luminous 3D Ring Front
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color.White,
                                Color(0xFFFF4081),
                                Color(0xFF00E5FF),
                            ),
                        ),
                        startAngle = 0f,
                        sweepAngle = 180f, // Bottom half (0deg to 180deg - in front!)
                        useCenter = false,
                        topLeft = Offset(center.x - ringRx, center.y - ringRy),
                        size = Size(ringRx * 2f, ringRy * 2f),
                        style = Stroke(width = 4.2.dp.toPx()),
                    )

                    // Inner Fine 3D Accent Ring Front
                    drawArc(
                        color = Color.White.copy(alpha = 0.88f),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - ringRx * 0.82f, center.y - ringRy * 0.82f),
                        size = Size(ringRx * 1.64f, ringRy * 1.64f),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )

                    // 3D Orbiting Energy Nodes on Saturn Ring Plane
                    for (i in 0 until 3) {
                        val angleRad = ((rotationAngle * 2.2f + i * 120f) * (Math.PI / 180.0)).toFloat()
                        val beadX = center.x + ringRx * cos(angleRad)
                        val beadY = center.y + ringRy * sin(angleRad)

                        // Z-Sort: Only draw nodes when they are on the front side of the ring plane
                        if (sin(angleRad) > -0.2f) {
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(beadX, beadY),
                            )
                            drawCircle(
                                color = Color(0xFF00E5FF),
                                radius = 7.dp.toPx(),
                                center = Offset(beadX, beadY),
                                style = Stroke(width = 1.5.dp.toPx()),
                            )
                        }
                    }
                }
            }

            // Core Glowing White Light
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.cardBackground)
                    .shadow(elevation = 10.dp, shape = CircleShape, spotColor = Color(0xFF00E5FF)),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Loading Label
        Text(
            text = statusText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7A1C7D),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "DiaWell AI is configuring your personal routine...",
            fontSize = 12.sp,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HealthPreferencesScreenPreview() {
    Diawell123Theme {
        HealthPreferencesScreen()
    }
}
