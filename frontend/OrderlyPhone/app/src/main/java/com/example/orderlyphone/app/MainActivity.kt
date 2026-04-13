package com.example.orderlyphone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.orderlyphone.data.remote.websocket.OrderWebSocketClient
import com.example.orderlyphone.ui.navigation.AppNav
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var webSocketClient: OrderWebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNav(webSocketClient = webSocketClient)
        }
    }
}
