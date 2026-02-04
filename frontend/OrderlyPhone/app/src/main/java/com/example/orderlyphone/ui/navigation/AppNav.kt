package com.example.orderlyphone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.orderlyphone.ui.screen.login.LoginScreen
import com.example.orderlyphone.ui.screen.login.LoginViewModel

@Composable
fun AppNav(
    loginVm: LoginViewModel
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                vm = loginVm,
                onSuccess = { nav.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("home") {
            // TODO: твой главный экран
        }
    }
}
