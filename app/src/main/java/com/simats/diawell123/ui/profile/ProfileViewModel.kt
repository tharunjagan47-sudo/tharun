package com.simats.diawell123.ui.profile
import android.util.Log
import com.simats.diawell123.ui.theme.AppTheme
import com.simats.diawell123.util.toCleanErrorMessage

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
        if (sessionManager.isLoggedIn() && sessionManager.getUserId() != null) {
            loadProfile()
        }
    }

    fun loadProfile() {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.profile == null, errorMessage = null) }
            val result = repository.getProfile()
            result.fold(
                onSuccess = { data ->
                    if (sessionManager.getUserId() != currentUserId) {
                        return@fold
                    }
                    ProfileState.syncFromServer(
                        userId = data.userId,
                        fullName = data.fullName,
                        email = data.email,
                        profilePicBase64 = data.profilePic,
                        profilePhotoUrl = data.profilePhotoUrl
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = data,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    Log.w("ProfileViewModel", "Profile pre-fetch silent fallback: ${error.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
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
        phone: String? = null,
        gender: String? = null,
        targetFastingGlucose: Int? = null,
        targetPostmealGlucose: Int? = null,
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
                weightKg = weightKg,
                phone = phone,
                gender = gender,
                targetFastingGlucose = targetFastingGlucose,
                targetPostmealGlucose = targetPostmealGlucose
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
                            errorMessage = error.toCleanErrorMessage("Unable to update profile. Please try again.")
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
                            errorMessage = error.toCleanErrorMessage("Unable to save patient details. Please try again.")
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
                            errorMessage = error.toCleanErrorMessage("Unable to save health preferences. Please try again.")
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
                    _uiState.update { it.copy(foodPreferences = prefs, errorMessage = null) }
                },
                onFailure = { error ->
                    val raw = error.message ?: ""
                    val safeMsg = if (raw.contains("IllegalStateException", ignoreCase = true) ||
                        raw.contains("BEGIN_ARRAY", ignoreCase = true) ||
                        raw.contains("Gson", ignoreCase = true)
                    ) {
                        null
                    } else {
                        raw.ifEmpty { null }
                    }
                    _uiState.update { it.copy(errorMessage = safeMsg) }
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

    fun uploadProfilePic(userId: Int, base64: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            ProfileState.isUploadingAvatar = true
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            val result = repository.updateProfilePic(userId, base64)
            ProfileState.isUploadingAvatar = false
            result.fold(
                onSuccess = { pic ->
                    if (sessionManager.getUserId() == userId) {
                        sessionManager.saveUserPfp(userId, pic, sessionManager.getUserSession()?.email)
                        ProfileState.setProfilePicFromBase64(pic, userId)
                    }
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            profile = it.profile?.copy(profilePic = pic)
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = error.message ?: "Failed to upload profile picture."
                        )
                    }
                }
            )
        }
    }

    fun uploadProfilePhoto(photoFile: File, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            ProfileState.isUploadingAvatar = true
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            val result = repository.uploadProfilePhoto(photoFile)
            ProfileState.isUploadingAvatar = false
            result.fold(
                onSuccess = { data ->
                    val currentUid = sessionManager.getUserId()
                    data.photoPath?.let { sessionManager.saveProfilePhoto(it) }
                    data.photoUrl?.let {
                        sessionManager.saveProfilePhotoUrl(it)
                        if (currentUid != null && currentUid > 0 && it.isNotBlank()) {
                            ProfileState.loadPhotoFromUrl(it, currentUid)
                        }
                    }
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
