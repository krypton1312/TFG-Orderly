package com.example.orderlyphone.domain.model.response

import java.math.BigDecimal

data class OrderDetailsResponse(
    val id: Long,
    val productId: Long,
    val name: String,
    val orderId: Long,
    val comment: String?,
    val amount: Int,
    val unitPrice: BigDecimal,
    val status: String,
    val paymentMethod: String?,
    val createdAt: String,
    val destination: String,
    val batchId: String
)
