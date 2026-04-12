package com.example.orderlyphone.domain.model.response

data class ProductsWithSupplementsResponse(
    val products: List<ProductResponse> = emptyList(),
    val supplements: List<SupplementResponse> = emptyList()
)