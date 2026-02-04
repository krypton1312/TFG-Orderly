package com.example.orderlyphone.data.remote

import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // ⚠️ Эмулятор: 10.0.2.2
    // Телефон: IP твоего ПК (например 192.168.1.50)
    private const val BASE_URL = "http://10.0.2.2:8080"

    fun authApi(tokenStore: TokenStore): AuthApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
