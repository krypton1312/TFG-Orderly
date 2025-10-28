package com.example.orderlytablet

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.orderlytablet.response.OrderResponse
import com.example.orderlytablet.services.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // при запуске экрана сразу делаем запрос
        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                Log.d("ORDERS", "🔹 Отправляю запрос к /orders ...")

                // получаем данные с сервера
                val orders: List<OrderResponse> = RetrofitClient.instance.getOrders()

                Log.d("ORDERS", "✅ Получено ${orders.size} заказов")

                // выводим каждый заказ в лог
                for (order in orders) {
                    Log.d("ORDERS", """
                        --------------------------
                        ID: ${order.id}
                        Mesa: ${order.restTable.name}
                        Total: ${order.total} €
                        Estado: ${order.state}
                        Pago: ${order.paymentMethod}
                        Fecha: ${order.dateTime}
                        --------------------------
                    """.trimIndent())
                }

                Toast.makeText(this@MainActivity, "OK: ${orders.size} заказов", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("ORDERS", "❌ Ошибка при запросе: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
