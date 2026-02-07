package com.example.orderlyphone.ui.screen.orderDetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.remote.OrderDetailApi
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val api: OrderDetailApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"]) {
        "orderId is missing in SavedStateHandle"
    }

    private val _state = MutableStateFlow<OrderDetailState>(OrderDetailState.Idle)
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    private val _items = MutableStateFlow<List<OrderDetailsResponse>>(emptyList())
    val items: StateFlow<List<OrderDetailsResponse>> = _items.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = OrderDetailState.Loading
            try {
                val response = api.getOrderDetails(orderId)
                Log.d("OrderDetailVM", "orderId=$orderId details=$response")

                _items.value = response
                _state.value = OrderDetailState.Success(response)
            } catch (e: Exception) {
                _state.value = OrderDetailState.Error(e.message ?: e.toString())
            }
        }
    }

    fun increase(item: OrderDetailsResponse) {
        _items.value = _items.value.map {
            if (it.id == item.id) it.copy(amount = it.amount + 1) else it
        }
    }

    fun decrease(item: OrderDetailsResponse) {
        _items.value = _items.value.map {
            if (it.id == item.id) it.copy(amount = (it.amount - 1).coerceAtLeast(1)) else it
        }
    }

    fun remove(item: OrderDetailsResponse) {
        _items.value = _items.value.filterNot { it.id == item.id }
    }

    fun clear() {
        _items.value = emptyList()
    }

    fun getOrderId(): Long = orderId
}
