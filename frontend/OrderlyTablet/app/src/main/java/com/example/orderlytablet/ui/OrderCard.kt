package com.example.orderlytablet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import com.example.orderlytablet.ui.screens.OrdersViewModel

@Composable
fun StatusDropdown(
    detailId: Long,
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {

    var expanded by remember(detailId) { mutableStateOf(false) }
    var selectedStatus by remember(detailId) { mutableStateOf(currentStatus) }

    val statusOptions = listOf("PENDING", "SENT", "IN_PROGRESS", "SERVED")

    fun statusLabel(s: String) = when (s) {
        "PENDING"     -> "PENDIENTE"
        "SENT"        -> "ENVIADO"
        "IN_PROGRESS" -> "EN PROCESO"
        "SERVED"      -> "SERVIDO"
        else          -> s
    }

    fun statusColor(s: String) = when (s) {
        "PENDING"     -> Color(0xFF9E9E9E)
        "SENT"        -> Color(0xFFF59E0B)
        "IN_PROGRESS" -> Color(0xFFF97316)
        "SERVED"      -> Color(0xFF3B82F6)
        else          -> Color.Gray
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Row(
            modifier = Modifier
                .background(statusColor(selectedStatus), shape = RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusLabel(selectedStatus),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statusOptions.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(statusColor(status), shape = RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusLabel(status),
                                fontWeight = if (status == selectedStatus) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        selectedStatus = status
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderWithOrderDetailResponse, viewModel: OrdersViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔹 Заголовок карточки
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Order #${order.orderId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "🪑 ${order.tableName}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Секция с деталями
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F7F7), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    order.details.forEach { detail ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${detail.amount}x ${detail.productName}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (!detail.comment.isNullOrBlank()) {
                                    Text(
                                        text = "• ${detail.comment}",
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 2.dp)
                                    )
                                }
                            }

                            // 🔹 Выпадающий список со статусами
                            StatusDropdown(
                                detailId = detail.id, // 👈 теперь у каждого уникальный state
                                currentStatus = detail.status,
                                onStatusChange = { newStatus ->
                                    detail.status = newStatus
                                    viewModel.updateOrderDetailStatus(listOf(detail.id), newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
