package com.example.orderlytablet.ui.screens

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orderlytablet.ui.components.OrderCard

@Composable
fun OrdersScreen(viewModel: OrdersViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

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

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Нет активных заказов")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF3F6FB)) // общий фон страницы
                ) {
                    // Хедер: фон ДО padding, чтобы залил всю ширину
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

                    HorizontalDivider(thickness = 1.dp, color = Color(0x11000000))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(orders) { order -> OrderCard(order) }
                    }
                }


            }
        }
    }
}
