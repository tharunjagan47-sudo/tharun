package com.simats.diawell123.ui.diet
import com.simats.diawell123.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.model.RecommendedFoodItem
import com.simats.diawell123.data.model.resolveFoodEmoji
import com.simats.diawell123.data.model.SnapFoodAnalysis
import com.simats.diawell123.data.repository.HealthRecommendationResult
import com.simats.diawell123.data.repository.SnapFoodAnalyzer
import com.simats.diawell123.ui.components.ElementPopUpContainer
import com.simats.diawell123.ui.components.aiRotatingBorder
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
    initialOpenSnapFood: Boolean = false,
    onSnapFoodConsumed: () -> Unit = {},
) {
    val uiState by viewModel.dailyDietState.collectAsState()
    val aiState by viewModel.aiRecommendationsState.collectAsState()
    val dailyRecState by viewModel.dailyRecommendationState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf("Diet") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAdjustInputsDialog by remember { mutableStateOf(false) }
    var showSnapFoodCamera by remember { mutableStateOf(false) }
    var isAnalyzingFood by remember { mutableStateOf(false) }
    var activeSnapFoodAnalysis by remember { mutableStateOf<SnapFoodAnalysis?>(null) }

    LaunchedEffect(initialOpenSnapFood) {
        if (initialOpenSnapFood) {
            showSnapFoodCamera = true
            onSnapFoodConsumed()
        }
    }

    LaunchedEffect(uiState.selectedCategory) {
        viewModel.refreshDailyRecommendation(context, category = uiState.selectedCategory)
    }

    LaunchedEffect(dailyRecState.logSuccessMessage) {
        dailyRecState.logSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

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
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Bar with Inline Date Switcher & Sleek Action Pills
                ElementPopUpContainer(delayMillis = 0) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                StyledBackButton(onBackClick = onBackClick)

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Diet",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // Compact Inline Date Selector Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AppTheme.colors.cardBackground)
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "‹",
                                    fontSize = 18.sp,
                                    color = Color(0xFFA855F7),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.navigateDate(-1) }
                                        .padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showDatePickerDialog = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarToday,
                                        contentDescription = "Select Date",
                                        tint = Color(0xFFA855F7),
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = uiState.selectedDateLabel,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.colors.textPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                }
                                Text(
                                    text = "›",
                                    fontSize = 18.sp,
                                    color = Color(0xFFA855F7),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.navigateDate(1) }
                                        .padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Primary Snap Food Action
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFF00E5FF)))
                                    )
                                    .clickable {
                                        showSnapFoodCamera = true
                                        coroutineScope.launch { SnapFoodAnalyzer.preWarmModel() }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Snap Food",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                                    .clickable { onFoodLogClick() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Food Log",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(0.95f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF2EFF6))
                                    .border(1.dp, AppTheme.colors.border, RoundedCornerShape(14.dp))
                                    .clickable { onExploreFoodClick() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = if (AppTheme.colors.isDark) AppTheme.colors.primaryAccent else Color(0xFF5B1761),
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Explore",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (AppTheme.colors.isDark) AppTheme.colors.primaryAccent else Color(0xFF5B1761),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Summarized Health & Glucose Context Strip
                        ActiveHealthMatchCard(
                            glucose = currentGlucose,
                            glucoseStatus = glucoseStatus,
                            dietPref = currentDietPref,
                            healthFocus = currentFocus,
                            diabetesType = diabetesType,
                            hypoglycemiaAlert = aiState.data?.hypoglycemiaAlert,
                            onAdjustClick = { showAdjustInputsDialog = true },
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clean Meal Slot Filter Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        ) {
                            listOf("All", "Breakfast", "Lunch", "Snack", "Dinner").forEach { category ->
                                val isSelected = uiState.selectedCategory.equals(category, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFA855F7) else AppTheme.colors.cardBackground)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFA855F7) else AppTheme.colors.border,
                                            shape = CircleShape,
                                        )
                                        .clickable { viewModel.onDailyDietCategoryChanged(category) }
                                        .padding(horizontal = 15.dp, vertical = 7.dp),
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prominent Dataset-Driven Daily Rotating AI Food Recommendation Card (with Rotating AI Glowing Border)
                dailyRecState.recommendation?.let { rec ->
                    ElementPopUpContainer(delayMillis = 40) {
                        AiFoodRecommendationForHealthCard(
                            result = rec,
                            isLogging = dailyRecState.isMealLogging,
                            onTryAnotherClick = {
                                viewModel.cycleNextRecommendation(context)
                            },
                            onLogMealClick = {
                                viewModel.logRecommendedMeal(context, rec.item)
                            },
                            onItemClick = {
                                onMealDetailsClick(rec.item.mealSlot.lowercase(), rec.item.id, rec.item.name)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Clean Daily Meal Plan Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Today's Meal Plan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (aiState.isLoading || aiState.isUpdatingInputs) {
                            CircularProgressIndicator(
                                color = Color(0xFFA855F7),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (AppTheme.colors.isDark) Color(0xFF2E1065) else Color(0xFFF3E8FF))
                                .clickable { viewModel.cycleDailyMealPlan() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = null,
                                    tint = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Shuffle",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Recommended Foods List (Clean, Uncluttered Sections)
                val dailyPlan = aiState.data?.dailyPlan
                val recommendations = aiState.data?.recommendations ?: emptyList()
                val isAllCategory = uiState.selectedCategory.equals("All", ignoreCase = true)

                if (isAllCategory && dailyPlan != null &&
                    (dailyPlan.breakfast.isNotEmpty() || dailyPlan.lunch.isNotEmpty() || dailyPlan.dinner.isNotEmpty())) {

                    if (dailyPlan.breakfast.isNotEmpty()) {
                        MealTimeSectionHeader(title = "Breakfast", subtitle = "Steady morning energy")
                        Spacer(modifier = Modifier.height(6.dp))
                        dailyPlan.breakfast.take(2).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("breakfast", item.id, item.title) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (dailyPlan.lunch.isNotEmpty()) {
                        MealTimeSectionHeader(title = "Lunch", subtitle = "Balanced midday macros")
                        Spacer(modifier = Modifier.height(6.dp))
                        dailyPlan.lunch.take(2).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("lunch", item.id, item.title) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (dailyPlan.snack.isNotEmpty()) {
                        MealTimeSectionHeader(title = "Snack", subtitle = "Fibre-rich afternoon bite")
                        Spacer(modifier = Modifier.height(6.dp))
                        dailyPlan.snack.take(1).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("snack", item.id, item.title) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (dailyPlan.dinner.isNotEmpty()) {
                        MealTimeSectionHeader(title = "Dinner", subtitle = "Light overnight support")
                        Spacer(modifier = Modifier.height(6.dp))
                        dailyPlan.dinner.take(2).forEach { item ->
                            AiDietMealCard(item = item, onClick = { onMealDetailsClick("dinner", item.id, item.title) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                } else {
                    // Category-Filtered Recommendations
                    val filtered = recommendations

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
                                color = AppTheme.colors.textSecondary,
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
                    color = AppTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 100.dp),
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

        // Snap Food Camera & Framing Overlay
        if (showSnapFoodCamera) {
            SnapFoodCameraOverlay(
                onDismiss = { showSnapFoodCamera = false },
                onPhotoCaptured = { bitmap, uri, query ->
                    showSnapFoodCamera = false
                    activeSnapFoodAnalysis = null
                    isAnalyzingFood = true
                    coroutineScope.launch {
                        try {
                            val analysis = SnapFoodAnalyzer.analyzeFood(
                                context = context,
                                bitmap = bitmap,
                                imageUri = uri,
                                query = query,
                                userGlucose = aiState.activeGlucose
                            )
                            activeSnapFoodAnalysis = analysis
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to analyze photo right now. Please try again.", Toast.LENGTH_SHORT).show()
                        } finally {
                            isAnalyzingFood = false
                        }
                    }
                }
            )
        }

        // Vision AI Loading / Analyzing Dialog
        if (isAnalyzingFood) {
            SnapFoodAnalyzingDialog()
        }

        // Meal Nutrition Breakdown Sheet with Macro Readout, Stepper, Sub-items, and Done Action
        activeSnapFoodAnalysis?.let { analysis ->
            MealNutritionBreakdownSheet(
                analysis = analysis,
                userGlucose = aiState.activeGlucose,
                onDismiss = { activeSnapFoodAnalysis = null },
                onLogMeal = { dishName, mealType, servings, calories, carbs, protein, fat, fiber, subSummary ->
                    viewModel.logSnapFoodMeal(
                        dishName = dishName,
                        mealType = mealType,
                        servings = servings,
                        calories = calories,
                        carbs = carbs,
                        protein = protein,
                        fat = fat,
                        subItemsSummary = subSummary,
                        onSuccess = {
                            Toast.makeText(context, "Logged $dishName ($calories kcal) to today's diet log!", Toast.LENGTH_LONG).show()
                        }
                    )
                    activeSnapFoodAnalysis = null
                }
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
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = AppTheme.colors.textMuted,
        )
    }
}

@Composable
fun ActiveHealthMatchCard(
    glucose: Float,
    glucoseStatus: String,
    dietPref: String,
    healthFocus: String,
    diabetesType: String,
    hypoglycemiaAlert: String? = null,
    onAdjustClick: () -> Unit,
) {
    val (statusText, statusColor, statusBg) = when (glucoseStatus) {
        "hypoglycemia" -> Triple("Low (<70)", AppTheme.colors.errorText, AppTheme.colors.errorBg)
        "high" -> Triple("High (>180)", AppTheme.colors.errorText, AppTheme.colors.errorBg)
        "elevated" -> Triple("Elevated", AppTheme.colors.warningText, AppTheme.colors.warningBg)
        else -> Triple("In Target", AppTheme.colors.successText, AppTheme.colors.successBg)
    }

    val summarizedPrefs = remember(dietPref, healthFocus, diabetesType) {
        listOf(
            dietPref.split(",").firstOrNull()?.trim().orEmpty(),
            healthFocus.split(",").firstOrNull()?.trim().orEmpty(),
            diabetesType.trim(),
        ).filter { it.isNotEmpty() }.joinToString(" • ")
    }

    val conciseInsight = when {
        !hypoglycemiaAlert.isNullOrBlank() -> hypoglycemiaAlert
        glucoseStatus == "high" -> "High sugar: prioritizing low-carb (<10g), high-fibre meals."
        glucoseStatus == "elevated" -> "Slightly elevated: favoring low-GI & lean protein pairings."
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "${glucose.toInt()} mg/dL",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
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
                        if (summarizedPrefs.isNotEmpty()) {
                            Text(
                                text = summarizedPrefs,
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.secondaryBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(10.dp))
                        .clickable { onAdjustClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "Adjust",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                        )
                    }
                }
            }

            if (conciseInsight != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg.copy(alpha = 0.72f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = if (glucoseStatus == "high" || glucoseStatus == "hypoglycemia") {
                            Icons.Rounded.Shield
                        } else {
                            Icons.Rounded.InsertChart
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = conciseInsight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp,
                    )
                }
            }
        }
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
    var glucoseInput by remember(currentGlucose) { mutableStateOf(currentGlucose.toInt().toString()) }

    // Multi-select dietary preferences
    val initialDiets = remember(currentDiet) {
        val list = currentDiet.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (list.isNotEmpty()) list.toSet() else setOf("Vegetarian")
    }
    var selectedDiets by remember(initialDiets) { mutableStateOf(initialDiets) }

    // Multi-select health focuses
    val initialFocuses = remember(currentFocus) {
        val list = currentFocus.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (list.isNotEmpty()) list.toSet() else setOf("Glucose Control")
    }
    var selectedFocuses by remember(initialFocuses) { mutableStateOf(initialFocuses) }

    val dietOptions = listOf("Vegetarian", "Non-Vegetarian", "Vegan", "Low-Carb / Keto", "Eggetarian")
    val focusOptions = listOf("Glucose Control", "Weight Management", "Heart Health", "Active Energy", "Immunity & Wellness")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.cardBackground)
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = "Adjust Health & Glucose Inputs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = "Meal recommendations dynamically adapt to these parameters.",
                    fontSize = 11.sp,
                    color = AppTheme.colors.textSecondary,
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Glucose Level Field
                Text(
                    text = "Current Blood Glucose (mg/dL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = glucoseInput,
                    onValueChange = { glucoseInput = it.filter { ch -> ch.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick glucose preset chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("65" to "Low", "110" to "Target", "155" to "Elevated", "195" to "High").forEach { (valStr, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (glucoseInput == valStr) Color(0xFFA855F7) else AppTheme.colors.secondaryBackground)
                                .clickable { glucoseInput = valStr }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "$valStr ($label)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (glucoseInput == valStr) Color.White else AppTheme.colors.textSecondary,
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
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Multiple selection",
                        fontSize = 10.sp,
                        color = Color(0xFFA855F7),
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
                                        Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF7E22CE), Color(0xFFA855F7)))
                                    } else {
                                        Brush.horizontalGradient(listOf(AppTheme.colors.secondaryBackground, AppTheme.colors.secondaryBackground))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFA855F7) else AppTheme.colors.border,
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
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = diet,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
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
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Multiple selection",
                        fontSize = 10.sp,
                        color = Color(0xFFA855F7),
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
                                        Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF7E22CE), Color(0xFFA855F7)))
                                    } else {
                                        Brush.horizontalGradient(listOf(AppTheme.colors.secondaryBackground, AppTheme.colors.secondaryBackground))
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFA855F7) else AppTheme.colors.border,
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
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = focus,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else AppTheme.colors.textSecondary,
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
                            .background(AppTheme.colors.secondaryBackground)
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF7E22CE), Color(0xFFA855F7)),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(text = "Update Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiDietMealCard(
    item: RecommendedFoodItem,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = resolveFoodEmoji(item.icon, item.title), fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.portion} • ${item.caloriesLabel} • ${item.carbsG}g carbs",
                    fontSize = 11.sp,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val badgeText = item.glycemicImpact?.badge ?: item.highlights.firstOrNull()
                if (!badgeText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item.glycemicImpact?.badge?.let { giBadge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppTheme.colors.successBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = giBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.successText,
                                )
                            }
                        }
                        item.highlights.firstOrNull()?.let { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppTheme.colors.secondaryBackground)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (AppTheme.colors.isDark) Color(0xFF2E1065) else Color(0xFFF3E8FF))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${item.recommendationScore}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF6D28D9),
                )
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
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.cardBackground)
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = resolveFoodEmoji(icon, name), fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = serving,
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = macros,
                        fontSize = 10.sp,
                        color = AppTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "›",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textMuted,
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
                .background(AppTheme.colors.cardBackground)
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Select Diet Date",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = "Currently viewing $selectedDateText",
                    fontSize = 12.sp,
                    color = AppTheme.colors.textSecondary,
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
                                .background(if (AppTheme.colors.isDark) AppTheme.colors.elevatedSurface else Color(0xFFF5F3FF))
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
                                color = if (AppTheme.colors.isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.secondaryBackground)
                        .clickable { onDismissRequest() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Close",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

/**
 * Prominent AI Food Recommendation For Your Health Card.
 * Features the animated rotating gradient border ("line suthu varamari")
 * to visually indicate active AI-driven recommendation.
 */
@Composable
fun AiFoodRecommendationForHealthCard(
    result: HealthRecommendationResult,
    isLogging: Boolean,
    onTryAnotherClick: () -> Unit,
    onLogMealClick: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = result.item
    val isDark = AppTheme.colors.isDark

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF1E1435), Color(0xFF140F24))
                    } else {
                        listOf(Color(0xFFFAF5FF), Color(0xFFFFFFFF))
                    }
                )
            )
            .aiRotatingBorder(strokeWidth = 1.5.dp, shape = RoundedCornerShape(18.dp), durationMillis = 3600)
            .padding(14.dp),
    ) {
        Column {
            // Compact Top Header: Top Pick badge + Meal slot & GL pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFF3E8FF) else Color(0xFF6D28D9),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Top Pick • ${item.mealSlot}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF3E8FF) else Color(0xFF6D28D9),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                result.glucoseBadgeText.contains("High") -> AppTheme.colors.errorBg
                                result.glucoseBadgeText.contains("Stabilizer") -> AppTheme.colors.warningBg
                                else -> AppTheme.colors.successBg
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "${item.glycemicLoad} GL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            result.glucoseBadgeText.contains("High") -> AppTheme.colors.errorText
                            result.glucoseBadgeText.contains("Stabilizer") -> AppTheme.colors.warningText
                            else -> AppTheme.colors.successText
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clean Dish Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(Color(0xFF3B1D66), Color(0xFF241542))
                                } else {
                                    listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = resolveFoodEmoji(item.icon, item.name), fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.portion} • ${item.calories} kcal • ${item.dietType}",
                        fontSize = 11.5.sp,
                        color = Color(0xFFA855F7),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Single Summarized Macro & Insight Line
            Text(
                text = "Carbs ${item.carbsGrams.toInt()}g • Protein ${item.proteinGrams.toInt()}g • Fiber ${item.fiberGrams.toInt()}g • Fat ${item.fatGrams.toInt()}g",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (result.matchedReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = result.matchedReason,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = AppTheme.colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compact Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(AppTheme.colors.cardBackground)
                        .border(1.dp, AppTheme.colors.border, RoundedCornerShape(11.dp))
                        .clickable { onTryAnotherClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFF3E8FF) else Color(0xFF7E22CE),
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Swap",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF3E8FF) else Color(0xFF7E22CE),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF7E22CE), Color(0xFFA855F7), Color(0xFF00E5FF))
                            )
                        )
                        .clickable(enabled = !isLogging) { onLogMealClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLogging) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(15.dp),
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Log Meal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
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
