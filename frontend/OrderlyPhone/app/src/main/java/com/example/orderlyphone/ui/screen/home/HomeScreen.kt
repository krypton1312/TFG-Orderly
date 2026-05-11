package com.example.orderlyphone.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOrders: () -> Unit = {},
    onNewOrder: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val shiftLoading by vm.shiftLoading.collectAsState()
    val cashSessionId by vm.cashSessionId.collectAsStateWithLifecycle()
    val hasSession = cashSessionId != null

    LaunchedEffect(Unit) {
        vm.loadEmployeeData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0F))
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = Color(0xFF555558),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Sin conexión",
                            color = Color(0xFFCCCCCC),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Comprueba la red e inténtalo de nuevo",
                            color = Color(0xFF777779),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { vm.loadEmployeeData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A3D))
                        ) {
                            Text("Reintentar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HomeState.Idle -> Unit

            is HomeState.Success -> {
                val response = s.dashboardStartResponse
                val employee = response.employee
                val roles = employee.roles.joinToString("/") { it.name }
                val isClockedIn = response.shiftRecord != null

                HomeContent(
                    employeeName = "${employee.name} ${employee.lastname}",
                    role = roles,
                    isClockedIn = isClockedIn,
                    shiftStartTime = response.shiftRecord?.startTime,
                    shiftLoading = shiftLoading,
                    online = true,
                    activeTables = response.availableTables,
                    occupiedTables = response.occupiedTables,
                    hasSession = hasSession,
                    onOrders = onOrders,
                    onNewOrder = onNewOrder,
                    onShiftToggle = { if (isClockedIn) vm.clockOut() else vm.clockIn() },
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
    isClockedIn: Boolean,
    shiftStartTime: LocalDateTime?,
    shiftLoading: Boolean,
    online: Boolean,
    activeTables: Int,
    occupiedTables: Int,
    hasSession: Boolean,
    onOrders: () -> Unit,
    onNewOrder: () -> Unit,
    onShiftToggle: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
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
                    isClockedIn = isClockedIn,
                    shiftStartTime = shiftStartTime,
                    online = online
                )

                InfoCard(activeTables = activeTables, occupiedTables = occupiedTables, onClick = onNewOrder)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!hasSession) {
                    Surface(
                        color = Color(0x59_1B_1B_1B), // #1B1B1B α=0.35 — UI-SPEC.md §Color
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("home-no-session-banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color(0xFFFF8A3D), // phone accent orange — UI-SPEC.md
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "No hay turno abierto. Contacta con el encargado.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                NewOrderButton(onClick = onNewOrder, enabled = hasSession)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ActionTile(
                        title = if (isClockedIn) "Fichar salida" else "Fichar entrada",
                        icon = if (isClockedIn) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        loading = shiftLoading,
                        onClick = onShiftToggle
                    )
                    ActionTile(
                        title = "Ajustes",
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
    isClockedIn: Boolean,
    shiftStartTime: LocalDateTime?,
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
            ShiftStatusBadge(isClockedIn = isClockedIn, startTime = shiftStartTime)
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
                    text = if (online) "En línea" else "Sin conexión",
                    color = if (online) Color(0xFF35D07F) else Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ShiftStatusBadge(isClockedIn: Boolean, startTime: LocalDateTime?) {
    val green = Color(0xFF35D07F)
    val inactive = Color.White.copy(alpha = 0.45f)
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM HH:mm") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isClockedIn) green else inactive)
        )
        Spacer(Modifier.width(5.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (isClockedIn) "Fichado" else "No fichado",
                color = if (isClockedIn) green else Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (isClockedIn && startTime != null) {
                Text(
                    text = "desde ${startTime.format(formatter)}",
                    color = Color.White.copy(alpha = 0.50f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun NewOrderButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val orange = Color(0xFFFF8A3D)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .testTag("home-new-order")
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
                        "Nuevo pedido",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Abrir una mesa",
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
    loading: Boolean = false,
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
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFFFF8A3D),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFFF8A3D)
                    )
                }
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
private fun InfoCard(
    activeTables: Int,
    occupiedTables: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color(0xFF1A1A1D).copy(alpha = 0.90f),
        shadowElevation = 10.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Левая часть: заголовок + 2 метрики в ряд
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Resumen de mesas",
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Metric(
                        value = activeTables.toString(),
                        label = "disponible"
                    )

                    Spacer(Modifier.width(14.dp))

                    VerticalDividerLine()

                    Spacer(Modifier.width(14.dp))

                    Metric(
                        value = occupiedTables.toString(),
                        label = "ocupada"
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Иконка справа
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
private fun Metric(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(Color.White.copy(alpha = 0.12f))
    )
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
            text = "Cerrar sesión",
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
