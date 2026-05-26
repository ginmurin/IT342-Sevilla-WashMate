package com.example.washmate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.OrderRequest
import com.example.washmate.api.OrderServiceInput
import com.example.washmate.api.WashServiceDTO
import com.example.washmate.databinding.ActivityPaymentReviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentReviewBinding
    private var serviceIds: LongArray = longArrayOf()
    private var quantities: DoubleArray = doubleArrayOf()
    private var variantIds: LongArray = longArrayOf()
    private var specialInstructions: String = ""
    private var totalAmount: Double = 0.0
    private var pickupDate: String = ""
    private var pickupTime: String = ""
    private var deliveryDate: String = ""
    private var deliveryTime: String = ""
    private var address: String = ""
    private var phone: String = ""
    private var currentOrderId: Long? = null
    
    private val selectedServicesList = mutableListOf<WashServiceDTO>()
    private var selectedPaymentMethod = "GCASH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve extras
        serviceIds = intent.getLongArrayExtra("SERVICE_IDS") ?: longArrayOf()
        quantities = intent.getDoubleArrayExtra("QUANTITIES") ?: doubleArrayOf()
        variantIds = intent.getLongArrayExtra("VARIANT_IDS") ?: longArrayOf()
        specialInstructions = intent.getStringExtra("SPECIAL_INSTRUCTIONS") ?: ""
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        pickupDate = intent.getStringExtra("PICKUP_DATE") ?: ""
        pickupTime = intent.getStringExtra("PICKUP_TIME") ?: ""
        deliveryDate = intent.getStringExtra("DELIVERY_DATE") ?: ""
        deliveryTime = intent.getStringExtra("DELIVERY_TIME") ?: ""

        // Setup Payment Methods
        binding.cardGCash.setOnClickListener { selectPaymentMethod("GCASH") }
        binding.cardMaya.setOnClickListener { selectPaymentMethod("PAYMAYA") }
        binding.cardCard.setOnClickListener { selectPaymentMethod("CARD") }
        binding.cardGrabPay.setOnClickListener { selectPaymentMethod("GRAB_PAY") }
        binding.cardWallet.setOnClickListener { selectPaymentMethod("WALLET") }
        address = intent.getStringExtra("ADDRESS") ?: ""
        phone = intent.getStringExtra("PHONE") ?: ""

        // Display Info
        binding.tvPickupInfo.text = "Pickup: $pickupDate at $pickupTime"
        binding.tvDeliveryInfo.text = "Delivery: $deliveryDate at $deliveryTime"
        binding.tvAddressInfo.text = "Address: $address\nPhone: $phone"
        binding.tvTotalAmount.text = "₱${String.format("%.2f", totalAmount)}"

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirmOrder.setOnClickListener {
            submitOrder()
        }

        fetchAndDisplayItems()
    }

    private fun fetchAndDisplayItems() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getServices()
                }

                if (response.isSuccessful && response.body() != null) {
                    val allServices = response.body()!!
                    binding.llOrderItems.removeAllViews()

                    for (i in serviceIds.indices) {
                        val id = serviceIds[i]
                        val qty = quantities[i]
                        val service = allServices.find { it.serviceId == id }

                        if (service != null) {
                            selectedServicesList.add(service)
                            
                            val variantId = if (i < variantIds.size) variantIds[i] else 0L
                            val variant = service.variants?.find { it.variantId == variantId }
                            
                            val price = variant?.variantPrice ?: service.basePricePerUnit
                            val name = if (variant != null) "${service.serviceName} (${variant.variantName})" else service.serviceName
                            
                            val tv = TextView(this@PaymentReviewActivity).apply {
                                text = "$name x $qty ${service.unitType.lowercase()} - ₱${String.format("%.2f", qty * price)}"
                                textSize = 14f
                                setTextColor(Color.parseColor("#64748B"))
                                setPadding(0, 0, 0, 8)
                            }
                            binding.llOrderItems.addView(tv)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@PaymentReviewActivity, "Error loading summary: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitOrder() {
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to the Terms of Service and Privacy Policy", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceInputs = mutableListOf<OrderServiceInput>()
        var totalWeight = 0.0

        for (i in serviceIds.indices) {
            val id = serviceIds[i]
            val qty = quantities[i]
            val variantId = if (i < variantIds.size) variantIds[i] else 0L
            
            serviceInputs.add(OrderServiceInput(
                serviceId = id, 
                quantity = qty, 
                selectedVariantId = if (variantId > 0) variantId else null
            ))
            
            val service = selectedServicesList.find { it.serviceId == id }
            if (service?.unitType?.lowercase() == "kg") {
                totalWeight += qty
            }
        }

        val paymentMethod = selectedPaymentMethod

        val request = OrderRequest(
            services = serviceInputs,
            totalWeight = totalWeight,
            specialInstructions = specialInstructions,
            pickupAddressString = "$address (Ph: $phone)",
            deliveryAddressString = "$address (Ph: $phone)",
            pickupSchedule = formatSchedule(pickupDate, pickupTime).takeIf { it.isNotEmpty() },
            deliverySchedule = formatSchedule(deliveryDate, deliveryTime).takeIf { it.isNotEmpty() }
        )

        lifecycleScope.launch {
            try {
                binding.btnConfirmOrder.isEnabled = false
                
                val orderIdToPay: Long
                
                if (currentOrderId != null) {
                    orderIdToPay = currentOrderId!!
                } else {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.createOrder(request)
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val order = response.body()!!
                        currentOrderId = order.orderId
                        orderIdToPay = currentOrderId!!
                    } else {
                        Toast.makeText(this@PaymentReviewActivity, "Failed to place order: ${response.message()}", Toast.LENGTH_SHORT).show()
                        binding.btnConfirmOrder.isEnabled = true
                        return@launch
                    }
                }

                // Call processOrderPayment with selected method
                val paymentRequest = mapOf("paymentMethod" to selectedPaymentMethod)
                val paymentResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.processOrderPayment(orderIdToPay, paymentRequest)
                }

                    if (paymentResponse.isSuccessful && paymentResponse.body() != null) {
                        val paymentData = paymentResponse.body()!!
                        val checkoutUrl = paymentData["checkoutUrl"] as? String

                        if (checkoutUrl != null) {
                            Toast.makeText(this@PaymentReviewActivity, "Redirecting to PayMongo...", Toast.LENGTH_SHORT).show()
                            
                            // Open checkout URL in WebView
                            val intent = Intent(this@PaymentReviewActivity, PaymentWebViewActivity::class.java)
                            intent.putExtra("URL", checkoutUrl)
                            intent.putExtra("PAYMENT_METHOD", selectedPaymentMethod)
                            startActivity(intent)
                            // DO NOT finish() here so the user can return to this screen if they cancel payment
                            binding.btnConfirmOrder.isEnabled = true
                        } else {
                            // If no checkoutUrl (maybe it's wallet payment or failed), just go to success screen
                            Toast.makeText(this@PaymentReviewActivity, "Order placed successfully!", Toast.LENGTH_LONG).show()
                            val successIntent = Intent(this@PaymentReviewActivity, OrderSuccessActivity::class.java)
                            successIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(successIntent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@PaymentReviewActivity, "Payment failed to process, please try another method.", Toast.LENGTH_SHORT).show()
                        binding.btnConfirmOrder.isEnabled = true
                    }
            } catch (e: Exception) {
                Toast.makeText(this@PaymentReviewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnConfirmOrder.isEnabled = true
            }
        }
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method
        
        val dp2 = (2 * resources.displayMetrics.density).toInt()
        
        // Reset all
        binding.cardGCash.setStrokeWidth(0)
        binding.cardGCash.setCardBackgroundColor(android.graphics.Color.WHITE)
        binding.cardMaya.setStrokeWidth(0)
        binding.cardMaya.setCardBackgroundColor(android.graphics.Color.WHITE)
        binding.cardCard.setStrokeWidth(0)
        binding.cardCard.setCardBackgroundColor(android.graphics.Color.WHITE)
        binding.cardGrabPay.setStrokeWidth(0)
        binding.cardGrabPay.setCardBackgroundColor(android.graphics.Color.WHITE)
        binding.cardWallet.setStrokeWidth(0)
        binding.cardWallet.setCardBackgroundColor(android.graphics.Color.WHITE)
        
        // Select one
        when (method) {
            "GCASH" -> {
                binding.cardGCash.setStrokeWidth(dp2)
                binding.cardGCash.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FDFA"))
            }
            "PAYMAYA" -> {
                binding.cardMaya.setStrokeWidth(dp2)
                binding.cardMaya.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FDFA"))
            }
            "CARD" -> {
                binding.cardCard.setStrokeWidth(dp2)
                binding.cardCard.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FDFA"))
            }
            "GRAB_PAY" -> {
                binding.cardGrabPay.setStrokeWidth(dp2)
                binding.cardGrabPay.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FDFA"))
            }
            "WALLET" -> {
                binding.cardWallet.setStrokeWidth(dp2)
                binding.cardWallet.setCardBackgroundColor(android.graphics.Color.parseColor("#F0FDFA"))
            }
        }
    }

    private fun formatSchedule(date: String, timeSlot: String): String {
        if (date.isEmpty() || timeSlot.isEmpty()) return ""
        val startTime = timeSlot.split("–")[0].trim()
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.US)
            val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val parsed = inputFormat.parse("$date $startTime")
            outputFormat.format(parsed!!)
        } catch (e: Exception) {
            ""
        }
    }
}
