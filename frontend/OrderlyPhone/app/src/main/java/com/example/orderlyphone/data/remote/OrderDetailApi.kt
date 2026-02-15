package com.example.orderlyphone.data.remote

import com.example.orderlyphone.domain.model.request.OrderDetailRequest
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderDetailApi {
    @GET("orderDetails/order/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: Long): List<OrderDetailsResponse>

    @POST("orderDetails/list")
    suspend fun createOrderDetails(@Body orderDetails: List<OrderDetailRequest>): List<OrderDetailsResponse>

    @PUT("orderDetails/{id}")
    suspend fun updateOrderDetail(@Path("id") id: Long, @Body orderDetail: OrderDetailRequest): OrderDetailsResponse

    @PUT("orderDetails/decrease-amount/{id},{amount}")
    suspend fun decreaseAmount(@Path("id") id: Long, @Path("amount") amount: Int)

    @POST("orderDetails/table/{tableId}")
    suspend fun createOrderDetailsByTable(@Path("tableId") tableId: Long, @Body orderDetails: List<OrderDetailRequest>): List<OrderDetailsResponse>
}