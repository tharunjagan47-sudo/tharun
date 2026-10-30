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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.RecommendedFoodItem
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.theme.Diawell123Theme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyDietPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: DietViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onMealDetailsClick: (category: String, foodId: Int?, dishName: String?) -> Unit = { _, _, _ -> },
    onFoodLogClick: () -> Unit = {},
    onExploreFoodClick: () -> Unit = {},
    onHomeTabClick: () -> Unit = {},
    onDietTabClick: () -> Unit = {},
    onMedsTabClick: () -> Unit = {},
    onReportsTabClick: () -> Unit = {},
    onSettingsTabClick: () -> Unit = {},
) {
    val uiState by viewModel.dailyDietState.collectAsState()
    val aiState by viewModel.aiRecommendationsState.collectAsState()

    var selectedTab by remember { mutableStateOf("Diet") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAdjustInputsDialog by remember { mutableStateOf(false) }

    val patientContext = aiState.data?.patientContext
    val currentGlucose = aiState.activeGlucose ?: patientContext?.currentGlucoseMgDl ?: 120f
    val currentDietPref = aiState.activeDietPreference.ifBlank { patientContext?.dietaryPreference ?: "Vegetarian" }
    val currentFocus = aiState.activeHealthFocus.ifBlank { patientContext?.healthFocus ?: "Glucose Control" }
    val diabetesType = patientContext?.diabetesType ?: "Type 2"
    val glucoseStatus = patientContext?.glucoseStatus ?: when {
        currentGlucose < 70f -> "hypoglycemia"
        currentGlucose > 180f -> "high"
        currentGlucose >= 140f -> "elevated"
        else -> "normal"
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
                .padding(bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar & Action Shortcuts
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StyledBackButton(onBackClick = onBackClick)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Diet & Nutrition",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                )
                                Text(
                                    text = "AI-matched Indian foods for your health",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6E6877),
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFDEEF7))
                                        .clickable { onFoodLogClick() }
                                        .padding(horizontal = 9.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = "📖 Log",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF811978),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF2EFF6))
                                        .clickable { onExploreFoodClick() }
                                        .padding(horizontal = 9.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = "🔍 Explore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5B1761),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Patient Health & Glucose Match Banner
                        ActiveHealthMatchCard(
                            glucose = currentGlucose,
                            glucoseStatus = glucoseStatus,
                            dietPref = currentDietPref,
                            healthFocus = currentFocus,
                            diabetesType = diabetesType,
                            onAdjustClick = { showAdjustInputsDialog = true }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Unified Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onDailyDietSearchChanged(it) },
                            placeholder = {
                                Text(
                                    text = "🔍 Search foods, recipes or ingredients...",
                                    fontSize = 13.sp,
                                    color = Color(0xFFA09BAA),
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFFBD1E85),
                                unfocusedBorderColor = Color(0xFFEFECEF),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Filter Pills
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
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF6E6877),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                                Text(
                                    text = "‹",
                                    fontSize = 20.sp,
                                    color = Color(0xFFB81D85),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.navigateDate(-1) }
                                        .padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = "📅  ${uiState.selectedDateLabel}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E22),
                                    modifier = Modifier.clickable { showDatePickerDialog = true }
                                )
                                Text(
                                    text = "›",
                                    fontSize = 20.sp,
                                    color = Color(0xFFB81D85),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.navigateDate(1) }
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Glucose Level Clinical Explanation Banner
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
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (glucoseStatus == "high") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFE53935).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hyperglycemia Stabilization Active (${currentGlucose.toInt()} mg/dL)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                )
                                Text(
                                    text = "AI prioritized ultra-low carbohydrate Indian dishes (<10g) with high dietary fibre to safely stabilize your blood sugar.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 15.sp,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (glucoseStatus == "elevated") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3E0))
                            .border(1.dp, Color(0xFFFB8C00).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Mildly Elevated Glucose (${currentGlucose.toInt()} mg/dL)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                )
                                Text(
                                    text = "AI selected low-glycemic, high-fibre dishes with satiating lean protein to slow glucose release.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 15.sp,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // AI Suggested Daily Meal Plan Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "✨ AI Recommended for Your Health",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E22),
                    )

                    if (aiState.isLoading || aiState.isUpdatingInputs) {
                        CircularProgressIndicator(
                            color = Color(0xFFB81D85),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Recommended Foods List
                val dailyPlan = aiState.data?.dailyPlan
                val recommendations = aiState.data?.recommendations ?: emptyList()
                val isAllCategory = uiState.selectedCategory.equals("All", ignoreCase = true)

                if (isAllCategory && uiState.searchQuery.isBlank() && dailyPlan != null && 
                    (dailyPlan.breakfast.isNotEmpty() || dailyPlan.lunch.isNotEmpty() || dailyPlan.dinner.isNotEmpty())) {

                    // Structured Complete Day Plan: Breakfast, Lunch, Snack, Dinner
                    if (dailyPlan.breakfast.isNotEmpty()) {
                        MealTimeSectionHeader(title = "🌅 Breakfast", subtitle = "Gentle morning glycemic release")
                        Spacer(modifier = Modifier.height(8.dp))
                        dailyPlan.breakfast.take(2).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("breakfast", item.id, item.title) })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (dailyPlan.lunch.isNotEmpty()) {
                        MealTimeSectionHeader(title = "☀️ Lunch", subtitle = "Sustained afternoon energy & protein")
                        Spacer(modifier = Modifier.height(8.dp))
                        dailyPlan.lunch.take(2).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("lunch", item.id, item.title) })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (dailyPlan.snack.isNotEmpty()) {
                        MealTimeSectionHeader(title = "☕ Afternoon Snack", subtitle = "High fibre to prevent evening spikes")
                        Spacer(modifier = Modifier.height(8.dp))
                        dailyPlan.snack.take(1).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("snack", item.id, item.title) })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (dailyPlan.dinner.isNotEmpty()) {
                        MealTimeSectionHeader(title = "🌙 Dinner", subtitle = "Light, nutrient-dense overnight support")
                        Spacer(modifier = Modifier.height(8.dp))
                        dailyPlan.dinner.take(2).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("dinner", item.id, item.title) })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                } else {
                    // Category-Filtered or Search-Filtered Recommendations
                    val filtered = if (uiState.searchQuery.isBlank()) {
                        recommendations
                    } else {
                        recommendations.filter {
                            it.title.contains(uiState.searchQuery, ignoreCase = true) ||
                            it.category.contains(uiState.searchQuery, ignoreCase = true)
                        }
                    }

                    if (filtered.isNotEmpty()) {
                        filtered.forEachIndexed { index, rec ->
                            ElementPopUpContainer(delayMillis = 40 * (index + 1)) {
                                AiDietMealCard(
                                    item = rec,
                                    onClick = { onMealDetailsClick(rec.category.lowercase(), rec.id, rec.title) },
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else if (uiState.filteredMeals.isNotEmpty()) {
                        uiState.filteredMeals.forEachIndexed { index, meal ->
                            ElementPopUpContainer(delayMillis = 40 * (index + 1)) {
                                DietPlanMealCard(
                                    category = meal.category.uppercase(),
                                    name = meal.title,
                                    serving = "${meal.portion} • ${meal.calories}",
                                    macros = "C:${meal.carbsG.toInt()}g • P:${meal.proteinG.toInt()}g • F:${meal.fatG.toInt()}g • GI: ${meal.glycemicIndex}",
                                    icon = meal.icon,
                                    onClick = { onMealDetailsClick(meal.category.lowercase(), meal.id, meal.title) },
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Medical Disclaimer
                Text(
                    text = aiState.data?.disclaimer
                        ?: "Medical Disclaimer: Suggestions are calculated from nutritional dataset values and personal health context. Always consult your physician or registered dietitian.",
                    fontSize = 10.sp,
                    color = Color(0xFF8A8595),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                )
            }
        }

        // Adjust Health Inputs Dialog Modal
        if (showAdjustInputsDialog) {
            AdjustHealthInputsDialog(
                currentGlucose = currentGlucose,
                currentDiet = currentDietPref,
                currentFocus = currentFocus,
                onDismiss = { showAdjustInputsDialog = false },
                onSave = { newGlucose, newDiet, newFocus ->
                    viewModel.updateHealthInputs(newGlucose, newDiet, newFocus)
                    showAdjustInputsDialog = false
                }
            )
        }

        // Calendar Date Picker Modal Dialog
        if (showDatePickerDialog) {
            CalendarDatePickerDialog(
                selectedDateText = uiState.selectedDateLabel,
                onDateSelected = { year, month, day ->
                    viewModel.setSpecificDate(year, month, day)
                },
                onDismissRequest = { showDatePickerDialog = false },
            )
        }

        // Floating iOS Liquid Glass Bottom Navigation Dock
        ElementPopUpContainer(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            IosLiquidGlassBottomNav(
                selectedTab = selectedTab,
                onHomeTabClick = onHomeTabClick,
                onDietTabClick = { selectedTab = "Diet" },
                onMedsTabClick = onMedsTabClick,
                onReportsTabClick = onReportsTabClick,
                onSettingsTabClick = onSettingsTabClick,
            )
        }
    }
}

@Composable
fun MealTimeSectionHeader(
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E22),
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF6E6877),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveHealthMatchCard(
    glucose: Float,
    glucoseStatus: String,
    dietPref: String,
    healthFocus: String,
    diabetesType: String,
    onAdjustClick: () -> Unit,
) {
    val (statusText, statusColor, statusBg) = when (glucoseStatus) {
        "hypoglycemia" -> Triple("Hypoglycemia (<70)", Color(0xFFC62828), Color(0xFFFFEBEE))
        "high" -> Triple("High Glucose (>180)", Color(0xFFC62828), Color(0xFFFFEBEE))
        "elevated" -> Triple("Elevated (140-180)", Color(0xFFE65100), Color(0xFFFFF3E0))
        else -> Triple("In Target (70-139)", Color(0xFF2E7D32), Color(0xFFE8F5E9))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEFECEF), RoundedCornerShape(16.dp))
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDEEF7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🩸", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Blood Glucose: ${glucose.toInt()} mg/dL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E22),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8F7FA))
                        .border(1.dp, Color(0xFFBD1E85).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onAdjustClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "⚙️ Adjust",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF811978),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                dietPref.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { diet ->
                    HealthTagPill(
                        icon = if (diet.contains("Non", ignoreCase = true)) "🍗" else "🥗",
                        text = diet
                    )
                }
                healthFocus.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { focus ->
                    HealthTagPill(icon = "🎯", text = focus)
                }
                HealthTagPill(icon = "🩺", text = diabetesType)
            }
        }
    }
}

@Composable
fun HealthTagPill(icon: String, text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8F7FA))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "$icon $text",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5E5A66),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdjustHealthInputsDialog(
    currentGlucose: Float,
    currentDiet: String,
    currentFocus: String,
    onDismiss: () -> Unit,
    onSave: (glucose: Float, diet: String, focus: String) -> Unit,
) {
    var glucoseInput by remember { mutableStateOf(currentGlucose.toInt().toString()) }

    // Multi-select dietary preferences
    val initialDiets = remember(currentDiet) {
        val list = currentDiet.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (list.isNotEmpty()) list.toSet() else setOf("Vegetarian")
    }
    var selectedDiets by remember { mutableStateOf(initialDiets) }

    // Multi-select health focuses
    val initialFocuses = remember(currentFocus) {
        val list = currentFocus.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (list.isNotEmpty()) list.toSet() else setOf("Glucose Control")
    }
    var selectedFocuses by remember { mutableStateOf(initialFocuses) }

    val dietOptions = listOf("Vegetarian", "Non-Vegetarian", "Vegan", "Low-Carb / Keto", "Eggetarian")
    val focusOptions = listOf("Glucose Control", "Weight Management", "Heart Health", "Active Energy", "Immunity & Wellness")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = "Adjust Health & Glucose Inputs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = "AI recommendations dynamically adapt to these parameters.",
                    fontSize = 11.sp,
                    color = Color(0xFF6E6877),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Glucose Level Field
                Text(
                    text = "Current Blood Glucose (mg/dL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1E22),
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = glucoseInput,
                    onValueChange = { glucoseInput = it.filter { ch -> ch.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F7FA),
                        unfocusedContainerColor = Color(0xFFF8F7FA),
                        focusedBorderColor = Color(0xFFBD1E85),
                        unfocusedBorderColor = Color(0xFFEFECEF),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick glucose preset chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("65" to "Low", "110" to "Target", "155" to "Elevated", "195" to "High").forEach { (valStr, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (glucoseInput == valStr) Color(0xFFBD1E85) else Color(0xFFF8F7FA))
                                .clickable { glucoseInput = valStr }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "$valStr ($label)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (glucoseInput == valStr) Color.White else Color(0xFF6E6877),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dietary Preference Chips (Multi-Select)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Dietary Preference",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Multiple selection",
                        fontSize = 10.sp,
                        color = Color(0xFFBD1E85),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    dietOptions.forEach { diet ->
                        val isSelected = selectedDiets.contains(diet)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(listOf(Color(0xFF811978), Color(0xFFBD1E85)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color(0xFFF8F7FA), Color(0xFFF8F7FA)))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFBD1E85) else Color(0xFFEFECEF),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedDiets = if (isSelected) {
                                        if (selectedDiets.size > 1) selectedDiets - diet else selectedDiets
                                    } else {
                                        selectedDiets + diet
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Text(
                                        text = "✓ ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = diet,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF6E6877),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Health Focus Chips (Multi-Select)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Health Focus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E22),
                    )
                    Text(
                        text = "Multiple selection",
                        fontSize = 10.sp,
                        color = Color(0xFFBD1E85),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    focusOptions.forEach { focus ->
                        val isSelected = selectedFocuses.contains(focus)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(listOf(Color(0xFF6B156E), Color(0xFF9E1B80)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color(0xFFF8F7FA), Color(0xFFF8F7FA)))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFBD1E85) else Color(0xFFEFECEF),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedFocuses = if (isSelected) {
                                        if (selectedFocuses.size > 1) selectedFocuses - focus else selectedFocuses
                                    } else {
                                        selectedFocuses + focus
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Text(
                                        text = "✓ ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = focus,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF6E6877),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Update Plan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F7FA))
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6E6877))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF6B156E), Color(0xFFB81D85)),
                                ),
                            )
                            .clickable {
                                val gFloat = glucoseInput.toFloatOrNull() ?: currentGlucose
                                val dietString = if (selectedDiets.isNotEmpty()) selectedDiets.joinToString(", ") else "Vegetarian"
                                val focusString = if (selectedFocuses.isNotEmpty()) selectedFocuses.joinToString(", ") else "Glucose Control"
                                onSave(gFloat, dietString, focusString)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "✓ Update Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiDietMealCard(
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDEEF7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = item.icon, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
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
                        Text(
                            text = "${item.portion} • ${item.caloriesLabel}",
                            fontSize = 11.sp,
                            color = Color(0xFF6E6877),
                        )
                    }
                }

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

            Text(
                text = "Carbs ${item.carbsG}g • Fibre ${item.fibreG}g • Protein ${item.proteinG}g • Sugar ${item.freeSugarG}g",
                fontSize = 11.sp,
                color = Color(0xFF8A8595),
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

            if (item.highlights.isNotEmpty() || item.glycemicImpact != null) {
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
fun DietPlanMealCard(
    category: String,
    name: String,
    serving: String,
    macros: String,
    icon: String,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(14.dp))
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
                        fontSize = 14.sp,
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
                text = "View ›",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB81D85),
            )
        }
    }
}

@Composable
fun CalendarDatePickerDialog(
    selectedDateText: String,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Select Diet Date",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E22),
                )
                Text(
                    text = "Currently viewing $selectedDateText",
                    fontSize = 12.sp,
                    color = Color(0xFF6E6877),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf("Yesterday" to -1, "Today" to 0, "Tomorrow" to 1).forEach { (label, offset) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDEEF7))
                                .clickable {
                                    val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, offset) }
                                    onDateSelected(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH))
                                    onDismissRequest()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF811978),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8F7FA))
                        .clickable { onDismissRequest() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Close",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6E6877),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DailyDietPlanScreenPreview() {
    Diawell123Theme {
        DailyDietPlanScreen()
    }
}
