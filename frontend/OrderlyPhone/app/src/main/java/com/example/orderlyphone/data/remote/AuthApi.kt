package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.AuthResponse
import com.example.orderlyphone.domain.model.request.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse
}
