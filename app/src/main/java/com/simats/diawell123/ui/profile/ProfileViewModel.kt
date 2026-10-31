package com.simats.diawell123.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.FoodPreferencesData
import com.simats.diawell123.data.model.ProfileData
import com.simats.diawell123.data.repository.ProfileRepository
import com.simats.diawell123.data.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val profile: ProfileData? = null,
    val foodPreferences: FoodPreferencesData? = null,
    val isUploadingPhoto: Boolean = false,
    val uploadedPhotoUrl: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl(),
    private val sessionManager: SessionManager = SessionManager.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.profile == null, errorMessage = null) }
            val result = repository.getProfile()
            result.fold(
                onSuccess = { data ->
                    // Synchronize shared ProfileState
                    ProfileState.userName = data.fullName
                    ProfileState.userEmail = data.email

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = data,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
            )
        }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        diabetesType: String,
        age: Int?,
        heightCm: Float?,
        weightKg: Float?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }
            val result = repository.updateProfile(
                fullName = fullName,
                email = email,
                diabetesType = diabetesType,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg
            )
            result.fold(
                onSuccess = { updated ->
                    ProfileState.userName = updated.fullName
                    ProfileState.userEmail = updated.email
                    sessionManager.saveUserName(updated.fullName)
                    sessionManager.saveUserEmail(updated.email)

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = true,
                            profile = updated,
                            errorMessage = null
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Failed to update profile."
                        )
                    }
                }
            )
        }
    }

    fun savePatientDetails(
        diabetesType: String,
        age: Int?,
        heightCm: Float?,
        weightKg: Float?,
        currentGlucose: Float?,
        targetGlucose: Float?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }
            val result = repository.savePatientDetails(
                diabetesType = diabetesType,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                currentGlucose = currentGlucose,
                targetGlucose = targetGlucose
            )
            result.fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = true,
                            profile = updated,
                            errorMessage = null
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Failed to save patient details."
                        )
                    }
                }
            )
        }
    }

    fun saveHealthPreferences(
        healthFocus: String?,
        dietaryPreferences: List<String>,
        allergies: List<String>,
        activityLevel: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }
            val result = repository.saveHealthPreferences(
                healthFocus = healthFocus,
                dietaryPreferences = dietaryPreferences,
                allergies = allergies,
                activityLevel = activityLevel
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isSaving = false, isSuccess = true, errorMessage = null)
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Failed to save health preferences."
                        )
                    }
                }
            )
        }
    }

    fun loadFoodPreferences() {
        viewModelScope.launch {
            val result = repository.getFoodPreferences()
            result.fold(
                onSuccess = { prefs ->
                    _uiState.update { it.copy(foodPreferences = prefs) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    fun saveFoodPreferences(
        preferredDiet: String,
        allergies: List<String>,
        preferredStyles: List<String>,
        foodsToAvoid: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }
            val avoidList = foodsToAvoid.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val result = repository.saveFoodPreferences(
                preferredDiet = preferredDiet,
                allergies = allergies,
                preferredStyles = preferredStyles,
                foodsToAvoid = avoidList
            )
            result.fold(
                onSuccess = { saved ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = true,
                            foodPreferences = saved,
                            errorMessage = null
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Failed to save food preferences."
                        )
                    }
                }
            )
        }
    }

    fun uploadProfilePhoto(photoFile: File, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            val result = repository.uploadProfilePhoto(photoFile)
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            uploadedPhotoUrl = data.photoUrl,
                            profile = it.profile?.copy(
                                profilePhoto = data.photoPath,
                                profilePhotoUrl = data.photoUrl
                            )
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = error.message ?: "Failed to upload photo."
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
