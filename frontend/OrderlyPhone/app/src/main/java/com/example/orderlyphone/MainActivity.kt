package com.example.orderlyphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orderlyphone.data.local.TokenStore
import com.example.orderlyphone.data.remote.ApiClient
import com.example.orderlyphone.data.repository.AuthRepository
import com.example.orderlyphone.ui.navigation.AppNav
import com.example.orderlyphone.ui.screen.login.LoginViewModel
import com.example.orderlyphone.util.SimpleVmFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenStore = TokenStore(applicationContext)
        val authApi = ApiClient.authApi(tokenStore)
        val repo = AuthRepository(authApi, tokenStore)

        setContent {
            val loginVm: LoginViewModel = viewModel(
                factory = SimpleVmFactory { LoginViewModel(repo) }
            )
            AppNav(loginVm)
        }
    }
}
