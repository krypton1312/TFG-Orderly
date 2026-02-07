package com.example.orderlyphone.ui.screen.orders

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.orderlyphone.domain.model.response.OrderWithTableResponse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val BgTop = Color(0xFF0B0B0C)
private val BgMid = Color(0xFF111113)
private val BgBot = Color(0xFF070708)
private val Card = Color(0xFF1A1A1D).copy(alpha = 0.92f)

enum class OrdersTab { ALL, DINE_IN, TAKEAWAY }

@Composable
fun ActiveOrdersScreen(
    vm: OrdersViewModel,
    onBack: () -> Unit,
    onNewOrder: () -> Unit,
    onOpenOrder: (Long) -> Unit // ✅ callback
)
 {
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
        when (val s = state) {
            OrdersState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }

            is OrdersState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error)
                }
            }

            OrdersState.Idle -> Unit

            is OrdersState.Success -> {

                val filteredOrders = remember(s.orders, query, tab) {
                    s.orders.filter { order ->
                        val title =
                            if (order.tableName.equals("Sin mesa", true))
                                "Cuenta #${order.order.orderId}"
                            else order.tableName

                        val hasTable = !order.tableName.equals("Sin mesa", true)

                        val matchesQuery =
                            query.isBlank() || (title?.contains(query, ignoreCase = true) == true)

                        val matchesTab = when (tab) {
                            OrdersTab.ALL -> true
                            OrdersTab.DINE_IN -> hasTable
                            OrdersTab.TAKEAWAY -> !hasTable
                        }

                        matchesQuery && matchesTab
                    }
                }

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
                        placeholder = "Search table..."
                    )

                    OrdersGrid(
                        orders = filteredOrders,
                        onOpenOrder = onOpenOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
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
private fun OrdersGrid(
    orders: List<OrderWithTableResponse>,
    onOpenOrder: (Long) -> Unit, // ✅ только id
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp)
    ) {
        items(orders) { o ->
            OrderCard(
                order = o,
                onClick = { onOpenOrder(o.order.orderId) }, // ✅ тут
                modifier = Modifier.height(130.dp)
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderWithTableResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    val tableTitle =
        if (order.tableName.equals("Sin mesa", true))
            "Cuenta #${order.order.orderId}"
        else (order.tableName ?: "Mesa")

    val total = order.order.total
    val totalText = total.formatEuro()

    val GreenAvailable = Color(0xFF35D07F)
    val RedOccupied = Color(0xFFFF5A5A)

    val isAvailable = (total == null) || total.compareTo(BigDecimal.ZERO) == 0
    val statusText = if (isAvailable) "Disponible" else "Ocupado"
    val statusColor = if (isAvailable) GreenAvailable else RedOccupied

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = shape,
        color = Card,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            if (order.tableName == null) Icons.Filled.Waves else Icons.Filled.LocalDining,
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = tableTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = totalText,
                    color = Orange,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

fun BigDecimal?.formatEuro(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    return nf.format(this ?: BigDecimal.ZERO)
}
