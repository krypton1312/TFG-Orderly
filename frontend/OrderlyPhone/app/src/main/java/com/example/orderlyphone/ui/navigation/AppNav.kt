package com.example.orderlyphone.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.orderlyphone.ui.screen.home.HomeScreen
import com.example.orderlyphone.ui.screen.home.HomeViewModel
import com.example.orderlyphone.ui.screen.login.LoginScreen
import com.example.orderlyphone.ui.screen.login.LoginViewModel
import com.example.orderlyphone.ui.screen.orderDetails.OrderDetailScreen
import com.example.orderlyphone.ui.screen.orderDetails.OrderDetailViewModel
import com.example.orderlyphone.ui.screen.orders.ActiveOrdersScreen
import com.example.orderlyphone.ui.screen.orders.OrdersViewModel
import com.example.orderlyphone.ui.screen.products.ProductsScreen
import com.example.orderlyphone.ui.screen.products.ProductsViewModel

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        /* ───────────── LOGIN ───────────── */
        composable("login") {
            val vm: LoginViewModel = hiltViewModel()
            LoginScreen(
                vm = vm,
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        /* ───────────── HOME ───────────── */
        composable("home") {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                vm = vm,
                onOrders = { navController.navigate("orders") },
                onNewOrder = { /* TODO */ },
                onShiftToggle = { /* TODO */ },
                onSettings = { /* TODO */ },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        /* ───────────── ORDERS LIST ───────────── */
        composable("orders") {
            val vm: OrdersViewModel = hiltViewModel()

            ActiveOrdersScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onNewOrder = { /* TODO */ },
                onOpenOrder = { orderId ->
                    navController.navigate("order_details/$orderId")
                }
            )
        }

        /* ───────────── ORDER DETAILS ───────────── */
        composable(
            route = "order_details/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            val vm: OrderDetailViewModel = hiltViewModel(backStackEntry)

            OrderDetailScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onAddItem = { navController.navigate("products/$orderId") },
                onFireOrder = { /* TODO */ }
            )
        }

        /* ───────────── PRODUCTS ───────────── */
        composable(
            route = "products/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable

            // ✅ Берём entry родительского экрана, чтобы получить ТОТ ЖЕ OrderDetailViewModel
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("order_details/$orderId")
            }
            val orderVm: OrderDetailViewModel = hiltViewModel(parentEntry)

            val productsVm: ProductsViewModel = hiltViewModel(backStackEntry)

            ProductsScreen(
                vm = productsVm,
                orderId = orderId,
                onReviewOrder = { cart ->
                    // ✅ Вот этого тебе не хватало:
                    orderVm.addProductsToOrder(cart)
                    navController.popBackStack()
                }
            )
        }

    }
}
