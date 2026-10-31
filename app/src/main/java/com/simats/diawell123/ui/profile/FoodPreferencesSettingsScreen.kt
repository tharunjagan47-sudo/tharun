package com.simats.diawell123.ui.profile
import com.simats.diawell123.ui.theme.AppTheme

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.diet.IosLiquidGlassButton
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodPreferencesSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaveFoodPreferencesClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedDiet by remember { mutableStateOf("Non-Vegetarian") }
    var foodsToAvoid by remember { mutableStateOf("") }
    var showSuccessBanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFoodPreferences()
    }

    LaunchedEffect(uiState.foodPreferences) {
        uiState.foodPreferences?.let { fp ->
            selectedDiet = fp.preferredDiet
            foodsToAvoid = fp.foodsToAvoid.joinToString(", ")
        }
    }

    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner) {
            delay(1200)
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
                        text = "Food Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Settings",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
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
                                text = "Your diet preferences have been updated in your profile.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }
                }
            }

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
                        text = uiState.errorMessage ?: "Failed to save food preferences.",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Text(
                text = "Profile • ${ProfileState.userName}",
                fontSize = 11.sp,
                color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Preferred Diet Section
            Text(
                text = "Preferred diet",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
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
                    color = AppTheme.colors.textPrimary,
                )

                Text(
                    text = "+ Common allergies",
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
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else AppTheme.colors.secondaryBackground)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { allergies[index] = name to !isSelected }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = if (isSelected) "✓  $name" else name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textSecondary,
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
                color = AppTheme.colors.textPrimary,
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
                            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else AppTheme.colors.secondaryBackground)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable { preferredStyles[index] = name to !isSelected }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = if (isSelected) "✓  $name" else name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Foods to avoid (optional)
            Text(
                text = "Foods to avoid (comma-separated)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = foodsToAvoid,
                onValueChange = { foodsToAvoid = it },
                placeholder = {
                    Text(
                        text = "e.g. Shellfish, Raw onions, Grapefruit",
                        fontSize = 13.sp,
                        color = AppTheme.colors.textMuted,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    cursorColor = Color(0xFFBD1E85),
                    focusedContainerColor = AppTheme.colors.inputBackground,
                    unfocusedContainerColor = AppTheme.colors.inputBackground,
                    focusedBorderColor = Color(0xFFBD1E85),
                    unfocusedBorderColor = AppTheme.colors.inputBorder,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Info Callout Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFFDEEF7))
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = "Meal ideas, shaped around you",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) Color(0xFFF472B6) else Color(0xFF811978),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DiaWell AI uses your preferences for general nutrition guidance, not medical advice. Check for allergies and ingredients independently; suggestions do not guarantee allergy safety.",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary,
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Food Preferences Button
            IosLiquidGlassButton(
                text = if (uiState.isSaving) "Saving Preferences..." else "Save Food Preferences",
                onClick = {
                    val allergiesSelected = allergies.filter { it.second }.map { it.first }
                    val stylesSelected = preferredStyles.filter { it.second }.map { it.first }

                    viewModel.saveFoodPreferences(
                        preferredDiet = selectedDiet,
                        allergies = allergiesSelected,
                        preferredStyles = stylesSelected,
                        foodsToAvoid = foodsToAvoid,
                        onSuccess = {
                            showSuccessBanner = true
                        }
                    )
                },
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
            .background(if (isSelected) (if (AppTheme.colors.isDark) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFFFDEEF7)) else AppTheme.colors.secondaryBackground)
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.border,
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
                color = if (isSelected) Color(0xFFB81D85) else AppTheme.colors.textPrimary,
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
