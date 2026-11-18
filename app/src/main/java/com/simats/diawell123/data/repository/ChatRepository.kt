package com.simats.diawell123.data.repository

import com.simats.diawell123.data.api.ApiClient
import com.simats.diawell123.data.api.DiaWellApiService
import com.simats.diawell123.data.api.NetworkConstants
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.model.ChatHistoryMessage
import com.simats.diawell123.data.model.ChatPatientContext
import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import com.simats.diawell123.ui.ai.OfflineJarvisResponder
import retrofit2.Response
import java.io.IOException

/**
 * Repository interface for Dr. Jarvis AI communication with Live Patient Context Awareness and Multi-Turn Conversation Memory.
 */
interface ChatRepository {
    suspend fun sendMessage(
        message: String,
        patientContext: ChatPatientContext? = null,
        history: List<ChatHistoryMessage>? = null
    ): Result<ChatResponse>
}

/**
 * Implementation of ChatRepository communicating with DiaWell PHP -> Local Ollama architecture.
 * Prioritizes http://127.0.0.1:8080/diawell_backend/ (ADB reverse) with multi-port fallbacks
 * and an offline clinical Personal Health RAG doctor fallback so Dr. Jarvis never fails even if unplugged.
 */
class ChatRepositoryImpl(
    private val apiService: DiaWellApiService = ApiClient.diaWellApiService
) : ChatRepository {

    override suspend fun sendMessage(
        message: String,
        patientContext: ChatPatientContext?,
        history: List<ChatHistoryMessage>?
    ): Result<ChatResponse> {
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
            temperature = 0.3,
            userId = userId,
            patientContext = patientContext,
            history = history
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
            // Intelligent Personal Health RAG fallback for unexpected exceptions
            val fallbackReply = OfflineJarvisResponder.generateResponse(trimmed, patientContext, history)
            Result.success(
                ChatResponse(
                    success = true,
                    model = "diva-offline",
                    response = fallbackReply,
                    message = "DIVA (DiaWell Intelligent Virtual Assistant)"
                )
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

        // OFFLINE DEMO FALLBACK FOR DIVA (DiaWell Intelligent Virtual Assistant)
        // If cable is unplugged or server is unreachable, DIVA answers accurately using live on-device context!
        val fallbackReply = OfflineJarvisResponder.generateResponse(request.prompt, request.patientContext, request.history)
        return Result.success(
            ChatResponse(
                success = true,
                model = "diva-offline",
                response = fallbackReply,
                message = "DIVA (Offline Personal Safe Mode)"
            )
        )
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
                val serverMsg = body?.message ?: "Received an empty response from DIVA."
                Result.failure(Exception(serverMsg))
            }
        }
        val code = response.code()
        if (code == 404) {
            return null
        }
        val errorBody = response.errorBody()?.string()
        val errorMsg = when (code) {
            500 -> "Internal error in DIVA AI backend (HTTP 500): ${errorBody ?: ""}"
            503 -> "Local Ollama service is unavailable. Please verify 'ollama' is running on Port 11434."
            else -> "Network request failed with HTTP $code at $endpointLabel: ${errorBody ?: response.message()}"
        }
        return Result.failure(Exception(errorMsg))
    }
}
