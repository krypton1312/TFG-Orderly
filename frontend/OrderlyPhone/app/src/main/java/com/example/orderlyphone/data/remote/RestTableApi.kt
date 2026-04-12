package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.RestTableResponse
import retrofit2.http.GET

interface RestTableApi {
    @GET("tables")
    suspend fun getAllTables(): List<RestTableResponse>
}