package com.example.orderlyphone.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.EmployeeApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val employeeApi: EmployeeApi
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)

    val state: StateFlow<HomeState> = _state

    fun loadEmployeeData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                _state.value = HomeState.Success(
                    employeeApi.getCurrentEmployee()
                )
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: e.toString())
            }
        }
    }
}