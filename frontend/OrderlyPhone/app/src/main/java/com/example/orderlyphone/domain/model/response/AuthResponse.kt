package com.example.orderlyphone.domain.model.response

data class AuthResponse(
    val token: String,
    val mustChangePassword: Boolean = false
)