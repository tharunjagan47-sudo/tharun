package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit Service Interface for Dr. Jarvis AI Chatbot Endpoint.
 * Communicates directly with the Python FastAPI Microservice Gateway on Port 8000,
 * and supports the Port 80 PHP bridge fallback.
 */
interface DiaWellApiService {

    @POST(NetworkConstants.CHAT_ENDPOINT)
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @POST(NetworkConstants.PHP_BRIDGE_CHAT_ENDPOINT)
    suspend fun sendBridgeMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @POST
    suspend fun sendMessageToUrl(
        @Url url: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
