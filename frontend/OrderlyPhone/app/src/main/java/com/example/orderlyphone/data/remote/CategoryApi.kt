package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.CategoryResponse
import retrofit2.http.GET

interface CategoryApi {
    @GET("categories")
    suspend fun getAllCategories(): List<CategoryResponse>
}
