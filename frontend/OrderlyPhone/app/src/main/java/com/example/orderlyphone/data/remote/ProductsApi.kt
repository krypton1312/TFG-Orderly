package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.ProductResponse
import retrofit2.http.GET

interface ProductsApi {
    @GET("products")
    suspend fun getAllProducts(): List<ProductResponse>

}