package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OrderDetailApi {
    @GET("orderDetails/order/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: Long): List<OrderDetailsResponse>
}