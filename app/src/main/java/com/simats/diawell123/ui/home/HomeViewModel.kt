package com.simats.diawell123.ui.home
import com.simats.diawell123.ui.theme.AppTheme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.DashboardData
import com.simats.diawell123.data.repository.HomeRepository
import com.simats.diawell123.data.repository.HomeRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.util.MedicationStatusUpdater
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
    private val repository: HomeRepository = HomeRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("HomeViewModel", "Uncaught coroutine exception: ${throwable.message}", throwable)
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = throwable.localizedMessage ?: "Unable to connect to service. Please retry."
            )
        }
    }

    init {
        loadDashboard(isPullToRefresh = false)
        viewModelScope.launch(exceptionHandler) {
            try {
                MedicationStatusUpdater.events.collect {
                    loadDashboard(isPullToRefresh = true)
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
                        // Synchronize user profile name if present
                        data.profile?.name?.let { name ->
                            if (name.isNotBlank()) {
                                ProfileState.userName = name
                            }
                        }

                        // Synchronize user profile photo if present and not yet loaded
                        data.profile?.photoUrl?.let { url ->
                            if (url.isNotBlank() && ProfileState.profileImageBitmap == null) {
                                ProfileState.loadPhotoFromUrl(url, data.userId)
                            }
                        }

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
                                errorMessage = error.message ?: "Failed to load dashboard data."
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
                        errorMessage = e.localizedMessage ?: "Network error loading dashboard."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

