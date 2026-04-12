package com.example.orderlyphone.ui.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.remote.CategoryApi
import com.example.orderlyphone.data.remote.ProductsApi
import com.example.orderlyphone.domain.model.response.ProductsWithSupplementsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productsApi: ProductsApi,
    private val categoryApi: CategoryApi
) : ViewModel() {
    private val _state = MutableStateFlow<ProductsState>(ProductsState.Idle)
    private val categoryPayloadCache = mutableMapOf<Long, ProductsWithSupplementsResponse>()

    val state: StateFlow<ProductsState> = _state

    fun loadData() {
        viewModelScope.launch {
            _state.value = ProductsState.Loading
            try {
                val categories = categoryApi.getAllCategories()
                val firstCategoryId = categories.firstOrNull()?.id

                if (firstCategoryId == null) {
                    _state.value = ProductsState.Success(
                        products = emptyList(),
                        categories = emptyList(),
                        selectedCategoryId = null,
                        supplements = emptyList()
                    )
                    return@launch
                }

                val payload = getCategoryPayload(firstCategoryId)
                _state.value = ProductsState.Success(
                    products = payload.products.sortedBy { it.name.lowercase() },
                    categories = categories,
                    selectedCategoryId = firstCategoryId,
                    supplements = payload.supplements.sortedBy { it.name.lowercase() }
                )
            } catch (e: Exception) {
                _state.value = ProductsState.Error(e.message ?: e.toString())
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        val currentState = state.value as? ProductsState.Success ?: return
        if (currentState.selectedCategoryId == categoryId) {
            return
        }

        viewModelScope.launch {
            _state.value = ProductsState.Loading
            try {
                val payload = getCategoryPayload(categoryId)
                _state.value = currentState.copy(
                    products = payload.products.sortedBy { it.name.lowercase() },
                    selectedCategoryId = categoryId,
                    supplements = payload.supplements.sortedBy { it.name.lowercase() }
                )
            } catch (e: Exception) {
                _state.value = ProductsState.Error(e.message ?: e.toString())
            }
        }
    }

    private suspend fun getCategoryPayload(categoryId: Long): ProductsWithSupplementsResponse {
        val cachedPayload = categoryPayloadCache[categoryId]
        if (cachedPayload != null) {
            return cachedPayload
        }

        val remotePayload = productsApi.getProductsWithSupplementsByCategory(categoryId)
        categoryPayloadCache[categoryId] = remotePayload
        return remotePayload
    }
}