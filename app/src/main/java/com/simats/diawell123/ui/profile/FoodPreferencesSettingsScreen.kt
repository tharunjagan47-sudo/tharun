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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun FoodPreferencesSettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveFoodPreferencesClick: () -> Unit = {},
) {
    var selectedDiet by remember { mutableStateOf("Non-Vegetarian") }
    var foodsToAvoid by remember { mutableStateOf("") }
    var showSuccessBanner by remember { mutableStateOf(value = false) }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1000)
            showSuccessBanner = false
            onSaveFoodPreferencesClick()
        }
    }

    val allergies = remember {
        mutableStateListOf("Nuts" to true, "Milk" to false, "Dairy" to false, "Gluten" to false, "Eggs" to false, "Soy" to false)
    }

    val preferredStyles = remember {
        mutableStateListOf("Whole grains" to true, "Vegetables" to true, "Fish" to true, "Poultry" to true)
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
                        text = "Food Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Settings",
                        fontSize = 12.sp,
                        color = Color(0xFF6E6877),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1-Second Success Confirmation Banner
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
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
                                text = "Food Preferences Saved Successfully!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                            )
                            Text(
                                text = "Your diet preferences have been updated.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

            Text(
                text = "Demo profile • Maya Johnson",
                fontSize = 11.sp,
                color = Color(0xFF811978),
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Preferred Diet Section
            Text(
                text = "Preferred diet",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DietTypeOptionCard(
                    title = "Vegetarian",
                    iconText = "🥦",
                    isSelected = selectedDiet == "Vegetarian",
                    onClick = { selectedDiet = "Vegetarian" },
                    modifier = Modifier.weight(1f),
                )
                DietTypeOptionCard(
                    title = "Non-Vegetarian",
                    iconText = "🍗",
                    isSelected = selectedDiet == "Non-Vegetarian",
                    onClick = { selectedDiet = "Non-Vegetarian" },
                    modifier = Modifier.weight(1f),
                )
                DietTypeOptionCard(
                    title = "Vegan",
                    iconText = "🌱",
                    isSelected = selectedDiet == "Vegan",
                    onClick = { selectedDiet = "Vegan" },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Food Allergies Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Food allergies",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )

                Text(
                    text = "+ Add allergy",
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
                allergies.forEachIndexed { index, (name, isSelected) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { allergies[index] = name to !isSelected }
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

            // Preferred foods and meal styles
            Text(
                text = "Preferred foods and meal styles",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                preferredStyles.forEachIndexed { index, (name, isSelected) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { preferredStyles[index] = name to !isSelected }
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

            // Foods to avoid (optional)
            Text(
                text = "Foods to avoid (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = foodsToAvoid,
                onValueChange = { foodsToAvoid = it },
                placeholder = {
                    Text(
                        text = "Add foods you prefer not to eat",
                        fontSize = 13.sp,
                        color = Color(0xFFA09BAA),
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F7FA),
                    unfocusedContainerColor = Color(0xFFF8F7FA),
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = Color(0xFFEFECEF),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Info Callout Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "Meal ideas, shaped around you",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI uses your preferences for general nutrition guidance, not medical advice. Check for allergies and ingredients independently; suggestions do not guarantee allergy safety.",
                        fontSize = 11.sp,
                        color = Color(0xFF6E6877),
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Food Preferences Button
            IosLiquidGlassButton(
                text = "Save Food Preferences",
                onClick = { showSuccessBanner = true },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DietTypeOptionCard(
    title: String,
    iconText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFDEEF7) else Color(0xFFF8F7FA))
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color(0xFFB81D85) else Color(0xFFEFECEF),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = iconText, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFB81D85) else Color(0xFF1E1E22),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodPreferencesSettingsScreenPreview() {
    Diawell123Theme {
        FoodPreferencesSettingsScreen()
    }
}
