package com.example.orderlyphone.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.EmployeeApi
import com.example.orderlyphone.data.remote.OverviewApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val overviewApi: OverviewApi
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)

    val state: StateFlow<HomeState> = _state

    fun loadEmployeeData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val response = overviewApi.getDashboardStart()
                Log.d("CheckOverview", response.toString())
                _state.value = HomeState.Success(
                    response
                )

            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: e.toString())
            }
        }
    }
}