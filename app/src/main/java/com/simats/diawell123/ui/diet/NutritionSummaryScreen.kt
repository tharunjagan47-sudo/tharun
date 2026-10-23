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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun NutritionSummaryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onReturnToFoodLogClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFAFC))
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                            text = "Nutrition summary",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(
                            text = "A snapshot of your estimated meals.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${ProfileState.userName} • Illustrative sample data",
                    fontSize = 11.sp,
                    color = Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selector Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "‹", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                        Text(
                            text = "📅  Thu, 15 Oct 2026",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Text(text = "›", fontSize = 18.sp, color = Color(0xFFB81D85), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Consumed Calories & Meals Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFDEEF7))
                            .padding(16.dp),
                    ) {
                        Column {
                            Text(
                                text = "Approx. consumed",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1,410 kcal",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7A1C7D),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Column {
                            Text(
                                text = "4 of 4",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E22),
                            )
                            Text(
                                text = "Meals logged",
                                fontSize = 11.sp,
                                color = Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nutrition Distribution Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        Text(
                            text = "Nutrition distribution",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Multi-color Segmented Progress Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                        ) {
                            Box(modifier = Modifier.weight(0.42f).fillMaxSize().background(Color(0xFFFF5722)))
                            Box(modifier = Modifier.weight(0.29f).fillMaxSize().background(Color(0xFF7A1C7D)))
                            Box(modifier = Modifier.weight(0.29f).fillMaxSize().background(Color(0xFFFFC107)))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            MacroLegendItem(color = Color(0xFFFF5722), label = "Carbs 147 g", percent = "42% energy")
                            MacroLegendItem(color = Color(0xFF7A1C7D), label = "Protein 103 g", percent = "29% energy")
                            MacroLegendItem(color = Color(0xFFFFC107), label = "Fat 41 g", percent = "29% energy")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Meal Breakdown
                Text(
                    text = "Meal breakdown",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Spacer(modifier = Modifier.height(10.dp))

                MealBreakdownRow(icon = "🥣", name = "Breakfast", calories = "320 kcal")
                Spacer(modifier = Modifier.height(8.dp))
                MealBreakdownRow(icon = "🥗", name = "Lunch", calories = "450 kcal")
                Spacer(modifier = Modifier.height(8.dp))
                MealBreakdownRow(icon = "🫐", name = "Snacks", calories = "160 kcal")
                Spacer(modifier = Modifier.height(8.dp))
                MealBreakdownRow(icon = "🍱", name = "Dinner", calories = "480 kcal")

                Spacer(modifier = Modifier.height(20.dp))

                // AI Nutrition Insight Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFDEEF7))
                        .padding(14.dp),
                ) {
                    Column {
                        Text(
                            text = "✨ AI • nutrition insight",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF811978),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your 4 logged meals include grains, fruit and protein. Add varied vegetables across meals.",
                            fontSize = 12.sp,
                            color = Color(0xFF6E6877),
                            lineHeight = 17.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Return Button
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
                        .clickable { onReturnToFoodLogClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Return to Food Log",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MacroLegendItem(
    color: Color,
    label: String,
    percent: String,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
                    .padding(4.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E22))
        }
        Text(text = percent, fontSize = 10.sp, color = Color(0xFF6E6877))
    }
}

@Composable
fun MealBreakdownRow(
    icon: String,
    name: String,
    calories: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
            }
            Text(
                text = calories,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A1C7D),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NutritionSummaryScreenPreview() {
    Diawell123Theme {
        NutritionSummaryScreen()
    }
}
