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
import androidx.compose.ui.graphics.Color
import com.example.orderlytablet.data.TokenStore
import com.example.orderlytablet.services.RetrofitClient
import com.example.orderlytablet.ui.screens.ChangePasswordScreen
import com.example.orderlytablet.ui.screens.LoginScreen
import com.example.orderlytablet.ui.screens.LoginUiState
import com.example.orderlytablet.ui.screens.LoginViewModel
import com.example.orderlytablet.ui.screens.NoSessionScreen
import com.example.orderlytablet.ui.screens.OrdersScreen
import com.example.orderlytablet.ui.screens.OrdersViewModel
import com.example.orderlytablet.ui.screens.SessionState

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
                is LoginUiState.NavigateToOrders -> OrdersScreenWithSessionGate()
            }
        }
    }
}

/**
 * Phase 10 — D-01/D-02: wraps OrdersScreen with a session-state gate.
 *
 * The OrdersViewModel is obtained here (shared with OrdersScreen via viewModel())
 * so a single VM instance handles both the session gate and order loading.
 *
 * State routing:
 *   SessionState.Loading  → CircularProgressIndicator (HTTP check in progress)
 *   SessionState.Blocked  → NoSessionScreen (no OPEN cash session; waits for WS)
 *   SessionState.Open     → OrdersScreen (normal kitchen display)
 */
@Composable
fun OrdersScreenWithSessionGate() {
    val ordersViewModel: OrdersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val sessionState by ordersViewModel.sessionState.collectAsState()

    when (sessionState) {
        is SessionState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFFA100))
            }
        }
        is SessionState.Blocked -> NoSessionScreen()
        is SessionState.Open -> OrdersScreen(viewModel = ordersViewModel)
    }
}
