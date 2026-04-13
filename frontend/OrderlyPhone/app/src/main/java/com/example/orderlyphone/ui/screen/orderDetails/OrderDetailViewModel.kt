package com.example.orderlyphone.ui.screen.orderDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.CashSessionStore
import com.example.orderlyphone.data.remote.OrderDetailApi
import com.example.orderlyphone.data.remote.OverviewApi
import com.example.orderlyphone.domain.model.ConfiguredOrderLineUi
import com.example.orderlyphone.domain.model.DraftOrderDetailUi
import com.example.orderlyphone.domain.model.request.OrderDetailRequest
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import com.example.orderlyphone.data.remote.websocket.OrderWebSocketClient
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SubmitDraftResult {
    data object NoDraft : SubmitDraftResult
    data object ExistingOrderUpdated : SubmitDraftResult
    data class OpenedNewOrder(val orderId: Long, val tableId: Long?) : SubmitDraftResult
    data object Failed : SubmitDraftResult
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val api: OrderDetailApi,
    private val overviewApi: OverviewApi,
    savedStateHandle: SavedStateHandle,
    private val cashSessionStore: CashSessionStore,
    private val webSocketClient: OrderWebSocketClient
) : ViewModel() {

    private val orderId: Long? = savedStateHandle["orderId"]
    private val tableId: Long? = savedStateHandle.get<String>("tableRef")?.toLongOrNull()
        ?: savedStateHandle.get<String>("tableId")?.toLongOrNull()
        ?: savedStateHandle.get<Long>("tableId")

    private val _state = MutableStateFlow<OrderDetailState>(
        if (orderId == null) {
            OrderDetailState.Success(emptyList())
        } else {
            OrderDetailState.Loading
        }
    )
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    private val _items = MutableStateFlow<List<OrderDetailsResponse>>(emptyList())
    val items: StateFlow<List<OrderDetailsResponse>> = _items.asStateFlow()

    private val _draftRequests = MutableStateFlow<List<DraftOrderDetailUi>>(emptyList())
    val draftRequests: StateFlow<List<DraftOrderDetailUi>> = _draftRequests.asStateFlow()

    private val _submissionError = MutableStateFlow<String?>(null)
    val submissionError: StateFlow<String?> = _submissionError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        if (orderId != null) {
            load()
            val oid: Long = orderId
            viewModelScope.launch {
                webSocketClient.events
                    .filter { it.orderId == oid }
                    .collect { load() }
            }
        }
    }

    fun load() {
        val persistedOrderId = orderId ?: run {
            _items.value = emptyList()
            _state.value = OrderDetailState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _state.value = OrderDetailState.Loading
            runCatching {
                api.getOrderDetails(persistedOrderId)
            }.onSuccess { response ->
                _items.value = response
                _state.value = OrderDetailState.Success(response)
            }.onFailure { error ->
                _state.value = OrderDetailState.Error(error.message ?: error.toString())
            }
        }
    }

    fun getOrderId(): Long? = orderId

    fun getTableId(): Long? = tableId

    fun addConfiguredDraft(line: ConfiguredOrderLineUi) {
        _draftRequests.update { current ->
            current + DraftOrderDetailUi(
                uiId = UUID.randomUUID().toString(),
                line = line
            )
        }
        _submissionError.value = null
    }

    fun removeDraft(uiId: String) {
        _draftRequests.update { current -> current.filterNot { it.uiId == uiId } }
    }

    fun clearDraft() {
        _draftRequests.value = emptyList()
        _submissionError.value = null
    }

    suspend fun submitDraft(): SubmitDraftResult {
        val drafts = _draftRequests.value
        if (drafts.isEmpty()) {
            return SubmitDraftResult.NoDraft
        }

        _isSubmitting.value = true
        _submissionError.value = null

        return try {
            val cashSessionId = cashSessionStore.cashSessionId.first()
            if (cashSessionId == null) {
                _submissionError.value = "No hay caja abierta."
                SubmitDraftResult.Failed
            } else {
                val batchId = UUID.randomUUID().toString()
                val requests = drafts.map { draft ->
                    draft.line.toRequest(
                        persistedOrderId = orderId,
                        cashSessionId = cashSessionId,
                        batchId = batchId
                    )
                }

                if (orderId == null) {
                    if (tableId != null) {
                        api.createOrderDetailsByTable(tableId, requests)
                        val resolvedOrderId = resolveOpenedOrderId()

                        if (resolvedOrderId == null) {
                            _submissionError.value = "No se pudo resolver el pedido abierto."
                            SubmitDraftResult.Failed
                        } else {
                            _draftRequests.value = emptyList()
                            SubmitDraftResult.OpenedNewOrder(
                                orderId = resolvedOrderId,
                                tableId = tableId
                            )
                        }
                    } else {
                        val createdOrder = api.createOrderDetailsWithoutTable(requests)
                        _draftRequests.value = emptyList()
                        SubmitDraftResult.OpenedNewOrder(
                            orderId = createdOrder.id,
                            tableId = null
                        )
                    }
                } else {
                    api.createOrderDetails(requests)
                    val refreshedItems = api.getOrderDetails(orderId)
                    _items.value = refreshedItems
                    _state.value = OrderDetailState.Success(refreshedItems)
                    _draftRequests.value = emptyList()
                    SubmitDraftResult.ExistingOrderUpdated
                }
            }
        } catch (error: Exception) {
            _submissionError.value = error.message ?: "No se pudo enviar a cocina"
            SubmitDraftResult.Failed
        } finally {
            _isSubmitting.value = false
        }
    }

    private suspend fun resolveOpenedOrderId(): Long? {
        val currentTableId = tableId ?: return null
        return overviewApi.getOrdersWithTable()
            .firstOrNull { overviewRow -> overviewRow.tableId == currentTableId }
            ?.order
            ?.orderId
    }

    private fun ConfiguredOrderLineUi.toRequest(
        persistedOrderId: Long?,
        cashSessionId: Long,
        batchId: String
    ): OrderDetailRequest {
        return OrderDetailRequest(
            productId = productId,
            orderId = persistedOrderId,
            name = displayName,
            comment = comment,
            amount = quantity,
            unitPrice = unitPrice,
            status = "SENT",
            paymentMethod = null,
            batchId = batchId,
            createdAt = LocalDateTime.now(),
            cashSessionId = cashSessionId
        )
    }
}