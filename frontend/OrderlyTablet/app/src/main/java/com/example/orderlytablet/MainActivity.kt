package com.example.orderlytablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.orderlytablet.ui.screens.OrdersScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Вместо XML — Jetpack Compose контент
        setContent {
            OrderlyTabletApp()
        }
    }
}

@Composable
fun OrderlyTabletApp() {
    // Оборачиваем всё в MaterialTheme
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            OrdersScreen()  // Показываем экран с заказами
        }
    }
}
