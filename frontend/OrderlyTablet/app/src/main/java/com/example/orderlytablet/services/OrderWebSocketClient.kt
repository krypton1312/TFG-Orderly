package com.example.orderlytablet.services

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OrderWebSocketClient {

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    // 🔹 Передаем callback с типизированным событием
    fun connect(
        serverUrl: String,
        onEventReceived: (WsEvent) -> Unit
    ) {
        val request = Request.Builder().url(serverUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Connected to $serverUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📩 WS message: $text")
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type", "")
                    val orderId = json.optLong("orderId", -1)
                    val overviewId = json.optString("overviewId", null)
                    val ts = json.optString("ts", "")
                    val event = WsEvent(type, orderId, overviewId, ts)
                    onEventReceived(event)
                } catch (e: Exception) {
                    Log.e("WebSocket", "⚠️ Parse error: ${e.message}")
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

    data class WsEvent(
        val type: String,
        val orderId: Long,
        val overviewId: String?,
        val ts: String
    )
}
