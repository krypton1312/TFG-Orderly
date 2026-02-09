package com.example.orderlyphone.domain.model

import com.example.orderlyphone.domain.model.request.OrderDetailRequest

data class DraftOrderDetailUi(
    val uiId: String,
    val req: OrderDetailRequest
)

