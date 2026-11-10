package com.simats.diawell123.ui.settings
import com.simats.diawell123.ui.theme.AppTheme

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ExportRecordsData
import com.simats.diawell123.data.model.ExportReportOptions
import com.simats.diawell123.data.model.NotificationPreferencesData
import com.simats.diawell123.data.repository.SettingsRepository
import com.simats.diawell123.data.repository.SettingsRepositoryImpl
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.util.ClinicalReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class NotificationPrefsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val medicationReminders: Boolean = true,
    val mealLoggingReminders: Boolean = true,
    val glucoseCheckins: Boolean = true,
    val weeklyReportAlerts: Boolean = true,
    val reminderSound: String = "Gentle Chime",
    val vibration: Boolean = true,
    val quietHours: String = "10:00 PM - 7:00 AM",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class PrivacySecurityUiState(
    val appLock: Boolean = false,
    val biometricUnlock: Boolean = false,
    val dataAiPrefs: Boolean = true,
)

data class ChangePasswordUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

data class ExportRecordsUiState(
    val isLoading: Boolean = false,
    val selectedRange: String = "30",
    val selectedRangeLabel: String = "Last 30 Days",
    val selectedFormat: String = "PDF",
    val options: ExportReportOptions = ExportReportOptions(),
    val exportedData: ExportRecordsData? = null,
    val generatedFile: File? = null,
    val generatedFileName: String? = null,
    val generatedMimeType: String = "application/pdf",
    val previewBitmaps: List<Bitmap> = emptyList(),
    val csvPreviewText: String? = null,
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

    private val _prefsState = MutableStateFlow(
        NotificationPrefsUiState(
            reminderSound = sessionManager.getReminderSound(),
            vibration = sessionManager.isVibrationEnabled(),
            quietHours = sessionManager.getQuietHours()
        )
    )
    val prefsState: StateFlow<NotificationPrefsUiState> = _prefsState.asStateFlow()

    private val _privacyState = MutableStateFlow(
        PrivacySecurityUiState(
            appLock = sessionManager.isAppLockEnabled(),
            biometricUnlock = sessionManager.isBiometricUnlockEnabled(),
            dataAiPrefs = sessionManager.isDataAiPrefsEnabled()
        )
    )
    val privacyState: StateFlow<PrivacySecurityUiState> = _privacyState.asStateFlow()

    private val _passwordState = MutableStateFlow(ChangePasswordUiState())
    val passwordState: StateFlow<ChangePasswordUiState> = _passwordState.asStateFlow()

    private val _exportState = MutableStateFlow(ExportRecordsUiState())
    val exportState: StateFlow<ExportRecordsUiState> = _exportState.asStateFlow()

    private val _deleteState = MutableStateFlow(DeleteAccountUiState())
    val deleteState: StateFlow<DeleteAccountUiState> = _deleteState.asStateFlow()

    init {
        loadNotificationPreferences()
    }

    fun toggleAppLock(enabled: Boolean) {
        sessionManager.setAppLockEnabled(enabled)
        _privacyState.update { it.copy(appLock = enabled) }
    }

    fun toggleBiometricUnlock(enabled: Boolean) {
        sessionManager.setBiometricUnlockEnabled(enabled)
        _privacyState.update { it.copy(biometricUnlock = enabled) }
    }

    fun toggleDataAiPrefs(enabled: Boolean) {
        sessionManager.setDataAiPrefsEnabled(enabled)
        _privacyState.update { it.copy(dataAiPrefs = enabled) }
    }

    fun setReminderSound(sound: String) {
        sessionManager.setReminderSound(sound)
        _prefsState.update { it.copy(reminderSound = sound) }
    }

    fun toggleVibration(enabled: Boolean) {
        sessionManager.setVibrationEnabled(enabled)
        _prefsState.update { it.copy(vibration = enabled) }
    }

    fun setQuietHours(hours: String) {
        sessionManager.setQuietHours(hours)
        _prefsState.update { it.copy(quietHours = hours) }
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

    fun generateAndExportReport(
        context: Context,
        options: ExportReportOptions,
        onSuccess: () -> Unit = {}
    ) {
        if (!options.includeGlucose && !options.includeMeals && !options.includeMedications && !options.includeActivity) {
            _exportState.update { it.copy(errorMessage = "Select at least one data section to include.") }
            return
        }

        viewModelScope.launch {
            _exportState.update {
                it.copy(
                    isLoading = true,
                    selectedRange = options.daysParam,
                    selectedRangeLabel = options.timeRangeLabel,
                    selectedFormat = options.format,
                    options = options,
                    errorMessage = null
                )
            }

            val result = repository.exportRecords(range = options.daysParam, format = "json")
            result.fold(
                onSuccess = { rawData ->
                    val sessionUser = sessionManager.getUserSession()
                    val mergedPatient = rawData.patient.copy(
                        name = rawData.patient.name.ifBlank { sessionUser?.fullName?.ifBlank { ProfileState.userName } ?: ProfileState.userName },
                        email = rawData.patient.email.ifBlank { sessionUser?.email ?: "" },
                        diabetesType = rawData.patient.diabetesType.ifBlank { sessionUser?.diabetesType?.ifBlank { "Type 2" } ?: "Type 2" },
                        age = rawData.patient.age ?: sessionUser?.age,
                        heightCm = rawData.patient.heightCm ?: sessionUser?.heightCm,
                        weightKg = rawData.patient.weightKg ?: sessionUser?.weightKg,
                        currentGlucose = rawData.patient.currentGlucose ?: sessionUser?.currentGlucoseMgDl,
                        targetGlucose = rawData.patient.targetGlucose ?: sessionUser?.targetGlucoseMgDl ?: 120f
                    )
                    val enrichedData = rawData.copy(patient = mergedPatient)

                    try {
                        val generated = withContext(Dispatchers.IO) {
                            ClinicalReportGenerator.generateReport(
                                context = context.applicationContext,
                                data = enrichedData,
                                options = options
                            )
                        }

                        _exportState.update {
                            it.copy(
                                isLoading = false,
                                exportedData = enrichedData,
                                generatedFile = generated.file,
                                generatedFileName = generated.fileName,
                                generatedMimeType = generated.mimeType,
                                previewBitmaps = generated.previewBitmaps,
                                csvPreviewText = generated.csvPreviewText,
                                errorMessage = null
                            )
                        }
                        onSuccess()
                    } catch (e: Exception) {
                        _exportState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to generate ${options.format} file: ${e.message}"
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _exportState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load patient records."
                        )
                    }
                }
            )
        }
    }

    fun clearExportError() {
        _exportState.update { it.copy(errorMessage = null) }
    }

    fun openGeneratedReport(context: Context, onResult: (Boolean, String) -> Unit) {
        val state = _exportState.value
        val file = state.generatedFile
        if (file == null || !file.exists()) {
            onResult(false, "Report file not ready. Please generate the report again.")
            return
        }
        ClinicalReportGenerator.openReportFile(context, file, state.generatedMimeType).fold(
            onSuccess = { onResult(true, "Opening ${state.generatedFileName}...") },
            onFailure = { err -> onResult(false, err.message ?: "No compatible viewer app found.") }
        )
    }

    fun saveGeneratedReportToDownloads(context: Context, onResult: (Boolean, String) -> Unit) {
        val state = _exportState.value
        val file = state.generatedFile
        if (file == null || !file.exists()) {
            onResult(false, "Report file not ready. Please generate the report again.")
            return
        }
        viewModelScope.launch {
            val saveRes = withContext(Dispatchers.IO) {
                ClinicalReportGenerator.saveReportToDownloads(context.applicationContext, file, state.generatedMimeType)
            }
            saveRes.fold(
                onSuccess = { pathLabel -> onResult(true, "Saved to $pathLabel") },
                onFailure = { err -> onResult(false, err.message ?: "Download failed.") }
            )
        }
    }

    fun shareGeneratedReport(context: Context, onResult: (Boolean, String) -> Unit) {
        val state = _exportState.value
        val file = state.generatedFile
        if (file == null || !file.exists()) {
            onResult(false, "Report file not ready. Please generate the report again.")
            return
        }
        val patientName = state.exportedData?.patient?.name ?: ProfileState.userName
        ClinicalReportGenerator.shareReportFile(context, file, state.generatedMimeType, patientName).fold(
            onSuccess = { onResult(true, "Opening share sheet with ${state.generatedFileName}...") },
            onFailure = { err -> onResult(false, err.message ?: "Unable to share report.") }
        )
    }

    fun printGeneratedReport(context: Context, onResult: (Boolean, String) -> Unit) {
        val state = _exportState.value
        val file = state.generatedFile
        if (file == null || !file.exists()) {
            onResult(false, "Report file not ready. Please generate the report again.")
            return
        }
        if (!state.generatedMimeType.contains("pdf", ignoreCase = true)) {
            // PrintManager prints PDFs directly; for CSV share to spreadsheet/print handler
            shareGeneratedReport(context, onResult)
            return
        }
        val patientName = state.exportedData?.patient?.name?.ifBlank { "Patient" } ?: "Patient"
        ClinicalReportGenerator.printPdfReport(context, file, "DiaWell_Report_$patientName").fold(
            onSuccess = { onResult(true, "Print preview opened.") },
            onFailure = { err -> onResult(false, err.message ?: "Unable to print report.") }
        )
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
