package com.simats.diawell123.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.LoginRequest
import com.simats.diawell123.data.model.RegisterRequest
import com.simats.diawell123.data.model.UserData
import com.simats.diawell123.data.repository.AuthRepository
import com.simats.diawell123.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Auth operations following Unidirectional Data Flow (UDF).
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val user: UserData? = null,
    val token: String? = null,
    val nextScreen: String? = null
)

class AuthViewModel @JvmOverloads constructor(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // If session was automatically cleared due to inactivity or 401, display reason on login
        checkExpiredReason()
    }

    fun checkExpiredReason() {
        try {
            val reason = SessionManager.getInstance().consumeSessionExpiredReason()
            if (!reason.isNullOrBlank()) {
                _uiState.update { it.copy(errorMessage = reason) }
            }
        } catch (_: Exception) {}
    }

    fun resetState() {
        _uiState.update { AuthUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Executes Login with client-side input validation.
     */
    fun onLogin(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }

        if (trimmedPassword.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

        viewModelScope.launch {
            val result = repository.login(
                LoginRequest(
                    email = trimmedEmail,
                    password = trimmedPassword
                )
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null,
                            successMessage = response.message,
                            user = response.data,
                            token = response.data?.token ?: response.data?.userId?.toString(),
                            nextScreen = response.data?.nextScreen ?: if (response.data?.isProfileCompleted == true) "home" else "patient_profile"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Login failed. Please check credentials."
                        )
                    }
                }
            )
        }
    }

    /**
     * Executes Registration with client-side input validation.
     */
    fun onRegister(name: String, email: String, password: String, confirmPassword: String? = null) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirm = confirmPassword?.trim()

        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your full name.") }
            return
        }

        if (trimmedEmail.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }

        if (trimmedPassword.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter a password.") }
            return
        }

        if (trimmedPassword.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters long.") }
            return
        }

        if (trimmedConfirm != null && trimmedPassword != trimmedConfirm) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

        viewModelScope.launch {
            val result = repository.register(
                RegisterRequest(
                    fullName = trimmedName,
                    email = trimmedEmail,
                    password = trimmedPassword,
                    confirmPassword = trimmedConfirm ?: trimmedPassword
                )
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null,
                            successMessage = response.message,
                            user = response.data,
                            token = response.data?.token ?: response.data?.userId?.toString(),
                            nextScreen = response.data?.nextScreen ?: "patient_profile"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = error.message ?: "Registration failed. Please try again."
                        )
                    }
                }
            )
        }
    }

    /**
     * Explicit Logout: Clears local session and calls remote logout.
     */
    fun onLogout(onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { AuthUiState() }
            onCompleted()
        }
    }
}
