package com.example.orderlytablet.services
import com.example.orderlytablet.response.OrderResponse
import retrofit2.http.GET
interface ApiService {
    @GET("orders")
    suspend fun getOrders():List<OrderResponse>
}