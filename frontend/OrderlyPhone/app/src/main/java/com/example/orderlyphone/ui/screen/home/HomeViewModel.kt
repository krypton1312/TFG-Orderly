package com.example.orderlyphone.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.CashSessionStore
import com.example.orderlyphone.data.remote.OverviewApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val overviewApi: OverviewApi,
    private val cashSessionStore: CashSessionStore
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)

    val state: StateFlow<HomeState> = _state

    fun loadEmployeeData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val response = overviewApi.getDashboardStart()
                _state.value = HomeState.Success(
                    response
                )
                val cashSessionId = response.cashSessionId

                if (cashSessionId != null) {
                    cashSessionStore.saveCashSessionId(cashSessionId)
                } else {
                    cashSessionStore.clear()
                }

            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: e.toString())
            }
        }
    }
}