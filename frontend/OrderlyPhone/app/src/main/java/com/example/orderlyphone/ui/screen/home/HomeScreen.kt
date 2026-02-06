package com.example.orderlyphone.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onNewOrder: () -> Unit = {},
    onShiftToggle: () -> Unit = {},   // Start/End Shift (заглушка)
    onSettings: () -> Unit = {},      // Settings (заглушка)
    onLogout: () -> Unit = {}         // Logout (заглушка)
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadEmployeeData()
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
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        when (val s = state) {
            HomeState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF8A3D))
                }
            }

            is HomeState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            HomeState.Idle -> Unit

            is HomeState.Success -> {
                val response = s.dashboardStartResponse
                val employee = response.employee

                val roles = employee.roles.joinToString("/") { it.name }

                HomeContent(
                    employeeName = "${employee.name} ${employee.lastname}",
                    role = roles,
                    shiftLabel = response.shiftRecord?.let {
                        "Turno #${it.id}"
                    } ?: "No estas fichado/a",
                    online = true,
                    activeTables = response.availableTables,
                    occupiedTables = response.occupiedTables,
                    onNewOrder = onNewOrder,
                    onShiftToggle = onShiftToggle,
                    onSettings = onSettings,
                    onLogout = onLogout
                )

            }
        }
    }
}

@Composable
private fun HomeContent(
    employeeName: String,
    role: String,
    shiftLabel: String,
    online: Boolean,
    activeTables: Int,
    occupiedTables: Int,
    onNewOrder: () -> Unit,
    onShiftToggle: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    // фоновые “свечения”
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TopHeader(
                    employeeName = employeeName,
                    role = role,
                    shiftLabel = shiftLabel,
                    online = online
                )

                InfoCard(activeTables = activeTables, occupiedTables = occupiedTables)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NewOrderButton(onClick = onNewOrder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ActionTile(
                        title = "Start/End Shift",
                        icon = if (online) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        onClick = onShiftToggle
                    )
                    ActionTile(
                        title = "Settings",
                        icon = Icons.Filled.Settings,
                        onClick = onSettings
                    )
                }

                LogoutRow(onLogout = onLogout)
            }
        }

    }
}

@Composable
private fun TopHeader(
    employeeName: String,
    role: String,
    shiftLabel: String,
    online: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // аватар (пока просто кружок)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1F))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // маленькая “точка онлайн”
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (online) Color(0xFF35D07F) else Color(0xFF8A8A8A))
                        .border(2.dp, Color(0xFF0B0B0C), CircleShape)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = employeeName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = shiftLabel,
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = null,
                    tint = if (online) Color(0xFF35D07F) else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (online) "Online" else "Offline",
                    color = if (online) Color(0xFF35D07F) else Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InfoCard(activeTables: Int, occupiedTables: Int) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color(0xFF1A1A1D).copy(alpha = 0.90f),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Current Tables",
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = activeTables.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "disponible",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = occupiedTables.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ocupada",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF8A3D).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = Color(0xFFFF8A3D),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NewOrderButton(
    onClick: () -> Unit
) {
    val orange = Color(0xFFFF8A3D)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // ✅ Glow слой 1 (широкий мягкий)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)          // <— НЕ на всю ширину
                .height(72.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(orange.copy(alpha = 0.22f))
                .blur(38.dp)
        )

        // ✅ Glow слой 2 (более яркий, узкий, чтобы "кольцо" было вокруг кнопки)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(56.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(orange.copy(alpha = 0.28f))
                .blur(28.dp)
        )

        // 🟧 Кнопка
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = orange,
                contentColor = Color(0xFF1B1B1B)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "New Order",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Start a fresh table",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B1B1B).copy(alpha = 0.75f)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun RowScope.ActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = Modifier
            .weight(1f)
            .height(110.dp),
        shape = shape,
        color = Color(0xFF1A1A1D).copy(alpha = 0.92f),
        shadowElevation = 10.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFF8A3D)
                )
            }

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LogoutRow(onLogout: () -> Unit) {
    TextButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Logout,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.65f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Logout",
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
