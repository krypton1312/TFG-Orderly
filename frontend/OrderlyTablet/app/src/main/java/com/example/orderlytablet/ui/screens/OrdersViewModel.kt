package com.example.orderlytablet.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlytablet.response.OrderWithOrderDetailResponse
import com.example.orderlytablet.services.OrderWebSocketClient
import com.example.orderlytablet.services.RetrofitClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class OrdersUiState {
    object Loading : OrdersUiState()
    data class Success(val orders: List<OrderWithOrderDetailResponse>) : OrdersUiState()
    data class Error(val message: String) : OrdersUiState()
}

/**
 * Phase 10 — D-01/D-02: session lifecycle state for the tablet block-screen gate.
 * - Loading: HTTP check in progress at startup.
 * - Open: an OPEN cash session exists; OrdersScreen is shown.
 * - Blocked: no OPEN session found; NoSessionScreen is shown until SESSION_OPENED WS event.
 */
sealed class SessionState {
    data object Loading : SessionState()
    data object Open : SessionState()
    data object Blocked : SessionState()
}

class OrdersViewModel : ViewModel() {

    private val wsClient = OrderWebSocketClient()

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState

    // Phase 10 — D-01: session gate state, starts as Loading until HTTP check completes.
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState

    var isRefreshing = MutableStateFlow(false)
        private set

    private var reloadJob: Job? = null
    var isSingleUpdate = false

    init {
        connectWebSocket()
        loadOrders()
        checkOpenSession()
    }

    private fun connectWebSocket() {
        val serverUrl = RetrofitClient.BASE_URL
            .trimEnd('/')
            .replaceFirst("http://", "ws://") + "/ws/overview/tablet"

        wsClient.connect(serverUrl, RetrofitClient.getToken() ?: "") { event ->
            Log.d("OrdersViewModel", "📡 WS Event: ${event.type}")

            when (event.type) {
                "ORDER_DETAIL_CREATED",
                "ORDER_DETAIL_UPDATED",
                "ORDER_DETAIL_DELETED",
                "ORDER_DETAIL_STATUS_CHANGED" -> {
                    if (event.overviewId != null) {
                        updateSingleOrder(event.overviewId)
                    } else {
                        reloadDebounced()
                    }
                }
                "ORDER_TOTAL_CHANGED",
                "ORDER_CREATED",
                "ORDER_DELETED" -> reloadDebounced()

                // Phase 10 — D-02: forward SESSION_OPENED to the one-way session transition.
                "SESSION_OPENED" -> onSessionOpened(event.sessionId)

                // Phase 10 — D-02: forward SESSION_CLOSED → block kitchen display.
                "SESSION_CLOSED" -> onSessionClosed()
            }
        }
    }

    /**
     * Phase 10 — D-01: check whether a cash session is currently OPEN at startup.
     * Sets sessionState to Open on HTTP 200, Blocked on 404 or any network failure.
     */
    private fun checkOpenSession() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getOpenCashSession()
                _sessionState.value = if (response.isSuccessful && response.body() != null) {
                    SessionState.Open
                } else {
                    SessionState.Blocked
                }
            } catch (e: Exception) {
                // Network failure → treat as no-session for safety
                Log.e("OrdersViewModel", "Session check failed: ${e.message}")
                _sessionState.value = SessionState.Blocked
            }
        }
    }

    /**
     * Phase 10 — D-02: one-way transition from Blocked/Loading → Open on WS SESSION_OPENED.
     *
     * INVARIANT (T-10-11): this method NEVER transitions Open → Blocked from a WS event.
     * A stale or replayed SESSION_OPENED payload must not blank the kitchen display while
     * orders are live. The only way to reach Blocked is via the HTTP check at startup
     * (RESEARCH.md Pitfall 2).
     *
     * Trusts the payload sessionId without re-fetching (RESEARCH.md Pitfall 3 — backend
     * saveAndFlush runs before wsNotifier.send, so the row is visible once tx commits).
     */
    private fun onSessionOpened(sessionId: Long) {
        if (_sessionState.value != SessionState.Open) {
            _sessionState.value = SessionState.Open
            loadOrders() // refresh orders list now that a session is live
        }
        // If already Open: ignore — transition is idempotent (T-10-14).
    }

    /**
     * Phase 10 — D-02: one-way transition Open → Blocked on WS SESSION_CLOSED.
     * Clears the orders list so the kitchen display goes blank alongside the gate screen.
     */
    private fun onSessionClosed() {
        _sessionState.value = SessionState.Blocked
    }

    private fun reloadDebounced(delayMs: Long = 700) {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            delay(delayMs)
            refreshOrders()
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            isRefreshing.value = true
            loadOrders()
            delay(300)
            isRefreshing.value = false
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.instance.getOrdersWithDetails()
                _uiState.value = OrdersUiState.Success(result)
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "❌ Failed to load: ${e.message}")
                _uiState.value = OrdersUiState.Error("Не удалось загрузить заказы: ${e.localizedMessage}")
            }
        }
    }

    private fun updateSingleOrder(overviewId: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is OrdersUiState.Success) {
                    isSingleUpdate = true
                    val refreshedOrders = RetrofitClient.instance.getOrdersWithDetails()
                    val updatedOrder = refreshedOrders.find { it.overviewId == overviewId }

                    val newList = if (updatedOrder != null) {
                        currentState.orders.map {
                            if (it.overviewId == overviewId) updatedOrder else it
                        }
                    } else {
                        currentState.orders.filterNot { it.overviewId == overviewId }
                    }

                    _uiState.value = OrdersUiState.Success(newList)
                    isSingleUpdate = false
                } else {
                    loadOrders()
                }
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "⚠️ Ошибка updateSingleOrder: ${e.message}")
                isSingleUpdate = false
            }
        }
    }

    fun updateOrderDetailStatus(ids: List<Long>, newStatus: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateOrderDetailStatus(newStatus, ids)
                // Fallback reload — por si el WS event llega tarde o la conexión está caída
                reloadDebounced(300)
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "❌ Ошибка PUT: ${e.message}")
                reloadDebounced(500)
            }
        }
    }

    override fun onCleared() {
        wsClient.disconnect()
        super.onCleared()
        Log.d("OrdersViewModel", "🔌 WebSocket disconnected")
    }
}
