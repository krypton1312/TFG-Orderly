package com.example.orderlyphone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.orderlyphone.ui.screen.home.HomeScreen
import com.example.orderlyphone.ui.screen.home.HomeViewModel
import com.example.orderlyphone.ui.screen.login.LoginScreen
import com.example.orderlyphone.ui.screen.login.LoginViewModel
import com.example.orderlyphone.ui.screen.orders.ActiveOrdersScreen
import com.example.orderlyphone.ui.screen.orders.OrdersViewModel

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            val vm: LoginViewModel = hiltViewModel()
            LoginScreen(
                vm = vm,
                onSuccess = { nav.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("home") {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                vm = vm,
                onOrders = { nav.navigate("orders") },
                onNewOrder = { /* TODO */ },
                onShiftToggle = { /* TODO */ },
                onSettings = { /* TODO */ },
                onLogout = { nav.navigate("login") { popUpTo("home"){ inclusive = true } } }
            )
        }
        composable("orders") {
            val vm: OrdersViewModel = hiltViewModel()
            ActiveOrdersScreen(
                vm = vm,
                onBack = { nav.navigate("home") },
                onNewOrder = { /* TODO */ }
            )
        }
    }
}
