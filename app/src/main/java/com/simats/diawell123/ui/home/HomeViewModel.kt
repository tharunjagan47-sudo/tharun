package com.simats.diawell123.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.DashboardData
import com.simats.diawell123.data.repository.HomeRepository
import com.simats.diawell123.data.repository.HomeRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
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

    init {
        loadDashboard(isPullToRefresh = false)
    }

    /**
     * Loads or refreshes the home dashboard metrics from the PHP backend.
     */
    fun loadDashboard(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
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
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

