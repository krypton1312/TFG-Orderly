package com.example.orderlyphone.domain.model.response

import java.math.BigDecimal

data class OrderWithTableResponse(
    val tableId: Long,
    val tableName: String,
    val order: OrderSummary
)

data class OrderSummary(
    val orderId: Long,
    val total: BigDecimal
)