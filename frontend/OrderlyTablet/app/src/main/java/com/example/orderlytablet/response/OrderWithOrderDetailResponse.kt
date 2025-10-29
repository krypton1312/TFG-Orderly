package com.example.orderlytablet.response

data class OrderWithOrderDetailResponse(
    val id: Long,
    val tableName: String,
    val details: List<OrderDetailSummary>,
    val datetime: String
)

data class OrderDetailSummary(
    val id: Long,
    val productName: String,
    val comment: String,
    val amount: Int,
    val status: String
)