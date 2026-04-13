package com.example.orderlytablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.orderlytablet.data.TokenStore
import com.example.orderlytablet.services.RetrofitClient
import com.example.orderlytablet.ui.screens.ChangePasswordScreen
import com.example.orderlytablet.ui.screens.LoginScreen
import com.example.orderlytablet.ui.screens.LoginUiState
import com.example.orderlytablet.ui.screens.LoginViewModel
import com.example.orderlytablet.ui.screens.OrdersScreen

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(TokenStore(this))
        setContent {
            OrderlyTabletApp(loginViewModel)
        }
    }
}

@Composable
fun OrderlyTabletApp(loginViewModel: LoginViewModel) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val uiState by loginViewModel.uiState.collectAsState()
            when (uiState) {
                is LoginUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoginUiState.ShowLogin,
                is LoginUiState.LoginError -> LoginScreen(viewModel = loginViewModel)
                is LoginUiState.MustChangePassword -> ChangePasswordScreen(viewModel = loginViewModel)
                is LoginUiState.NavigateToOrders -> OrdersScreen()
            }
        }
    }
}
