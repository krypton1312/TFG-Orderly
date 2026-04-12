package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.ProductsWithSupplementsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductsApi {
    @GET("overview/products-with-supplements-by-category/id/{id}")
    suspend fun getProductsWithSupplementsByCategory(
        @Path("id") categoryId: Long
    ): ProductsWithSupplementsResponse

}