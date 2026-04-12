package com.example.orderlyphone.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.orderlyphone.domain.model.TablePickerItemUi
import com.example.orderlyphone.domain.model.TablePickerSectionUi
import com.example.orderlyphone.domain.model.TablePickerZone
import com.example.orderlyphone.ui.screen.tablePicker.TablePickerContent
import com.example.orderlyphone.ui.screen.tablePicker.TablePickerState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tablePickerShowsSectionsTotalsAndOccupiedTapBranch() {
        val occupiedTable = TablePickerItemUi(
            tableId = 1L,
            zone = TablePickerZone.Inside,
            number = 4,
            displayName = "Mesa 4",
            status = "OCCUPIED",
            orderId = 77L,
            total = BigDecimal("12.50")
        )
        val freeTable = TablePickerItemUi(
            tableId = 2L,
            zone = TablePickerZone.Outside,
            number = 8,
            displayName = "Terraza 8",
            status = "FREE",
            orderId = null,
            total = null
        )
        val state = TablePickerState(
            sections = listOf(
                TablePickerSectionUi(TablePickerZone.Inside, listOf(occupiedTable)),
                TablePickerSectionUi(TablePickerZone.Outside, listOf(freeTable))
            )
        )
        var tappedTable: TablePickerItemUi? = null

        composeRule.setContent {
            TablePickerContent(
                state = state,
                onBack = {},
                onRetry = {},
                onTableTapped = { tappedTable = it },
                onDismissFreeTable = {},
                onConfirmFreeTable = {},
                onStartWithoutTable = {}
            )
        }

        composeRule.onNodeWithText("Interior").assertIsDisplayed()
        composeRule.onNodeWithText("Exterior").assertIsDisplayed()
        composeRule.onNodeWithText(currency(BigDecimal("12.50"))).assertIsDisplayed()

        composeRule.onNodeWithTag("table-card-1").performClick()

        composeRule.runOnIdle {
            assertEquals(1L, tappedTable?.tableId)
            assertEquals(77L, tappedTable?.orderId)
        }
    }

    @Test
    fun freeTableSheetShowsExplicitConfirmation() {
        val freeTable = TablePickerItemUi(
            tableId = 2L,
            zone = TablePickerZone.Outside,
            number = 8,
            displayName = "Terraza 8",
            status = "FREE",
            orderId = null,
            total = null
        )
        var confirmed = false

        composeRule.setContent {
            TablePickerContent(
                state = TablePickerState(pendingFreeTable = freeTable),
                onBack = {},
                onRetry = {},
                onTableTapped = {},
                onDismissFreeTable = {},
                onConfirmFreeTable = { confirmed = true },
                onStartWithoutTable = {}
            )
        }

        composeRule.onNodeWithText("Mesa libre. ¿Quieres abrir un pedido nuevo?").assertIsDisplayed()
        composeRule.onNodeWithText("Abrir pedido").performClick()

        composeRule.runOnIdle {
            assertTrue(confirmed)
        }
    }

    @Test
    fun noTableShortcutStartsTakeawayOrder() {
        var startedWithoutTable = false

        composeRule.setContent {
            TablePickerContent(
                state = TablePickerState(),
                onBack = {},
                onRetry = {},
                onTableTapped = {},
                onDismissFreeTable = {},
                onConfirmFreeTable = {},
                onStartWithoutTable = { startedWithoutTable = true }
            )
        }

        composeRule.onNodeWithText("Pedido sin mesa").assertIsDisplayed()

        composeRule.runOnIdle {
            assertFalse(startedWithoutTable)
        }

        composeRule.onNodeWithTag("start-order-without-table").performClick()

        composeRule.runOnIdle {
            assertTrue(startedWithoutTable)
        }
    }

    private fun currency(value: BigDecimal): String {
        return NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)
    }
}