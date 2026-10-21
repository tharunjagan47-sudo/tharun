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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun FoodDetailsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddToFoodLogClick: () -> Unit = {},
) {
    var portionCount by remember { mutableIntStateOf(1) }

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
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackClick() }
                        .padding(4.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Food details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Demo profile • Approximate nutrition",
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
                    Text(text = "🥣", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fresh Oats with Berries & Almonds",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF811978),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Subtitle
            Text(
                text = "Berry & almond oats",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Text(
                text = "Breakfast • serving size: 1 bowl (290 g)",
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
                        NutritionStatItem(value = (320 * portionCount).toString() + " kcal", label = "Calories")
                        NutritionStatItem(value = "${42 * portionCount} g", label = "Carbs")
                        NutritionStatItem(value = "${18 * portionCount} g", label = "Protein")
                        NutritionStatItem(value = "${10 * portionCount} g", label = "Fat")
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
                        text = "$portionCount bowl",
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
                text = "Whole-grain oats bring fiber, while yogurt and almonds add protein and texture. Berries offer natural sweetness. Choose unsweetened ingredients when possible.",
                fontSize = 13.sp,
                color = Color(0xFF6E6877),
                lineHeight = 19.sp,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Add to Food Log Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF7A1C7D),
                                Color(0xFFBD1E85),
                                Color(0xFFFF5722),
                            ),
                        ),
                    )
                    .clickable { onAddToFoodLogClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+  Add to Food Log",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
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
