package com.example.orderlytablet.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlytablet.data.AuthApi
import com.example.orderlytablet.data.LoginRequest
import com.example.orderlytablet.data.RefreshRequest
import com.example.orderlytablet.data.TokenStore
import com.example.orderlytablet.services.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Loading : LoginUiState()
    object ShowLogin : LoginUiState()
    object NavigateToOrders : LoginUiState()
    data class LoginError(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val authApi: AuthApi = RetrofitClient.authInstance.create(AuthApi::class.java)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        viewModelScope.launch { tryRestoreSession() }
    }

    private suspend fun tryRestoreSession() {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken != null) {
            try {
                val resp = authApi.refresh(RefreshRequest(refreshToken))
                tokenStore.save(resp.token, resp.refreshToken)
                _uiState.value = LoginUiState.NavigateToOrders
                return
            } catch (e: Exception) {
                tokenStore.clear()
            }
        }
        _uiState.value = LoginUiState.ShowLogin
    }

    fun login(email: String, password: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val resp = authApi.login(LoginRequest(email, password))
                tokenStore.save(resp.token, resp.refreshToken)
                _uiState.value = LoginUiState.NavigateToOrders
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    401, 403 -> "Credenciales incorrectas."
                    else -> "Error del servidor (${e.code()})."
                }
                _uiState.value = LoginUiState.LoginError(msg)
            } catch (e: java.net.ConnectException) {
                _uiState.value = LoginUiState.LoginError("No se puede conectar al servidor. Comprueba la red.")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.LoginError("Error: ${e.message}")
            }
        }
    }
}
