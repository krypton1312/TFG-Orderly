package com.example.orderlyphone.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.toString

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onSuccess: () -> Unit
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadEmployeeData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            HomeState.Loading -> {
                CircularProgressIndicator()
            }

            is HomeState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Welcome")
                    Text("${s.currentEmployee.name} ${s.currentEmployee.lastname}")
                }
            }

            is HomeState.Error -> {
                Text(s.message, color = Color.Red)
            }

            HomeState.Idle -> {}
        }
    }
}
