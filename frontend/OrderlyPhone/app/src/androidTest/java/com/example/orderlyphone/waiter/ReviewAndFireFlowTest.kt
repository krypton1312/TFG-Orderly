package com.example.orderlyphone.waiter

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.orderlyphone.domain.model.DraftOrderDetailUi
import com.example.orderlyphone.domain.model.buildConfiguredOrderLine
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import com.example.orderlyphone.domain.model.response.ProductResponse
import com.example.orderlyphone.ui.screen.orderDetails.OrderDetailContent
import com.example.orderlyphone.ui.screen.orderDetails.OrderDetailState
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewAndFireFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun reviewContentSeparatesSentAndDraftAndAllowsFire() {
        val sentItem = OrderDetailsResponse(
            id = 1L,
            productId = 20L,
            name = "Café",
            orderId = 99L,
            comment = "Sin hielo",
            amount = 1,
            unitPrice = BigDecimal("2.10"),
            status = "SENT",
            paymentMethod = null,
            createdAt = "2026-04-12T00:00:00",
            destination = "BAR",
            batchId = "batch-1"
        )
        val product = ProductResponse(
            id = 30L,
            name = "Tostada",
            price = BigDecimal("3.00"),
            stock = 10,
            categoryId = 4L,
            destination = "KITCHEN"
        )
        val draft = DraftOrderDetailUi(
            uiId = "draft-1",
            line = buildConfiguredOrderLine(
                product = product,
                quantity = 2,
                comment = "Muy hecha",
                selectedSupplements = emptyList()
            )
        )
        var fired = false

        composeRule.setContent {
            OrderDetailContent(
                state = OrderDetailState.Success(listOf(sentItem)),
                items = listOf(sentItem),
                draft = listOf(draft),
                orderLabel = "Cuenta #99",
                tableLabel = "Mesa 4",
                total = BigDecimal("8.10"),
                snackbarHostState = SnackbarHostState(),
                submissionError = null,
                isSubmitting = false,
                onBack = {},
                onAddItem = {},
                onFireOrder = { fired = true },
                onRemoveDraft = {}
            )
        }

        composeRule.onNodeWithText("Ya enviado").assertIsDisplayed()
        composeRule.onNodeWithText("Borrador actual").assertIsDisplayed()
        composeRule.onNodeWithText("Enviado").assertIsDisplayed()
        composeRule.onNodeWithText("Enviar a cocina").assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertTrue(fired)
        }
    }

    @Test
    fun fireButtonStaysDisabledWhenDraftIsEmpty() {
        composeRule.setContent {
            OrderDetailContent(
                state = OrderDetailState.Success(emptyList()),
                items = emptyList(),
                draft = emptyList(),
                orderLabel = "Cuenta #12",
                tableLabel = "Mesa 2",
                total = BigDecimal.ZERO,
                snackbarHostState = SnackbarHostState(),
                submissionError = null,
                isSubmitting = false,
                onBack = {},
                onAddItem = {},
                onFireOrder = {},
                onRemoveDraft = {}
            )
        }

        composeRule.onNodeWithText("Enviar a cocina").assertIsNotEnabled()
    }
}