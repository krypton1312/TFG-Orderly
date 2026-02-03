package com.example.orderlytablet.response

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderResponse(
    val id:Long,
    val dateTime: String,
    val state: String,
    val paymentMethod: String,
    val total: BigDecimal,
    val idEmployee: Long,
    val idClient: Long,
    val restTable: RestTableResponse
)