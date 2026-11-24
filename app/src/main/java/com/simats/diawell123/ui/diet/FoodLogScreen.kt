package com.simats.diawell123.ui.diet
import com.simats.diawell123.ui.theme.AppTheme

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.resolveFoodEmoji
import com.simats.diawell123.ui.theme.Diawell123Theme

@Composable
fun FoodLogScreen(
    modifier: Modifier = Modifier,
    viewModel: DietViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddFoodClick: () -> Unit = {},
    onMealDetailsClick: () -> Unit = {},
    onNutritionSummaryClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.foodLogState.collectAsState()

    var showAddFoodDialog by remember { mutableStateOf(false) }
    var foodName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Lunch") }
    var caloriesText by remember { mutableStateOf("") }
    var portionText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadFoodLog()
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
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StyledBackButton(onBackClick = onBackClick)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Food Log",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selector Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 18.sp,
                            color = Color(0xFFA855F7),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.navigateDate(-1) }
                                .padding(horizontal = 4.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = AppTheme.colors.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = uiState.selectedDateLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                            )
                        }
                        Text(
                            text = "›",
                            fontSize = 18.sp,
                            color = Color(0xFFA855F7),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.navigateDate(1) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Nutrition Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
                        .clickable { onNutritionSummaryClick() }
                        .padding(16.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Daily nutrition",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                            )
                            Text(
                                text = "${uiState.totalCaloriesNum} kcal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7E22CE),
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Carbs ${uiState.totalCarbsG} g • Protein ${uiState.totalProteinG} g • Fat ${uiState.totalFatG} g",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Logged Meals List
                if (uiState.meals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No meals logged for this day yet.",
                            fontSize = 13.sp,
                            color = AppTheme.colors.textMuted
                        )
                    }
                } else {
                    uiState.meals.forEach { meal ->
                        FoodLogItem(
                            category = meal.category,
                            time = meal.time,
                            title = meal.title,
                            portion = meal.portion,
                            calories = meal.calories,
                            icon = meal.icon,
                            onClick = onMealDetailsClick,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Add Food Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E1B4B), Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFFF0ABFC)),
                            ),
                        )
                        .clickable { showAddFoodDialog = true }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+  Add Food",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Add Food Dialog with Quick Entry & Database Search Option
        if (showAddFoodDialog) {
            Dialog(onDismissRequest = { showAddFoodDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Add Meal to Log",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = AppTheme.colors.textMuted,
                                modifier = Modifier
                                    .clickable { showAddFoodDialog = false }
                                    .padding(4.dp)
                                    .size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Breakfast", "Lunch", "Snack", "Dinner").forEach { cat ->
                                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFFA855F7) else AppTheme.colors.secondaryBackground)
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else AppTheme.colors.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Food / Dish Name Field
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            placeholder = { Text("Dish name (e.g. Oatmeal, Dal, Dosa)", fontSize = 12.5.sp, color = AppTheme.colors.textMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary,
                                cursorColor = Color(0xFFA855F7),
                                focusedContainerColor = AppTheme.colors.inputBackground,
                                unfocusedContainerColor = AppTheme.colors.inputBackground,
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = AppTheme.colors.inputBorder,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Portion & Calories Fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = portionText,
                                onValueChange = { portionText = it },
                                placeholder = { Text("Portion (1 bowl)", fontSize = 12.sp, color = AppTheme.colors.textMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = AppTheme.colors.textPrimary,
                                    unfocusedTextColor = AppTheme.colors.textPrimary,
                                    cursorColor = Color(0xFFA855F7),
                                    focusedContainerColor = AppTheme.colors.inputBackground,
                                    unfocusedContainerColor = AppTheme.colors.inputBackground,
                                    focusedBorderColor = Color(0xFFA855F7),
                                    unfocusedBorderColor = AppTheme.colors.inputBorder,
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = caloriesText,
                                onValueChange = { caloriesText = it },
                                placeholder = { Text("Calories (250)", fontSize = 12.sp, color = AppTheme.colors.textMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = AppTheme.colors.textPrimary,
                                    unfocusedTextColor = AppTheme.colors.textPrimary,
                                    cursorColor = Color(0xFFA855F7),
                                    focusedContainerColor = AppTheme.colors.inputBackground,
                                    unfocusedContainerColor = AppTheme.colors.inputBackground,
                                    focusedBorderColor = Color(0xFFA855F7),
                                    unfocusedBorderColor = AppTheme.colors.inputBorder,
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Log Food Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFF00E5FF))
                                    )
                                )
                                .clickable {
                                    val name = foodName.trim()
                                    if (name.isBlank()) {
                                        Toast.makeText(context, "Please enter a food name", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }
                                    val calInt = caloriesText.toIntOrNull() ?: 250
                                    val portion = portionText.ifBlank { "1 serving" }
                                    viewModel.logCustomMeal(
                                        category = selectedCategory,
                                        title = name,
                                        portion = portion,
                                        calories = calInt,
                                        onSuccess = {
                                            Toast.makeText(context, "Added $name to your food log!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    foodName = ""
                                    caloriesText = ""
                                    portionText = ""
                                    showAddFoodDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save to Food Log",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Or Explore Food Database Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.secondaryBackground)
                                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    showAddFoodDialog = false
                                    onAddFoodClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Search Food Database",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA855F7)
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodLogItem(
    category: String,
    time: String,
    title: String,
    portion: String,
    calories: String,
    icon: String,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = resolveFoodEmoji(icon, title), fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA855F7),
                            letterSpacing = 0.5.sp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = time,
                            fontSize = 10.sp,
                            color = AppTheme.colors.textMuted,
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        softWrap = true,
                        lineHeight = 18.sp,
                    )
                    Text(
                        text = portion,
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary,
                        softWrap = true,
                    )
                }
            }

            Text(
                text = calories,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7E22CE),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoodLogScreenPreview() {
    Diawell123Theme {
        FoodLogScreen()
    }
}
