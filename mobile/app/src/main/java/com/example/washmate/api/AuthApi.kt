package com.example.washmate.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun sync(@Body request: SyncRequest): Response<AuthResponse>
}

data class SyncRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val username: String? = null,
    val phoneNumber: String? = null,
    val role: String = "CUSTOMER"
)
