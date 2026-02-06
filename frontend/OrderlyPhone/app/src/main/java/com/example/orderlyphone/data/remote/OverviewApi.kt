package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.DashboardStartResponse
import com.example.orderlyphone.domain.model.response.OrderWithTableResponse
import retrofit2.http.GET

interface OverviewApi {
    @GET("overview/phone/dashboard-start")
    suspend fun getDashboardStart(): DashboardStartResponse

    @GET("overview")
    suspend fun getOrdersWithTable(): List<OrderWithTableResponse>
}