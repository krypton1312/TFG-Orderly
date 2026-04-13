package com.example.orderlyphone.data.repository

import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.AuthApi
import com.example.orderlyphone.domain.model.request.ChangePasswordRequest
import com.example.orderlyphone.domain.model.request.LoginRequest

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun login(identifier: String, password: String): Boolean {
        val resp = api.login(LoginRequest(identifier, password))
        tokenStore.save(resp.token)
        tokenStore.saveEmail(identifier)
        return resp.mustChangePassword
    }

    suspend fun changePassword(newPassword: String) {
        api.changePassword(ChangePasswordRequest(newPassword))
    }
}
