package com.example.orderlyphone.domain.model.response

import java.time.LocalDate

data class EmployeeResponse(
    val id: Long,
    val name: String,
    val lastname: String,
    val roles: List<RoleResponse>,
    val email: String,
    val hireDate: LocalDate,
    val status: String
)
