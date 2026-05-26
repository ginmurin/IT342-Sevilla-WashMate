package com.example.washmate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.databinding.ActivitySubscriptionsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionsBinding
    private var selectedPaymentMethod = "GCASH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.cardGcash.setOnClickListener {
            selectedPaymentMethod = "GCASH"
            updatePaymentMethodUI()
        }

        binding.cardMaya.setOnClickListener {
            selectedPaymentMethod = "PAYMAYA"
            updatePaymentMethodUI()
        }

        binding.cardWallet.setOnClickListener {
            selectedPaymentMethod = "WALLET"
            updatePaymentMethodUI()
        }

        binding.btnUpgrade.setOnClickListener {
            processUpgrade()
        }

        updatePaymentMethodUI()
        fetchSubscriptionData()
    }

    private fun fetchSubscriptionData() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMySubscription()
                }

                if (response.isSuccessful && response.body() != null) {
                    val sub = response.body()!!
                    binding.tvCurrentPlan.text = sub.planType.uppercase()
                    
                    if (sub.planType.uppercase() == "PREMIUM") {
                        binding.btnUpgrade.text = "Already Premium"
                        binding.btnUpgrade.isEnabled = false
                        binding.btnUpgrade.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#94A3B8"))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SubscriptionsActivity", "Error: ${e.message}")
            }
        }
    }

    private fun processUpgrade() {
        lifecycleScope.launch {
            try {
                binding.btnUpgrade.isEnabled = false
                binding.btnUpgrade.text = "Processing..."

                val request = mapOf("paymentMethod" to selectedPaymentMethod)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.processSubscriptionUpgrade("PREMIUM", request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    if (selectedPaymentMethod == "WALLET") {
                        // Confirm immediately
                        val userSubId = (body["userSubscriptionId"] as? Number)?.toLong() ?: -1L
                        val paymentId = body["paymentId"] as? String ?: ""
                        
                        if (userSubId != -1L && paymentId.isNotEmpty()) {
                            val confirmResponse = withContext(Dispatchers.IO) {
                                RetrofitClient.instance.confirmSubscriptionUpgrade(userSubId, paymentId, "WALLET")
                            }
                            
                            if (confirmResponse.isSuccessful) {
                                Toast.makeText(this@SubscriptionsActivity, "Upgraded to Premium via Wallet!", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@SubscriptionsActivity, DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@SubscriptionsActivity, "Upgrade confirmation failed", Toast.LENGTH_SHORT).show()
                                resetUpgradeButton()
                            }
                        } else {
                            Toast.makeText(this@SubscriptionsActivity, "Invalid upgrade response", Toast.LENGTH_SHORT).show()
                            resetUpgradeButton()
                        }
                    } else {
                        // GCASH or PAYMAYA
                        val checkoutUrl = body["checkoutUrl"] as? String
                        if (checkoutUrl != null) {
                            val intent = Intent(this@SubscriptionsActivity, PaymentWebViewActivity::class.java)
                            intent.putExtra("URL", checkoutUrl)
                            intent.putExtra("PAYMENT_METHOD", selectedPaymentMethod)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@SubscriptionsActivity, "Failed to get payment checkout URL", Toast.LENGTH_SHORT).show()
                            resetUpgradeButton()
                        }
                    }
                } else {
                    Toast.makeText(this@SubscriptionsActivity, "Upgrade processing failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    resetUpgradeButton()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SubscriptionsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                resetUpgradeButton()
            }
        }
    }

    private fun resetUpgradeButton() {
        binding.btnUpgrade.isEnabled = true
        binding.btnUpgrade.text = "Upgrade to Premium"
    }

    private fun updatePaymentMethodUI() {
        // Reset GCash
        binding.cardGcash.strokeWidth = 1 * resources.displayMetrics.density.toInt()
        binding.cardGcash.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E2E8F0")))

        // Reset Maya
        binding.cardMaya.strokeWidth = 1 * resources.displayMetrics.density.toInt()
        binding.cardMaya.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E2E8F0")))

        // Reset Wallet
        binding.cardWallet.strokeWidth = 1 * resources.displayMetrics.density.toInt()
        binding.cardWallet.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E2E8F0")))

        when (selectedPaymentMethod) {
            "GCASH" -> {
                binding.cardGcash.strokeWidth = 2 * resources.displayMetrics.density.toInt()
                binding.cardGcash.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")))
            }
            "PAYMAYA" -> {
                binding.cardMaya.strokeWidth = 2 * resources.displayMetrics.density.toInt()
                binding.cardMaya.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")))
            }
            "WALLET" -> {
                binding.cardWallet.strokeWidth = 2 * resources.displayMetrics.density.toInt()
                binding.cardWallet.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B")))
            }
        }
    }
}
