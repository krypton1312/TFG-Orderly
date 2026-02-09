package com.example.orderlyphone.domain.model.request

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDetailRequest(
    val productId: Long,
    val orderId: Long,
    val name: String,
    val comment: String?,
    val amount: Int,
    val unitPrice: BigDecimal,
    val status: String,
    val paymentMethod: String?,
    val batchId: String,
    val createdAt: LocalDateTime,
    val cashSessionId: Long
)
