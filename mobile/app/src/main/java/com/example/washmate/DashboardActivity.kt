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
import androidx.core.view.GravityCompat
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
        var firstName = sharedPref.getString("USER_FIRST_NAME", "User")
        
        if (firstName == "User") {
            val email = sharedPref.getString("USER_EMAIL", "") ?: ""
            if (email.isNotEmpty()) {
                firstName = email.substringBefore("@")
                // Save it for next time
                sharedPref.edit().putString("USER_FIRST_NAME", firstName).apply()
            }
        }
        
        binding.tvWelcomeMessage.text = "Welcome back, $firstName!"

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

        binding.cardSubscription.setOnClickListener {
            val intent = Intent(this, SubscriptionsActivity::class.java)
            startActivity(intent)
        }

        binding.btnNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        // Drawer Menu Button Toggle (3 Bars)
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Side Navigation Drawer Item Clicks
        val headerView = binding.navigationView.getHeaderView(0)
        val tvDrawerUserEmail = headerView.findViewById<TextView>(R.id.tvDrawerUserEmail)
        val userEmail = sharedPref.getString("USER_EMAIL", "user@washmate.com")
        tvDrawerUserEmail.text = userEmail

        binding.navigationView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.menu_services -> {
                    val intent = Intent(this, ServicesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_subscription -> {
                    val intent = Intent(this, SubscriptionsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_transactions -> {
                    val intent = Intent(this, TransactionHistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_orders -> {
                    val intent = Intent(this, OrdersActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_wallet -> {
                    val intent = Intent(this, WalletActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
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
