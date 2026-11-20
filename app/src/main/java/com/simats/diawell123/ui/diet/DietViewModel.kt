package com.simats.diawell123.ui.diet
import com.simats.diawell123.ui.theme.AppTheme

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.AiRecommendationData
import com.simats.diawell123.data.model.FoodDetailsData
import com.simats.diawell123.data.model.FoodItem
import com.simats.diawell123.data.model.FoodLogItem
import com.simats.diawell123.data.model.FoodLogRequest
import com.simats.diawell123.data.model.HealthFoodItem
import com.simats.diawell123.data.model.RecommendedFoodItem
import com.simats.diawell123.data.repository.AiFoodRecommendationEngine
import com.simats.diawell123.data.repository.DietRepository
import com.simats.diawell123.data.repository.DietRepositoryImpl
import com.simats.diawell123.data.repository.HealthRecommendationResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DailyRecommendationUiState(
    val recommendation: HealthRecommendationResult? = null,
    val isLoading: Boolean = false,
    val isMealLogging: Boolean = false,
    val shuffleCount: Int = 0,
    val logSuccessMessage: String? = null
)

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
    val meals: List<FoodLogItem> = emptyList(),
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

    private val _dailyRecommendationState = MutableStateFlow(DailyRecommendationUiState())
    val dailyRecommendationState: StateFlow<DailyRecommendationUiState> = _dailyRecommendationState.asStateFlow()

    private val _dailyPlanShuffleOffset = MutableStateFlow(0)
    val dailyPlanShuffleOffset: StateFlow<Int> = _dailyPlanShuffleOffset.asStateFlow()

    fun cycleDailyMealPlan() {
        _dailyPlanShuffleOffset.update { it + 1 }
        loadAiRecommendations()
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("DietViewModel", "Uncaught exception in DietViewModel", throwable)
        val msg = throwable.localizedMessage ?: "Network error loading diet data."
        _dailyDietState.update { it.copy(isLoading = false, errorMessage = msg) }
        _detailsState.update { it.copy(isLoading = false, isLogging = false, errorMessage = msg) }
        _foodLogState.update { it.copy(isLoading = false, errorMessage = msg) }
        _aiRecommendationsState.update { it.copy(isLoading = false, isUpdatingInputs = false, errorMessage = msg) }
    }

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
        viewModelScope.launch(exceptionHandler) {
            val cat = category ?: _dailyDietState.value.selectedCategory
            val g = overrideGlucose ?: _aiRecommendationsState.value.activeGlucose
            val d = overrideDiet ?: _aiRecommendationsState.value.activeDietPreference
            val f = overrideFocus ?: _aiRecommendationsState.value.activeHealthFocus

            _aiRecommendationsState.update {
                it.copy(
                    isLoading = true,
                    activeGlucose = g ?: it.activeGlucose,
                    activeDietPreference = if (!d.isNullOrBlank()) d else it.activeDietPreference,
                    activeHealthFocus = if (!f.isNullOrBlank()) f else it.activeHealthFocus,
                    errorMessage = null
                )
            }

            val result = dietRepository.getAiFoodRecommendations(
                limit = 10,
                category = cat,
                currentGlucose = g,
                dietType = d,
                healthFocus = f
            )
            result.onSuccess { data ->
                val sessionManager = try { SessionManager.getInstance() } catch (_: Exception) { null }
                val foodPrefs = sessionManager?.getCachedFoodPreferences()
                val currentCal = _dailyDietState.value.selectedDateCalendar
                val rotatingPlan = AiFoodRecommendationEngine.getDailyRotatingMealPlan(
                    userDietType = data.patientContext?.dietaryPreference ?: d,
                    userAllergies = foodPrefs?.allergies ?: emptyList(),
                    userFoodsToAvoid = foodPrefs?.foodsToAvoid ?: emptyList(),
                    currentGlucose = data.patientContext?.currentGlucoseMgDl ?: g,
                    calendar = currentCal,
                    shuffleOffset = _dailyPlanShuffleOffset.value
                )
                val finalData = data.copy(dailyPlan = rotatingPlan)

                _aiRecommendationsState.update {
                    it.copy(
                        isLoading = false,
                        data = finalData,
                        activeGlucose = finalData.patientContext?.currentGlucoseMgDl ?: g ?: it.activeGlucose,
                        activeDietPreference = finalData.patientContext?.dietaryPreference ?: d ?: it.activeDietPreference,
                        activeHealthFocus = finalData.patientContext?.healthFocus ?: f ?: it.activeHealthFocus
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
        viewModelScope.launch(exceptionHandler) {
            _aiRecommendationsState.update {
                it.copy(
                    isUpdatingInputs = true,
                    activeGlucose = glucose,
                    activeDietPreference = dietPreference,
                    activeHealthFocus = healthFocus,
                    errorMessage = null
                )
            }
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
            _foodLogState.update { it.copy(isLoading = true) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = date ?: sdf.format(_dailyDietState.value.selectedDateCalendar.time)
            val result = dietRepository.getFoodLog(targetDate)
            result.onSuccess { data ->
                _foodLogState.update { state ->
                    state.copy(
                        isLoading = false,
                        meals = data.meals
                    )
                }
            }.onFailure { error ->
                _foodLogState.update { state ->
                    state.copy(
                        isLoading = false,
                        meals = emptyList(),
                        errorMessage = error.localizedMessage
                    )
                }
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
        loadAiRecommendations()
        loadFoodLog()
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun loadFoodDetails(foodId: Int? = null, dishName: String? = null, category: String? = null) {
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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

    /**
     * Logs any custom food or searched meal directly into the user's food log.
     */
    fun logCustomMeal(
        category: String,
        title: String,
        portion: String,
        calories: Int,
        carbs: Float = 25f,
        protein: Float = 10f,
        fat: Float = 5f,
        icon: String = "🥗",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch(exceptionHandler) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(_dailyDietState.value.selectedDateCalendar.time)
            val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentTimeStr = timeSdf.format(Calendar.getInstance().time)

            val cat = category.uppercase()
            val request = FoodLogRequest(
                mealCategory = cat,
                foodName = title,
                portion = portion,
                calories = calories,
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
                calories = "$calories kcal",
                caloriesNum = calories,
                carbsG = carbs,
                proteinG = protein,
                fatG = fat,
                icon = icon
            )

            val result = dietRepository.logMeal(request)
            result.onSuccess { loggedItem ->
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + loggedItem)
                }
                onSuccess()
            }.onFailure {
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + newLogItem)
                }
                onSuccess()
            }
        }
    }

    /**
     * Refreshes or cycles the AI Daily Rotating Non-Repeating Food Recommendation.
     */
    fun refreshDailyRecommendation(
        context: Context,
        category: String? = null,
        shuffle: Boolean = false
    ) {
        val nextShuffle = if (shuffle) {
            _dailyRecommendationState.value.shuffleCount + 1
        } else {
            _dailyRecommendationState.value.shuffleCount
        }

        val sessionManager = try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            SessionManager.init(context)
        }
        val foodPrefs = sessionManager.getCachedFoodPreferences()
        val user = sessionManager.getUserSession()
        val currentGlucose = _aiRecommendationsState.value.activeGlucose
            ?: user?.currentGlucoseMgDl
            ?: 120f
        val dietType = foodPrefs?.preferredDiet
            ?: _aiRecommendationsState.value.activeDietPreference.ifBlank { "Vegetarian" }
        val allergies = foodPrefs?.allergies ?: emptyList()
        val foodsToAvoid = foodPrefs?.foodsToAvoid ?: emptyList()
        val targetCategory = category ?: _dailyDietState.value.selectedCategory

        val result = AiFoodRecommendationEngine.getDailyRecommendation(
            context = context,
            userDietType = dietType,
            userAllergies = allergies,
            userFoodsToAvoid = foodsToAvoid,
            currentGlucose = currentGlucose,
            targetMealSlot = if (targetCategory.equals("All", ignoreCase = true)) null else targetCategory,
            shuffleOffset = nextShuffle
        )

        _dailyRecommendationState.update {
            it.copy(
                recommendation = result,
                shuffleCount = nextShuffle,
                isLoading = false,
                logSuccessMessage = null
            )
        }
    }

    /**
     * Cycles to the next eligible non-repeating food recommendation on demand ("Try Another").
     */
    fun cycleNextRecommendation(context: Context) {
        refreshDailyRecommendation(context = context, shuffle = true)
    }

    /**
     * Quickly logs the recommended meal item directly into today's diet log.
     */
    fun logRecommendedMeal(
        context: Context,
        item: HealthFoodItem,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch(exceptionHandler) {
            _dailyRecommendationState.update { it.copy(isMealLogging = true) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(_dailyDietState.value.selectedDateCalendar.time)
            val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentTimeStr = timeSdf.format(Calendar.getInstance().time)

            val cat = item.mealSlot.uppercase()
            val request = FoodLogRequest(
                mealCategory = cat,
                foodName = item.name,
                portion = item.portion,
                calories = item.calories,
                carbsG = item.carbsGrams,
                proteinG = item.proteinGrams,
                fatG = item.fatGrams,
                icon = item.icon,
                logDate = dateStr,
                logTime = currentTimeStr
            )

            val newLogItem = FoodLogItem(
                id = (System.currentTimeMillis() % 100000).toInt(),
                category = cat,
                time = currentTimeStr,
                title = item.name,
                portion = item.portion,
                calories = "${item.calories} kcal",
                caloriesNum = item.calories,
                carbsG = item.carbsGrams,
                proteinG = item.proteinGrams,
                fatG = item.fatGrams,
                icon = item.icon
            )

            val result = dietRepository.logMeal(request)
            result.onSuccess { loggedItem ->
                _dailyRecommendationState.update {
                    it.copy(isMealLogging = false, logSuccessMessage = "Logged ${item.name} to today's plan!")
                }
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + loggedItem)
                }
                onSuccess()
            }.onFailure { error ->
                _dailyRecommendationState.update {
                    it.copy(isMealLogging = false, logSuccessMessage = "Logged ${item.name} to today's plan!")
                }
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + newLogItem)
                }
                onSuccess()
            }
        }
    }

    /**
     * Directly logs a snapped meal into today's diet log, updates database via API,
     * updates consumed calories, and triggers local UI refresh.
     */
    fun logSnapFoodMeal(
        dishName: String,
        mealType: String,
        servings: Int,
        calories: Int,
        carbs: Float,
        protein: Float,
        fat: Float,
        subItemsSummary: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch(exceptionHandler) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(_dailyDietState.value.selectedDateCalendar.time)
            val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentTimeStr = timeSdf.format(Calendar.getInstance().time)

            val cat = mealType.uppercase(Locale.ROOT)
            val portionDesc = if (servings == 1) "1 serving" else "$servings servings"

            val request = FoodLogRequest(
                mealCategory = cat,
                foodName = dishName,
                portion = portionDesc,
                calories = calories,
                carbsG = carbs,
                proteinG = protein,
                fatG = fat,
                icon = "📸",
                logDate = dateStr,
                logTime = currentTimeStr
            )

            val newLogItem = FoodLogItem(
                id = (System.currentTimeMillis() % 100000).toInt(),
                category = cat,
                time = currentTimeStr,
                title = dishName,
                portion = portionDesc,
                calories = "$calories kcal",
                caloriesNum = calories,
                carbsG = carbs,
                proteinG = protein,
                fatG = fat,
                icon = "📸"
            )

            val result = dietRepository.logMeal(request)
            result.onSuccess { loggedItem ->
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + loggedItem)
                }
                loadFoodLog()
                onSuccess()
            }.onFailure { error ->
                // Local fallback update so food is added to state log even offline or on API failure
                _foodLogState.update { state ->
                    state.copy(meals = state.meals + newLogItem)
                }
                onSuccess()
            }
        }
    }
}
