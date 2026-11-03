package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit Service Interface for Dr. Jarvis AI Chatbot Endpoint.
 * Communicates with the local PHP-to-FastAPI bridge.
 */
interface DiaWellApiService {

    @POST("DiaWell_backend/ai/chat.php")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

