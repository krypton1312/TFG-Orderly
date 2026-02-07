package com.example.orderlyphone.ui.screen.orderDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val BgTop = Color(0xFF0B0B0C)
private val BgMid = Color(0xFF111113)
private val BgBot = Color(0xFF070708)
private val Card = Color(0xFF1A1A1D).copy(alpha = 0.92f)

@Composable
fun OrderDetailScreen(
    vm: OrderDetailViewModel,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onFireOrder: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val items by vm.items.collectAsState()
    val orderId = vm.getOrderId()

    val total = remember(items) {
        items.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.unitPrice.multiply(item.amount.toBigDecimal())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgMid, BgBot)))
            .statusBarsPadding()
    ) {
        when (val s = state) {
            OrderDetailState.Idle, OrderDetailState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }

            is OrderDetailState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is OrderDetailState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                ) {
                    // HEADER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Current Order",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Orange.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ORDER #$orderId",
                                    color = Orange,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        TextButton(onClick = { vm.clear() }) {
                            Text("Clear", color = Orange)
                        }
                    }

                    // LIST
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            OrderItemCard(
                                item = item,
                                onIncrease = { vm.increase(item) },
                                onDecrease = { vm.decrease(item) },
                                onRemove = { vm.remove(item) }
                            )
                        }

                        item {
                            TotalOnlyCard(total = total)
                        }
                    }
                }

                // BOTTOM BAR
                BottomBar(
                    total = total,
                    onAddItem = onAddItem,
                    onFireOrder = onFireOrder,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun OrderItemCard(
    item: OrderDetailsResponse,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.unitPrice.formatEuro(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "x${item.amount}",
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRemove) {
                    Text("Remove", color = Color(0xFFFF5A5A))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) {
                        Text("−", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = item.amount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onIncrease) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalOnlyCard(total: BigDecimal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = total.formatEuro(),
                color = Orange,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun BottomBar(
    total: BigDecimal,
    onAddItem: () -> Unit,
    onFireOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onAddItem,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(999.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                )
            ) {
                Text("+ Add Item", color = Color.White)
            }

            Button(
                onClick = onFireOrder,
                modifier = Modifier.weight(1.4f),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    "Fire Order  ${total.formatEuro()}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun BigDecimal?.formatEuro(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    return nf.format(this ?: BigDecimal.ZERO)
}
