package com.example.orderlyphone.ui.screen.productConfigurator

import com.example.orderlyphone.domain.model.response.ProductResponse
import com.example.orderlyphone.domain.model.response.SupplementResponse

data class ProductConfiguratorState(
    val isLoading: Boolean = false,
    val product: ProductResponse? = null,
    val compatibleSupplements: List<SupplementResponse> = emptyList(),
    val selectedSupplementIds: Set<Long> = emptySet(),
    val quantity: Int = 1,
    val comment: String = "",
    val errorMessage: String? = null
)