package com.example.orderlyphone.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                repo.login(email, password)
                _state.value = LoginState.Success
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }
}
