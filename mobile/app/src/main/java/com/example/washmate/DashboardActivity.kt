package com.example.washmate

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.OrderDTO
import com.example.washmate.api.UserSubscriptionDTO
import com.example.washmate.databinding.ActivityDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", "User")
        
        binding.tvWelcomeMessage.text = "Welcome back, $email!"

        binding.btnLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnNewOrder.setOnClickListener {
            val intent = Intent(this, ServicesActivity::class.java)
            startActivity(intent)
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_orders -> {
                    Toast.makeText(this, "Orders clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_wallet -> {
                    val intent = Intent(this, WalletActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Fetch real data from backend
        fetchDashboardData()
    }


    private fun fetchDashboardData() {
        lifecycleScope.launch {
            try {
                // 1. Fetch Subscription
                val subResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMySubscription()
                }
                if (subResponse.isSuccessful && subResponse.body() != null) {
                    updateSubscriptionUI(subResponse.body()!!)
                }

                // 2. Fetch Orders
                val ordersResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMyOrders()
                }
                if (ordersResponse.isSuccessful && ordersResponse.body() != null) {
                    updateOrdersUI(ordersResponse.body()!!)
                }
                

            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error loading dashboard: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSubscriptionUI(sub: UserSubscriptionDTO) {
        binding.tvSubscriptionPlan.text = sub.planType
        
        // If Premium, make the card gold/amber like the web app
        if (sub.planType.uppercase() == "PREMIUM") {
            binding.cardSubscription.setCardBackgroundColor(Color.parseColor("#F59E0B")) // Amber 500
            binding.tvSubscriptionPlan.setTextColor(Color.WHITE)
            // Update the label color too if we can access it, or just leave it
        }
    }

    private fun updateOrdersUI(orders: List<OrderDTO>) {
        // Calculate Active Orders
        val activeCount = orders.filter { 
            it.status.uppercase() !in listOf("DELIVERED", "CANCELLED") 
        }.size
        binding.tvActiveOrdersCount.text = activeCount.toString()

        // Calculate Total Spent (completed orders)
        val totalSpent = orders
            .filter { it.status.uppercase() !in listOf("CANCELLED", "PENDING") }
            .sumOf { it.totalAmount }
        binding.tvTotalSpent.text = "₱${String.format("%.2f", totalSpent)}"

        // Display Recent Orders (Top 3)
        val recentOrders = orders
            .sortedByDescending { it.createdAt }
            .take(3)

        if (recentOrders.isNotEmpty()) {
            binding.tvNoOrders.visibility = View.GONE
            binding.llRecentOrders.removeAllViews() // Clear placeholder

            for (order in recentOrders) {
                // Inflate a simple list item programmatically or use a standard layout
                val itemView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
                val text1 = itemView.findViewById<TextView>(android.R.id.text1)
                val text2 = itemView.findViewById<TextView>(android.R.id.text2)

                text1.text = "Order ${order.orderNumber} - ₱${order.totalAmount}"
                text1.setTextColor(Color.parseColor("#0F172A"))
                text1.textSize = 16f
                text1.setTypeface(null, android.graphics.Typeface.BOLD)
                
                text2.text = "Status: ${order.status.replace("_", " ")}"
                text2.setTextColor(Color.parseColor("#64748B"))
                text2.textSize = 14f
                text2.setPadding(0, 4, 0, 12) // Add some space between items

                binding.llRecentOrders.addView(itemView)
            }
        }
    }
}
