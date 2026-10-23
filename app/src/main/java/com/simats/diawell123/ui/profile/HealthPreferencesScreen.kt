package com.simats.diawell123.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthPreferencesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSavePreferencesClick: () -> Unit = {},
) {
    var selectedHealthFocus by remember { mutableStateOf("Glucose Control") }
    var selectedActivityLevel by remember { mutableStateOf("Moderate (3-5 days)") }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    val selectedDiets = remember {
        mutableStateListOf(
            "Non-Vegetarian" to true,
            "Low-Carb / Keto" to true,
            "Vegetarian" to false,
            "Vegan" to false,
            "Pescatarian" to false,
        )
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onSavePreferencesClick()
        }
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
                        text = "Health & Lifestyle Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Customize your daily goals, dietary rules, and care routine.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1-Second Success Confirmation Banner
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✓", fontSize = 22.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Health Preferences Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your preferences have been updated.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            // Profile Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Demo profile • ${ProfileState.userName}",
                    fontSize = 12.sp,
                    color = Color(0xFF811978),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Primary Health Focus Section
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
                        text = "🎯 Primary Health Focus",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listOf("Glucose Control", "Weight Balance", "Heart Health", "More Energy").forEach { focus ->
                            val isSelected = selectedHealthFocus == focus
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                        shape = RoundedCornerShape(20.dp),
                                    )
                                    .clickable { selectedHealthFocus = focus }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = focus,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Dietary Lifestyle Section (Multi-Select Support)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Dietary Lifestyle",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = "Select Multiple",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB81D85),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf(
                    "Vegetarian" to "🥦",
                    "Non-Vegetarian" to "🍗",
                    "Vegan" to "🌱",
                    "Low-Carb / Keto" to "🥗",
                    "Pescatarian" to "🐟",
                ).forEach { (diet, icon) ->
                    val index = selectedDiets.indexOfFirst { it.first == diet }
                    val isSelected = if (index != -1) selectedDiets[index].second else false

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color.White)
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                if (index != -1) {
                                    selectedDiets[index] = diet to !isSelected
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (isSelected) "✓ $icon" else icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = diet,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFF1E1E22),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Food Allergies & Intolerances
            Text(
                text = "Food Allergies & Intolerances",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                allergiesList.forEachIndexed { index, (name, isSelected) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { allergiesList[index] = name to !isSelected }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = if (isSelected) "✓  $name" else name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF6E6877),
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
                color = Color(0xFF1E1E22),
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
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { selectedActivityLevel = level }
                            .padding(12.dp),
                    ) {
                        Text(
                            text = if (isSelected) "●  $level" else "○  $level",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFB81D85) else Color(0xFF1E1E22),
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
                    .background(Color(0xFFFDEEF7))
                    .padding(12.dp),
            ) {
                Text(
                    text = "Your health preferences guide JARVIS AI to suggest balanced, personalized meal plans tailored for your diabetes management.",
                    fontSize = 12.sp,
                    color = Color(0xFF811978),
                    lineHeight = 17.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Preferences Button
            IosLiquidGlassButton(
                text = "Save Health Preferences",
                onClick = { showSuccessBanner = true },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "General meal and lifestyle ideas, not a medical diet prescription.",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HealthPreferencesScreenPreview() {
    Diawell123Theme {
        HealthPreferencesScreen()
    }
}
