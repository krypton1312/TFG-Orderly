package com.example.orderlyphone.ui.screen.tablePicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.orderlyphone.domain.model.TablePickerItemUi
import com.example.orderlyphone.domain.model.TablePickerSectionUi
import com.example.orderlyphone.domain.model.TablePickerZone
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val Orange = Color(0xFFFF8A3D)
private val Card = Color(0xFF1A1A1D).copy(alpha = 0.92f)

@Composable
fun TablePickerScreen(
    vm: TablePickerViewModel,
    onBack: () -> Unit,
    onOpenOrder: (Long, Long) -> Unit,
    onStartNewOrder: (Long?) -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) {
        vm.navigationEvents.collect { event ->
            when (event) {
                is TablePickerNavigationEvent.OpenExistingOrder -> {
                    onOpenOrder(event.orderId, event.tableId)
                }

                is TablePickerNavigationEvent.StartNewOrder -> {
                    onStartNewOrder(event.tableId)
                }
            }
        }
    }

    TablePickerContent(
        state = state,
        onBack = onBack,
        onRetry = vm::refresh,
        onTableTapped = vm::onTableTapped,
        onDismissFreeTable = vm::dismissFreeTableConfirmation,
        onConfirmFreeTable = vm::confirmFreeTable,
        onStartWithoutTable = vm::startOrderWithoutTable
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablePickerContent(
    state: TablePickerState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onTableTapped: (TablePickerItemUi) -> Unit,
    onDismissFreeTable: () -> Unit,
    onConfirmFreeTable: () -> Unit,
    onStartWithoutTable: () -> Unit
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.errorMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF1B1B1B))
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Reintentar")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    TablePickerHeader(onBack = onBack, onRetry = onRetry)
                    Spacer(Modifier.height(18.dp))
                    NoTableOrderCard(onStartWithoutTable = onStartWithoutTable)
                    Spacer(Modifier.height(18.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.sections, key = { it.zone.name }) { section ->
                            TablePickerSection(section = section, onTableTapped = onTableTapped)
                        }
                    }
                }
            }
        }
    }

    val pendingFreeTable = state.pendingFreeTable
    if (pendingFreeTable != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissFreeTable,
            containerColor = Color(0xFF141416),
            contentColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = pendingFreeTable.displayName,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Mesa libre. ¿Quieres abrir un pedido nuevo?",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onConfirmFreeTable,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF1B1B1B))
                ) {
                    Text("Abrir pedido")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismissFreeTable,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White)
                ) {
                    Text("Cancelar")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun NoTableOrderCard(
    onStartWithoutTable: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Card,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Orange.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Waves,
                        contentDescription = null,
                        tint = Orange
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pedido sin mesa",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Para llevar o para clientes sin mesa asignada",
                        color = Color.White.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = onStartWithoutTable,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start-order-without-table"),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF1B1B1B))
            ) {
                Text("Abrir sin mesa")
            }
        }
    }
}

@Composable
private fun TablePickerHeader(
    onBack: () -> Unit,
    onRetry: () -> Unit
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

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Selecciona una mesa",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Interior primero o abre un pedido sin mesa",
                color = Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onRetry,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = Orange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TablePickerSection(
    section: TablePickerSectionUi,
    onTableTapped: (TablePickerItemUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("table-picker-section-${section.zone.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = section.zone.toLabel(),
            color = Orange,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            section.items.forEach { item ->
                TableCard(item = item, onTableTapped = onTableTapped)
            }
        }
    }
}

@Composable
private fun TableCard(
    item: TablePickerItemUi,
    onTableTapped: (TablePickerItemUi) -> Unit
) {
    val isOccupied = item.orderId != null

    Surface(
        modifier = Modifier
            .testTag("table-card-${item.tableId}")
            .fillMaxWidth(0.48f)
            .clickable { onTableTapped(item) },
        shape = RoundedCornerShape(20.dp),
        color = Card,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.displayName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isOccupied) Orange else Color.White.copy(alpha = 0.75f)
                )
            }

            Text(
                text = if (isOccupied) "Ocupada" else "Libre",
                color = if (isOccupied) Orange else Color(0xFF35D07F),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (isOccupied) {
                Text(
                    text = item.total.formatCurrency(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            } else {
                Text(
                    text = "Pulsa para confirmar",
                    color = Color.White.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun TablePickerZone.toLabel(): String {
    return when (this) {
        TablePickerZone.Inside -> "Interior"
        TablePickerZone.Outside -> "Exterior"
        TablePickerZone.Other -> "Otras"
    }
}

private fun BigDecimal?.formatCurrency(): String {
    val value = this ?: BigDecimal.ZERO
    return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)
}