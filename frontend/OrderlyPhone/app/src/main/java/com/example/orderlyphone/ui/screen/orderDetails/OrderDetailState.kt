package com.example.orderlyphone.ui.screen.orderDetails

import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import com.example.orderlyphone.domain.model.response.OrderWithTableResponse

sealed class OrderDetailState {
    data object Idle : OrderDetailState()
    data object Loading : OrderDetailState()
    data class Success(val orderDetails: List<OrderDetailsResponse>) : OrderDetailState()
    data class Error(val message: String) : OrderDetailState()
}