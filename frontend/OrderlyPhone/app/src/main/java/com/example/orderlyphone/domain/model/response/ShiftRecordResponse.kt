package com.example.orderlyphone.domain.model.response

import java.time.LocalDateTime

data class ShiftRecordResponse(
    val id: Long,
    val employeeId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val notes: String
)
