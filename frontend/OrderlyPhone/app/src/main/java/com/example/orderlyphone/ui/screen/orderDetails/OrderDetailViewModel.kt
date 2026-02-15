package com.example.orderlyphone.ui.screen.orderDetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.local.CashSessionStore
import com.example.orderlyphone.data.remote.OrderDetailApi
import com.example.orderlyphone.domain.model.DraftOrderDetailUi
import com.example.orderlyphone.domain.model.request.OrderDetailRequest
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import com.example.orderlyphone.domain.model.response.ProductResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val api: OrderDetailApi,
    savedStateHandle: SavedStateHandle,
    private val cashSessionStore: CashSessionStore
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"]) {
        "orderId is missing in SavedStateHandle"
    }

    private val tableId: Long = checkNotNull(savedStateHandle["tableId"]) {
        "tableId is missing in SavedStateHandle"
    }

    private val _state = MutableStateFlow<OrderDetailState>(OrderDetailState.Idle)
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    private val _items = MutableStateFlow<List<OrderDetailsResponse>>(emptyList())
    val items: StateFlow<List<OrderDetailsResponse>> = _items.asStateFlow()

    // ✅ Временные позиции, которые пришли с ProductsScreen, но пока не сохранены
    private val _draftRequests = MutableStateFlow<List<DraftOrderDetailUi>>(emptyList())
    val draftRequests: StateFlow<List<DraftOrderDetailUi>> = _draftRequests.asStateFlow()


    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = OrderDetailState.Loading
            try {
                val response = api.getOrderDetails(orderId)
                _items.value = response
                _state.value = OrderDetailState.Success(response)
            } catch (e: Exception) {
                _state.value = OrderDetailState.Error(e.message ?: e.toString())
            }
        }
    }

    fun increase(item: OrderDetailsResponse) {
        addDraftOrderDetailToOrder(item)
    }

    fun decrease(item: OrderDetailsResponse) {
        val newAmount = item.amount - 1
        if (newAmount < 1) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.decreaseAmount(item.id, newAmount)
                }

                _items.update { list ->
                    list.map {
                        if (it.id == item.id) it.copy(amount = newAmount) else it
                    }
                }

            } catch (e: Exception) {
                _state.value = OrderDetailState.Error(e.message ?: e.toString())
            }
        }
    }

    fun remove(item: OrderDetailsResponse) {
        _items.value = _items.value.filterNot { it.id == item.id }
    }

    fun clear() {
        _items.value = emptyList()
    }

    fun getOrderId(): Long? = orderId

    fun getTableId(): Long? = tableId

    fun addDraftProductsToOrder(products: Map<ProductResponse, Int>) {
        viewModelScope.launch {
            try {
                val cashSessionId: Long? = cashSessionStore.cashSessionId.first()
                if (cashSessionId == null) {
                    _state.value = OrderDetailState.Error("No hay caja abierta (cash session).")
                    return@launch
                }

                val batchId = UUID.randomUUID().toString()

                val newDrafts = products.map { (product, amount) ->
                    DraftOrderDetailUi(
                        uiId = UUID.randomUUID().toString(),
                        req = OrderDetailRequest(
                            productId = product.id,
                            orderId = orderId,
                            name = product.name,
                            comment = null,
                            amount = amount,
                            unitPrice = product.price,
                            status = "PENDING",
                            paymentMethod = null,
                            batchId = batchId,
                            createdAt = LocalDateTime.now(),
                            cashSessionId = cashSessionId
                        )
                    )
                }

                _draftRequests.update { old -> old + newDrafts }

            } catch (e: Exception) {
                _state.value = OrderDetailState.Error(e.message ?: e.toString())
            }
        }
    }

    fun addDraftOrderDetailToOrder(item: OrderDetailsResponse) {
        viewModelScope.launch {
            try {
                val cashSessionId: Long? = cashSessionStore.cashSessionId.first()
                if (cashSessionId == null) {
                    _state.value = OrderDetailState.Error("No hay caja abierta (cash session).")
                    return@launch
                }

                val batchId = UUID.randomUUID().toString()

                val newDrafts =
                    DraftOrderDetailUi(
                        uiId = UUID.randomUUID().toString(),
                        req = OrderDetailRequest(
                            productId = item.productId,
                            orderId = orderId,
                            name = item.name,
                            comment = null,
                            amount = 1,
                            unitPrice = item.unitPrice,
                            status = "PENDING",
                            paymentMethod = null,
                            batchId = batchId,
                            createdAt = LocalDateTime.now(),
                            cashSessionId = cashSessionId
                        )
                    )
                _draftRequests.update { old -> old + newDrafts }

            } catch (e: Exception) {
                _state.value = OrderDetailState.Error(e.message ?: e.toString())
            }
        }
    }


    fun clearDraft() {
        _draftRequests.value = emptyList()
    }

    fun removeDraft(uiId: String) {
        _draftRequests.value = _draftRequests.value.filterNot { it.uiId == uiId }
    }

    fun addProductsToOrder() {
        viewModelScope.launch {
            val reqs = _draftRequests.value.map { it.req }

            Log.d("CheckDraft", reqs.toString())
            Log.d("CheckDraftTableId", tableId.toString())

            try {
                val hasZeroOrderId = reqs.any { it.orderId == 0L }

                if (hasZeroOrderId) {
                    Log.d("CheckDraft", "hasZeroOrderId")
                    api.createOrderDetailsByTable(tableId,reqs)
                } else {
                    api.createOrderDetails(reqs)
                }

            } catch (e: Exception) {
                Log.d("CheckDraftError", e.message.toString())
            }
        }
    }


}