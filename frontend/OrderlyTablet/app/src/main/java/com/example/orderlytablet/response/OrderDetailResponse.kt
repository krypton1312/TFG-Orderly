package com.example.orderlytablet.response

data class OrderDetailResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val orderId: Long,
    val comment: String,
    val amount: Int,
    val unitPrice: Double,
    val status: String,
    val paymentMethod: String
){
}