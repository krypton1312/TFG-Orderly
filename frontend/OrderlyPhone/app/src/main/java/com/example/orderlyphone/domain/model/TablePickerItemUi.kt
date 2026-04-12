package com.example.orderlyphone.domain.model

import com.example.orderlyphone.domain.model.response.OrderWithTableResponse
import com.example.orderlyphone.domain.model.response.RestTableResponse
import java.math.BigDecimal

enum class TablePickerZone {
    Inside,
    Outside,
    Other;

    companion object {
        fun fromBackend(position: String?): TablePickerZone {
            return when (position?.trim()?.uppercase()) {
                "INSIDE" -> Inside
                "OUTSIDE" -> Outside
                else -> Other
            }
        }
    }
}

data class TablePickerItemUi(
    val tableId: Long,
    val zone: TablePickerZone,
    val number: Int,
    val displayName: String,
    val status: String,
    val orderId: Long?,
    val total: BigDecimal?
)

data class TablePickerSectionUi(
    val zone: TablePickerZone,
    val items: List<TablePickerItemUi>
)

fun mergeTablePickerSections(
    tables: List<RestTableResponse>,
    overview: List<OrderWithTableResponse>
): List<TablePickerSectionUi> {
    val openOrdersByTableId = overview
        .mapNotNull { overviewItem ->
            val tableId = overviewItem.tableId
            val orderId = overviewItem.order.orderId
            if (tableId == null || orderId == null) {
                null
            } else {
                tableId to overviewItem.order
            }
        }
        .toMap()

    val orderedZones = listOf(TablePickerZone.Inside, TablePickerZone.Outside, TablePickerZone.Other)

    val items = tables
        .map { table ->
            val openOrder = openOrdersByTableId[table.id]

            TablePickerItemUi(
                tableId = table.id,
                zone = TablePickerZone.fromBackend(table.position),
                number = table.number,
                displayName = table.name.ifBlank { "Mesa ${table.number}" },
                status = table.status,
                orderId = openOrder?.orderId,
                total = openOrder?.total
            )
        }

    return orderedZones.mapNotNull { zone ->
        val zoneItems = items
            .filter { it.zone == zone }
            .sortedBy { it.number }

        if (zoneItems.isEmpty()) {
            null
        } else {
            TablePickerSectionUi(zone = zone, items = zoneItems)
        }
    }
}