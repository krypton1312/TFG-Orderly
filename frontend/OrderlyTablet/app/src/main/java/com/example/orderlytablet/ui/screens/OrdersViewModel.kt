package com.example.orderlytablet.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import com.example.orderlytablet.services.OrderWebSocketClient
import com.example.orderlytablet.services.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<OrderWithOrderDetailResponse>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

class OrdersViewModel : ViewModel() {
    // В ViewModel
    var isRefreshing = MutableStateFlow(false)
        private set

    fun refreshOrders() {
        viewModelScope.launch {
            isRefreshing.value = true
            loadOrders()
            delay(500) // немного подержать эффект
            isRefreshing.value = false
        }
    }

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState

    // 🔹 Наш WebSocket клиент
    private val wsClient = OrderWebSocketClient()

    init {
        // Подключаем WebSocket при инициализации ViewModel
        connectWebSocket()
        // Загружаем заказы при первом запуске
        loadOrders()
    }

    private fun connectWebSocket() {
        val serverUrl = "ws://10.0.2.2:8080/ws/overview/tablet" // ⚠️ замени на свой IP

        wsClient.connect(serverUrl) {
            Log.d("OrdersViewModel", "📡 Received ORDER_CHANGED → Reloading orders")
            refreshOrders()
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = OrdersUiState.Loading
                val result = RetrofitClient.instance.getOrdersWithDetails()
                _uiState.value = OrdersUiState.Success(result)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = OrdersUiState.Error("❌ Не удалось загрузить заказы: ${e.localizedMessage}")
            }
        }
    }

    fun updateOrderDetailStatus(ids: List<Long>, newStatus: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateOrderDetailStatus(newStatus, ids)
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "❌ Ошибка запроса PUT: ${e.message}")
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
        Log.d("OrdersViewModel", "🔌 WebSocket disconnected")
    }
}
