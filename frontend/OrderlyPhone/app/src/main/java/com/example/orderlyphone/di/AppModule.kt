package com.example.orderlyphone.di

import android.content.Context
import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.AuthApi
import com.example.orderlyphone.data.remote.EmployeeApi
import com.example.orderlyphone.data.remote.OverviewApi
import com.example.orderlyphone.data.remote.adapter.LocalDateAdapter
import com.example.orderlyphone.data.remote.interceptor.AuthInterceptor
import com.example.orderlyphone.data.repository.AuthRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDate
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:8080/" // эмулятор

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
        TokenStore(context)

    @Provides
    @Singleton
    fun provideOkHttp(tokenStore: TokenStore): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .build()

    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()


    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, tokenStore: TokenStore): AuthRepository =
        AuthRepository(api, tokenStore)

    @Provides
    @Singleton
    fun provideEmployeeApi(retrofit: Retrofit): EmployeeApi =
        retrofit.create(EmployeeApi::class.java)

    @Provides
    @Singleton
    fun provideOverviewApi(retrofit: Retrofit): OverviewApi =
        retrofit.create(OverviewApi::class.java)
}
