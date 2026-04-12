package com.example.orderlyphone.ui.screen.tablePicker

import com.example.orderlyphone.domain.model.TablePickerItemUi
import com.example.orderlyphone.domain.model.TablePickerSectionUi

data class TablePickerState(
    val isLoading: Boolean = false,
    val sections: List<TablePickerSectionUi> = emptyList(),
    val pendingFreeTable: TablePickerItemUi? = null,
    val errorMessage: String? = null
)

sealed interface TablePickerNavigationEvent {
    data class OpenExistingOrder(val orderId: Long, val tableId: Long) : TablePickerNavigationEvent
    data class StartNewOrder(val tableId: Long?) : TablePickerNavigationEvent
}