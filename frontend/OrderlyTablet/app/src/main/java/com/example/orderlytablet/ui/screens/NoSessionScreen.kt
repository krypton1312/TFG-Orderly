package com.example.orderlytablet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Phase 10 — D-01: tablet block screen shown when GET /cashSession/byStatus/OPEN
 * returns no session. Light-theme variant per UI-SPEC.md §1 (background #F3F6FB,
 * matching the existing OrdersScreen palette).
 *
 * Intentionally has NO interactive elements (no button, no spinner, no clickable):
 * dismissed solely by the WS SESSION_OPENED event flipping
 * OrdersViewModel.sessionState from Blocked to Open.
 */
@Composable
fun NoSessionScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6FB))
            .testTag("tablet-no-session-screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0xFFFFA100),
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "No hay turno abierto",
                color = Color(0xFF1B1B1B),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Espera a que el encargado abra la sesión.",
                color = Color(0xFF1B1B1B).copy(alpha = 0.65f),
                fontSize = 16.sp
            )
        }
    }
}
