package com.example.orderlyphone.ui.screen.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.remote.CategoryApi
import com.example.orderlyphone.data.remote.ProductsApi
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

    val state: StateFlow<ProductsState> = _state

    fun loadData() {
        viewModelScope.launch {
            _state.value = ProductsState.Loading
            try {
                val products = productsApi.getAllProducts()
                val categories = categoryApi.getAllCategories()
                _state.value = ProductsState.Success(products, categories)
            } catch (e: Exception) {
                _state.value = ProductsState.Error(e.message ?: e.toString())
            }
        }
    }
}