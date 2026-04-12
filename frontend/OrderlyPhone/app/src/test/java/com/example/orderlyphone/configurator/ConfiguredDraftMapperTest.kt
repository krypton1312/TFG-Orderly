package com.example.orderlyphone.configurator

import com.example.orderlyphone.domain.model.buildConfiguredOrderLine
import com.example.orderlyphone.domain.model.response.ProductResponse
import com.example.orderlyphone.domain.model.response.SupplementResponse
import com.example.orderlyphone.domain.model.response.SupplementProductSummary
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguredDraftMapperTest {

    @Test
    fun buildConfiguredOrderLine_preservesQuantityCommentAndSupplementPricing() {
        val product = ProductResponse(
            id = 10L,
            name = "Café",
            price = BigDecimal("1.50"),
            stock = 10,
            categoryId = 3L,
            destination = "BAR"
        )
        val oatMilk = SupplementResponse(
            id = 1L,
            name = "Avena",
            price = BigDecimal("0.40"),
            products = listOf(SupplementProductSummary(id = 10L, name = "Café"))
        )
        val caramel = SupplementResponse(
            id = 2L,
            name = "Caramelo",
            price = BigDecimal("0.60"),
            products = listOf(SupplementProductSummary(id = 10L, name = "Café"))
        )

        val line = buildConfiguredOrderLine(
            product = product,
            quantity = 2,
            comment = "  Sin canela  ",
            selectedSupplements = listOf(caramel, oatMilk)
        )

        assertEquals("Café (Avena, Caramelo)", line.displayName)
        assertEquals("Sin canela", line.comment)
        assertEquals(2, line.quantity)
        assertEquals(0, line.unitPrice.compareTo(BigDecimal("2.50")))
        assertEquals(0, line.lineTotal.compareTo(BigDecimal("5.00")))
        assertEquals(2, line.supplements.size)
        assertTrue(line.supplements.any { it.name == "Avena" })
        assertTrue(line.supplements.any { it.name == "Caramelo" })
    }
}