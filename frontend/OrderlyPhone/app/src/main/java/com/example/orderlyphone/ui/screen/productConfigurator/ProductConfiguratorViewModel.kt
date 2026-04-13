package com.example.orderlyphone.ui.screen.productConfigurator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.remote.ProductsApi
import com.example.orderlyphone.domain.model.ConfiguredOrderLineUi
import com.example.orderlyphone.domain.model.buildConfiguredOrderLine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductConfiguratorViewModel @Inject constructor(
    private val productsApi: ProductsApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = checkNotNull(savedStateHandle["categoryId"]) {
        "categoryId is missing in SavedStateHandle"
    }
    private val productId: Long = checkNotNull(savedStateHandle["productId"]) {
        "productId is missing in SavedStateHandle"
    }

    private val _state = MutableStateFlow(ProductConfiguratorState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { current -> current.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val payload = productsApi.getProductsWithSupplementsByCategory(categoryId)
                val product = payload.products.firstOrNull { it.id == productId }
                    ?: error("No se encontró el producto")

                product to payload.supplements
                    .filter { it.isCompatibleWith(product) }
                    .sortedBy { it.name.lowercase() }
            }.onSuccess { (product, supplements) ->
                _state.value = ProductConfiguratorState(
                    isLoading = false,
                    product = product,
                    compatibleSupplements = supplements,
                    quantity = 1
                )
            }.onFailure { error ->
                _state.value = ProductConfiguratorState(
                    isLoading = false,
                    errorMessage = error.message ?: "No se pudo cargar el configurador"
                )
            }
        }
    }

    fun increaseQuantity() {
        _state.update { current ->
            current.copy(quantity = (current.quantity + 1).coerceAtMost(99))
        }
    }

    fun decreaseQuantity() {
        _state.update { current -> current.copy(quantity = (current.quantity - 1).coerceAtLeast(1)) }
    }

    fun updateComment(comment: String) {
        _state.update { current -> current.copy(comment = comment.take(180)) }
    }

    fun toggleSupplement(supplementId: Long) {
        _state.update { current ->
            val nextIds = current.selectedSupplementIds.toMutableSet()
            if (!nextIds.add(supplementId)) {
                nextIds.remove(supplementId)
            }
            current.copy(selectedSupplementIds = nextIds)
        }
    }

    fun buildConfiguredLine(): ConfiguredOrderLineUi? {
        val currentState = _state.value
        val product = currentState.product ?: return null
        val selectedSupplements = currentState.compatibleSupplements.filter {
            currentState.selectedSupplementIds.contains(it.id)
        }

        return buildConfiguredOrderLine(
            product = product,
            quantity = currentState.quantity,
            comment = currentState.comment,
            selectedSupplements = selectedSupplements
        )
    }
}