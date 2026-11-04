package com.simats.diawell123.data.repository

import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DiaWellApiService
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository interface for Dr. Jarvis AI communication.
 */
interface ChatRepository {
    suspend fun sendMessage(message: String): Result<ChatResponse>
}

/**
 * Implementation of ChatRepository communicating with local DiaWell FastAPI microservice via Retrofit.
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
            val userId = try {
                SessionManager.getInstance().getUserId()
            } catch (_: Exception) {
                null
            }

            val request = ChatRequest(
                prompt = trimmed,
                model = "qwen2.5:3b",
                temperature = 0.7,
                userId = userId
            )

            val response = apiService.sendMessage(request)

            if (response.isSuccessful) {
                val body = response.body()
                val cleanText = body?.getCleanResponse()
                if (body != null && !cleanText.isNullOrBlank()) {
                    Result.success(body)
                } else {
                    val serverMsg = body?.message ?: "Received an empty response from Dr. Jarvis."
                    Result.failure(Exception(serverMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val code = response.code()
                val errorMsg = when (code) {
                    404 -> "Chat endpoint not found (HTTP 404). Check FastAPI deployment at ${ApiClient.aiBaseUrl}."
                    500 -> "Internal error in Dr. Jarvis AI backend (HTTP 500): ${errorBody ?: ""}"
                    503 -> "Local Ollama service is unavailable. Please verify 'ollama' is running on Port 11434."
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
                Exception("Cannot connect to DiaWell AI gateway at ${ApiClient.aiBaseUrl}. Please verify FastAPI is running on Port 8000 and the device network is connected.")
            )
        } catch (e: UnknownHostException) {
            Result.failure(
                Exception("Cannot resolve host at ${ApiClient.aiBaseUrl}. Please verify your device Wi-Fi/network connection.")
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: "An unexpected error occurred while communicating with Dr. Jarvis.")
            )
        }
    }
}

