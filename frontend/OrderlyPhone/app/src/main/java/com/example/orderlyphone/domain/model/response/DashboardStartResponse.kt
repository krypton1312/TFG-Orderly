package com.example.orderlyphone.domain.model.response

data class DashboardStartResponse(
    val employee: EmployeeResponse,
    val availableTables: Int,
    val occupiedTables: Int,
    val shiftRecord: ShiftRecordResponse
)
