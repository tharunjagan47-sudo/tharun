package com.simats.diawell123.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.AiRecommendationData
import com.simats.diawell123.data.model.FoodDetailsData
import com.simats.diawell123.data.model.RecommendedFoodItem
import com.simats.diawell123.data.model.FoodItem
import com.simats.diawell123.data.model.FoodLogItem
import com.simats.diawell123.data.model.FoodLogRequest
import com.simats.diawell123.data.repository.DietRepository
import com.simats.diawell123.data.repository.DietRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AiRecommendationUiState(
    val isLoading: Boolean = false,
    val isUpdatingInputs: Boolean = false,
    val data: AiRecommendationData? = null,
    val activeGlucose: Float? = null,
    val activeDietPreference: String = "Vegetarian",
    val activeHealthFocus: String = "Glucose Control",
    val errorMessage: String? = null
)

data class DailyDietUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedDateCalendar: Calendar = Calendar.getInstance(),
    val selectedDateLabel: String = "Today",
    val meals: List<FoodItem> = emptyList(),
    val errorMessage: String? = null
) {
    val filteredMeals: List<FoodItem>
        get() = meals.filter { item ->
            val matchesCategory = selectedCategory.equals("All", ignoreCase = true) ||
                item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
}

data class FoodDetailsUiState(
    val isLoading: Boolean = false,
    val isLogging: Boolean = false,
    val details: FoodDetailsData? = null,
    val errorMessage: String? = null
)

data class FoodLogUiState(
    val isLoading: Boolean = false,
    val selectedDateCalendar: Calendar = Calendar.getInstance(),
    val selectedDateLabel: String = "Today",
    val meals: List<FoodLogItem> = listOf(
        FoodLogItem(id = 1, category = "BREAKFAST", time = "8:30 AM", title = "Berry & almond oats", portion = "1 bowl • 290 g", calories = "320 kcal", caloriesNum = 320, carbsG = 42f, proteinG = 9f, fatG = 8f, icon = "🥣"),
        FoodLogItem(id = 2, category = "LUNCH", time = "1:00 PM", title = "Grilled chicken bowl", portion = "1 bowl • 350 g", calories = "450 kcal", caloriesNum = 450, carbsG = 48f, proteinG = 38f, fatG = 12f, icon = "🥗"),
        FoodLogItem(id = 3, category = "SNACKS", time = "4:30 PM", title = "Greek yogurt & berries", portion = "1 cup • 170 g", calories = "160 kcal", caloriesNum = 160, carbsG = 18f, proteinG = 12f, fatG = 4f, icon = "🫐"),
        FoodLogItem(id = 4, category = "DINNER", time = "7:30 PM", title = "Salmon & brown rice", portion = "1 plate • 380 g", calories = "480 kcal", caloriesNum = 480, carbsG = 52f, proteinG = 32f, fatG = 14f, icon = "🍱")
    ),
    val errorMessage: String? = null
) {
    val totalCaloriesNum: Int get() = meals.sumOf { it.caloriesNum }
    val totalCarbsG: Int get() = meals.sumOf { it.carbsG.toDouble() }.toInt()
    val totalProteinG: Int get() = meals.sumOf { it.proteinG.toDouble() }.toInt()
    val totalFatG: Int get() = meals.sumOf { it.fatG.toDouble() }.toInt()
}

class DietViewModel(
    private val dietRepository: DietRepository = DietRepositoryImpl()
) : ViewModel() {

    private val _dailyDietState = MutableStateFlow(DailyDietUiState())
    val dailyDietState: StateFlow<DailyDietUiState> = _dailyDietState.asStateFlow()

    private val _detailsState = MutableStateFlow(FoodDetailsUiState())
    val detailsState: StateFlow<FoodDetailsUiState> = _detailsState.asStateFlow()

    private val _foodLogState = MutableStateFlow(FoodLogUiState())
    val foodLogState: StateFlow<FoodLogUiState> = _foodLogState.asStateFlow()

    private val _aiRecommendationsState = MutableStateFlow(AiRecommendationUiState())
    val aiRecommendationsState: StateFlow<AiRecommendationUiState> = _aiRecommendationsState.asStateFlow()

    init {
        loadExploreFoods()

        loadFoodLog()
        loadAiRecommendations()
    }

    fun loadAiRecommendations(
        category: String? = null,
        overrideGlucose: Float? = null,
        overrideDiet: String? = null,
        overrideFocus: String? = null
    ) {
        viewModelScope.launch {
            _aiRecommendationsState.update { it.copy(isLoading = true, errorMessage = null) }
            val cat = category ?: _dailyDietState.value.selectedCategory
            val g = overrideGlucose ?: _aiRecommendationsState.value.activeGlucose
            val d = overrideDiet ?: _aiRecommendationsState.value.activeDietPreference
            val f = overrideFocus ?: _aiRecommendationsState.value.activeHealthFocus

            val result = dietRepository.getAiFoodRecommendations(
                limit = 10,
                category = cat,
                currentGlucose = g,
                dietType = d,
                healthFocus = f
            )
            result.onSuccess { data ->
                _aiRecommendationsState.update {
                    it.copy(
                        isLoading = false,
                        data = data,
                        activeGlucose = data.patientContext?.currentGlucoseMgDl ?: it.activeGlucose,
                        activeDietPreference = data.patientContext?.dietaryPreference ?: it.activeDietPreference,
                        activeHealthFocus = data.patientContext?.healthFocus ?: it.activeHealthFocus
                    )
                }
            }.onFailure { error ->
                _aiRecommendationsState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage
                    )
                }
            }
        }
    }

    fun updateHealthInputs(glucose: Float, dietPreference: String, healthFocus: String) {
        viewModelScope.launch {
            _aiRecommendationsState.update { it.copy(isUpdatingInputs = true, errorMessage = null) }
            val result = dietRepository.updateHealthInputsAndRecommend(
                glucose = glucose,
                dietPreference = dietPreference,
                healthFocus = healthFocus
            )
            result.onSuccess { data ->
                _aiRecommendationsState.update {
                    it.copy(
                        isUpdatingInputs = false,
                        data = data,
                        activeGlucose = glucose,
                        activeDietPreference = dietPreference,
                        activeHealthFocus = healthFocus
                    )
                }
                loadExploreFoods()
            }.onFailure { error ->
                _aiRecommendationsState.update {
                    it.copy(
                        isUpdatingInputs = false,
                        errorMessage = error.localizedMessage
                    )
                }
                loadAiRecommendations(
                    overrideGlucose = glucose,
                    overrideDiet = dietPreference,
                    overrideFocus = healthFocus
                )
            }
        }
    }

    fun loadExploreFoods() {
        viewModelScope.launch {
            _dailyDietState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = dietRepository.exploreFoods(
                query = _dailyDietState.value.searchQuery,
                category = _dailyDietState.value.selectedCategory
            )
            result.onSuccess { data ->
                _dailyDietState.update {
                    it.copy(
                        isLoading = false,
                        meals = data.foods
                    )
                }
            }.onFailure { error ->
                _dailyDietState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage
                    )
                }
            }
        }
    }

    fun loadFoodLog(date: String? = null) {
        viewModelScope.launch {
            _foodLogState.update { it.copy(isLoading = true) }
            val result = dietRepository.getFoodLog(date)
            result.onSuccess { data ->
                if (data.meals.isNotEmpty()) {
                    _foodLogState.update { state ->
                        state.copy(
                            isLoading = false,
                            meals = data.meals
                        )
                    }
                } else {
                    _foodLogState.update { it.copy(isLoading = false) }
                }
            }.onFailure {
                _foodLogState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDailyDietSearchChanged(query: String) {
        _dailyDietState.update { it.copy(searchQuery = query) }
        loadExploreFoods()

    }

    fun onDailyDietCategoryChanged(category: String) {
        _dailyDietState.update { it.copy(selectedCategory = category) }
        loadAiRecommendations(category)
        loadExploreFoods()

    }

    fun navigateDate(days: Int) {
        val newCal = (_dailyDietState.value.selectedDateCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, days)
        }
        updateSelectedDate(newCal)
    }

    fun setSpecificDate(year: Int, month: Int, day: Int) {
        val newCal = Calendar.getInstance().apply {
            set(year, month, day)
        }
        updateSelectedDate(newCal)
    }

    private fun updateSelectedDate(calendar: Calendar) {
        val today = Calendar.getInstance()
        val label = when {
            isSameDay(calendar, today) -> "Today"
            isSameDay(calendar, (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }) -> "Yesterday"
            isSameDay(calendar, (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }) -> "Tomorrow"
            else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(calendar.time)
        }
        _dailyDietState.update {
            it.copy(
                selectedDateCalendar = calendar,
                selectedDateLabel = label
            )
        }
        _foodLogState.update {
            it.copy(
                selectedDateCalendar = calendar,
                selectedDateLabel = label
            )
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun loadFoodDetails(foodId: Int? = null, dishName: String? = null, category: String? = null) {
        viewModelScope.launch {
            _detailsState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = dietRepository.getFoodDetails(foodId = foodId, dishName = dishName, category = category)
            result.onSuccess { data ->
                _detailsState.update { it.copy(isLoading = false, details = data) }
            }.onFailure { error ->
                _detailsState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
            }
        }
    }

    fun addCurrentFoodToLog(
        categoryFallback: String,
        titleFallback: String,
        portionFallback: String,
        caloriesFallback: Int,
        carbsFallback: Float,
        proteinFallback: Float,
        fatFallback: Float,
        iconFallback: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _detailsState.update { it.copy(isLogging = true) }
            val details = _detailsState.value.details
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(_dailyDietState.value.selectedDateCalendar.time)
            val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentTimeStr = timeSdf.format(Calendar.getInstance().time)

            val cat = (details?.category ?: categoryFallback).uppercase()
            val title = details?.title ?: titleFallback
            val portion = details?.portion ?: portionFallback
            val cal = details?.calories ?: caloriesFallback
            val carbs = details?.macros?.carbsG ?: carbsFallback
            val protein = details?.macros?.proteinG ?: proteinFallback
            val fat = details?.macros?.fatG ?: fatFallback
            val icon = details?.icon ?: iconFallback

            val request = FoodLogRequest(
                mealCategory = cat,
                foodName = title,
                portion = portion,
                calories = cal,
                carbsG = carbs,
                proteinG = protein,
                fatG = fat,
                icon = icon,
                logDate = dateStr,
                logTime = currentTimeStr
            )

            val newLogItem = FoodLogItem(
                id = (System.currentTimeMillis() % 100000).toInt(),
                category = cat,
                time = currentTimeStr,
                title = title,
                portion = portion,
                calories = "$cal kcal",
                caloriesNum = cal,
                carbsG = carbs,
                proteinG = protein,
                fatG = fat,
                icon = icon
            )

            val result = dietRepository.logMeal(request)
            result.onSuccess { loggedItem ->
                _detailsState.update { it.copy(isLogging = false) }
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + loggedItem)
                }
                onSuccess()
            }.onFailure { error ->
                _detailsState.update { it.copy(isLogging = false, errorMessage = error.localizedMessage) }
                // Local fallback update so food is added to state log even offline or on API failure
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + newLogItem)
                }
                onSuccess()
            }
        }
    }
}
