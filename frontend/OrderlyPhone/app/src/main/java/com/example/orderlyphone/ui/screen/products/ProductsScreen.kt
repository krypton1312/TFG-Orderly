package com.example.orderlyphone.ui.screen.products

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.response.CategoryResponse
import com.example.orderlyphone.domain.model.response.ProductResponse
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val Card = Color(0xFF141416).copy(alpha = 0.92f)

@Composable
fun ProductsScreen(
    vm: ProductsViewModel,
    orderLabel: String,
    tableLabel: String,
    draftCount: Int,
    draftTotal: BigDecimal,
    onBack: () -> Unit,
    onSelectProduct: (categoryId: Long, productId: Long) -> Unit,
    onReviewOrder: () -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state is ProductsState.Idle) {
            vm.loadData()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0F))
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .padding(top = 10.dp)
    ) {
        when (val currentState = state) {
            ProductsState.Idle, ProductsState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }

            is ProductsState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is ProductsState.Success -> {
                ProductsContent(
                    state = currentState,
                    orderLabel = orderLabel,
                    tableLabel = tableLabel,
                    draftCount = draftCount,
                    draftTotal = draftTotal,
                    onBack = onBack,
                    onSelectCategory = vm::selectCategory,
                    onSelectProduct = onSelectProduct,
                    onReviewOrder = onReviewOrder
                )
            }
        }
    }
}

@Composable
fun ProductsContent(
    state: ProductsState.Success,
    orderLabel: String,
    tableLabel: String,
    draftCount: Int,
    draftTotal: BigDecimal,
    onBack: () -> Unit,
    onSelectCategory: (Long) -> Unit,
    onSelectProduct: (categoryId: Long, productId: Long) -> Unit,
    onReviewOrder: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredProducts by remember(state.products, query) {
        derivedStateOf {
            state.products.filter { product ->
                val cleanQuery = query.trim()
                if (cleanQuery.isBlank()) {
                    true
                } else {
                    product.name.contains(cleanQuery, ignoreCase = true) ||
                        product.destination.contains(cleanQuery, ignoreCase = true)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 182.dp)
        ) {
            CatalogHeader(
                orderLabel = orderLabel,
                tableLabel = tableLabel,
                onBack = onBack
            )

            Spacer(Modifier.height(14.dp))

            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Buscar producto..."
            )

            Spacer(Modifier.height(14.dp))

            CategoriesBar(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelectCategory = onSelectCategory
            )

            Spacer(Modifier.height(14.dp))

            if (filteredProducts.isEmpty()) {
                EmptyProductsState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onSelectProduct(product.categoryId, product.id) }
                        )
                    }
                }
            }
        }

        BottomReviewBar(
            draftCount = draftCount,
            total = draftTotal,
            onReview = onReviewOrder,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CatalogHeader(
    orderLabel: String,
    tableLabel: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = orderLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Orange,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = tableLabel,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchField(
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
            cursorColor = Orange
        )
    )
}

@Composable
private fun CategoriesBar(
    categories: List<CategoryResponse>,
    selectedCategoryId: Long?,
    onSelectCategory: (Long) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFF141416),
        shadowElevation = 14.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 6.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryPill(
                    title = category.name,
                    icon = iconForCategoryName(category.name),
                    selected = selectedCategoryId == category.id,
                    onClick = { onSelectCategory(category.id) }
                )
            }
        }
    }
}

private fun iconForCategoryName(name: String): ImageVector {
    val normalizedName = name.lowercase()
    return when {
        "coffee" in normalizedName || "café" in normalizedName || "cafe" in normalizedName -> Icons.Filled.LocalCafe
        "drink" in normalizedName || "bebida" in normalizedName || "bar" in normalizedName -> Icons.Filled.LocalBar
        else -> Icons.Filled.Restaurant
    }
}

@Composable
private fun CategoryPill(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Orange else Color.White.copy(alpha = 0.06f)
    val contentColor = if (selected) Color(0xFF1B1B1B) else Color.White.copy(alpha = 0.88f)

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor,
        modifier = Modifier
            .height(44.dp)
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
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductResponse,
    onClick: () -> Unit
) {
    val isAvailable = product.stock > 0

    Surface(
        modifier = Modifier
            .testTag("product-card-${product.id}")
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = isAvailable, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Card,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = product.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.destination,
                color = Color.White.copy(alpha = 0.58f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = product.price.formatEuro(),
                color = Orange,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isAvailable) Orange.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.06f)
            ) {
                Text(
                    text = if (isAvailable) "Stock ${product.stock}" else "Sin stock",
                    color = if (isAvailable) Orange else Color.White.copy(alpha = 0.48f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomReviewBar(
    draftCount: Int,
    total: BigDecimal,
    onReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF141416),
        shadowElevation = 18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (draftCount == 0) "Borrador vacío" else "$draftCount líneas en borrador",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = total.setScale(2, RoundingMode.HALF_UP).formatEuro(),
                    color = Orange,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onReview,
                enabled = draftCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color(0xFF1B1B1B),
                    disabledContainerColor = Color.White.copy(alpha = 0.08f),
                    disabledContentColor = Color.White.copy(alpha = 0.42f)
                )
            ) {
                Text("Revisar pedido")
            }
        }
    }
}

@Composable
private fun EmptyProductsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay productos para esta categoría",
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun BigDecimal.formatEuro(): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(this)
}