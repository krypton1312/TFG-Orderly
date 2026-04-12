package com.example.orderlyphone.ui.screen.tablePicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderlyphone.data.remote.OverviewApi
import com.example.orderlyphone.data.remote.RestTableApi
import com.example.orderlyphone.domain.model.TablePickerItemUi
import com.example.orderlyphone.domain.model.TablePickerSectionUi
import com.example.orderlyphone.domain.model.TablePickerZone
import com.example.orderlyphone.domain.model.mergeTablePickerSections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TablePickerViewModel @Inject constructor(
    private val restTableApi: RestTableApi,
    private val overviewApi: OverviewApi
) : ViewModel() {

    private val _state = MutableStateFlow(TablePickerState(isLoading = true))
    val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<TablePickerNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { current -> current.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val tables = restTableApi.getAllTables()
                val overview = overviewApi.getOrdersWithTable()
                mergeTablePickerSections(tables, overview)
                    .filter { section -> section.zone != TablePickerZone.Other }
                    .map { section ->
                        TablePickerSectionUi(
                            zone = section.zone,
                            items = section.items.sortedBy(TablePickerItemUi::number)
                        )
                    }
            }.onSuccess { sections ->
                _state.value = TablePickerState(isLoading = false, sections = sections)
            }.onFailure { error ->
                _state.value = TablePickerState(
                    isLoading = false,
                    errorMessage = error.message ?: "No se pudieron cargar las mesas"
                )
            }
        }
    }

    fun onTableTapped(item: TablePickerItemUi) {
        viewModelScope.launch {
            if (item.orderId != null) {
                _navigationEvents.emit(
                    TablePickerNavigationEvent.OpenExistingOrder(
                        orderId = item.orderId,
                        tableId = item.tableId
                    )
                )
            } else {
                _state.update { current -> current.copy(pendingFreeTable = item) }
            }
        }
    }

    fun dismissFreeTableConfirmation() {
        _state.update { current -> current.copy(pendingFreeTable = null) }
    }

    fun startOrderWithoutTable() {
        viewModelScope.launch {
            _navigationEvents.emit(TablePickerNavigationEvent.StartNewOrder(tableId = null))
        }
    }

    fun confirmFreeTable() {
        val pendingTable = _state.value.pendingFreeTable ?: return

        viewModelScope.launch {
            _state.update { current -> current.copy(pendingFreeTable = null) }
            _navigationEvents.emit(TablePickerNavigationEvent.StartNewOrder(pendingTable.tableId))
        }
    }
}