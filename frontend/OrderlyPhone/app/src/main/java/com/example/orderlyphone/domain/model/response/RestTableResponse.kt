package com.example.orderlyphone.domain.model.response

data class RestTableResponse(
    val id: Long,
    val number: Int,
    val name: String,
    val status: String,
    val position: String
)