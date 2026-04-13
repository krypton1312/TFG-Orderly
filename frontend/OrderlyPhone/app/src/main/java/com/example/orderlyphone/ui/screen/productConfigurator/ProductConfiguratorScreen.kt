package com.example.orderlyphone.ui.screen.productConfigurator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.ConfiguredOrderLineUi
import com.example.orderlyphone.domain.model.response.SupplementResponse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val Card = Color(0xFF141416).copy(alpha = 0.92f)

@Composable
fun ProductConfiguratorScreen(
    vm: ProductConfiguratorViewModel,
    onBack: () -> Unit,
    onConfirm: (ConfiguredOrderLineUi) -> Unit
) {
    val state by vm.state.collectAsState()
    val previewLine = vm.buildConfiguredLine()

    ProductConfiguratorContent(
        state = state,
        previewLine = previewLine,
        onBack = onBack,
        onIncreaseQuantity = vm::increaseQuantity,
        onDecreaseQuantity = vm::decreaseQuantity,
        onCommentChange = vm::updateComment,
        onToggleSupplement = vm::toggleSupplement,
        onConfirm = {
            val configuredLine = vm.buildConfiguredLine() ?: return@ProductConfiguratorContent
            onConfirm(configuredLine)
        }
    )
}

@Composable
fun ProductConfiguratorContent(
    state: ProductConfiguratorState,
    previewLine: ConfiguredOrderLineUi?,
    onBack: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onCommentChange: (String) -> Unit,
    onToggleSupplement: (Long) -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0F))
            .statusBarsPadding()
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }

            state.errorMessage != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }

            state.product != null && previewLine != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    ConfiguratorHeader(productName = state.product.name, onBack = onBack)
                    Spacer(Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            ConfiguratorSummaryCard(previewLine = previewLine)
                        }

                        item {
                            QuantityCard(
                                quantity = state.quantity,
                                onDecrease = onDecreaseQuantity,
                                onIncrease = onIncreaseQuantity
                            )
                        }

                        item {
                            Text(
                                text = "Suplementos",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.compatibleSupplements.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(20.dp), color = Card) {
                                    Text(
                                        text = "Sin suplementos para este producto",
                                        color = Color.White.copy(alpha = 0.72f),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            items(state.compatibleSupplements, key = { it.id }) { supplement ->
                                SupplementRow(
                                    supplement = supplement,
                                    checked = state.selectedSupplementIds.contains(supplement.id),
                                    onToggle = { onToggleSupplement(supplement.id) }
                                )
                            }
                        }

                        item {
                            CommentCard(value = state.comment, onValueChange = onCommentChange)
                        }
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF1B1B1B))
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfiguratorHeader(
    productName: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = "Configurar producto",
                color = Orange,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = productName,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ConfiguratorSummaryCard(previewLine: ConfiguredOrderLineUi) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Card,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = previewLine.displayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Precio unitario: ${previewLine.unitPrice.formatEuro()}",
                color = Color.White.copy(alpha = 0.72f)
            )
            Text(
                text = "Cantidad: ${previewLine.quantity}",
                color = Color.White.copy(alpha = 0.72f)
            )
            Text(
                text = "Total: ${previewLine.lineTotal.formatEuro()}",
                color = Orange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuantityCard(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Card,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cantidad",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                QuantityButton(icon = Icons.Filled.Remove, onClick = onDecrease)
                Text(text = quantity.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                QuantityButton(icon = Icons.Filled.Add, onClick = onIncrease)
            }
        }
    }
}

@Composable
private fun QuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Orange.copy(alpha = 0.16f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Orange)
        }
    }
}

@Composable
private fun CommentCard(
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Card,
        shadowElevation = 10.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Comentario",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Opcional", color = Color.White.copy(alpha = 0.4f)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.14f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Orange
                )
            )
        }
    }
}

@Composable
private fun SupplementRow(
    supplement: SupplementResponse,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("configurator-supplement-${supplement.id}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplement.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "+ ${supplement.price.formatEuro()}",
                    color = Orange,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun BigDecimal.formatEuro(): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(this)
}