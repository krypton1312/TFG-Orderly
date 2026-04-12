package com.example.orderlytablet.services

import com.example.orderlytablet.data.AuthInterceptor
import com.example.orderlytablet.data.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 is the Android emulator alias for the host machine's localhost
    const val BASE_URL = "http://10.0.2.2:8080/"

    private var tokenStore: TokenStore? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    // Unauthenticated — used only by AuthApi (login / refresh)
    val authInstance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Authenticated instance — must call init() before use
    private var _instance: ApiService? = null

    fun init(store: TokenStore) {
        tokenStore = store
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(store))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        _instance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getToken(): String? = tokenStore?.getAccessToken()

    val instance: ApiService
        get() = _instance
            ?: throw IllegalStateException("RetrofitClient.init(tokenStore) must be called before use")
}
