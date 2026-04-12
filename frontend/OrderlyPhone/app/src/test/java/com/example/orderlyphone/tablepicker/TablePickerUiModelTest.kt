package com.example.orderlyphone.tablepicker

import com.example.orderlyphone.domain.model.TablePickerZone
import com.example.orderlyphone.domain.model.mergeTablePickerSections
import com.example.orderlyphone.domain.model.response.OrderSummary
import com.example.orderlyphone.domain.model.response.OrderWithTableResponse
import com.example.orderlyphone.domain.model.response.RestTableResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class TablePickerUiModelTest {

    @Test
    fun mergeTablePickerSections_ordersZonesAndTableNumbers() {
        val tables = listOf(
            RestTableResponse(id = 3L, number = 3, name = "Mesa 3", status = "Disponible", position = "INSIDE"),
            RestTableResponse(id = 2L, number = 2, name = "Mesa 2", status = "Ocupado", position = "OUTSIDE"),
            RestTableResponse(id = 1L, number = 1, name = "Mesa 1", status = "Disponible", position = "INSIDE")
        )
        val overview = listOf(
            OrderWithTableResponse(
                tableId = 2L,
                tableName = "Mesa 2",
                order = OrderSummary(orderId = 200L, total = BigDecimal("14.50"))
            )
        )

        val sections = mergeTablePickerSections(tables, overview)

        assertEquals(listOf(TablePickerZone.Inside, TablePickerZone.Outside), sections.map { it.zone })
        assertEquals(listOf(1, 3), sections.first().items.map { it.number })
        assertEquals(listOf(2), sections.last().items.map { it.number })
    }

    @Test
    fun mergeTablePickerSections_attachesOpenOrderTotalsOnlyToMatchingTable() {
        val tables = listOf(
            RestTableResponse(id = 10L, number = 10, name = "Mesa 10", status = "Ocupado", position = "INSIDE"),
            RestTableResponse(id = 11L, number = 11, name = "Mesa 11", status = "Disponible", position = "INSIDE")
        )
        val overview = listOf(
            OrderWithTableResponse(
                tableId = 10L,
                tableName = "Mesa 10",
                order = OrderSummary(orderId = 999L, total = BigDecimal("22.30"))
            ),
            OrderWithTableResponse(
                tableId = null,
                tableName = "Sin mesa",
                order = OrderSummary(orderId = 501L, total = BigDecimal("4.20"))
            )
        )

        val items = mergeTablePickerSections(tables, overview).flatMap { it.items }
        val occupiedTable = items.first { it.tableId == 10L }
        val freeTable = items.first { it.tableId == 11L }

        assertEquals(999L, occupiedTable.orderId)
        assertEquals(BigDecimal("22.30"), occupiedTable.total)
        assertNull(freeTable.orderId)
        assertNull(freeTable.total)
    }
}