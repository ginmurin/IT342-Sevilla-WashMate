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
import com.example.washmate.api.FeedbackRequest
import com.example.washmate.api.NotificationDTO
import com.example.washmate.api.RetrofitClient
import com.example.washmate.databinding.ActivityNotificationsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnMarkAllRead.setOnClickListener {
            markAllNotificationsAsRead()
        }

        fetchNotifications()
    }

    private fun fetchNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.scrollViewNotifications.visibility = View.GONE
                }

                val response = RetrofitClient.instance.getNotifications()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val list = response.body() ?: emptyList()
                        if (list.isEmpty()) {
                            binding.llEmptyState.visibility = View.VISIBLE
                            binding.scrollViewNotifications.visibility = View.GONE
                        } else {
                            binding.llEmptyState.visibility = View.GONE
                            binding.scrollViewNotifications.visibility = View.VISIBLE
                            populateNotifications(list)
                        }
                    } else {
                        Toast.makeText(this@NotificationsActivity, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@NotificationsActivity, "Error: $", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateNotifications(list: List<NotificationDTO>) {
        binding.llNotificationsContainer.removeAllViews()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val sdfOut = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

        for (notif in list) {
            val item = layoutInflater.inflate(R.layout.item_notification, binding.llNotificationsContainer, false)
            
            val tvTitle = item.findViewById<TextView>(R.id.tvNotificationTitle)
            val tvMsg = item.findViewById<TextView>(R.id.tvNotificationMessage)
            val tvTime = item.findViewById<TextView>(R.id.tvNotificationTime)
            val indicator = item.findViewById<View>(R.id.vIndicator)

            tvTitle.text = notif.title
            tvMsg.text = notif.message

            try {
                val date = sdfIn.parse(notif.createdAt)
                tvTime.text = date?.let { sdfOut.format(it) } ?: notif.createdAt
            } catch (e: Exception) {
                tvTime.text = notif.createdAt
            }

            val typeColor = when (notif.notificationType.uppercase()) {
                "ORDER_UPDATE" -> Color.parseColor("#3B82F6") // Blue
                "PAYMENT" -> Color.parseColor("#10B981") // Green
                "PROMOTION" -> Color.parseColor("#F59E0B") // Amber
                "FEEDBACK_REQUEST" -> Color.parseColor("#F59E0B") // Amber
                else -> Color.parseColor("#64748B") // Slate
            }
            indicator.background.setTint(typeColor)

            if (!notif.isRead) {
                indicator.visibility = View.VISIBLE
                item.setBackgroundColor(Color.parseColor("#EFF6FF"))
            } else {
                indicator.visibility = View.GONE
                item.setBackgroundColor(Color.TRANSPARENT)
            }

            item.setOnClickListener {
                if (!notif.isRead) {
                    markSingleAsRead(notif.notificationId)
                }
                if (notif.notificationType == "FEEDBACK_REQUEST" && notif.referenceId != null) {
                    showFeedbackDialog(notif.referenceId)
                }
            }

            binding.llNotificationsContainer.addView(item)
        }
    }

    private fun showFeedbackDialog(orderId: Long) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComments = dialogView.findViewById<EditText>(R.id.etComments)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating.toInt()
                if (rating == 0) {
                    Toast.makeText(this, "Please provide a star rating", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                submitFeedback(orderId, rating, etComments.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitFeedback(orderId: Long, rating: Int, comments: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = FeedbackRequest(
                    orderId = orderId,
                    starRating = rating,
                    commentText = comments
                )
                val response = RetrofitClient.instance.submitOrderFeedback(orderId, request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@NotificationsActivity, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@NotificationsActivity, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationsActivity, "Error: $", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun markSingleAsRead(notificationId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.markNotificationAsRead(notificationId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        fetchNotifications()
                    } else {
                        Toast.makeText(this@NotificationsActivity, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationsActivity, "Error: $", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun markAllNotificationsAsRead() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.markAllNotificationsAsRead()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        fetchNotifications()
                    } else {
                        Toast.makeText(this@NotificationsActivity, "Failed to mark all as read", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NotificationsActivity, "Error: $", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
