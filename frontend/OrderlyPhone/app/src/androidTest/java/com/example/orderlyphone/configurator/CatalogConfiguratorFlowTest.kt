package com.example.orderlyphone.configurator

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.orderlyphone.domain.model.ConfiguredOrderLineUi
import com.example.orderlyphone.domain.model.buildConfiguredOrderLine
import com.example.orderlyphone.domain.model.response.CategoryResponse
import com.example.orderlyphone.domain.model.response.ProductResponse
import com.example.orderlyphone.domain.model.response.SupplementResponse
import com.example.orderlyphone.domain.model.response.SupplementProductSummary
import com.example.orderlyphone.ui.screen.productConfigurator.ProductConfiguratorContent
import com.example.orderlyphone.ui.screen.productConfigurator.ProductConfiguratorState
import com.example.orderlyphone.ui.screen.products.ProductsContent
import com.example.orderlyphone.ui.screen.products.ProductsState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogConfiguratorFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun productsContentShowsDraftSummaryAndSelectableCards() {
        val categories = listOf(
            CategoryResponse(id = 1L, name = "Bebidas"),
            CategoryResponse(id = 2L, name = "Postres")
        )
        val availableProduct = ProductResponse(
            id = 11L,
            name = "Café",
            price = BigDecimal("1.80"),
            stock = 5,
            categoryId = 1L,
            destination = "BAR"
        )
        val unavailableProduct = ProductResponse(
            id = 12L,
            name = "Tarta",
            price = BigDecimal("3.20"),
            stock = 0,
            categoryId = 1L,
            destination = "KITCHEN"
        )
        var selectedProduct: Pair<Long, Long>? = null

        composeRule.setContent {
            ProductsContent(
                state = ProductsState.Success(
                    products = listOf(availableProduct, unavailableProduct),
                    categories = categories,
                    selectedCategoryId = 1L,
                    supplements = emptyList()
                ),
                orderLabel = "Nuevo pedido",
                tableLabel = "Mesa 4",
                draftCount = 1,
                draftTotal = BigDecimal("4.20"),
                onBack = {},
                onSelectCategory = {},
                onSelectProduct = { categoryId, productId ->
                    selectedProduct = categoryId to productId
                },
                onReviewOrder = {}
            )
        }

        composeRule.onNodeWithText("1 líneas en borrador").assertIsDisplayed()
        composeRule.onNodeWithText("Sin stock").assertIsDisplayed()
        composeRule.onNodeWithTag("product-card-11").performClick()

        composeRule.runOnIdle {
            assertEquals(1L to 11L, selectedProduct)
        }
    }

    @Test
    fun configuratorShowsSupplementsAndConfirmsFinalizedLine() {
        val product = ProductResponse(
            id = 11L,
            name = "Café",
            price = BigDecimal("1.80"),
            stock = 5,
            categoryId = 1L,
            destination = "BAR"
        )
        val milk = SupplementResponse(
            id = 21L,
            name = "Leche extra",
            price = BigDecimal("0.50"),
            products = listOf(SupplementProductSummary(id = 11L, name = "Café"))
        )
        val syrup = SupplementResponse(
            id = 22L,
            name = "Sirope",
            price = BigDecimal("0.30"),
            products = listOf(SupplementProductSummary(id = 11L, name = "Café"))
        )
        val previewLine = buildConfiguredOrderLine(
            product = product,
            quantity = 2,
            comment = "Sin azúcar",
            selectedSupplements = listOf(milk, syrup)
        )
        var confirmed: ConfiguredOrderLineUi? = null

        composeRule.setContent {
            ProductConfiguratorContent(
                state = ProductConfiguratorState(
                    product = product,
                    compatibleSupplements = listOf(milk, syrup),
                    selectedSupplementIds = setOf(milk.id, syrup.id),
                    quantity = 2,
                    comment = "Sin azúcar"
                ),
                previewLine = previewLine,
                onBack = {},
                onIncreaseQuantity = {},
                onDecreaseQuantity = {},
                onCommentChange = {},
                onToggleSupplement = {},
                onConfirm = { confirmed = previewLine }
            )
        }

        composeRule.onNodeWithTag("configurator-supplement-21").assertIsDisplayed()
        composeRule.onNodeWithText("+ ${currency(BigDecimal("0.50"))}").assertIsDisplayed()
        composeRule.onNodeWithText("Añadir al borrador").performClick()

        composeRule.runOnIdle {
            assertNotNull(confirmed)
            assertEquals("Café (Leche extra, Sirope)", confirmed?.displayName)
        }
    }

    private fun currency(value: BigDecimal): String {
        return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)
    }
}