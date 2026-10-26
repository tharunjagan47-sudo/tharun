package com.simats.diawell123.ui.diet

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun FoodDetailsScreen(
    mealCategory: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddToFoodLogClick: () -> Unit = {},
) {
    val mealDetails = rememberMealDetailsForCategory(mealCategory)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
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
                        color = Color(0xFF1E1E22),
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
                            Text(text = mealDetails.icon, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = mealDetails.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${mealDetails.portion} • ${mealDetails.calories} kcal",
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📉", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Glycemic Index: ${mealDetails.glycemicIndex}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = mealDetails.giNote,
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
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
                        color = Color(0xFF1E1E22),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        NutrientStatCard(title = "Carbs", value = mealDetails.carbs, color = Color(0xFFE53935), modifier = Modifier.weight(1f))
                        NutrientStatCard(title = "Protein", value = mealDetails.protein, color = Color(0xFF1E88E5), modifier = Modifier.weight(1f))
                        NutrientStatCard(title = "Fats", value = mealDetails.fats, color = Color(0xFFFDD835), modifier = Modifier.weight(1f))
                        NutrientStatCard(title = "Fiber", value = mealDetails.fiber, color = Color(0xFF43A047), modifier = Modifier.weight(1f))
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
                        color = Color(0xFF1E1E22),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    mealDetails.ingredients.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                        ) {
                            Text(
                                text = "• $item",
                                fontSize = 12.sp,
                                color = Color(0xFF1E1E22),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // iOS Liquid Glass Action Button
            IosLiquidGlassButton(
                text = "+ Add to Food Log",
                onClick = onAddToFoodLogClick,
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// 3D FLOATING LIQUID GLASS BUBBLE BACK NAVIGATION BUTTON (Matches User Image Exactly!)
@Composable
fun StyledBackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackButtonFloat")

    // Gentle floating translation Y
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backFloatOffsetY",
    )

    // Gentle breathing scale
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
            val centerY = h / 2f

            val mainRadius = 17.dp.toPx()
            val mainCenterX = 20.dp.toPx()

            // 1. Ambient Blue/Purple Outer Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF5363FF).copy(alpha = glowAlpha * 0.45f),
                        Color(0xFFA173FF).copy(alpha = glowAlpha * 0.25f),
                        Color.Transparent,
                    ),
                ),
                radius = mainRadius * 1.45f,
                center = Offset(mainCenterX, centerY),
            )

            // 2. Main Organic Liquid Bubble Metaball Path
            val metaballPath = Path().apply {
                // Main Sphere
                addOval(
                    Rect(
                        left = mainCenterX - mainRadius,
                        top = centerY - mainRadius,
                        right = mainCenterX + mainRadius,
                        bottom = centerY + mainRadius,
                    ),
                )

                // Droplet Extension Tail
                val tailLeft = mainCenterX + mainRadius * 0.55f
                val tailRight = mainCenterX + mainRadius * 1.50f
                val tailTop = centerY - mainRadius * 0.52f
                val tailBottom = centerY + mainRadius * 0.52f

                addOval(
                    Rect(
                        left = tailLeft,
                        top = tailTop,
                        right = tailRight,
                        bottom = tailBottom,
                    ),
                )
            }

            // Draw Liquid Bubble Translucent Gradient Fill
            drawPath(
                path = metaballPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8BA5FF).copy(alpha = 0.95f),
                        Color(0xFF5363FF).copy(alpha = 0.85f),
                        Color(0xFF8E24AA).copy(alpha = 0.75f),
                        Color(0xFF3B1E94).copy(alpha = 0.65f),
                    ),
                    center = Offset(mainCenterX - mainRadius * 0.3f, centerY - mainRadius * 0.3f),
                    radius = mainRadius * 1.6f,
                ),
            )

            // Luminous Glass Rim Outline
            drawPath(
                path = metaballPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFF00E5FF).copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.50f),
                    ),
                ),
                style = Stroke(width = 1.6.dp.toPx()),
            )

            // Glass Specular Crescent Arc Highlight
            drawArc(
                color = Color.White.copy(alpha = 0.85f),
                startAngle = 190f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(mainCenterX - mainRadius * 0.82f, centerY - mainRadius * 0.82f),
                size = Size(mainRadius * 1.64f, mainRadius * 1.64f),
                style = Stroke(width = 2.0.dp.toPx()),
            )

            // 3. Small Satellite Floating Bubble Sphere (To the right)
            val satCenterX = w - 7.dp.toPx()
            val satRadius = 5.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFF7CA3FF).copy(alpha = 0.80f),
                        Color(0xFF5363FF).copy(alpha = 0.60f),
                    ),
                    center = Offset(satCenterX - satRadius * 0.3f, centerY - satRadius * 0.3f),
                    radius = satRadius * 1.2f,
                ),
                radius = satRadius,
                center = Offset(satCenterX, centerY),
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.90f),
                radius = satRadius,
                center = Offset(satCenterX, centerY),
                style = Stroke(width = 1.2.dp.toPx()),
            )

            // 4. White Crisp Back Chevron Arrow Icon `<`
            val arrowPath = Path().apply {
                moveTo(mainCenterX + 3.5.dp.toPx(), centerY - 6.dp.toPx())
                lineTo(mainCenterX - 3.5.dp.toPx(), centerY)
                lineTo(mainCenterX + 3.5.dp.toPx(), centerY + 6.dp.toPx())
            }

            drawPath(
                path = arrowPath,
                color = Color.White,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
fun IosLiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(
        Color(0xFF7A1C7D),
        Color(0xFFBD1E85),
        Color(0xFFFF5722),
    ),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFB81D85).copy(alpha = 0.35f),
                spotColor = Color(0xFFB81D85).copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(colors = gradientColors),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0f),
                        ),
                    ),
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
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E22))
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
