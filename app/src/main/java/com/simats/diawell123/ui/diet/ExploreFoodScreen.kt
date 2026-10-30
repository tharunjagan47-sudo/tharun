package com.simats.diawell123.ui.diet

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.RecommendedFoodItem
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.theme.Diawell123Theme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreFoodScreen(
    modifier: Modifier = Modifier,
    viewModel: DietViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onMealDetailsClick: (category: String, foodId: Int?, dishName: String?) -> Unit = { _, _, _ -> },
) {
    val uiState by viewModel.dailyDietState.collectAsState()
    val aiState by viewModel.aiRecommendationsState.collectAsState()

    val patientContext = aiState.data?.patientContext
    val userPrefText = buildString {
        append("🥗 ")
        append(patientContext?.dietaryPreference ?: "Vegetarian")
        if (!patientContext?.diabetesType.isNullOrBlank()) {
            append(" • ")
            append(patientContext?.diabetesType)
        }
        if (patientContext?.currentGlucoseMgDl != null) {
            append(" • Glucose: ${patientContext.currentGlucoseMgDl.toInt()} mg/dL")
        }
    }

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

                // Top Bar & Search Bar
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StyledBackButton(onBackClick = onBackClick)

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Explore food",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "AI-powered recommendations & 1000+ dish dataset",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onDailyDietSearchChanged(it) },
                            placeholder = {
                                Text(
                                    text = "🔍 Search 1000+ dishes (e.g., Paneer, Dal, Chicken)...",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA09BAA),
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8F7FA),
                                unfocusedContainerColor = Color(0xFFF8F7FA),
                                focusedBorderColor = Color(0xFFBD1E85),
                                unfocusedBorderColor = Color(0xFFEFECEF),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Pills
                ElementPopUpContainer(delayMillis = 80) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listOf("All", "Breakfast", "Lunch", "Snack", "Dinner").forEach { category ->
                            val isSelected = uiState.selectedCategory.equals(category, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFFB81D85) else Color(0xFFF8F7FA))
                                    .clickable { viewModel.onDailyDietCategoryChanged(category) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF6E6877),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Health & Preference Tag
                Text(
                    text = userPrefText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hypoglycemia Clinical Alert Banner (if glucose < 70)
                if (aiState.data?.hypoglycemiaAlert != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3CD))
                            .border(1.dp, Color(0xFFFFC107), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                    ) {
                        Text(
                            text = aiState.data!!.hypoglycemiaAlert!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF856404),
                            lineHeight = 17.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // AI Recommendations Section
                ElementPopUpContainer(delayMillis = 140) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "✨ AI Recommended for You",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E22),
                            )

                            if (aiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFBD1E85),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val recommendations = aiState.data?.recommendations ?: emptyList()
                        if (recommendations.isNotEmpty()) {
                            recommendations.take(4).forEach { rec ->
                                AiRecommendedFoodCard(
                                    item = rec,
                                    onClick = { onMealDetailsClick(rec.category.lowercase(), rec.id, rec.title) },
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else if (!aiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFDEEF7))
                                    .padding(12.dp),
                            ) {
                                Text(
                                    text = "Analyzing your profile and nutritional dataset to rank optimal meals...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Explore Meals List from Dataset
                ElementPopUpContainer(delayMillis = 200) {
                    Column {
                        Text(
                            text = "Explore meals (${uiState.filteredMeals.size} available)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.filteredMeals.isNotEmpty()) {
                            uiState.filteredMeals.forEach { meal ->
                                ExploreMealCard(
                                    icon = meal.icon,
                                    category = meal.category.uppercase(),
                                    name = meal.title,
                                    serving = "${meal.portion} • ${meal.calories}",
                                    macros = "C:${meal.carbsG.toInt()}g • P:${meal.proteinG.toInt()}g • F:${meal.fatG.toInt()}g",
                                    onClick = { onMealDetailsClick(meal.category.lowercase(), meal.id, meal.title) },
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No matching dishes found in nutrition dataset.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clinical Disclaimer Note
                ElementPopUpContainer(delayMillis = 260) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F7FA))
                            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Text(
                            text = aiState.data?.disclaimer
                                ?: "Medical Disclaimer: Suggestions are calculated from nutritional dataset values and personal health context. Always consult your physician or registered dietitian before altering your diabetes meal plan.",
                            fontSize = 10.sp,
                            color = Color(0xFF8A8595),
                            lineHeight = 14.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiRecommendedFoodCard(
    item: RecommendedFoodItem,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDEEF7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = item.icon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB81D85),
                            letterSpacing = 0.5.sp,
                        )
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                    }
                }

                // Match Suitability Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B156E), Color(0xFFB81D85)),
                            ),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${item.recommendationScore}% Match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Portion and Nutrition Macro Summary
            Text(
                text = "${item.portion} • ${item.caloriesLabel} • Carbs ${item.carbsG}g • Fibre ${item.fibreG}g • Sugar ${item.freeSugarG}g",
                fontSize = 11.sp,
                color = Color(0xFF6E6877),
            )

            if (item.recommendationReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💡 ${item.recommendationReason}",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    lineHeight = 15.sp,
                )
            }

            if (item.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item.highlights.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF8F7FA))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF811978),
                            )
                        }
                    }

                    item.glycemicImpact?.badge?.let { giBadge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = giBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreMealCard(
    icon: String,
    category: String,
    name: String,
    serving: String,
    macros: String,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB81D85),
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = serving,
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                    )
                    Text(
                        text = macros,
                        fontSize = 10.sp,
                        color = Color(0xFF8A8595),
                    )
                }
            }

            Text(
                text = "+ Add",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreFoodScreenPreview() {
    Diawell123Theme {
        ExploreFoodScreen()
    }
}
