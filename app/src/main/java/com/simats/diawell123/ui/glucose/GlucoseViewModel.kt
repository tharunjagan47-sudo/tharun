package com.simats.diawell123.ui.glucose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.GlucoseDetailsData
import com.simats.diawell123.data.model.GlucoseHistoryData
import com.simats.diawell123.data.repository.GlucoseRepository
import com.simats.diawell123.data.repository.GlucoseRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class GlucoseHistoryUiState(
    val isLoading: Boolean = false,
    val selectedRange: String = "7 days",
    val historyData: GlucoseHistoryData? = null,
    val errorMessage: String? = null
)

data class LogGlucoseUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val readingValue: String = "120",
    val readingType: String = "Fasting",
    val readingDate: String = "",
    val readingTime: String = "",
    val notes: String = "",
    val errorMessage: String? = null
)

data class ReadingDetailsUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val details: GlucoseDetailsData? = null,
    val errorMessage: String? = null
)

class GlucoseViewModel(
    private val repository: GlucoseRepository = GlucoseRepositoryImpl()
) : ViewModel() {

    private val _historyState = MutableStateFlow(GlucoseHistoryUiState())
    val historyState: StateFlow<GlucoseHistoryUiState> = _historyState.asStateFlow()

    private val _logState = MutableStateFlow(
        LogGlucoseUiState(
            readingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            readingTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
    )
    val logState: StateFlow<LogGlucoseUiState> = _logState.asStateFlow()

    private val _detailsState = MutableStateFlow(ReadingDetailsUiState())
    val detailsState: StateFlow<ReadingDetailsUiState> = _detailsState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory(range: String? = null) {
        val targetRange = range ?: _historyState.value.selectedRange
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true, selectedRange = targetRange, errorMessage = null) }
            val result = repository.getGlucoseHistory(range = targetRange)
            result.onSuccess { data ->
                _historyState.update {
                    it.copy(
                        isLoading = false,
                        historyData = data,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _historyState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load glucose history."
                    )
                }
            }
        }
    }

    fun onRangeSelected(range: String) {
        _historyState.update { it.copy(selectedRange = range) }
        loadHistory(range)
    }

    fun onReadingValueChanged(value: String) {
        _logState.update { it.copy(readingValue = value.filter { ch -> ch.isDigit() || ch == '.' }) }
    }

    fun onReadingTypeChanged(type: String) {
        _logState.update { it.copy(readingType = type) }
    }

    fun onReadingDateChanged(date: String) {
        _logState.update { it.copy(readingDate = date) }
    }

    fun onReadingTimeChanged(time: String) {
        _logState.update { it.copy(readingTime = time) }
    }

    fun onNotesChanged(notes: String) {
        _logState.update { it.copy(notes = notes) }
    }

    fun logGlucose(
        readingValue: Float,
        readingType: String,
        readingDate: String,
        readingTime: String,
        notes: String
    ) {
        if (readingValue < 40f || readingValue > 600f) {
            _logState.update { it.copy(errorMessage = "Please enter a valid reading between 40 and 600 mg/dL.") }
            return
        }

        viewModelScope.launch {
            _logState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            val result = repository.logGlucose(
                readingValue = readingValue,
                readingType = readingType,
                readingDate = readingDate,
                readingTime = readingTime,
                notes = notes
            )
            result.onSuccess {
                _logState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
                // Refresh history with new reading
                loadHistory()
            }.onFailure { error ->
                _logState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = error.localizedMessage ?: "Failed to save reading."
                    )
                }
            }
        }
    }

    fun resetLogState() {
        _logState.update {
            it.copy(
                isLoading = false,
                isSuccess = false,
                errorMessage = null,
                readingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                readingTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            )
        }
    }

    fun loadReadingDetails(readingId: Int? = null) {
        viewModelScope.launch {
            _detailsState.update { it.copy(isLoading = true, isDeleted = false, errorMessage = null) }
            val result = repository.getReadingDetails(readingId = readingId)
            result.onSuccess { data ->
                _detailsState.update {
                    it.copy(
                        isLoading = false,
                        details = data,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _detailsState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load reading details."
                    )
                }
            }
        }
    }

    fun deleteReading(readingId: Int) {
        viewModelScope.launch {
            _detailsState.update { it.copy(isDeleting = true, errorMessage = null) }
            val result = repository.deleteReading(readingId = readingId)
            result.onSuccess {
                _detailsState.update {
                    it.copy(
                        isDeleting = false,
                        isDeleted = true,
                        errorMessage = null
                    )
                }
                // Refresh history after deletion
                loadHistory()
            }.onFailure { error ->
                _detailsState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = error.localizedMessage ?: "Failed to delete reading."
                    )
                }
            }
        }
    }
}

