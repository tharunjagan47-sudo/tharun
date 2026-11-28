package com.simats.diawell123.ui.home
import com.simats.diawell123.ui.theme.AppTheme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.DiawellApplication
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.DashboardData
import com.simats.diawell123.data.model.UpdateProfileRequest
import com.simats.diawell123.data.repository.HomeRepository
import com.simats.diawell123.data.repository.HomeRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.util.MedicationStatusUpdater
import com.simats.diawell123.util.toCleanErrorMessage
import com.simats.diawell123.widget.DiaWellWidgetDataManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dashboardData: DashboardData? = null,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: HomeRepository = HomeRepositoryImpl(),
    private val sessionManager: SessionManager = SessionManager.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("HomeViewModel", "Uncaught coroutine exception: ${throwable.message}", throwable)
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = throwable.toCleanErrorMessage("Unable to refresh dashboard. Pull down to retry.")
            )
        }
    }

    init {
        if (sessionManager.isLoggedIn() && sessionManager.getUserId() != null) {
            loadDashboard(isPullToRefresh = false)
        }
        viewModelScope.launch(exceptionHandler) {
            try {
                MedicationStatusUpdater.events.collect {
                    if (sessionManager.isLoggedIn() && sessionManager.getUserId() != null) {
                        loadDashboard(isPullToRefresh = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Medication status observer exception", e)
            }
        }
    }

    /**
     * Loads or refreshes the home dashboard metrics from the PHP backend.
     */
    fun loadDashboard(isPullToRefresh: Boolean = false) {
        val requestUserId = sessionManager.getUserId()
        if (!sessionManager.isLoggedIn() || requestUserId == null) {
            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            return
        }
        viewModelScope.launch(exceptionHandler) {
            try {
                _uiState.update {
                    if (isPullToRefresh) {
                        it.copy(isRefreshing = true, errorMessage = null)
                    } else {
                        it.copy(isLoading = it.dashboardData == null, errorMessage = null)
                    }
                }

                val result = repository.getDashboardData()
                result.fold(
                    onSuccess = { data ->
                        if (sessionManager.getUserId() != requestUserId) {
                            return@fold
                        }
                        ProfileState.syncFromServer(
                            userId = data.userId,
                            fullName = data.profile?.name,
                            profilePicBase64 = data.profile?.profilePic,
                            profilePhotoUrl = data.profile?.photoUrl
                        )

                        // Sync latest dashboard health metrics to Home Screen Widget
                        try {
                            DiaWellWidgetDataManager.syncFromDashboard(DiawellApplication.instance, data)
                        } catch (_: Exception) {}

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                dashboardData = data,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = error.toCleanErrorMessage("Unable to refresh dashboard. Pull down to retry.")
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception loading dashboard", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.toCleanErrorMessage("Unable to refresh dashboard. Pull down to retry.")
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Updates patient's fasting and post-meal glucose targets directly from Home screen.
     */
    fun updateTargetGlucose(
        fastingTarget: Int,
        postmealTarget: Int = 140,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val currentName = _uiState.value.dashboardData?.profile?.name?.takeIf { it.isNotBlank() } ?: ProfileState.userName
                val currentEmail = ProfileState.userEmail
                val currentDiabetes = _uiState.value.dashboardData?.profile?.diabetesType ?: "Type 2"
                val request = UpdateProfileRequest(
                    fullName = currentName,
                    email = currentEmail,
                    diabetesType = currentDiabetes,
                    targetFastingGlucose = fastingTarget,
                    targetPostmealGlucose = postmealTarget
                )
                val response = ApiClient.profileApiService.updateProfile(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadDashboard(isPullToRefresh = true)
                    onSuccess()
                } else {
                    val msg = response.body()?.message ?: "Failed to update target."
                    onError(msg)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to update target glucose", e)
                onError(e.localizedMessage ?: "Network error updating target.")
            }
        }
    }
}

