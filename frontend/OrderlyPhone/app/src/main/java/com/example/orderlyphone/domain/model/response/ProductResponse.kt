package com.example.orderlyphone.domain.model.response

import java.math.BigDecimal

data class ProductResponse (
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val categoryId: Long,
    val destination: String
)