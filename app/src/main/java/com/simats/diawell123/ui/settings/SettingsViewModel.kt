package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ExportRecordsData
import com.simats.diawell123.data.model.NotificationPreferencesData
import com.simats.diawell123.data.repository.SettingsRepository
import com.simats.diawell123.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationPrefsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val medicationReminders: Boolean = true,
    val mealLoggingReminders: Boolean = true,
    val glucoseCheckins: Boolean = true,
    val weeklyReportAlerts: Boolean = true,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class ChangePasswordUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

data class ExportRecordsUiState(
    val isLoading: Boolean = false,
    val selectedRange: String = "all",
    val selectedFormat: String = "json",
    val exportedData: ExportRecordsData? = null,
    val errorMessage: String? = null
)

data class DeleteAccountUiState(
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val repository: SettingsRepository = SettingsRepositoryImpl(),
    private val sessionManager: SessionManager = SessionManager.getInstance()
) : ViewModel() {

    private val _prefsState = MutableStateFlow(NotificationPrefsUiState())
    val prefsState: StateFlow<NotificationPrefsUiState> = _prefsState.asStateFlow()

    private val _passwordState = MutableStateFlow(ChangePasswordUiState())
    val passwordState: StateFlow<ChangePasswordUiState> = _passwordState.asStateFlow()

    private val _exportState = MutableStateFlow(ExportRecordsUiState())
    val exportState: StateFlow<ExportRecordsUiState> = _exportState.asStateFlow()

    private val _deleteState = MutableStateFlow(DeleteAccountUiState())
    val deleteState: StateFlow<DeleteAccountUiState> = _deleteState.asStateFlow()

    init {
        loadNotificationPreferences()
    }

    fun loadNotificationPreferences() {
        viewModelScope.launch {
            _prefsState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getNotificationPreferences()
            result.fold(
                onSuccess = { data ->
                    _prefsState.update {
                        it.copy(
                            isLoading = false,
                            medicationReminders = data.medicationReminders,
                            mealLoggingReminders = data.mealLoggingReminders,
                            glucoseCheckins = data.glucoseCheckins,
                            weeklyReportAlerts = data.weeklyReportAlerts,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _prefsState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
            )
        }
    }

    fun toggleMedicationReminders(enabled: Boolean) {
        _prefsState.update { it.copy(medicationReminders = enabled) }
        saveNotificationPreferences()
    }

    fun toggleMealLogging(enabled: Boolean) {
        _prefsState.update { it.copy(mealLoggingReminders = enabled) }
        saveNotificationPreferences()
    }

    fun toggleMealLoggingReminders(enabled: Boolean) = toggleMealLogging(enabled)

    fun toggleGlucoseCheckins(enabled: Boolean) {
        _prefsState.update { it.copy(glucoseCheckins = enabled) }
        saveNotificationPreferences()
    }

    fun toggleWeeklyReport(enabled: Boolean) {
        _prefsState.update { it.copy(weeklyReportAlerts = enabled) }
        saveNotificationPreferences()
    }

    fun toggleWeeklyReportAlerts(enabled: Boolean) = toggleWeeklyReport(enabled)

    fun saveNotificationPreferences(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val s = _prefsState.value
            _prefsState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = repository.updateNotificationPreferences(
                medicationReminders = s.medicationReminders,
                mealLoggingReminders = s.mealLoggingReminders,
                glucoseCheckins = s.glucoseCheckins,
                weeklyReportAlerts = s.weeklyReportAlerts
            )
            result.fold(
                onSuccess = {
                    _prefsState.update { it.copy(isSaving = false, isSuccess = true) }
                    onSuccess()
                },
                onFailure = { error ->
                    _prefsState.update { it.copy(isSaving = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _passwordState.update { it.copy(isSubmitting = true, errorMessage = null, isSuccess = false) }
            val result = repository.changePassword(currentPassword, newPassword, confirmPassword)
            result.fold(
                onSuccess = { msg ->
                    _passwordState.update { it.copy(isSubmitting = false, isSuccess = true, successMessage = msg) }
                    onSuccess()
                },
                onFailure = { error ->
                    _passwordState.update { it.copy(isSubmitting = false, isSuccess = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun exportRecords(range: String, format: String = "json", onSuccess: (ExportRecordsData) -> Unit = {}) {
        viewModelScope.launch {
            _exportState.update { it.copy(isLoading = true, selectedRange = range, selectedFormat = format, errorMessage = null) }
            val result = repository.exportRecords(range = range, format = format)
            result.fold(
                onSuccess = { data ->
                    _exportState.update { it.copy(isLoading = false, exportedData = data) }
                    onSuccess(data)
                },
                onFailure = { error ->
                    _exportState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun deleteAccount(password: String, reason: String? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _deleteState.update { it.copy(isDeleting = true, errorMessage = null) }
            val result = repository.deleteAccount(password)
            result.fold(
                onSuccess = {
                    sessionManager.clearSession()
                    _deleteState.update { it.copy(isDeleting = false, isDeleted = true) }
                    onSuccess()
                },
                onFailure = { error ->
                    _deleteState.update { it.copy(isDeleting = false, errorMessage = error.message) }
                }
            )
        }
    }
}
