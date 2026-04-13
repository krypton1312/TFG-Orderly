package com.example.orderlyphone.ui.screen.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    data object Idle : ChangePasswordState()
    data object Loading : ChangePasswordState()
    data object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val state: StateFlow<ChangePasswordState> = _state

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _state.value = ChangePasswordState.Loading
            try {
                repo.changePassword(newPassword)
                _state.value = ChangePasswordState.Success
            } catch (e: Exception) {
                _state.value = ChangePasswordState.Error(e.message ?: "Error al cambiar la contraseña")
            }
        }
    }
}
