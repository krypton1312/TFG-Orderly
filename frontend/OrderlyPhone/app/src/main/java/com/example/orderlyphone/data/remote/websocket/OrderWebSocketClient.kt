package com.example.orderlyphone.data.remote.websocket

import android.util.Log
import com.example.orderlyphone.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderWebSocketClient @Inject constructor() {

    private val wsUrl = "ws://${BuildConfig.SERVER_HOST}:8080/ws/orders/phone"

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var intentionalDisconnect = false
    private var reconnectDelayMs = 1_000L

    private val _events = MutableSharedFlow<WsEvent>(replay = 1, extraBufferCapacity = 16)
    val events: SharedFlow<WsEvent> = _events.asSharedFlow()

    fun connect() {
        intentionalDisconnect = false
        reconnectDelayMs = 1_000L
        openSocket()
    }

    private fun openSocket() {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("PhoneWS", "✅ Connected to $wsUrl")
                reconnectDelayMs = 1_000L
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("PhoneWS", "📩 $text")
                try {
                    val json = JSONObject(text)
                    val event = WsEvent(
                        type       = json.optString("type", ""),
                        orderId    = json.optLong("orderId", -1),
                        overviewId = if (json.isNull("overviewId")) null
                                     else json.optString("overviewId"),
                        ts         = json.optString("ts", ""),
                        sessionId  = if (json.isNull("sessionId")) null
                                     else json.optLong("sessionId")
                    )
                    scope.launch { _events.emit(event) }
                } catch (e: Exception) {
                    Log.e("PhoneWS", "⚠️ Parse error: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("PhoneWS", "❌ Closed: $reason")
                if (!intentionalDisconnect) scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("PhoneWS", "⚠️ Error: ${t.message}")
                if (!intentionalDisconnect) scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            Log.d("PhoneWS", "🔄 Reconnecting in ${reconnectDelayMs}ms")
            delay(reconnectDelayMs)
            reconnectDelayMs = minOf(reconnectDelayMs * 2, 32_000L)
            openSocket()
        }
    }

    fun disconnect() {
        intentionalDisconnect = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnected")
    }

    data class WsEvent(
        val type: String,
        val orderId: Long,
        val overviewId: String?,
        val ts: String,
        val sessionId: Long? = null
    )
}
