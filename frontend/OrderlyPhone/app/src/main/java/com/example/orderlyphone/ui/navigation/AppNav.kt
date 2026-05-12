package com.example.orderlyphone.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.orderlyphone.data.remote.AuthEventBus
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.orderlyphone.data.remote.websocket.OrderWebSocketClient
import com.example.orderlyphone.domain.model.DraftOrderDetailUi
import com.example.orderlyphone.ui.screen.home.HomeScreen
import com.example.orderlyphone.ui.screen.home.HomeViewModel
import com.example.orderlyphone.ui.screen.login.LoginScreen
import com.example.orderlyphone.ui.screen.login.LoginViewModel
import com.example.orderlyphone.ui.screen.changepassword.ChangePasswordScreen
import com.example.orderlyphone.ui.screen.orderDetails.OrderDetailScreen
import com.example.orderlyphone.ui.screen.orderDetails.OrderDetailViewModel
import com.example.orderlyphone.ui.screen.orderDetails.SubmitDraftResult
import com.example.orderlyphone.ui.screen.orders.ActiveOrdersScreen
import com.example.orderlyphone.ui.screen.orders.OrdersViewModel
import com.example.orderlyphone.ui.screen.productConfigurator.ProductConfiguratorScreen
import com.example.orderlyphone.ui.screen.productConfigurator.ProductConfiguratorViewModel
import com.example.orderlyphone.ui.screen.products.ProductsScreen
import com.example.orderlyphone.ui.screen.products.ProductsViewModel
import com.example.orderlyphone.ui.screen.settings.SettingsScreen
import com.example.orderlyphone.ui.screen.settings.SettingsViewModel
import com.example.orderlyphone.ui.screen.tablePicker.TablePickerScreen
import com.example.orderlyphone.ui.screen.tablePicker.TablePickerViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.launch

private const val LoginRoute = "login"
private const val ChangePasswordRoute = "change_password"
private const val HomeRoute = "home"
private const val OrdersRoute = "orders"
private const val TablePickerRoute = "table_picker"
private const val NoTableRouteValue = "no-table"
private const val OrderDetailsRoute = "order_details/{orderId}/{tableRef}"
private const val ExistingProductsRoute = "products/{orderId}/{tableRef}"
private const val ExistingConfiguratorRoute = "product_configurator/{orderId}/{tableRef}/{categoryId}/{productId}"
private const val NewOrderGraphRoute = "order_flow/new/{tableRef}"
private const val NewProductsRoute = "products/new/{tableRef}"
private const val NewConfiguratorRoute = "product_configurator/new/{tableRef}/{categoryId}/{productId}"
private const val NewReviewRoute = "review/new/{tableRef}"
private const val SettingsRoute = "settings"
private const val SnackbarMessageKey = "snackbar_message"

private fun toTableRef(tableId: Long?) = tableId?.toString() ?: NoTableRouteValue

private fun existingOrderRoute(orderId: Long, tableId: Long?) = "order_details/$orderId/${toTableRef(tableId)}"
private fun existingProductsRoute(orderId: Long, tableId: Long?) = "products/$orderId/${toTableRef(tableId)}"
private fun existingConfiguratorRoute(orderId: Long, tableId: Long?, categoryId: Long, productId: Long) =
    "product_configurator/$orderId/${toTableRef(tableId)}/$categoryId/$productId"

private fun newOrderGraphRoute(tableId: Long?) = "order_flow/new/${toTableRef(tableId)}"
private fun newProductsRoute(tableId: Long?) = "products/new/${toTableRef(tableId)}"
private fun newConfiguratorRoute(tableId: Long?, categoryId: Long, productId: Long) =
    "product_configurator/new/${toTableRef(tableId)}/$categoryId/$productId"

private fun newReviewRoute(tableId: Long?) = "review/new/${toTableRef(tableId)}"

private fun NavBackStackEntry.tableIdOrNull(): Long? = arguments?.getString("tableRef")?.toLongOrNull()

private fun tableLabel(tableId: Long?): String = tableId?.let { "Mesa $it" } ?: "Sin mesa"

@Composable
fun AppNav(webSocketClient: OrderWebSocketClient) {
    val navController = rememberNavController()
    val loginVm: LoginViewModel = hiltViewModel()

    // Глобальный обработчик 401 — перебрасывает на логин из любого экрана
    LaunchedEffect(Unit) {
        AuthEventBus.unauthorizedEvent.collect {
            webSocketClient.disconnect()
            loginVm.logout()
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = LoginRoute
    ) {
        composable(LoginRoute) {
            LoginScreen(
                vm = loginVm,
                onSuccess = {
                    webSocketClient.connect()
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onMustChangePassword = {
                    navController.navigate(ChangePasswordRoute) {
                        popUpTo(LoginRoute) { inclusive = false }
                    }
                }
            )
        }

        composable(ChangePasswordRoute) {
            ChangePasswordScreen(
                onPasswordChanged = {
                    webSocketClient.connect()
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable(HomeRoute) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                vm = vm,
                onOrders = { navController.navigate(OrdersRoute) },
                onNewOrder = { navController.navigate(TablePickerRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
                onLogout = {
                    webSocketClient.disconnect()
                    loginVm.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        composable(OrdersRoute) {
            val vm: OrdersViewModel = hiltViewModel()

            ActiveOrdersScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onNewOrder = { navController.navigate(TablePickerRoute) },
                onOpenOrder = { orderId: Long, tableId: Long? ->
                    navController.navigate(existingOrderRoute(orderId, tableId))
                }
            )
        }

        composable(TablePickerRoute) {
            val vm: TablePickerViewModel = hiltViewModel()
            TablePickerScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpenOrder = { orderId, tableId ->
                    navController.navigate(existingOrderRoute(orderId, tableId))
                },
                onStartNewOrder = { tableId ->
                    navController.navigate(newOrderGraphRoute(tableId))
                }
            )
        }

        composable(
            route = OrderDetailsRoute,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("tableRef") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            val tableId = backStackEntry.tableIdOrNull()
            val vm: OrderDetailViewModel = hiltViewModel(backStackEntry)
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            BindQueuedSnackbar(backStackEntry = backStackEntry, snackbarHostState = snackbarHostState)

            OrderDetailScreen(
                vm = vm,
                orderLabel = "Cuenta #$orderId",
                tableLabel = tableLabel(tableId),
                snackbarHostState = snackbarHostState,
                onBack = { navController.popBackStack() },
                onAddItem = { navController.navigate(existingProductsRoute(orderId, tableId)) },
                onFireOrder = {
                    scope.launch {
                        when (vm.submitDraft()) {
                            SubmitDraftResult.ExistingOrderUpdated -> {
                                snackbarHostState.showSnackbar("Enviado a cocina")
                            }

                            else -> Unit
                        }
                    }
                }
            )
        }

        composable(
            route = ExistingProductsRoute,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("tableRef") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            val tableId = backStackEntry.tableIdOrNull()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(existingOrderRoute(orderId, tableId))
            }
            val orderVm: OrderDetailViewModel = hiltViewModel(parentEntry)
            val productsVm: ProductsViewModel = hiltViewModel(backStackEntry)
            val drafts by orderVm.draftRequests.collectAsState()

            ProductsScreen(
                vm = productsVm,
                orderLabel = "Cuenta #$orderId",
                tableLabel = tableLabel(tableId),
                draftCount = drafts.size,
                draftTotal = drafts.draftTotal(),
                onBack = { navController.popBackStack() },
                onSelectProduct = { categoryId, productId ->
                    navController.navigate(existingConfiguratorRoute(orderId, tableId, categoryId, productId))
                },
                onReviewOrder = { navController.popBackStack() }
            )
        }

        composable(
            route = ExistingConfiguratorRoute,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("tableRef") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("productId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            val tableId = backStackEntry.tableIdOrNull()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(existingOrderRoute(orderId, tableId))
            }
            val orderVm: OrderDetailViewModel = hiltViewModel(parentEntry)
            val configuratorVm: ProductConfiguratorViewModel = hiltViewModel(backStackEntry)

            ProductConfiguratorScreen(
                vm = configuratorVm,
                onBack = { navController.popBackStack() },
                onConfirm = { line ->
                    orderVm.addConfiguredDraft(line)
                    navController.popBackStack()
                }
            )
        }

        composable(SettingsRoute) {
            val vm: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        navigation(
            route = NewOrderGraphRoute,
            startDestination = NewProductsRoute,
            arguments = listOf(
                navArgument("tableRef") { type = NavType.StringType }
            )
        ) {
            composable(
                route = NewProductsRoute,
                arguments = listOf(
                    navArgument("tableRef") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tableId = backStackEntry.tableIdOrNull()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(newOrderGraphRoute(tableId))
                }
                val orderVm: OrderDetailViewModel = hiltViewModel(parentEntry)
                val productsVm: ProductsViewModel = hiltViewModel(backStackEntry)
                val drafts by orderVm.draftRequests.collectAsState()

                ProductsScreen(
                    vm = productsVm,
                    orderLabel = "Nuevo pedido",
                    tableLabel = tableLabel(tableId),
                    draftCount = drafts.size,
                    draftTotal = drafts.draftTotal(),
                    onBack = { navController.popBackStack() },
                    onSelectProduct = { categoryId, productId ->
                        navController.navigate(newConfiguratorRoute(tableId, categoryId, productId))
                    },
                    onReviewOrder = { navController.navigate(newReviewRoute(tableId)) }
                )
            }

            composable(
                route = NewConfiguratorRoute,
                arguments = listOf(
                    navArgument("tableRef") { type = NavType.StringType },
                    navArgument("categoryId") { type = NavType.LongType },
                    navArgument("productId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val tableId = backStackEntry.tableIdOrNull()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(newOrderGraphRoute(tableId))
                }
                val orderVm: OrderDetailViewModel = hiltViewModel(parentEntry)
                val configuratorVm: ProductConfiguratorViewModel = hiltViewModel(backStackEntry)

                ProductConfiguratorScreen(
                    vm = configuratorVm,
                    onBack = { navController.popBackStack() },
                    onConfirm = { line ->
                        orderVm.addConfiguredDraft(line)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = NewReviewRoute,
                arguments = listOf(
                    navArgument("tableRef") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tableId = backStackEntry.tableIdOrNull()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(newOrderGraphRoute(tableId))
                }
                val orderVm: OrderDetailViewModel = hiltViewModel(parentEntry)
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                OrderDetailScreen(
                    vm = orderVm,
                    orderLabel = "Nuevo pedido",
                    tableLabel = tableLabel(tableId),
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onAddItem = { navController.navigate(newProductsRoute(tableId)) },
                    onFireOrder = {
                        scope.launch {
                            when (val result = orderVm.submitDraft()) {
                                is SubmitDraftResult.OpenedNewOrder -> {
                                    val targetRoute = existingOrderRoute(result.orderId, result.tableId)
                                    navController.navigate(targetRoute) {
                                        popUpTo(newOrderGraphRoute(result.tableId)) { inclusive = true }
                                    }
                                    navController
                                        .getBackStackEntry(targetRoute)
                                        .savedStateHandle[SnackbarMessageKey] = "Enviado a cocina"
                                }

                                SubmitDraftResult.ExistingOrderUpdated -> {
                                    snackbarHostState.showSnackbar("Enviado a cocina")
                                }

                                else -> Unit
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BindQueuedSnackbar(
    backStackEntry: NavBackStackEntry,
    snackbarHostState: SnackbarHostState
) {
    val snackbarMessage by backStackEntry.savedStateHandle
        .getStateFlow<String?>(SnackbarMessageKey, null)
        .collectAsState()

    LaunchedEffect(snackbarMessage) {
        if (!snackbarMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            backStackEntry.savedStateHandle[SnackbarMessageKey] = null
        }
    }
}

private fun List<DraftOrderDetailUi>.draftTotal(): BigDecimal {
    return fold(BigDecimal.ZERO) { acc, item ->
        acc.add(item.line.lineTotal)
    }.setScale(2, RoundingMode.HALF_UP)
}