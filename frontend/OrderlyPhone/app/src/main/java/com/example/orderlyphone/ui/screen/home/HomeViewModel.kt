package com.example.orderlyphone.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.CashSessionStore
import com.example.orderlyphone.data.remote.OverviewApi
import com.example.orderlyphone.data.remote.ShiftRecordApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val overviewApi: OverviewApi,
    private val cashSessionStore: CashSessionStore,
    private val shiftRecordApi: ShiftRecordApi
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)
    val state: StateFlow<HomeState> = _state

    private val _shiftLoading = MutableStateFlow(false)
    val shiftLoading: StateFlow<Boolean> = _shiftLoading

    /**
     * Phase 10 (D-03): observable cash-session presence used to gate the
     * "Nuevo pedido" CTA on HomeScreen. Null = no OPEN session → button
     * is disabled and the info banner is shown.
     */
    val cashSessionId: StateFlow<Long?> = cashSessionStore.cashSessionId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun loadEmployeeData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val response = overviewApi.getDashboardStart()
                _state.value = HomeState.Success(response)
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

    fun clockIn() {
        viewModelScope.launch {
            _shiftLoading.value = true
            try {
                shiftRecordApi.clockIn()
                loadEmployeeData()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "clockIn failed", e)
            } finally {
                _shiftLoading.value = false
            }
        }
    }

    fun clockOut() {
        viewModelScope.launch {
            _shiftLoading.value = true
            try {
                shiftRecordApi.clockOut()
                loadEmployeeData()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "clockOut failed", e)
            } finally {
                _shiftLoading.value = false
            }
        }
    }
}