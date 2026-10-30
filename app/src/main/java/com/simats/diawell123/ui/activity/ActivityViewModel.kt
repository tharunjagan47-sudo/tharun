package com.simats.diawell123.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.ActivityHistoryData
import com.simats.diawell123.data.model.ActivityHistoryItem
import com.simats.diawell123.data.model.ActivityHistorySummary
import com.simats.diawell123.data.model.ActivityRecommendationItem
import com.simats.diawell123.data.model.ActivityUserContext
import com.simats.diawell123.data.repository.ActivityRepository
import com.simats.diawell123.data.repository.ActivityRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityTrackingUiState(
    val isLoading: Boolean = false,
    val selectedActivity: String = "Walking",
    val recommendations: List<ActivityRecommendationItem> = emptyList(),
    val userContext: ActivityUserContext? = null,
    val safetyGuidance: String? = null,
    val errorMessage: String? = null,
    val isLoggingWorkout: Boolean = false,
    val isWorkoutLogged: Boolean = false,
    val showCompletedBanner: Boolean = false
) {
    val activeRecommendation: ActivityRecommendationItem?
        get() = recommendations.find { it.activity.equals(selectedActivity, ignoreCase = true) }
            ?: recommendations.firstOrNull()
}

data class ActivityHistoryUiState(
    val isLoading: Boolean = false,
    val selectedFilter: String = "All",
    val summary: ActivityHistorySummary = ActivityHistorySummary(),
    val historyRecords: List<ActivityHistoryItem> = emptyList(),
    val errorMessage: String? = null
)

class ActivityViewModel(
    private val repository: ActivityRepository = ActivityRepositoryImpl()
) : ViewModel() {

    private val _trackingState = MutableStateFlow(ActivityTrackingUiState())
    val trackingState: StateFlow<ActivityTrackingUiState> = _trackingState.asStateFlow()

    private val _historyState = MutableStateFlow(ActivityHistoryUiState())
    val historyState: StateFlow<ActivityHistoryUiState> = _historyState.asStateFlow()

    init {
        loadRecommendations()
        loadHistory()
    }

    /**
     * Load personalized activity recommendations from Python AI service through PHP backend.
     */
    fun loadRecommendations(currentGlucose: Float? = null) {
        viewModelScope.launch {
            _trackingState.update { it.copy(isLoading = it.recommendations.isEmpty(), errorMessage = null) }

            val result = repository.getActivityRecommendations(currentGlucose = currentGlucose)
            result.fold(
                onSuccess = { data ->
                    val recs = data.recommendations
                    val defaultSelected = if (recs.any { it.activity.equals(_trackingState.value.selectedActivity, ignoreCase = true) }) {
                        _trackingState.value.selectedActivity
                    } else {
                        recs.firstOrNull()?.activity ?: "Walking"
                    }

                    _trackingState.update {
                        it.copy(
                            isLoading = false,
                            selectedActivity = defaultSelected,
                            recommendations = recs,
                            userContext = data.userContext,
                            safetyGuidance = data.safetyGuidance,
                            errorMessage = if (recs.isEmpty() && data.message != null) data.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _trackingState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load personalized activity recommendations."
                        )
                    }
                }
            )
        }
    }

    fun selectActivity(activityType: String) {
        _trackingState.update { it.copy(selectedActivity = activityType) }
    }

    /**
     * Log completed workout to PHP backend and MySQL activity_logs.
     */
    fun logCompletedWorkout(
        activityType: String,
        durationMins: Int,
        distanceKm: Float,
        caloriesKcal: Int,
        glucoseBefore: Float? = null,
        glucoseAfter: Float? = null,
        notes: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _trackingState.update { it.copy(isLoggingWorkout = true) }

            val result = repository.logActivity(
                activityType = activityType,
                durationMins = durationMins,
                distanceKm = distanceKm,
                caloriesKcal = caloriesKcal,
                glucoseBefore = glucoseBefore,
                glucoseAfter = glucoseAfter,
                notes = notes
            )

            result.fold(
                onSuccess = {
                    _trackingState.update {
                        it.copy(
                            isLoggingWorkout = false,
                            isWorkoutLogged = true,
                            showCompletedBanner = true
                        )
                    }
                    loadHistory() // Refresh history logs
                    loadRecommendations() // Refresh recommendations with updated progression
                    onSuccess()
                },
                onFailure = { error ->
                    _trackingState.update {
                        it.copy(
                            isLoggingWorkout = false,
                            errorMessage = error.message ?: "Failed to log workout."
                        )
                    }
                }
            )
        }
    }

    /**
     * Load activity workout history.
     */
    fun loadHistory(filter: String? = null) {
        viewModelScope.launch {
            val targetFilter = filter ?: _historyState.value.selectedFilter
            _historyState.update { it.copy(isLoading = it.historyRecords.isEmpty(), selectedFilter = targetFilter, errorMessage = null) }

            val result = repository.getActivityHistory(filter = targetFilter)
            result.fold(
                onSuccess = { data ->
                    _historyState.update {
                        it.copy(
                            isLoading = false,
                            summary = data.summary,
                            historyRecords = data.workouts,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _historyState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load activity history."
                        )
                    }
                }
            )
        }
    }

    fun dismissCompletedBanner() {
        _trackingState.update { it.copy(showCompletedBanner = false) }
    }
}

