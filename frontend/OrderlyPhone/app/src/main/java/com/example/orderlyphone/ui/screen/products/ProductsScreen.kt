package com.example.orderlyphone.ui.screen.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.response.CategoryResponse
import com.example.orderlyphone.domain.model.response.ProductResponse
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ProductsScreen(
    vm: ProductsViewModel,
    orderId: Long,
    orderNumber: String = "Cuenta #$orderId",
    tableName: String = "Table4",
    onReviewOrder: (Map<Long, Int>) -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state is ProductsState.Idle) {
            vm.loadData()
        }
    }

    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFF0B0B0C),
            Color(0xFF111113),
            Color(0xFF070708)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(WindowInsets.statusBars.asPaddingValues()) // ✅ отступ от статус-бара
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .padding(top = 10.dp) // ✅ чуть ниже верх
    ) {
        when (val s = state) {
            ProductsState.Idle -> Unit

            ProductsState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF8A3D))
                }
            }

            is ProductsState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = s.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is ProductsState.Success -> {
                ProductsContent(
                    products = s.products,
                    categories = s.categories,
                    orderNumber = orderNumber,
                    tableName = tableName,
                    onReviewOrder = onReviewOrder
                )
            }
        }
    }
}

@Composable
private fun ProductsContent(
    products: List<ProductResponse>,
    categories: List<CategoryResponse>,
    orderNumber: String,
    tableName: String,
    onReviewOrder: (cart: Map<Long, Int>) -> Unit
) {
    val orange = Color(0xFFFF8A3D)

    // ✅ Один и тот же цвет для Categories и Review Order
    val barColor = Color(0xFF141416)

    val cardColor = Color(0xFF141416).copy(alpha = 0.92f)
    val cardBorder = Color.White.copy(alpha = 0.06f)

    val cart = remember { mutableStateMapOf<Long, Int>() }

    var selectedCategoryId by remember(categories) { mutableStateOf<Long?>(null) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(products, selectedCategoryId, query) {
        products
            .asSequence()
            .filter { p -> selectedCategoryId == null || p.categoryId == selectedCategoryId }
            .filter { p ->
                val q = query.trim()
                if (q.isEmpty()) true
                else p.name.contains(q, ignoreCase = true) ||
                        p.destination.contains(q, ignoreCase = true)
            }
            .toList()
    }

    val itemsCount = remember(cart) { derivedStateOf { cart.values.sum() } }.value

    val total = remember(cart, products) {
        derivedStateOf {
            var sum = BigDecimal.ZERO
            val mapById = products.associateBy { it.id }
            cart.forEach { (id, qty) ->
                val p = mapById[id] ?: return@forEach
                sum = sum.add(p.price.multiply(BigDecimal(qty)))
            }
            sum.setScale(2, RoundingMode.HALF_UP)
        }
    }.value

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ Убрали странное оранжевое свечение (glow)

        Column(
            modifier = Modifier
                .fillMaxSize()
                // ✅ оставляем место снизу: категории + review bar
                .padding(bottom = 182.dp)
        ) {
            HeaderBlockDark(
                orderNumber = orderNumber,
                tableName = tableName
            )

            Spacer(Modifier.height(14.dp))

            SearchFieldDark(
                value = query,
                onValueChange = { query = it },
                placeholder = "Buscar producto..."
            )

            Spacer(Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filtered, key = { it.id }) { p ->
                    val qty = cart[p.id] ?: 0

                    ProductCardDark(
                        product = p,
                        qty = qty,
                        cardColor = cardColor,
                        borderColor = cardBorder,
                        onAdd = {
                            if (p.stock <= 0) return@ProductCardDark
                            val next = (qty + 1).coerceAtMost(p.stock)
                            cart[p.id] = next
                        },
                        onRemoveAll = { cart.remove(p.id) }
                    )
                }
            }
        }

        // ✅ Категории закреплены снизу и близко к Review Order
        CategoriesBarDark(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onSelect = { selectedCategoryId = it },
            containerColor = barColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp)
                .padding(bottom = 92.dp) // близко к review bar
        )

        BottomReviewBarDark(
            itemsCount = itemsCount,
            total = total,
            onReview = { onReviewOrder(cart.toMap()) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeaderBlockDark(
    orderNumber: String,
    tableName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = orderNumber,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFF8A3D),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = tableName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
        // ✅ Иконка поиска сверху убрана
    }
}

@Composable
private fun SearchFieldDark(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.40f)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.70f)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White.copy(alpha = 0.14f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
            focusedContainerColor = Color(0xFF1A1A1D),
            unfocusedContainerColor = Color(0xFF1A1A1D),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFFF8A3D)
        )
    )
}

@Composable
private fun CategoriesBarDark(
    categories: List<CategoryResponse>,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        shadowElevation = 14.dp, // ✅ как в Review Order
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 6.dp)
        ) {
            item(key = "all") {
                CategoryPillDark(
                    title = "All",
                    icon = Icons.Filled.Restaurant,
                    selected = selectedCategoryId == null,
                    onClick = { onSelect(null) }
                )
            }

            items(categories, key = { it.id }) { c ->
                CategoryPillDark(
                    title = c.name,
                    icon = iconForCategoryName(c.name),
                    selected = selectedCategoryId == c.id,
                    onClick = { onSelect(c.id) }
                )
            }
        }
    }
}

private fun iconForCategoryName(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        "coffee" in n || "café" in n || "cafe" in n -> Icons.Filled.LocalCafe
        "drink" in n || "bebida" in n || "bar" in n -> Icons.Filled.LocalBar
        else -> Icons.Filled.Restaurant
    }
}

@Composable
private fun CategoryPillDark(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val orange = Color(0xFFFF8A3D)

    val bg = if (selected) orange else Color.White.copy(alpha = 0.06f)
    val border = if (selected) Color.Transparent else Color.White.copy(alpha = 0.12f)
    val textColor = if (selected) Color(0xFF1B1B1B) else Color.White.copy(alpha = 0.88f)
    val iconColor = if (selected) Color(0xFF1B1B1B) else Color.White.copy(alpha = 0.72f)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        modifier = Modifier
            .height(44.dp)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProductCardDark(
    product: ProductResponse,
    qty: Int,
    cardColor: Color,
    borderColor: Color,
    onAdd: () -> Unit,
    onRemoveAll: () -> Unit
) {
    val selected = qty > 0
    val orange = Color(0xFFFF8A3D)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardColor,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selected) Modifier.border(2.dp, orange, RoundedCornerShape(22.dp))
                else Modifier.border(1.dp, borderColor, RoundedCornerShape(22.dp))
            )
    ) {
        Box(modifier = Modifier.padding(14.dp)) {

            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = orange,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = qty.toString(),
                            color = Color(0xFF1B1B1B),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = product.destination,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formatMoney(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (selected) Color(0xFFFF8A3D) else Color.White
                    )

                    Spacer(Modifier.weight(1f))

                    Surface(
                        shape = CircleShape,
                        color = if (selected) Color(0xFFFF8A3D) else Color(0xFFFF8A3D).copy(alpha = 0.16f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { if (selected) onRemoveAll() else onAdd() }) {
                            Text(
                                text = if (selected) "✓" else "+",
                                color = if (selected) Color(0xFF1B1B1B) else Color(0xFFFF8A3D),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomReviewBarDark(
    itemsCount: Int,
    total: BigDecimal,
    onReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orange = Color(0xFFFF8A3D)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFF141416),
            shadowElevation = 14.dp,
            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF232326),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = itemsCount.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "REVIEW ORDER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onReview,
                    enabled = itemsCount > 0,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange,
                        contentColor = Color(0xFF1B1B1B),
                        disabledContainerColor = orange.copy(alpha = 0.35f),
                        disabledContentColor = Color(0xFF1B1B1B).copy(alpha = 0.6f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(text = formatMoney(total), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private fun formatMoney(value: BigDecimal): String {
    return "$" + value.setScale(2, RoundingMode.HALF_UP).toPlainString()
}
