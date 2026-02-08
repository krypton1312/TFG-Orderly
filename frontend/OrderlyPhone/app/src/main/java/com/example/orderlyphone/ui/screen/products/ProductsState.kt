package com.example.orderlyphone.ui.screen.products

import com.example.orderlyphone.domain.model.response.CategoryResponse
import com.example.orderlyphone.domain.model.response.ProductResponse

sealed class ProductsState {
    object Idle : ProductsState()
    object Loading : ProductsState()
    data class Success(
        val products: List<ProductResponse>,
        val categories: List<CategoryResponse>
    ): ProductsState()
    data class Error(val message: String) : ProductsState()
}