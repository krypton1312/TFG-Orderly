package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.response.EmployeeResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface EmployeeApi {
    @GET("employees")
    suspend fun getEmployees(): List<EmployeeResponse>

    @GET("employees/{id}")
    suspend fun getEmployeeById(@Path("id") id: Long): EmployeeResponse

    @GET("employees/email/{email}")
    suspend fun getEmployeeByEmail(@Path("email") email: String): EmployeeResponse

    @GET("employees/currentEmployee")
    suspend fun getCurrentEmployee(): EmployeeResponse
}
