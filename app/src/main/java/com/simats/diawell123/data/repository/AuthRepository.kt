package com.simats.diawell123.data.repository

import com.google.gson.Gson
import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.AuthApiService
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.AuthResponse
import com.simats.diawell123.data.model.LoginRequest
import com.simats.diawell123.data.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout(): Result<AuthResponse>
}

class AuthRepositoryImpl(
    private val apiService: AuthApiService = ApiClient.authApiService
) : AuthRepository {

    private fun getSessionManager(): SessionManager? {
        return try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    // Persist session to SharedPreferences
                    body.data?.let { user ->
                        getSessionManager()?.saveUserSession(user)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Login failed." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Login failed with HTTP status ${response.code()}."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify that your local XAMPP Apache is running and accessible at ${ApiClient.baseUrl}"))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    // Persist session to SharedPreferences
                    body.data?.let { user ->
                        getSessionManager()?.saveUserSession(user)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message.ifEmpty { "Registration failed." }))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Registration failed with HTTP status ${response.code()}."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Cannot connect to server. Please verify that your local XAMPP Apache is running and accessible at ${ApiClient.baseUrl}"))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }

    override suspend fun logout(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            // Attempt remote logout to destroy PHP session
            apiService.logout()
        } catch (_: Exception) {
            // Ignore network errors on logout to ensure local session is always wiped
        } finally {
            // Guarantee local session in SharedPreferences is destroyed
            getSessionManager()?.clearSession(reason = null)
        }
        Result.success(AuthResponse(success = true, message = "Logged out successfully."))
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val parsed = Gson().fromJson(errorBody, AuthResponse::class.java)
            parsed.message
        } catch (_: Exception) {
            null
        }
    }
}
