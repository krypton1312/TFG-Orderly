package com.example.orderlyphone.ui.screen.orders

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.response.OrderWithTableResponse
import com.example.orderlyphone.ui.screen.home.HomeState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val BgTop = Color(0xFF0B0B0C)
private val BgMid = Color(0xFF111113)
private val BgBot = Color(0xFF070708)
private val Card = Color(0xFF1A1A1D).copy(alpha = 0.92f)

enum class OrdersTab { ALL, DINE_IN, TAKEAWAY }

data class OrderUi(
    val statusLabel: String,
    val statusColor: Color,
    val tableTitle: String,     // "Table12" или "Takeaway"
    val subtitle: String,       // "Order #8829 • 4 items"
    val price: String,          // "$42.50"
    val rightIcon: @Composable () -> Unit
)

@Composable
fun ActiveOrdersScreen(
    vm: OrdersViewModel,
    onBack: () -> Unit,
    onNewOrder: () -> Unit, // можешь убрать и подать свои
) {
    val bg = Brush.verticalGradient(listOf(BgTop, BgMid, BgBot))
    var tab by remember { mutableStateOf(OrdersTab.ALL) }
    var query by remember { mutableStateOf("") }

    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadOrdersData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
    ) {
        when(val s = state){
            OrdersState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF8A3D))
                }
            }

            is OrdersState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is OrdersState.Success -> {

                // мягкое свечение сверху (как в рефе) — очень лёгкое
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-40).dp)
                        .blur(70.dp)
                        .background(Orange.copy(alpha = 0.08f), CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                        .padding(top = 10.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OrdersHeader(
                        title = "Active Orders",
                        onBack = onBack,
                        onNewOrder = onNewOrder
                    )

                    OrdersTabs(
                        selected = tab,
                        onSelected = { tab = it }
                    )

                    SearchBar(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search table or order..."
                    )

                    val filteredOrders = remember(s.orders, query, tab) {
                        s.orders
                            .filter { order ->
                                // --- поиск по tableName
                                val matchesQuery =
                                    query.isBlank() ||
                                            order.tableName
                                                ?.contains(query, ignoreCase = true) == true

                                // --- фильтр по табам
                                val matchesTab = when (tab) {
                                    OrdersTab.ALL -> true
                                    OrdersTab.DINE_IN -> order.tableName != null
                                    OrdersTab.TAKEAWAY -> order.tableName == null
                                }

                                matchesQuery && matchesTab
                            }
                    }

                    OrdersList(
                        orders = filteredOrders,
                        onAddItems = { /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                }
            }

            OrdersState.Idle -> Unit
        }
    }
}

@Composable
private fun OrdersHeader(
    title: String,
    onBack: () -> Unit,
    onNewOrder: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onNewOrder,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Orange)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New order",
                tint = Color(0xFF1B1B1B)
            )
        }
    }
}

@Composable
private fun OrdersTabs(
    selected: OrdersTab,
    onSelected: (OrdersTab) -> Unit
) {
    val items = listOf(
        OrdersTab.ALL to "Todas",
        OrdersTab.DINE_IN to "Con Mesa",
        OrdersTab.TAKEAWAY to "Sin Mesa"
    )

    val selectedIndex = when (selected) {
        OrdersTab.ALL -> 0
        OrdersTab.DINE_IN -> 1
        OrdersTab.TAKEAWAY -> 2
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // --- КНОПКИ ТАБОВ (вся ширина, равные зоны)
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEach { (tab, label) ->
                val isSelected = tab == selected

                TextButton(
                    onClick = { onSelected(tab) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Orange else Color.White.copy(alpha = 0.55f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // --- ТРЕК + АНИМИРОВАННЫЙ UNDERLINE
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            val tabWidth = maxWidth / items.size

            val targetOffset = tabWidth * selectedIndex
            val animatedOffset by animateDpAsState(
                targetValue = targetOffset,
                animationSpec = tween(durationMillis = 220),
                label = "tab-underline"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Orange)
            )
        }
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(placeholder, color = Color.White.copy(alpha = 0.35f))
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Orange,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<OrderWithTableResponse>,
    onAddItems: (OrderWithTableResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp)
    ) {
        items(orders) { o ->
            OrderCard(order = o, onAddItems = { onAddItems(o) })
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderWithTableResponse,
    onAddItems: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Card,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // статус
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "status",//order.statusLabel.uppercase(),
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = order.tableName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "subtitle",
                    color = Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAddItems,
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.85f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Add Items", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Text(
                        text = order.order.total.formatEuro(),
                        color = Orange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // круглый статус-иконка справа
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalDining,
                    contentDescription = null,
                    tint = Orange
                )
            }
        }
    }
}

/** Демо-данные, чтобы сразу увидеть UI */
private fun demoOrders(): List<OrderUi> = listOf(
    OrderUi(
        statusLabel = "Kitchen preparing",
        statusColor = Orange,
        tableTitle = "Table12",
        subtitle = "Order #8829 • 4 items",
        price = "$42.50",
        rightIcon = {
            Icon(
                imageVector = Icons.Filled.LocalDining,
                contentDescription = null,
                tint = Orange
            )
        }
    ),
    OrderUi(
        statusLabel = "Just seated",
        statusColor = Color.White.copy(alpha = 0.55f),
        tableTitle = "Table 04",
        subtitle = "Order #8830 • 2 items",
        price = "$18.00",
        rightIcon = {
            Icon(
                imageVector = Icons.Filled.Waves,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.65f)
            )
        }
    ),
    OrderUi(
        statusLabel = "Ready to serve",
        statusColor = Color(0xFF35D07F),
        tableTitle = "Table21",
        subtitle = "Order #8831 • 6 items",
        price = "$112.40",
        rightIcon = {
            Icon(
                imageVector = Icons.Filled.LocalDining,
                contentDescription = null,
                tint = Color(0xFF35D07F)
            )
        }
    )
)
fun BigDecimal?.formatEuro(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    if (this == null) return nf.format(BigDecimal.ZERO)
    return nf.format(this)
}
