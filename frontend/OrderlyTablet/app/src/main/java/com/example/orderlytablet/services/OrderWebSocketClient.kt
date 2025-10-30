package com.example.orderlytablet.services

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OrderWebSocketClient {
    private val client = OkHttpClient.Builder()
        .pingInterval(1, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect(
        serverUrl: String,
        onOrderChanged: () -> Unit
    ) {
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Connected to $serverUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📩 Message received: $text")

                try {
                    val json = JSONObject(text)
                    val event = json.optString("event")

                    if (event == "ORDER_CHANGED") {
                        onOrderChanged()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "❌ Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "⚠️ Error: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
    }
}