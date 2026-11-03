package com.simats.diawell123.data.repository

import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DiaWellApiService
import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * Repository interface for Dr. Jarvis AI communication.
 */
interface ChatRepository {
    suspend fun sendMessage(message: String): Result<ChatResponse>
}

/**
 * Implementation of ChatRepository communicating with local DiaWell AI backend via Retrofit.
 */
class ChatRepositoryImpl(
    private val apiService: DiaWellApiService = ApiClient.diaWellApiService
) : ChatRepository {

    override suspend fun sendMessage(message: String): Result<ChatResponse> {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty."))
        }

        return try {
            val response = apiService.sendMessage(ChatRequest(message = trimmed))

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    val serverMsg = body?.message ?: "Received an empty response from Dr. Jarvis."
                    Result.failure(Exception(serverMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val code = response.code()
                val errorMsg = when (code) {
                    404 -> "Chat endpoint not found (HTTP 404). Check backend deployment at ${ApiClient.baseUrl}."
                    500 -> "Internal error in Dr. Jarvis AI backend (HTTP 500)."
                    503 -> "Local Ollama service is unavailable. Please verify 'ollama serve' is running."
                    else -> "Network request failed with HTTP $code: ${errorBody ?: response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(
                Exception("Dr. Jarvis took longer than 120 seconds to generate a response. Please check local Ollama model speed or try again.")
            )
        } catch (e: ConnectException) {
            Result.failure(
                Exception("Cannot connect to DiaWell server at ${ApiClient.baseUrl}. Please verify your XAMPP Apache is running and cleartext traffic is enabled.")
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: "An unexpected error occurred while communicating with Dr. Jarvis.")
            )
        }
    }
}

