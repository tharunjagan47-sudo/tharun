package com.simats.diawell123.ui.meds
import com.simats.diawell123.ui.theme.AppTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.model.CatalogMedicationItem
import com.simats.diawell123.data.model.MedicationItem
import com.simats.diawell123.data.model.MedicationsData
import com.simats.diawell123.data.repository.MedsRepository
import com.simats.diawell123.data.repository.MedsRepositoryImpl
import com.simats.diawell123.util.MedStatusChangedEvent
import com.simats.diawell123.util.MedicationStatusUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MedsListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val data: MedicationsData? = null,
    val errorMessage: String? = null
)

data class SearchMedsUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<CatalogMedicationItem> = emptyList(),
    val errorMessage: String? = null
)

data class SetReminderUiState(
    val medicationName: String = "",
    val medicationDose: String = "",
    val primaryIngredient: String = "",
    val therapeuticClass: String = "",
    val manufacturer: String = "",
    val packaging: String = "",
    val dosageForm: String = "",
    val priceInr: String = "",
    val clinicalInfo: String = "",
    val reminderDateText: String = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date()),
    val selectedTime: String = "8:00 AM",
    val selectedTiming: String = "After Meal",
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class ReminderDetailUiState(
    val medication: MedicationItem? = null,
    val isUpdating: Boolean = false,
    val isDeleted: Boolean = false,
    val isStatusUpdated: Boolean = false,
    val errorMessage: String? = null
)

class MedsViewModel(
    private val repository: MedsRepository = MedsRepositoryImpl()
) : ViewModel() {

    private val _listState = MutableStateFlow(MedsListUiState())
    val listState: StateFlow<MedsListUiState> = _listState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchMedsUiState())
    val searchState: StateFlow<SearchMedsUiState> = _searchState.asStateFlow()

    private val _setReminderState = MutableStateFlow(SetReminderUiState())
    val setReminderState: StateFlow<SetReminderUiState> = _setReminderState.asStateFlow()

    private val _detailState = MutableStateFlow(ReminderDetailUiState())
    val detailState: StateFlow<ReminderDetailUiState> = _detailState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadMedications()
        searchMedications("")
        observeStatusChanges()
    }

    private fun observeStatusChanges() {
        viewModelScope.launch {
            MedicationStatusUpdater.events.collect { event ->
                applyOptimisticStatusEvent(event)
                loadMedications(isPullToRefresh = true)
            }
        }
    }

    private fun statusStyleFor(status: String): Pair<String, String> {
        return when (status.lowercase(Locale.getDefault())) {
            "taken" -> "#2E7D32" to "#E8F5E9"
            "snoozed" -> "#1565C0" to "#E3F2FD"
            "skipped", "missed" -> "#C62828" to "#FFEBEE"
            else -> "#B81D85" to "#FDEEF7"
        }
    }

    private fun applyOptimisticStatusEvent(event: MedStatusChangedEvent) {
        val (sColor, sBg) = statusStyleFor(event.status)
        _listState.update { state ->
            val currentData = state.data ?: return@update state
            val meds = currentData.medications
            if (meds.isEmpty()) return@update state

            var matchedIndex = meds.indexOfFirst { it.id == event.medId }
            if (matchedIndex < 0 && event.medName.isNotBlank()) {
                matchedIndex = meds.indexOfLast { it.name.equals(event.medName.trim(), ignoreCase = true) }
            }
            if (matchedIndex < 0) return@update state

            val updatedList = meds.mapIndexed { idx, item ->
                if (idx == matchedIndex) {
                    item.copy(
                        status = event.status,
                        statusColor = sColor,
                        statusBg = sBg,
                        reminderTime = event.newTimeLabel ?: item.reminderTime
                    )
                } else {
                    item
                }
            }
            val takenCount = updatedList.count { it.status.equals("Taken", ignoreCase = true) }
            val upcomingCount = updatedList.count {
                it.status.equals("Upcoming", ignoreCase = true) ||
                    it.status.equals("Snoozed", ignoreCase = true)
            }
            val adherence = if (updatedList.isNotEmpty()) {
                kotlin.math.round((takenCount.toDouble() / updatedList.size) * 100).toInt()
            } else 0

            state.copy(
                data = currentData.copy(
                    stats = currentData.stats.copy(
                        total = updatedList.size,
                        taken = takenCount,
                        upcoming = upcomingCount,
                        adherencePercent = adherence
                    ),
                    medications = updatedList
                )
            )
        }

        _detailState.update { detail ->
            val med = detail.medication ?: return@update detail
            val matches = med.id == event.medId ||
                (event.medName.isNotBlank() && med.name.equals(event.medName.trim(), ignoreCase = true))
            if (!matches) return@update detail
            detail.copy(
                medication = med.copy(
                    status = event.status,
                    statusColor = sColor,
                    statusBg = sBg,
                    reminderTime = event.newTimeLabel ?: med.reminderTime
                )
            )
        }
    }

    /**
     * Load medications for the specified or current date.
     */
    fun loadMedications(date: String? = null, isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            val targetDate = date ?: _listState.value.selectedDate
            _listState.update {
                if (isPullToRefresh) {
                    it.copy(isRefreshing = true, selectedDate = targetDate, errorMessage = null)
                } else {
                    it.copy(isLoading = it.data == null, selectedDate = targetDate, errorMessage = null)
                }
            }

            val result = repository.getMedications(date = targetDate)
            result.fold(
                onSuccess = { data ->
                    _listState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            data = data,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _listState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Failed to load medications."
                        )
                    }
                }
            )
        }
    }

    /**
     * Search medications catalog from the dataset via backend with debounce & cancellation to eliminate glitches.
     */
    fun searchMedications(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.update { it.copy(query = query, isSearching = true, errorMessage = null) }
            if (query.isNotBlank()) {
                delay(200) // Debounce rapid keystrokes to ensure smooth UI and eliminate race conditions
            }
            val result = repository.searchMedications(query = query.trim())
            result.fold(
                onSuccess = { list ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            results = list,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = error.message
                        )
                    }
                }
            )
        }
    }

    /**
     * Select a medicine from dataset search results and populate reminder fields.
     */
    fun selectMedicineFromDataset(item: CatalogMedicationItem) {
        _setReminderState.update {
            it.copy(
                medicationName = item.name,
                medicationDose = item.dose,
                primaryIngredient = item.primaryIngredient,
                therapeuticClass = item.category,
                manufacturer = item.manufacturer,
                packaging = item.packaging,
                dosageForm = item.dosageForm,
                priceInr = item.priceInr,
                clinicalInfo = item.info,
                selectedTiming = if (item.timing.isNotBlank()) item.timing else it.selectedTiming,
                isSaving = false,
                isSuccess = false,
                errorMessage = null
            )
        }
    }

    /**
     * Prepare reminder screen for a selected medicine name and dose without clearing dataset fields if same.
     */
    fun selectMedicineForReminder(name: String, dose: String) {
        _setReminderState.update { current ->
            if (current.medicationName == name && current.medicationDose == dose && current.primaryIngredient.isNotBlank()) {
                current
            } else {
                current.copy(
                    medicationName = name,
                    medicationDose = dose,
                    isSaving = false,
                    isSuccess = false,
                    errorMessage = null
                )
            }
        }
    }

    fun updateReminderDate(dateText: String) {
        _setReminderState.update { it.copy(reminderDateText = dateText) }
    }

    fun updateReminderTime(time: String) {
        _setReminderState.update { it.copy(selectedTime = time) }
    }

    fun updateReminderTiming(timing: String) {
        _setReminderState.update { it.copy(selectedTiming = timing) }
    }

    fun updateReminderNotes(notes: String) {
        _setReminderState.update { it.copy(notes = notes) }
    }

    /**
     * Save newly configured medication reminder with dataset attributes to backend.
     */
    fun saveMedicationReminder(onSuccess: (MedicationItem) -> Unit = {}) {
        viewModelScope.launch {
            val state = _setReminderState.value
            _setReminderState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = repository.addMedication(
                name = state.medicationName,
                dose = state.medicationDose,
                reminderTime = state.selectedTime,
                reminderDate = state.reminderDateText,
                timing = state.selectedTiming,
                notes = state.notes,
                therapeuticClass = state.therapeuticClass,
                manufacturer = state.manufacturer,
                primaryIngredient = state.primaryIngredient,
                packaging = state.packaging,
                dosageForm = state.dosageForm
            )

            result.fold(
                onSuccess = { savedMed ->
                    _setReminderState.update { it.copy(isSaving = false, isSuccess = true) }
                    loadMedications(isPullToRefresh = true) // Refresh list
                    MedicationStatusUpdater.emitLocalChange(savedMed.id, savedMed.name, savedMed.status)
                    onSuccess(savedMed)
                },
                onFailure = { error ->
                    _setReminderState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Failed to set medication reminder."
                        )
                    }
                }
            )
        }
    }

    /**
     * Set the active medication for detail reminder screen.
     */
    fun setSelectedMedication(medication: MedicationItem) {
        _detailState.update {
            ReminderDetailUiState(
                medication = medication,
                isUpdating = false,
                isDeleted = false,
                isStatusUpdated = false,
                errorMessage = null
            )
        }
    }

    /**
     * Update status (e.g. "Taken", "Skipped", "Snoozed", "Missed", "Upcoming")
     */
    fun updateStatus(
        medicationId: Int,
        newStatus: String,
        snoozeMinutes: Int = 0,
        newReminderTime: String? = null,
        medicationName: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _detailState.update { it.copy(isUpdating = true, errorMessage = null) }
            val result = repository.updateMedicationStatus(
                medicationId = medicationId,
                status = newStatus,
                medicationName = medicationName ?: _detailState.value.medication?.name,
                snoozeMinutes = snoozeMinutes.takeIf { it > 0 },
                newReminderTime = newReminderTime
            )
            result.fold(
                onSuccess = {
                    val (sColor, sBg) = statusStyleFor(newStatus)
                    val newTimeLabel = if (newStatus.equals("Snoozed", ignoreCase = true) && snoozeMinutes > 0) {
                        MedicationStatusUpdater.computeSnoozeTimeStrings(snoozeMinutes).second
                    } else null
                    val medNameStr = medicationName ?: _detailState.value.medication?.name ?: ""
                    _detailState.update {
                        it.copy(
                            isUpdating = false,
                            isStatusUpdated = true,
                            medication = it.medication?.copy(
                                status = newStatus,
                                statusColor = sColor,
                                statusBg = sBg,
                                reminderTime = newTimeLabel ?: it.medication.reminderTime
                            )
                        )
                    }
                    MedicationStatusUpdater.emitLocalChange(medicationId, medNameStr, newStatus, newTimeLabel)
                    loadMedications(isPullToRefresh = true) // Refresh list
                    onSuccess()
                },
                onFailure = { error ->
                    _detailState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = error.message ?: "Failed to update medication status."
                        )
                    }
                }
            )
        }
    }

    /**
     * Delete medication reminder.
     */
    fun deleteMedication(medicationId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _detailState.update { it.copy(isUpdating = true, errorMessage = null) }
            val result = repository.deleteMedication(medicationId = medicationId)
            result.fold(
                onSuccess = {
                    val deletedName = _detailState.value.medication?.name ?: ""
                    _detailState.update { it.copy(isUpdating = false, isDeleted = true) }
                    MedicationStatusUpdater.emitLocalChange(medicationId, deletedName, "Deleted")
                    loadMedications(isPullToRefresh = true) // Refresh list
                    onSuccess()
                },
                onFailure = { error ->
                    _detailState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = error.message ?: "Failed to delete medication."
                        )
                    }
                }
            )
        }
    }

    fun clearErrors() {
        _listState.update { it.copy(errorMessage = null) }
        _searchState.update { it.copy(errorMessage = null) }
        _setReminderState.update { it.copy(errorMessage = null) }
        _detailState.update { it.copy(errorMessage = null) }
    }
}
