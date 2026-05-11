package com.example.orderlytablet.services
import com.example.orderlytablet.response.OrderResponse
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("orders")
    suspend fun getOrders():List<OrderResponse>

    @GET("overview/tablet")
    suspend fun getOrdersWithDetails(): List<OrderWithOrderDetailResponse>

    @PUT("orderDetails/change-status/{status}")
    suspend fun updateOrderDetailStatus(
        @Path("status") status: String,
        @Body ids: List<Long>
    )

    // Phase 10 — D-01: startup session gate. Returns Response<T> so HTTP 404 is
    // delivered as isSuccessful == false (no exception thrown for non-2xx codes).
    @GET("cashSession/byStatus/OPEN")
    suspend fun getOpenCashSession(): Response<CashSessionResponse>
}