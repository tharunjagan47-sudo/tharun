package com.simats.diawell123.ui.diet
import com.simats.diawell123.ui.theme.AppTheme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun FoodDetailsScreen(
    mealCategory: String,
    modifier: Modifier = Modifier,
    foodId: Int? = null,
    dishName: String? = null,
    viewModel: DietViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddToFoodLogClick: () -> Unit = {},
) {
    val uiState by viewModel.detailsState.collectAsState()
    val fallbackDetails = rememberMealDetailsForCategory(mealCategory)

    LaunchedEffect(mealCategory, foodId, dishName) {
        viewModel.loadFoodDetails(foodId = foodId, dishName = dishName, category = mealCategory)
    }

    val displayDetails = uiState.details
    val title = displayDetails?.title ?: fallbackDetails.title
    val portionText = if (displayDetails != null) "${displayDetails.portion} • ${displayDetails.caloriesLabel}" else "${fallbackDetails.portion} • ${fallbackDetails.calories} kcal"
    val icon = displayDetails?.icon ?: fallbackDetails.icon
    val giIndex = displayDetails?.glycemicIndex ?: fallbackDetails.glycemicIndex
    val giNote = displayDetails?.glycemicDetails?.description?.takeIf { it.isNotBlank() } ?: fallbackDetails.giNote
    val carbsVal = displayDetails?.macros?.carbsG?.let { "${it.toInt()}g" } ?: fallbackDetails.carbs
    val proteinVal = displayDetails?.macros?.proteinG?.let { "${it.toInt()}g" } ?: fallbackDetails.protein
    val fatVal = displayDetails?.macros?.fatG?.let { "${it.toInt()}g" } ?: fallbackDetails.fats
    val fiberVal = displayDetails?.macros?.fiberG?.let { "${it.toInt()}g" } ?: fallbackDetails.fiber
    val ingredientsList = displayDetails?.ingredients?.takeIf { it.isNotEmpty() } ?: fallbackDetails.ingredients

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

            // Top Bar with Styled Floating Bubble Back Button
            ElementPopUpContainer(delayMillis = 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$mealCategory Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Food Header Card (Image / Icon + Title)
            ElementPopUpContainer(delayMillis = 60) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B156E), Color(0xFFB81D85)),
                            ),
                        )
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = icon, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = portionText,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Glycemic & Diabetic Impact Callout
            ElementPopUpContainer(delayMillis = 120) {
                val (giColor, giBgColor) = when {
                    giIndex.contains("Low", ignoreCase = true) -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
                    giIndex.contains("Medium", ignoreCase = true) || giIndex.contains("Mod", ignoreCase = true) -> Color(0xFFE65100) to Color(0xFFFFF3E0)
                    else -> Color(0xFFC62828) to Color(0xFFFFEBEE)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(giBgColor)
                        .border(1.dp, giColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📉", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Glycemic Index: $giIndex",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = giColor,
                            )
                            Text(
                                text = giNote,
                                fontSize = 11.sp,
                                color = giColor.copy(alpha = 0.9f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Macronutrient Breakdown
            ElementPopUpContainer(delayMillis = 180) {
                Column {
                    Text(
                        text = "Macronutrient Breakdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        NutrientStatCard(title = "Carbs", value = carbsVal, color = Color(0xFFE53935), modifier = Modifier.weight(1f))
                        NutrientStatCard(title = "Protein", value = proteinVal, color = Color(0xFF1E88E5), modifier = Modifier.weight(1f))
                        NutrientStatCard(title = "Fats", value = fatVal, color = Color(0xFFFDD835), modifier = Modifier.weight(1f))
                        NutrientStatCard(title = "Fiber", value = fiberVal, color = Color(0xFF43A047), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Micronutrient Profile (Dataset Verified)
            val micro = displayDetails?.micronutrients
            if (micro != null) {
                Spacer(modifier = Modifier.height(20.dp))
                ElementPopUpContainer(delayMillis = 210) {
                    Column {
                        Text(
                            text = "Micronutrient Profile (Dataset Verified)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MicroStatCard(title = "Free Sugar", value = "${micro.freeSugarG}g", modifier = Modifier.weight(1f))
                            MicroStatCard(title = "Sodium", value = "${micro.sodiumMg.toInt()}mg", modifier = Modifier.weight(1f))
                            MicroStatCard(title = "Calcium", value = "${micro.calciumMg.toInt()}mg", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MicroStatCard(title = "Iron", value = "${micro.ironMg}mg", modifier = Modifier.weight(1f))
                            MicroStatCard(title = "Vit C", value = "${micro.vitaminCMg}mg", modifier = Modifier.weight(1f))
                            MicroStatCard(title = "Folate", value = "${micro.folateUg.toInt()}µg", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Diabetic Health Guidance & Benefits
            val healthBenefitsText = displayDetails?.healthBenefits ?: ""
            val diabeticGuidanceText = displayDetails?.diabeticGuidance ?: ""
            if (healthBenefitsText.isNotBlank() || diabeticGuidanceText.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                ElementPopUpContainer(delayMillis = 230) {
                    Column {
                        Text(
                            text = "Diabetic Health Guidance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.cardBackground)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                        ) {
                            Column {
                                if (healthBenefitsText.isNotBlank()) {
                                    Text(
                                        text = "🌱 $healthBenefitsText",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textPrimary,
                                        lineHeight = 17.sp,
                                    )
                                }
                                if (diabeticGuidanceText.isNotBlank()) {
                                    if (healthBenefitsText.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "🩺 $diabeticGuidanceText",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF6B156E),
                                        lineHeight = 17.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key Health Ingredients & Benefits
            ElementPopUpContainer(delayMillis = 240) {
                Column {
                    Text(
                        text = "Recommended Ingredients",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ingredientsList.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.cardBackground)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                        ) {
                            Text(
                                text = "• $item",
                                fontSize = 12.sp,
                                color = AppTheme.colors.textPrimary,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // iOS Liquid Glass Action Button (Adds meal to Food Log)
            IosLiquidGlassButton(
                text = if (uiState.isLogging) "Adding to Food Log..." else "+ Add to Food Log",
                onClick = {
                    if (!uiState.isLogging) {
                        val calInt = displayDetails?.calories ?: (fallbackDetails.calories.filter { it.isDigit() }.toIntOrNull() ?: 300)
                        val carbsFloat = displayDetails?.macros?.carbsG ?: (fallbackDetails.carbs.filter { it.isDigit() }.toFloatOrNull() ?: 30f)
                        val proteinFloat = displayDetails?.macros?.proteinG ?: (fallbackDetails.protein.filter { it.isDigit() }.toFloatOrNull() ?: 15f)
                        val fatFloat = displayDetails?.macros?.fatG ?: (fallbackDetails.fats.filter { it.isDigit() }.toFloatOrNull() ?: 8f)

                        viewModel.addCurrentFoodToLog(
                            categoryFallback = mealCategory.ifBlank { "BREAKFAST" },
                            titleFallback = title,
                            portionFallback = displayDetails?.portion ?: fallbackDetails.portion,
                            caloriesFallback = calInt,
                            carbsFallback = carbsFloat,
                            proteinFallback = proteinFloat,
                            fatFallback = fatFloat,
                            iconFallback = displayDetails?.icon ?: "🥗",
                            onSuccess = onAddToFoodLogClick,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// 3D FLOATING LIQUID GLASS BUBBLE BACK NAVIGATION BUTTON
@Composable
fun StyledBackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackButtonFloat")

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backFloatOffsetY",
    )

    val floatScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backFloatScale",
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backGlowAlpha",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = floatOffsetY.dp.toPx()
                scaleX = floatScale
                scaleY = floatScale
            }
            .clip(CircleShape)
            .clickable { onBackClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(width = 64.dp, height = 42.dp),
        ) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF80AB).copy(alpha = glowAlpha * 0.45f),
                        Color(0xFFBD1E85).copy(alpha = glowAlpha * 0.20f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.70f,
                ),
            )

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFBD1E85).copy(alpha = 0.35f),
                        Color(0xFF7A1C7D).copy(alpha = 0.20f),
                        Color(0xFF4A0E4E).copy(alpha = 0.15f),
                    ),
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2),
            )

            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        Color(0xFFFF80AB).copy(alpha = 0.60f),
                        Color(0xFFBD1E85).copy(alpha = 0.40f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2),
                style = Stroke(width = 1.8.dp.toPx()),
            )

            val arrowPath = Path().apply {
                moveTo(w * 0.60f, h * 0.28f)
                lineTo(w * 0.38f, h * 0.50f)
                lineTo(w * 0.60f, h * 0.72f)
            }
            drawPath(
                path = arrowPath,
                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF7A1C7D),
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}

// iOS LIQUID GLASS BUTTON
@Composable
fun IosLiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        Color(0xFF7A1C7D),
        Color(0xFFBD1E85),
        Color(0xFFFF4081),
    ),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassPulse")

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(27.dp),
                spotColor = (gradientColors.getOrNull(1) ?: gradientColors.firstOrNull() ?: Color(0xFFBD1E85)).copy(alpha = 0.55f),
                ambientColor = (gradientColors.firstOrNull() ?: Color(0xFF7A1C7D)).copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(27.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = gradientColors,
                ),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val shimmerX = width * shimmerProgress
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.28f),
                        Color.Transparent,
                    ),
                    start = Offset(shimmerX - 80f, 0f),
                    end = Offset(shimmerX + 80f, height),
                ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.2.dp)
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.70f),
                            Color.White.copy(alpha = 0.15f),
                        ),
                    ),
                    shape = RoundedCornerShape(26.dp),
                ),
        )

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
fun NutrientStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        }
    }
}

@Composable
fun MicroStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        }
    }
}

private data class MealDetailsData(
    val icon: String,
    val title: String,
    val calories: String,
    val portion: String,
    val glycemicIndex: String,
    val giNote: String,
    val carbs: String,
    val protein: String,
    val fats: String,
    val fiber: String,
    val ingredients: List<String>,
)

private fun rememberMealDetailsForCategory(category: String): MealDetailsData {
    return when (category.lowercase()) {
        "breakfast" -> MealDetailsData(
            icon = "🥣",
            title = "Oatmeal Bowl & Berries",
            calories = "340",
            portion = "1 Medium Bowl (250g)",
            glycemicIndex = "55 (Low GI)",
            giNote = "Gradual energy release, prevents morning glucose spikes.",
            carbs = "48g",
            protein = "14g",
            fats = "7g",
            fiber = "8g",
            ingredients = listOf(
                "Whole rolled oats (40g)",
                "Unsweetened Greek yogurt (100g)",
                "Fresh blueberries & strawberries (50g)",
                "Chia seeds & crushed almonds (10g)",
            ),
        )
        "lunch" -> MealDetailsData(
            icon = "🥗",
            title = "Grilled Chicken & Quinoa Salad",
            calories = "480",
            portion = "1 Large Bowl (350g)",
            glycemicIndex = "42 (Low GI)",
            giNote = "High lean protein & fiber keeps post-prandial blood sugar steady.",
            carbs = "42g",
            protein = "38g",
            fats = "12g",
            fiber = "9g",
            ingredients = listOf(
                "Skinless grilled chicken breast (150g)",
                "Cooked organic quinoa (100g)",
                "Mixed greens, cucumber & cherry tomatoes",
                "Cold-pressed extra virgin olive oil (1 tbsp)",
            ),
        )
        "dinner" -> MealDetailsData(
            icon = "🐟",
            title = "Baked Salmon & Steamed Broccoli",
            calories = "420",
            portion = "1 Plate (300g)",
            glycemicIndex = "28 (Very Low GI)",
            giNote = "Rich in Omega-3 fatty acids, excellent overnight metabolic control.",
            carbs = "18g",
            protein = "36g",
            fats = "18g",
            fiber = "6g",
            ingredients = listOf(
                "Fresh wild salmon fillet (160g)",
                "Steamed fresh broccoli florets (120g)",
                "Lemon juice, garlic & herbs seasoning",
            ),
        )
        else -> MealDetailsData(
            icon = "🥑",
            title = "Avocado & Roasted Chickpea Bowl",
            calories = "290",
            portion = "1 Serving (200g)",
            glycemicIndex = "35 (Low GI)",
            giNote = "Heart-healthy fats and plant protein for sustained energy.",
            carbs = "26g",
            protein = "10g",
            fats = "14g",
            fiber = "7g",
            ingredients = listOf(
                "Hass avocado slices (half)",
                "Roasted seasoned chickpeas (80g)",
                "Pumpkin seeds & microgreens",
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodDetailsScreenPreview() {
    Diawell123Theme {
        FoodDetailsScreen(mealCategory = "Breakfast")
    }
}
