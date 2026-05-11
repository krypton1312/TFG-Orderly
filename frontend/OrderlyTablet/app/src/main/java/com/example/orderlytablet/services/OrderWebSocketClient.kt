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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var intentionalDisconnect = false
    private var reconnectDelayMs = 1_000L

    private var lastUrl: String = ""
    private var lastToken: String = ""
    private var lastCallback: ((WsEvent) -> Unit)? = null

    fun connect(
        serverUrl: String,
        token: String,
        onEventReceived: (WsEvent) -> Unit
    ) {
        intentionalDisconnect = false
        reconnectDelayMs = 1_000L
        lastUrl = serverUrl
        lastToken = token
        lastCallback = onEventReceived
        openSocket(serverUrl, token, onEventReceived)
    }

    private fun openSocket(
        serverUrl: String,
        token: String,
        onEventReceived: (WsEvent) -> Unit
    ) {
        val request = Request.Builder()
            .url(serverUrl)
            .header("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Connected to $serverUrl")
                reconnectDelayMs = 1_000L
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📩 WS message: $text")
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type", "")
                    val orderId = json.optLong("orderId", -1)
                    val overviewId = json.optString("overviewId", null)
                    val ts = json.optString("ts", "")
                    val sessionId = json.optLong("sessionId", -1L) // Phase 10 — D-02: populated for SESSION_OPENED
                    onEventReceived(WsEvent(type, orderId, overviewId, ts, sessionId))
                } catch (e: Exception) {
                    Log.e("WebSocket", "⚠️ Parse error: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "❌ Closed: $reason")
                if (!intentionalDisconnect) scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "⚠️ Error: ${t.message}")
                if (!intentionalDisconnect) scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            Log.d("WebSocket", "🔄 Reconnecting in ${reconnectDelayMs}ms")
            delay(reconnectDelayMs)
            reconnectDelayMs = minOf(reconnectDelayMs * 2, 32_000L)
            val cb = lastCallback ?: return@launch
            openSocket(lastUrl, lastToken, cb)
        }
    }

    fun disconnect() {
        intentionalDisconnect = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnected")
    }

    /**
     * WS event payload. The `sessionId` field is -1L for non-session events;
     * positive for session lifecycle events (D-02).
     */
    data class WsEvent(
        val type: String,
        val orderId: Long,
        val overviewId: String?,
        val ts: String,
        val sessionId: Long = -1L
    )
}
