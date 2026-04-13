package com.example.orderlyphone.domain.model.request

data class LoginRequest(
    val identifier: String,
    val password: String
)