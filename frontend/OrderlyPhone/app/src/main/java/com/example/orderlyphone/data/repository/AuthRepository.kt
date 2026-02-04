package com.example.orderlyphone.data.repository

import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.AuthApi
import com.example.orderlyphone.domain.model.request.LoginRequest

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String) {
        val resp = api.login(LoginRequest(email, password))
        tokenStore.save(resp.token)
    }
}
