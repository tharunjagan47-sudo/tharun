package com.simats.diawell123.data.api

import com.simats.diawell123.data.model.AuthResponse
import com.simats.diawell123.data.model.LoginRequest
import com.simats.diawell123.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/signup.php")
    suspend fun signup(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/logout.php")
    suspend fun logout(): Response<AuthResponse>
}

