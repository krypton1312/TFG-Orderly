package com.example.orderlytablet.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import com.example.orderlytablet.services.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 🔹 Возможные состояния экрана
sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<OrderWithOrderDetailResponse>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

class OrdersViewModel : ViewModel() {

    // Один поток для всего состояния экрана
    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            try {
                val result = RetrofitClient.instance.getOrdersWithDetails()
                _uiState.value = OrdersUiState.Success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = OrdersUiState.Error("Не удалось загрузить заказы: ${e.localizedMessage}")
            }
        }
    }
}
