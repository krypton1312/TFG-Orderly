package com.example.orderlyphone.contract

import com.example.orderlyphone.data.remote.OrderDetailApi
import com.example.orderlyphone.data.remote.adapter.LocalDateAdapter
import com.example.orderlyphone.data.remote.adapter.LocalDateTimeAdapter
import com.example.orderlyphone.domain.model.request.OrderDetailRequest
import com.example.orderlyphone.domain.model.response.DashboardStartResponse
import com.example.orderlyphone.domain.model.response.OrderDetailsResponse
import com.example.orderlyphone.domain.model.response.OrderResponse
import com.example.orderlyphone.domain.model.response.OrderWithTableResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class PhoneContractAlignmentTest {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    @Test
    fun dashboardStartResponse_allowsNullShiftRecordAndCashSessionId() {
        val json = """
            {
              "employee": {
                "id": 1,
                "name": "Ana",
                "lastname": "Lopez",
                "roles": [{"id": 1, "name": "WAITER"}],
                "email": "ana@example.com",
                "hireDate": "2026-04-12",
                "status": "ACTIVE"
              },
              "availableTables": 5,
              "occupiedTables": 2,
              "shiftRecord": null,
              "cashSessionId": null
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, DashboardStartResponse::class.java)

        assertNull(parsed.shiftRecord)
        assertNull(parsed.cashSessionId)
    }

    @Test
    fun orderWithTableResponse_allowsRowsWithoutTableId() {
        val json = """
            {
              "tableId": null,
              "tableName": "Sin mesa",
              "order": {
                "orderId": 17,
                "total": 12.5
              }
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, OrderWithTableResponse::class.java)

        assertNull(parsed.tableId)
        assertEquals(17L, parsed.order.orderId)
        assertEquals(BigDecimal("12.5"), parsed.order.total)
    }

    @Test
    fun createOrderDetailsByTable_exposesNoBodySuccessContract() {
        val api = object : OrderDetailApi {
            override suspend fun getOrderDetails(orderId: Long): List<OrderDetailsResponse> = emptyList()

            override suspend fun createOrderDetails(orderDetails: List<OrderDetailRequest>): List<OrderDetailsResponse> = emptyList()

            override suspend fun updateOrderDetail(
                id: Long,
                orderDetail: OrderDetailRequest
            ): OrderDetailsResponse {
                throw UnsupportedOperationException("unused in contract test")
            }

            override suspend fun decreaseAmount(id: Long, amount: Int) {
            }

            override suspend fun createOrderDetailsByTable(
                tableId: Long,
                orderDetails: List<OrderDetailRequest>
            ) {
            }

            override suspend fun createOrderDetailsWithoutTable(orderDetails: List<OrderDetailRequest>): OrderResponse {
                return OrderResponse(id = 42L, state = "OPEN")
            }
        }

        assertNoBodyContract(api::createOrderDetailsByTable)
    }

    @Test
    fun createOrderDetailsWithoutTable_responseKeepsCreatedOrderId() {
        val json = """
            {
              "id": 42,
              "state": "OPEN",
              "paymentMethod": "N/A",
              "total": 0.0,
              "restTable": null
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, OrderResponse::class.java)

        assertEquals(42L, parsed.id)
        assertEquals("OPEN", parsed.state)
    }

    private fun assertNoBodyContract(contract: suspend (Long, List<OrderDetailRequest>) -> Unit) {
        assertNotNull(contract)
    }
}