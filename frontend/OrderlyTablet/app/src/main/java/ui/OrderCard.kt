package com.example.orderlytablet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orderlytablet.response.OrderWithOrderDetailResponse

@Composable
fun OrderCard(
    order: OrderWithOrderDetailResponse
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔹 Заголовок (номер + стол)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Order #${order.id}",
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

            // 🔹 Элементы заказа с индивидуальными статусами
            order.details.forEach { detail ->

                // Цвет статуса в зависимости от значения
                val statusColor = when (detail.status.lowercase()) {
                    "preparing" -> Color(0xFFFFC107)
                    "ready" -> Color(0xFF4CAF50)
                    "delivered" -> Color(0xFF2196F3)
                    else -> Color(0xFF9E9E9E)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF7F7F7), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    // 🔸 Название и количество
                    Text(
                        text = "${detail.amount}x ${detail.productName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // 🔸 Комментарий (если есть)
                    if (detail.comment.isNullOrBlank()) {
                        Text(
                            text = "• ${detail.comment}",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 🔸 Индивидуальный статус
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .background(statusColor, shape = RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = detail.status,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Время заказа
            Text(
                text = "🕒 ${order.datetime.replace('T', ' ')}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
