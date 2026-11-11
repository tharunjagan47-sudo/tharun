package com.simats.diawell123.data.repository

import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DiaWellApiService
import com.simats.diawell123.data.api.NetworkConstants
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import retrofit2.Response
import java.io.IOException
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
 * Implementation of ChatRepository communicating with DiaWell PHP -> Local Ollama architecture on Port 80.
 */
class ChatRepositoryImpl(
    private val apiService: DiaWellApiService = ApiClient.diaWellApiService
) : ChatRepository {

    override suspend fun sendMessage(message: String): Result<ChatResponse> {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty."))
        }

        val userId = try {
            SessionManager.getInstance().getUserId()
        } catch (_: Exception) {
            null
        }

        val request = ChatRequest(
            prompt = trimmed,
            model = "qwen2.5:0.5b",
            temperature = 0.7,
            userId = userId
        )

        return try {
            val primaryResult = executeChatCall(
                call = { apiService.sendMessage(request) },
                endpointLabel = ApiClient.baseUrl
            )
            if (primaryResult != null) {
                return primaryResult
            }

            // Primary returned HTTP 404 or connection-retriable failure: probe all fallback URLs
            tryFallbackUrls(request)
        } catch (e: IOException) {
            tryFallbackUrls(request, initialError = e)
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: "An unexpected error occurred while communicating with Dr. Jarvis.")
            )
        }
    }

    private suspend fun tryFallbackUrls(
        request: ChatRequest,
        initialError: Exception? = null
    ): Result<ChatResponse> {
        var lastError: Exception? = initialError
        for (fallbackUrl in NetworkConstants.AI_CHAT_FALLBACK_URLS) {
            try {
                val result = executeChatCall(
                    call = { apiService.sendMessageToUrl(fallbackUrl, request) },
                    endpointLabel = fallbackUrl
                )
                if (result != null) {
                    return result
                }
            } catch (e: IOException) {
                lastError = e
            } catch (e: Exception) {
                lastError = e
            }
        }
        return Result.failure(formatNetworkError(lastError))
    }

    private suspend fun executeChatCall(
        call: suspend () -> Response<ChatResponse>,
        endpointLabel: String
    ): Result<ChatResponse>? {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            val cleanText = body?.getCleanResponse()
            return if (body != null && !cleanText.isNullOrBlank()) {
                Result.success(body)
            } else {
                val serverMsg = body?.message ?: "Received an empty response from Dr. Jarvis."
                Result.failure(Exception(serverMsg))
            }
        }
        val code = response.code()
        if (code == 404) {
            return null
        }
        val errorBody = response.errorBody()?.string()
        val errorMsg = when (code) {
            500 -> "Internal error in Dr. Jarvis AI backend (HTTP 500): ${errorBody ?: ""}"
            503 -> "Local Ollama service is unavailable. Please verify 'ollama' is running on Port 11434."
            else -> "Network request failed with HTTP $code at $endpointLabel: ${errorBody ?: response.message()}"
        }
        return Result.failure(Exception(errorMsg))
    }

    private fun formatNetworkError(error: Exception?): Exception {
        return when (error) {
            is SocketTimeoutException -> Exception(
                "Dr. Jarvis took longer than expected to generate a response. Please check local Ollama model speed or try again."
            )
            is ConnectException -> Exception(
                "Cannot connect to DiaWell server at ${ApiClient.baseUrl}. Please verify Apache & ADB reverse are running."
            )
            is UnknownHostException -> Exception(
                "Cannot resolve host at ${ApiClient.baseUrl}. Please verify your device network connection."
            )
            else -> Exception(
                error?.localizedMessage
                    ?: "Cannot connect to DiaWell server at ${ApiClient.baseUrl}. Please check backend deployment."
            )
        }
    }
}
