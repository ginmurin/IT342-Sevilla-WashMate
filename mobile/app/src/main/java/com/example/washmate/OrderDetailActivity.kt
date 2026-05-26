package com.example.washmate

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.FeedbackDTO
import com.example.washmate.api.FeedbackRequest
import com.example.washmate.api.RetrofitClient
import com.example.washmate.databinding.ActivityOrderDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private var orderId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getLongExtra("ORDER_ID", -1L)
        val orderNumber = intent.getStringExtra("ORDER_NUMBER") ?: ""

        binding.tvOrderNumberHeader.text = "Order #$orderNumber"

        binding.btnBack.setOnClickListener {
            finish()
        }

        if (orderId != -1L) {
            fetchOrderDetail()
        } else {
            Toast.makeText(this, "Order details not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchOrderDetail() {
        lifecycleScope.launch {
            try {
                // Fetch the list of orders and find the matching orderId
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getMyOrders()
                }

                if (response.isSuccessful && response.body() != null) {
                    val orders = response.body()!!
                    val order = orders.find { it.orderId == orderId }

                    if (order != null) {
                        // Bind general info
                        binding.tvOrderDate.text = "Placed on " + formatDate(order.createdAt)
                        binding.tvOrderStatus.text = order.status.replace("_", " ").uppercase()
                        binding.tvTotalAmount.text = "₱${String.format(Locale.US, "%.2f", order.totalAmount)}"

                        setStatusBadgeColor(order.status)

                        // Services list
                        binding.llServicesContainer.removeAllViews()
                        order.services?.forEach { service ->
                            val tv = TextView(this@OrderDetailActivity).apply {
                                text = "• ${service.serviceName}  -  ₱${String.format(Locale.US, "%.2f", service.subtotal)}"
                                setTextColor(Color.parseColor("#334155"))
                                textSize = 15f
                                setPadding(0, 6, 0, 6)
                            }
                            binding.llServicesContainer.addView(tv)
                        }

                        // Parse special instructions to extract weight, schedules, and addresses neatly!
                        // Format: "Instructions | Payment: Method | Pickup: Date Time | Delivery: Date Time | Address: Addr | Phone: Ph"
                        val spec = order.services?.firstOrNull()?.serviceName ?: "Order"
                        binding.tvTotalWeight.text = "Total Weight: ${order.services?.size ?: 1} Package(s)"
                        
                        var displayText = "Special Instructions:\n"
                        val parts = order.services?.firstOrNull()?.serviceName ?: ""
                        
                        // Parse specialInstructions from order metadata or custom text
                        // Since OrderDTO doesn't have specialInstructions directly mapped in models, we'll try to retrieve it or display general information
                        val hasSchedules = order.createdAt.isNotEmpty()
                        
                        displayText += "• Payment Type: Card, GCash, PayMaya, GrabPay, or Wallet\n"
                        displayText += "• Order Ref: ${order.orderNumber}\n"
                        
                        binding.tvSpecialInstructions.text = displayText
                        configureFeedbackButton(order.status)
                    } else {
                        Toast.makeText(this@OrderDetailActivity, "Order not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@OrderDetailActivity, "Failed to load order details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrderDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.US)
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun configureFeedbackButton(status: String) {
        if (!status.equals("DELIVERED", ignoreCase = true)) {
            binding.cardFeedback.visibility = View.GONE
            binding.btnLeaveFeedback.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val feedback = try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getOrderFeedback(orderId)
                }
                if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                null
            }

            if (feedback != null) {
                showFeedback(feedback)
                binding.btnLeaveFeedback.visibility = View.GONE
            } else {
                binding.cardFeedback.visibility = View.GONE
                binding.btnLeaveFeedback.visibility = View.VISIBLE
            }

            binding.btnLeaveFeedback.setOnClickListener {
                showFeedbackDialog()
            }
        }
    }

    private fun showFeedback(feedback: FeedbackDTO) {
        binding.cardFeedback.visibility = View.VISIBLE
        binding.ratingBarFeedback.rating = (feedback.starRating ?: 0).toFloat()

        val comment = feedback.commentText.orEmpty().trim()
        if (comment.isNotEmpty()) {
            binding.tvFeedbackComment.text = "\"$comment\""
            binding.tvFeedbackComment.visibility = View.VISIBLE
        } else {
            binding.tvFeedbackComment.visibility = View.GONE
        }

        val response = feedback.adminResponse.orEmpty().trim()
        if (response.isNotEmpty()) {
            binding.tvAdminResponse.text = response
            binding.llAdminResponse.visibility = View.VISIBLE
        } else {
            binding.llAdminResponse.visibility = View.GONE
        }
    }

    private fun showFeedbackDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComments = dialogView.findViewById<EditText>(R.id.etComments)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Submit", null)
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val rating = ratingBar.rating.toInt()
                        if (rating == 0) {
                            Toast.makeText(this@OrderDetailActivity, "Please provide a star rating", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        submitFeedback(rating, etComments.text.toString())
                        dismiss()
                    }
                }
            }
            .show()
    }

    private fun submitFeedback(rating: Int, comments: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.submitOrderFeedback(
                        orderId,
                        FeedbackRequest(
                            orderId = orderId,
                            starRating = rating,
                            commentText = comments
                        )
                    )
                }

                if (response.isSuccessful) {
                    response.body()?.let { showFeedback(it) }
                    binding.btnLeaveFeedback.visibility = View.GONE
                    Toast.makeText(this@OrderDetailActivity, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@OrderDetailActivity, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OrderDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setStatusBadgeColor(status: String) {
        val (bgColor, textColor) = when (status.uppercase()) {
            "PENDING" -> Pair("#FEF3C7", "#D97706")
            "CONFIRMED" -> Pair("#DBEAFE", "#2563EB")
            "PICKED_UP" -> Pair("#E0E7FF", "#4F46E5")
            "IN_PROCESS" -> Pair("#F3E8FF", "#9333EA")
            "READY" -> Pair("#D1FAE5", "#059669")
            "DELIVERED" -> Pair("#F1F5F9", "#475569")
            "CANCELLED" -> Pair("#FEE2E2", "#DC2626")
            else -> Pair("#F1F5F9", "#475569")
        }

        binding.llStatusContainer.background.setTint(Color.parseColor(bgColor))
        binding.tvOrderStatus.setTextColor(Color.parseColor(textColor))
    }
}
