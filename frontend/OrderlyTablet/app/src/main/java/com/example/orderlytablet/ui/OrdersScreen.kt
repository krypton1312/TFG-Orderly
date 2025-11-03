package com.example.orderlytablet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orderlytablet.ui.components.OrderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: OrdersViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is OrdersUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is OrdersUiState.Error -> {
                val message = (uiState as OrdersUiState.Error).message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = message, color = Color.Red)
                }
            }

            is OrdersUiState.Success -> {
                val orders = (uiState as OrdersUiState.Success).orders

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF3F6FB))
                ) {
                    // 🔹 Заголовок
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pedidos activos",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 🔹 Вкладки
                    var selectedTabIndex by remember { mutableStateOf(0) }
                    val tabs = listOf("Todo", "Bebidas", "Barra", "Cocina")

                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                                color = Color(0xFFFFA100),
                                width = 100.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val icon = when (index) {
                                0 -> Icons.AutoMirrored.Filled.List
                                1 -> Icons.Filled.LocalBar
                                2 -> Icons.Filled.Restaurant
                                3 -> Icons.Filled.Kitchen
                                else -> Icons.AutoMirrored.Filled.List
                            }

                            Tab(
                                icon = { Icon(icon, contentDescription = title) },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color(0x11000000))

                    val displayedOrders = remember(selectedTabIndex, orders) {
                        orders.map { order ->
                            val filteredDetails = when (selectedTabIndex) {
                                1 -> order.details.filter { it.destination.equals("DRINKS", ignoreCase = true) }
                                2 -> order.details.filter { it.destination.equals("BAR", ignoreCase = true) }
                                3 -> order.details.filter { it.destination.equals("KITCHEN", ignoreCase = true) }
                                else -> order.details
                            }
                            order.copy(
                                overviewId = order.overviewId,
                                details = filteredDetails
                            )
                        }.filter { it.details.isNotEmpty() } // показываем только те, где есть детали
                    }

                    // 🔹 Контент
                    if (displayedOrders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No hay pedidos activos.")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 280.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            itemsIndexed(
                                displayedOrders,
                                key = { _, order -> order.overviewId }
                            ) { _, order ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 350)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 350))
                                ) {
                                    OrderCard(
                                        order = order,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔹 Мягкая анимация обновления (Overlay)
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF3F51B5))
            }
        }
    }
}
