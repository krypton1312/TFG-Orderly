package com.example.orderlyphone.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    init {
        viewModelScope.launch {
            if (tokenStore.load() != null) {
                _state.value = LoginState.Success
            }
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val mustChangePassword = repo.login(identifier, password)
                _state.value = if (mustChangePassword) LoginState.MustChangePassword else LoginState.Success
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        _state.value = LoginState.Idle  // синхронно — до navigate
        viewModelScope.launch {
            tokenStore.clear()
        }
    }
}
