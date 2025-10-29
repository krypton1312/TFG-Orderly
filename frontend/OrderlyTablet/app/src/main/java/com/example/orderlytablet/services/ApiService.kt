package com.example.orderlytablet.services
import com.example.orderlytablet.response.OrderResponse
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import retrofit2.http.GET
interface ApiService {
    @GET("orders")
    suspend fun getOrders():List<OrderResponse>

    @GET("overview/tablet")
    suspend fun getOrdersWithDetails(): List<OrderWithOrderDetailResponse>
}