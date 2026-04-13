package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.ShiftRecordResponse
import retrofit2.http.POST

interface ShiftRecordApi {

    @POST("shift-records/clock-in")
    suspend fun clockIn(): ShiftRecordResponse

    @POST("shift-records/clock-out")
    suspend fun clockOut(): ShiftRecordResponse
}
