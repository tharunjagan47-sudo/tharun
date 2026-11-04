package com.simats.diawell123.data.repository

import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DiaWellApiService
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
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
 * Implementation of ChatRepository communicating with DiaWell AI backend.
 * Provides resilient primary routing to FastAPI (Port 8000) with automatic
 * seamless fallback to Port 80 PHP bridge for physical LAN devices.
 */
class ChatRepositoryImpl(
    private val apiService: DiaWellApiService = ApiClient.diaWellApiService,
    private val bridgeService: DiaWellApiService = ApiClient.diaWellBridgeApiService
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
            model = "qwen2.5:3b",
            temperature = 0.5,
            userId = userId
        )

        // ---------------------------------------------------------------------
        // Step 1: Attempt direct FastAPI microservice gateway (Port 8000)
        // ---------------------------------------------------------------------
        var directException: Throwable? = null
        try {
            val response = apiService.sendMessage(request)
            if (response.isSuccessful) {
                val body = response.body()
                val cleanText = body?.getCleanResponse()
                if (body != null && !cleanText.isNullOrBlank()) {
                    return Result.success(body)
                }
            } else {
                // If 404 or other server error, record and allow bridge fallback
                directException = Exception("FastAPI gateway returned HTTP ${response.code()}")
            }
        } catch (e: Throwable) {
            // Port 8000 might be inaccessible from physical device due to firewall or routing
            directException = e
        }

        // ---------------------------------------------------------------------
        // Step 2: Resilient fallback to Port 80 PHP Bridge (Apache -> 127.0.0.1:8000)
        // ---------------------------------------------------------------------
        return try {
            val bridgeResponse = bridgeService.sendBridgeMessage(request)
            if (bridgeResponse.isSuccessful) {
                val body = bridgeResponse.body()
                val cleanText = body?.getCleanResponse()
                if (body != null && !cleanText.isNullOrBlank()) {
                    Result.success(body)
                } else {
                    val serverMsg = body?.message ?: "Received an empty response from Dr. Jarvis."
                    Result.failure(Exception(serverMsg))
                }
            } else {
                val errorBody = bridgeResponse.errorBody()?.string()
                val code = bridgeResponse.code()
                val errorMsg = when (code) {
                    404 -> "Chat endpoint not found (HTTP 404). Check backend deployment at ${ApiClient.baseUrl}."
                    500 -> "Internal error in Dr. Jarvis AI backend (HTTP 500): ${errorBody ?: ""}"
                    503 -> "Local Ollama service is unavailable. Please verify 'ollama' is running on Port 11434."
                    else -> "Network request failed with HTTP $code: ${errorBody ?: bridgeResponse.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(
                Exception("Dr. Jarvis took longer than expected to generate a response. Please check local Ollama model speed or try again.")
            )
        } catch (e: ConnectException) {
            Result.failure(
                Exception("Cannot connect to DiaWell server at ${ApiClient.baseUrl}. Please verify your device Wi-Fi/network connection.")
            )
        } catch (e: UnknownHostException) {
            Result.failure(
                Exception("Cannot resolve host at ${ApiClient.baseUrl}. Please verify your device Wi-Fi/network connection.")
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.localizedMessage ?: directException?.localizedMessage ?: "An unexpected error occurred while communicating with Dr. Jarvis.")
            )
        }
    }
}
