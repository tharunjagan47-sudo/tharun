package com.simats.diawell123.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.WeeklyReportData
import com.simats.diawell123.data.repository.ReportsRepository
import com.simats.diawell123.data.repository.ReportsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeeklyReportUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedEndDate: String? = null,
    val reportData: WeeklyReportData? = null,
    val errorMessage: String? = null
)

class ReportsViewModel(
    private val repository: ReportsRepository = ReportsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReportUiState())
    val uiState: StateFlow<WeeklyReportUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyReport()
    }

    fun loadWeeklyReport(endDate: String? = null, isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isPullToRefresh) it.copy(isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = it.reportData == null, selectedEndDate = endDate, errorMessage = null)
            }

            val result = repository.getWeeklyReport(endDate = endDate)
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            reportData = data,
                            selectedEndDate = data.endDate,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Failed to compile weekly report."
                        )
                    }
                }
            )
        }
    }
}

