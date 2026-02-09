package com.example.orderlyphone.ui.screen.orderDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.DraftOrderDetailUi
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val BgTop = Color(0xFF0B0B0C)
private val BgMid = Color(0xFF111113)
private val BgBot = Color(0xFF070708)
private val Card = Color(0xFF1A1A1D).copy(alpha = 0.92f)

private val Danger = Color(0xFFFF5A5A)
private val SoftTrack = Color.White.copy(alpha = 0.06f)
private val SoftText = Color.White.copy(alpha = 0.65f)

@Composable
fun OrderDetailScreen(
    vm: OrderDetailViewModel,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onFireOrder: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val items by vm.items.collectAsState()

    // ✅ DraftOrderDetailUi: (uiId: String, req: OrderDetailRequest)
    val draft by vm.draftRequests.collectAsState()

    val orderId = vm.getOrderId()

    // ✅ Total считаем по серверным + драфтам
    val total = remember(items, draft) {
        val serverSum = items.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.unitPrice.multiply(item.amount.toBigDecimal())
        }
        val draftSum = draft.fold(BigDecimal.ZERO) { acc, d ->
            acc + d.req.unitPrice.multiply(d.req.amount.toBigDecimal())
        }
        serverSum + draftSum
    }

    // высота нижней панели (чтобы список не прятался под ней)
    val bottomBarPadding = 140.dp

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
                    Header(
                        orderId = orderId,
                        onBack = onBack,
                        onClear = {
                            vm.clear()
                            // если у тебя есть метод очистки драфтов — раскомментируй:
                            // vm.clearDraft()
                        }
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = bottomBarPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // ====== SECTION: SERVER ITEMS ======
                        item(key = "hdr_server") {
                            Text(
                                text = "Items",
                                color = Color.White.copy(alpha = 0.75f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(items, key = { "srv_${it.id}" }) { item ->
                            OrderItemCard(
                                item = item,
                                description = item.comment,
                                onIncrease = { vm.increase(item) },
                                onDecrease = { vm.decrease(item) },
                                onRemove = { vm.remove(item) }
                            )
                        }

                        // ====== SECTION: DRAFT ITEMS ======
                        if (draft.isNotEmpty()) {
                            item(key = "hdr_draft") {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Added (draft)",
                                    color = Orange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            items(draft, key = { d -> "dr_${d.uiId}" }) { d ->
                                DraftItemCard(
                                    draft = d,
                                    onRemove = {
                                        // ✅ у тебя в VM должен быть метод removeDraft(uiId: String)
                                        vm.removeDraft(d.uiId)
                                    }
                                )
                            }
                        }
                    }
                }

                BottomTotalBar(
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
private fun Header(
    orderId: Long,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    val orderNumber = remember(orderId) { "Cuenta #$orderId" }
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
                .background(SoftTrack)
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
                text = "Cuenta actual",
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
                    text = orderNumber,
                    color = Orange,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onClear) {
            Text("Clear", color = Orange)
        }
    }
}

@Composable
private fun OrderItemCard(
    item: OrderDetailsResponse,
    description: String?,
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.unitPrice.formatEuro(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))
            if (description != null) {
                Text(
                    text = description,
                    color = SoftText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemovePill(onRemove = onRemove)

                QuantityStepper(
                    count = item.amount,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease
                )
            }
        }
    }
}

@Composable
private fun DraftItemCard(
    draft: DraftOrderDetailUi,
    onRemove: () -> Unit
) {
    val req = draft.req

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = req.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = req.unitPrice.formatEuro(),
                    color = Orange,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            // ✅ бейдж "DRAFT"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Orange.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DRAFT",
                    color = Orange,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                RemovePill(onRemove = onRemove)

                // пока без степпера — просто количество
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "x${req.amount}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemovePill(onRemove: () -> Unit) {
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
                text = "Remove",
                color = Danger,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QuantityStepper(
    count: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.05f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onDecrease,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.size(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("−", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(12.dp))

            Surface(
                onClick = onIncrease,
                shape = CircleShape,
                color = Orange,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.size(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BottomTotalBar(
    total: BigDecimal,
    onAddItem: () -> Unit,
    onFireOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomFade = Brush.verticalGradient(
        listOf(
            Color.Transparent,
            BgMid.copy(alpha = 0.60f),
            BgBot.copy(alpha = 0.95f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bottomFade)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Card,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = total.formatEuro(),
                        color = Orange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAddItem,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("+ Add Item", fontWeight = FontWeight.SemiBold)
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
}

fun BigDecimal?.formatEuro(): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    return nf.format(this ?: BigDecimal.ZERO)
}
