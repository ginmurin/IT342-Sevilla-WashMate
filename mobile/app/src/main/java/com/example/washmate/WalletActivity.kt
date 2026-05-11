package com.example.washmate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.databinding.ActivityWalletBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding
    private var selectedPaymentMethod = "GCASH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_wallet
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_orders -> {
                    Toast.makeText(this, "Orders clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_wallet -> true
                else -> false
            }
        }

        // Fetch wallet data
        fetchWalletData()

        // Handle Payment Method Selection
        binding.cardGcash.setOnClickListener {
            selectedPaymentMethod = "GCASH"
            updatePaymentMethodUI()
        }
        binding.cardMaya.setOnClickListener {
            selectedPaymentMethod = "MAYA"
            updatePaymentMethodUI()
        }
        binding.cardCard.setOnClickListener {
            selectedPaymentMethod = "CARD"
            updatePaymentMethodUI()
        }

        // Handle Quick Load
        binding.btnLoad500.setOnClickListener { initiateRecharge(500.0) }
        binding.btnLoad1000.setOnClickListener { initiateRecharge(1000.0) }
        binding.btnLoad2000.setOnClickListener { initiateRecharge(2000.0) }
        binding.btnLoad5000.setOnClickListener { initiateRecharge(5000.0) }

        // Handle Custom Amount
        binding.btnLoadMoney.setOnClickListener {
            val amountStr = binding.etCustomAmount.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    initiateRecharge(amount)
                } else {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchWalletData() {
        lifecycleScope.launch {
            try {
                // Fetch Balance
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getWalletBalance()
                }
                if (response.isSuccessful && response.body() != null) {
                    val wallet = response.body()!!
                    binding.tvWalletBalance.text = "₱${String.format("%.2f", wallet.availableBalance)}"
                }
                
                // Note: Transactions list requires a custom adapter. 
                // We will leave it empty for now or add dummy data if needed.
            } catch (e: Exception) {
                Toast.makeText(this@WalletActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initiateRecharge(amount: Double) {
        lifecycleScope.launch {
            try {
                val request = mapOf(
                    "amount" to amount,
                    "paymentMethod" to selectedPaymentMethod
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.processWalletTopup(request)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val checkoutUrl = response.body()!!["checkoutUrl"] as? String
                    if (checkoutUrl != null) {
                        val intent = Intent(this@WalletActivity, PaymentWebViewActivity::class.java)
                        intent.putExtra("URL", checkoutUrl)
                        intent.putExtra("PAYMENT_METHOD", selectedPaymentMethod)
                        startActivity(intent)
                    } else if (selectedPaymentMethod == "CARD") {
                        // Simulate card processing just like the web app!
                        Toast.makeText(this@WalletActivity, "Processing Card Payment...", Toast.LENGTH_SHORT).show()
                        
                        lifecycleScope.launch {
                            kotlinx.coroutines.delay(2000) // Wait 2 seconds
                            val intent = Intent(this@WalletActivity, OrderSuccessActivity::class.java)
                            intent.putExtra("IS_WALLET", true)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@WalletActivity, "Failed to get checkout URL", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@WalletActivity, "Failed to process recharge", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WalletActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePaymentMethodUI() {
        // Reset all
        binding.cardGcash.strokeWidth = 1 * resources.displayMetrics.density.toInt()
        binding.cardGcash.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2E8F0")))
        binding.tvGcash.setTextColor(android.graphics.Color.parseColor("#475569"))

        binding.cardMaya.strokeWidth = 1 * resources.displayMetrics.density.toInt()
        binding.cardMaya.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2E8F0")))
        binding.tvMaya.setTextColor(android.graphics.Color.parseColor("#475569"))

        binding.cardCard.strokeWidth = 1 * resources.displayMetrics.density.toInt()
        binding.cardCard.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2E8F0")))
        binding.tvCard.setTextColor(android.graphics.Color.parseColor("#475569"))

        // Set selected
        when (selectedPaymentMethod) {
            "GCASH" -> {
                binding.cardGcash.strokeWidth = 2 * resources.displayMetrics.density.toInt()
                binding.cardGcash.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0D9488")))
                binding.tvGcash.setTextColor(android.graphics.Color.parseColor("#0D9488"))
            }
            "MAYA" -> {
                binding.cardMaya.strokeWidth = 2 * resources.displayMetrics.density.toInt()
                binding.cardMaya.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0D9488")))
                binding.tvMaya.setTextColor(android.graphics.Color.parseColor("#0D9488"))
            }
            "CARD" -> {
                binding.cardCard.strokeWidth = 2 * resources.displayMetrics.density.toInt()
                binding.cardCard.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0D9488")))
                binding.tvCard.setTextColor(android.graphics.Color.parseColor("#0D9488"))
            }
        }
    }
}
