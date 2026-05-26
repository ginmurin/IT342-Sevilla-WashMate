package com.example.washmate

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.OrderDTO
import com.example.washmate.databinding.ActivityOrdersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set active navigation tab
        binding.bottomNavigation.selectedItemId = R.id.nav_orders
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_orders -> true
                R.id.nav_wallet -> {
                    val intent = Intent(this, WalletActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        binding.btnCreateFirstOrder.setOnClickListener {
            val intent = Intent(this, ServicesActivity::class.java)
            startActivity(intent)
        }

        fetchOrders()
    }

    private fun fetchOrders() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.llEmptyState.visibility = View.GONE
                binding.scrollViewOrders.visibility = View.GONE

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMyOrders()
                }

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val ordersList = response.body()!!
                    if (ordersList.isEmpty()) {
                        binding.llEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.scrollViewOrders.visibility = View.VISIBLE
                        populateOrders(ordersList)
                    }
                } else {
                    Toast.makeText(this@OrdersActivity, "Failed to load orders: ${response.code()}", Toast.LENGTH_SHORT).show()
                    binding.llEmptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.llEmptyState.visibility = View.VISIBLE
                Toast.makeText(this@OrdersActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateOrders(orders: List<OrderDTO>) {
        binding.llOrdersContainer.removeAllViews()

        val sortedOrders = orders.sortedByDescending { it.createdAt }

        for (order in sortedOrders) {
            val orderView = layoutInflater.inflate(R.layout.item_order, binding.llOrdersContainer, false)

            val tvOrderNumber = orderView.findViewById<TextView>(R.id.tvOrderNumber)
            val tvOrderDate = orderView.findViewById<TextView>(R.id.tvOrderDate)
            val tvOrderAmount = orderView.findViewById<TextView>(R.id.tvOrderAmount)
            val tvOrderStatus = orderView.findViewById<TextView>(R.id.tvOrderStatus)
            val llStatusContainer = orderView.findViewById<LinearLayout>(R.id.llStatusContainer)
            val btnViewDetails = orderView.findViewById<Button>(R.id.btnViewDetails)

            tvOrderNumber.text = "Order #${order.orderNumber}"
            tvOrderAmount.text = "₱${String.format(Locale.US, "%.2f", order.totalAmount)}"
            
            // Format Date
            tvOrderDate.text = formatDate(order.createdAt)

            // Status Badge
            tvOrderStatus.text = order.status.replace("_", " ").uppercase()
            setStatusBadgeColor(order.status, tvOrderStatus, llStatusContainer)

            btnViewDetails.setOnClickListener {
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("ORDER_ID", order.orderId)
                intent.putExtra("ORDER_NUMBER", order.orderNumber)
                startActivity(intent)
            }

            binding.llOrdersContainer.addView(orderView)
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr.split("T")[0]
        }
    }

    private fun setStatusBadgeColor(status: String, tvStatus: TextView, container: LinearLayout) {
        val (bgColor, textColor) = when (status.uppercase()) {
            "PENDING" -> Pair("#FEF3C7", "#D97706") // Yellow 100, Yellow 600
            "CONFIRMED" -> Pair("#DBEAFE", "#2563EB") // Blue 100, Blue 600
            "PICKED_UP" -> Pair("#E0E7FF", "#4F46E5") // Indigo 100, Indigo 600
            "IN_PROCESS" -> Pair("#F3E8FF", "#9333EA") // Purple 100, Purple 600
            "READY" -> Pair("#D1FAE5", "#059669") // Emerald 100, Emerald 600
            "DELIVERED" -> Pair("#F1F5F9", "#475569") // Slate 100, Slate 600
            "CANCELLED" -> Pair("#FEE2E2", "#DC2626") // Red 100, Red 600
            else -> Pair("#F1F5F9", "#475569")
        }

        container.background.setTint(Color.parseColor(bgColor))
        tvStatus.setTextColor(Color.parseColor(textColor))
    }
}
