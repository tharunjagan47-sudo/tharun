package com.simats.diawell123.ui.diet

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

data class IndianMealInfo(
    val categoryTitle: String,
    val mealName: String,
    val icon: String,
    val servingText: String,
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int,
    val description: String,
    val ingredients: String,
)

fun getIndianMealInfo(mealCategory: String): IndianMealInfo {
    return when (mealCategory.lowercase()) {
        "lunch" -> IndianMealInfo(
            categoryTitle = "Lunch",
            mealName = "Multigrain Roti, Dal & Bhindi Sabzi",
            icon = "🫓",
            servingText = "2 rotis + 1 bowl dal + sabzi (380 g)",
            calories = 420,
            carbs = 52,
            protein = 18,
            fat = 10,
            description = "High-fiber multigrain chapattis paired with protein-rich Bengal gram dal, stir-fried okra (bhindi sabzi), and a side of fresh cucumber salad.",
            ingredients = "• Multigrain Flour (Atta)\n• Yellow Chana Dal & Spices\n• Fresh Bhindi (Okra)\n• Olive/Mustard Oil & Cumin",
        )
        "snack" -> IndianMealInfo(
            categoryTitle = "Evening Snack",
            mealName = "Sprouts Chaat & Green Tea",
            icon = "🥗",
            servingText = "1 bowl sprouts chaat (180 g)",
            calories = 150,
            carbs = 22,
            protein = 11,
            fat = 2,
            description = "Steamed moong sprouts tossed with chopped cucumber, tomato, pomegranate seeds, lemon juice, and a warm cup of antioxidant-rich green tea.",
            ingredients = "• Steamed Moong Sprouts\n• Cucumber & Tomatoes\n• Lemon Juice & Chaat Masala\n• Antioxidant Green Tea",
        )
        "dinner" -> IndianMealInfo(
            categoryTitle = "Dinner",
            mealName = "Palak Paneer & Brown Rice",
            icon = "🍲",
            servingText = "1 plate palak paneer + brown rice (350 g)",
            calories = 380,
            carbs = 38,
            protein = 22,
            fat = 14,
            description = "Nutrient-dense spinach puree with soft low-fat cottage cheese (paneer) cubes served alongside fiber-rich brown rice.",
            ingredients = "• Fresh Spinach (Palak)\n• Low-fat Cottage Cheese (Paneer)\n• Steamed Brown Rice\n• Garlic, Ginger & Spices",
        )
        else -> IndianMealInfo( // Default: Breakfast
            categoryTitle = "Breakfast",
            mealName = "Idli Sambar & Coconut Chutney",
            icon = "🥣",
            servingText = "2 idlis + 1 bowl sambar (280 g)",
            calories = 280,
            carbs = 44,
            protein = 10,
            fat = 5,
            description = "Steamed fermented rice and lentil cakes served with vegetable-rich tur dal sambar and a side of fresh coconut chutney. Low GI and light.",
            ingredients = "• Steamed Rice & Urad Dal Batter\n• Tur Dal Sambar with Vegetables\n• Fresh Grated Coconut\n• Curry Leaves & Mustard Seeds",
        )
    }
}

@Composable
fun FoodDetailsScreen(
    modifier: Modifier = Modifier,
    mealCategory: String = "breakfast",
    onBackClick: () -> Unit = {},
    onAddToFoodLogClick: () -> Unit = {},
) {
    var portionCount by remember { mutableIntStateOf(1) }
    val meal = remember(mealCategory) { getIndianMealInfo(mealCategory) }

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

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StyledBackButton(onBackClick = onBackClick)

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Indian Food Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Diabetic-friendly Indian Nutrition",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Meal Banner Illustration Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFDEEF7)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = meal.icon, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Authentic Indian ${meal.categoryTitle}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Subtitle
            Text(
                text = meal.mealName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Text(
                text = "${meal.categoryTitle} • Serving: ${meal.servingText}",
                fontSize = 13.sp,
                color = Color(0xFF6E6877),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Approximate Nutrition Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = "APPROXIMATE NUTRITION PER SERVING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        NutritionStatItem(value = "${meal.calories * portionCount} kcal", label = "Calories")
                        NutritionStatItem(value = "${meal.carbs * portionCount} g", label = "Carbs")
                        NutritionStatItem(value = "${meal.protein * portionCount} g", label = "Protein")
                        NutritionStatItem(value = "${meal.fat * portionCount} g", label = "Fat")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Portion Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Your portion",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8F7FA))
                            .clickable { if (portionCount > 1) portionCount-- }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(text = "−", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81D85))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "$portionCount serving",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8F7FA))
                            .clickable { portionCount++ }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(text = "+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB81D85))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = meal.description,
                fontSize = 13.sp,
                color = Color(0xFF6E6877),
                lineHeight = 19.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "Key Ingredients",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = meal.ingredients,
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // iOS Liquid Glass Action Button
            IosLiquidGlassButton(
                text = "+ Add to Food Log",
                onClick = onAddToFoodLogClick,
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun StyledBackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF811978).copy(alpha = 0.12f),
                spotColor = Color(0xFF811978).copy(alpha = 0.18f),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFEFECEF),
                    ),
                ),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onBackClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "←",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E22),
        )
    }
}

@Composable
fun IosLiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        Color(0xFF7A1C7D),
        Color(0xFFBD1E85),
        Color(0xFFFF5722),
    ),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFFB81D85).copy(alpha = 0.35f),
                spotColor = Color(0xFFB81D85).copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(32.dp))
            .background(brush = Brush.horizontalGradient(colors = gradientColors))
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.2f),
                    ),
                ),
                shape = RoundedCornerShape(32.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
fun NutritionStatItem(
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7A1C7D),
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF6E6877),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodDetailsScreenPreview() {
    Diawell123Theme {
        FoodDetailsScreen()
    }
}
