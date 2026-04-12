package com.example.orderlyphone.domain.model.response

import java.math.BigDecimal

data class SupplementResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val categories: List<SupplementCategorySummary> = emptyList(),
    val products: List<SupplementProductSummary> = emptyList()
) {
    fun isCompatibleWith(product: ProductResponse): Boolean {
        if (products.isNotEmpty()) {
            return products.any { it.id == product.id }
        }

        if (categories.isNotEmpty()) {
            return categories.any { it.id == product.categoryId }
        }

        return true
    }
}

data class SupplementCategorySummary(
    val id: Long,
    val name: String
)

data class SupplementProductSummary(
    val id: Long,
    val name: String
)