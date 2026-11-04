package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit Service Interface for Dr. Jarvis AI Chatbot Endpoint.
 * Communicates directly with the Python FastAPI Microservice Gateway on Port 8000.
 */
interface DiaWellApiService {

    @POST(NetworkConstants.CHAT_ENDPOINT)
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}


