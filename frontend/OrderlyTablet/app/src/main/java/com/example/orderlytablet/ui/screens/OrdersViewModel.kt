package com.example.orderlytablet.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import com.example.orderlytablet.services.OrderWebSocketClient
import com.example.orderlytablet.services.RetrofitClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<OrderWithOrderDetailResponse>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

class OrdersViewModel : ViewModel() {

    private val wsClient = OrderWebSocketClient()

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState

    var isRefreshing = MutableStateFlow(false)
        private set

    private var reloadJob: Job? = null
    var isSingleUpdate = false

    init {
        connectWebSocket()
        loadOrders()
    }

    private fun connectWebSocket() {
        val serverUrl = "ws://192.168.1.136:8080/ws/overview/tablet"

        wsClient.connect(serverUrl) { event ->
            Log.d("OrdersViewModel", "📡 WS Event: ${event.type}")

            when (event.type) {
                "ORDER_DETAIL_CREATED",
                "ORDER_DETAIL_UPDATED",
                "ORDER_DETAIL_DELETED",
                "ORDER_DETAIL_STATUS_CHANGED" -> {
                    if (event.overviewId != null) {
                        updateSingleOrder(event.overviewId)
                    } else {
                        reloadDebounced()
                    }
                }
                "ORDER_TOTAL_CHANGED",
                "ORDER_CREATED",
                "ORDER_DELETED" -> reloadDebounced()
            }
        }
    }

    private fun reloadDebounced(delayMs: Long = 700) {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            delay(delayMs)
            refreshOrders()
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            isRefreshing.value = true
            loadOrders()
            delay(300)
            isRefreshing.value = false
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.instance.getOrdersWithDetails()
                _uiState.value = OrdersUiState.Success(result)
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "❌ Failed to load: ${e.message}")
                _uiState.value = OrdersUiState.Error("Не удалось загрузить заказы: ${e.localizedMessage}")
            }
        }
    }

    private fun updateSingleOrder(overviewId: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is OrdersUiState.Success) {
                    isSingleUpdate = true
                    val refreshedOrders = RetrofitClient.instance.getOrdersWithDetails()
                    val updatedOrder = refreshedOrders.find { it.overviewId == overviewId }

                    val newList = if (updatedOrder != null) {
                        currentState.orders.map {
                            if (it.overviewId == overviewId) updatedOrder else it
                        }
                    } else {
                        currentState.orders.filterNot { it.overviewId == overviewId }
                    }

                    _uiState.value = OrdersUiState.Success(newList)
                    isSingleUpdate = false
                } else {
                    loadOrders()
                }
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "⚠️ Ошибка updateSingleOrder: ${e.message}")
                isSingleUpdate = false
            }
        }
    }

    fun updateOrderDetailStatus(ids: List<Long>, newStatus: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateOrderDetailStatus(newStatus, ids)
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "❌ Ошибка PUT: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        wsClient.disconnect()
        super.onCleared()
        Log.d("OrdersViewModel", "🔌 WebSocket disconnected")
    }
}
