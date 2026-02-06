package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.DashboardStartResponse
import retrofit2.http.GET

interface OverviewApi {
    @GET("overview/phone/dashboard-start")
    suspend fun getDashboardStart(): DashboardStartResponse
}