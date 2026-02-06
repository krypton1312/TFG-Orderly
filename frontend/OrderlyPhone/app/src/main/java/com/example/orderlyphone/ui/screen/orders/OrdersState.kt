package com.example.orderlyphone.ui.screen.orders

import com.example.orderlyphone.domain.model.response.OrderWithTableResponse

sealed class OrdersState {
    data object Idle : OrdersState()
    data object Loading : OrdersState()
    data class Success(val orders: List<OrderWithTableResponse>) : OrdersState()
    data class Error(val message: String) : OrdersState()
}