package com.example.orderlyphone.ui.screen.orderDetails

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.DraftOrderDetailUi
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val Card = Color(0xFF1A1A1D).copy(alpha = 0.92f)
private val Danger = Color(0xFFFF5A5A)

@Composable
fun OrderDetailScreen(
    vm: OrderDetailViewModel,
    orderLabel: String,
    tableLabel: String,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onFireOrder: () -> Unit
) {
    val state by vm.state.collectAsState()
    val items by vm.items.collectAsState()
    val draft by vm.draftRequests.collectAsState()
    val submissionError by vm.submissionError.collectAsState()
    val isSubmitting by vm.isSubmitting.collectAsState()

    val total = remember(items, draft) {
        val sentTotal = items.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.unitPrice.multiply(item.amount.toBigDecimal())
        }
        val draftTotal = draft.fold(BigDecimal.ZERO) { acc, line ->
            acc + line.line.lineTotal
        }
        sentTotal + draftTotal
    }

        OrderDetailContent(
            state = state,
            items = items,
            draft = draft,
            orderLabel = orderLabel,
            tableLabel = tableLabel,
            total = total,
            snackbarHostState = snackbarHostState,
            submissionError = submissionError,
            isSubmitting = isSubmitting,
            onBack = onBack,
            onAddItem = onAddItem,
            onFireOrder = onFireOrder,
            onRemoveDraft = vm::removeDraft
        )

    }

    @Composable
    fun OrderDetailContent(
        state: OrderDetailState,
        items: List<OrderDetailsResponse>,
        draft: List<DraftOrderDetailUi>,
        orderLabel: String,
        tableLabel: String,
        total: BigDecimal,
        snackbarHostState: SnackbarHostState,
        submissionError: String?,
        isSubmitting: Boolean,
        onBack: () -> Unit,
        onAddItem: () -> Unit,
        onFireOrder: () -> Unit,
        onRemoveDraft: (String) -> Unit
    ) {
    val showBlockingError = state is OrderDetailState.Error && items.isEmpty() && draft.isEmpty()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!showBlockingError) {
                BottomActionBar(
                    total = total,
                    hasDraft = draft.isNotEmpty(),
                    isSubmitting = isSubmitting,
                    submissionError = submissionError,
                    onAddItem = onAddItem,
                    onFireOrder = onFireOrder
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E0E0F))
                .statusBarsPadding()
                .padding(paddingValues)
        ) {
            when {
                state is OrderDetailState.Loading && items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Orange)
                    }
                }

                showBlockingError -> {
                    val message = (state as OrderDetailState.Error).message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp)
                    ) {
                        ReviewHeader(
                            orderLabel = orderLabel,
                            tableLabel = tableLabel,
                            onBack = onBack
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(key = "sent-header") {
                                SectionTitle(title = "Ya enviado", accent = Color.White)
                            }

                            if (items.isEmpty()) {
                                item(key = "sent-empty") {
                                    EmptySectionCard(text = "Sin items enviados todavía")
                                }
                            } else {
                                items(items, key = { item -> "sent_${item.id}" }) { item ->
                                    SentOrderItemCard(item = item)
                                }
                            }

                            item(key = "draft-header") {
                                SectionTitle(title = "Borrador actual", accent = Orange)
                            }

                            if (draft.isEmpty()) {
                                item(key = "draft-empty") {
                                    EmptySectionCard(text = "Añade productos para preparar el envío")
                                }
                            } else {
                                items(draft, key = { line -> "draft_${line.uiId}" }) { line ->
                                    DraftOrderItemCard(
                                        draft = line,
                                        onRemove = { onRemoveDraft(line.uiId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewHeader(
    orderLabel: String,
    tableLabel: String,
    onBack: () -> Unit
) {
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
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = orderLabel,
                color = Orange,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = tableLabel,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    accent: Color
) {
    Text(
        text = title,
        color = accent,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun EmptySectionCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.68f),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun SentOrderItemCard(item: OrderDetailsResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusBadge(status = item.status)
            }

            Spacer(Modifier.height(6.dp))

            if (!item.comment.isNullOrBlank()) {
                Text(
                    text = item.comment,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "x${item.amount}",
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = item.unitPrice.formatEuro(),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DraftOrderItemCard(
    draft: DraftOrderDetailUi,
    onRemove: () -> Unit
) {
    val line = draft.line

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = line.displayName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = line.lineTotal.formatEuro(),
                    color = Orange,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!line.comment.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = line.comment,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (line.supplements.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = line.supplements.joinToString(separator = ", ") { it.name },
                    color = Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoveDraftButton(onRemove = onRemove)
                Text(
                    text = "x${line.quantity} · ${line.unitPrice.formatEuro()} ud.",
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RemoveDraftButton(onRemove: () -> Unit) {
    Surface(
        onClick = onRemove,
        shape = RoundedCornerShape(999.dp),
        color = Danger.copy(alpha = 0.10f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = Danger,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Quitar",
                color = Danger,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val badgeLabel = when (status.uppercase()) {
        "SENT" -> "Enviado"
        "IN_PROGRESS" -> "En preparación"
        "SERVED" -> "Servido"
        "PAID" -> "Pagado"
        else -> status.replace('_', ' ')
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Orange.copy(alpha = 0.14f)
    ) {
        Text(
            text = badgeLabel,
            color = Orange,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BottomActionBar(
    total: BigDecimal,
    hasDraft: Boolean,
    isSubmitting: Boolean,
    submissionError: String?,
    onAddItem: () -> Unit,
    onFireOrder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF111113),
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!submissionError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Danger.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = submissionError,
                        color = Danger,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total actual",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = total.formatEuro(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddItem,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Añadir producto")
                }

                Button(
                    onClick = onFireOrder,
                    enabled = hasDraft && !isSubmitting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange,
                        contentColor = Color(0xFF1B1B1B),
                        disabledContainerColor = Color.White.copy(alpha = 0.08f),
                        disabledContentColor = Color.White.copy(alpha = 0.42f)
                    )
                ) {
                    Text(if (isSubmitting) "Enviando..." else "Confirmar")
                }
            }
        }
    }
}

private fun BigDecimal.formatEuro(): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(this)
}