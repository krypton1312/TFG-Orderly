package com.example.orderlyphone.data.remote.interceptor

import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.AuthEventBus
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        // Не добавляем токен только на /auth/login
        if (req.url.encodedPath == "/auth/login") {
            return chain.proceed(req)
        }

        val token = runBlocking { tokenStore.load() }
        if (token.isNullOrBlank()) return chain.proceed(req)

        val authed = req.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authed)

        if (response.code == 401) {
            runBlocking { tokenStore.clear() }
            AuthEventBus.emitUnauthorized()
        }

        return response
    }
}
