package com.example.orderlyphone.domain.model

import com.example.orderlyphone.domain.model.response.ProductResponse
import com.example.orderlyphone.domain.model.response.SupplementResponse
import java.math.BigDecimal
import java.math.RoundingMode

data class ConfiguredSupplementUi(
    val id: Long,
    val name: String,
    val price: BigDecimal
)

data class ConfiguredOrderLineUi(
    val productId: Long,
    val baseName: String,
    val displayName: String,
    val comment: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal,
    val supplements: List<ConfiguredSupplementUi>
)

fun buildConfiguredOrderLine(
    product: ProductResponse,
    quantity: Int,
    comment: String?,
    selectedSupplements: List<SupplementResponse>
): ConfiguredOrderLineUi {
    val normalizedSupplements = selectedSupplements
        .sortedBy { it.name.lowercase() }
        .map { supplement ->
            ConfiguredSupplementUi(
                id = supplement.id,
                name = supplement.name,
                price = supplement.price.setScale(2, RoundingMode.HALF_UP)
            )
        }

    val cleanComment = comment
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.take(180)

    val supplementsLabel = normalizedSupplements.joinToString(separator = ", ") { it.name }
    val displayName = if (supplementsLabel.isBlank()) {
        product.name
    } else {
        "${product.name} ($supplementsLabel)"
    }

    val unitPrice = normalizedSupplements
        .fold(product.price.setScale(2, RoundingMode.HALF_UP)) { acc, supplement ->
            acc.add(supplement.price)
        }
        .setScale(2, RoundingMode.HALF_UP)

    val lineTotal = unitPrice
        .multiply(quantity.toBigDecimal())
        .setScale(2, RoundingMode.HALF_UP)

    return ConfiguredOrderLineUi(
        productId = product.id,
        baseName = product.name,
        displayName = displayName,
        comment = cleanComment,
        quantity = quantity.coerceAtLeast(1),
        unitPrice = unitPrice,
        lineTotal = lineTotal,
        supplements = normalizedSupplements
    )
}