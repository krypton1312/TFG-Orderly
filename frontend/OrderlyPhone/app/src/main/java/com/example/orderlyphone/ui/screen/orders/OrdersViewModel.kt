package com.example.orderlyphone.ui.screen.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.remote.OverviewApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val overviewApi: OverviewApi
): ViewModel(){
    private val _state = MutableStateFlow<OrdersState>(OrdersState.Idle)

    val state: StateFlow<OrdersState> = _state

    fun loadOrdersData(){
        viewModelScope.launch {
            _state.value = OrdersState.Loading
            try {
                val response = overviewApi.getOrdersWithTable()
                Log.d("CheckOverviewOrders", response.toString())
                _state.value = OrdersState.Success(
                    response
                )

            } catch (e: Exception) {
                _state.value = OrdersState.Error(e.message ?: e.toString())
            }

        }
    }
}