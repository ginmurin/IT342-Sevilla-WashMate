package com.example.washmate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.washmate.databinding.ActivityRoleSelectBinding

class RoleSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectBinding
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoleSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra("user_role") ?: "SHOPOWNER"

        // Admin Panel button - only show if ADMIN
        binding.btnAdminPanel.setOnClickListener {
            if (userRole == "ADMIN") {
                navigateToDashboard("/admin")
            }
        }

        // Shop Owner button - show for ADMIN or SHOPOWNER
        binding.btnShopOwner.setOnClickListener {
            if (userRole == "ADMIN" || userRole == "SHOPOWNER") {
                navigateToDashboard("/shop")
            }
        }

        // Customer button - always available
        binding.btnCustomer.setOnClickListener {
            navigateToDashboard("/customer")
        }

        // Back button
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Hide buttons based on role
        if (userRole != "ADMIN") {
            binding.btnAdminPanel.visibility = android.view.View.GONE
        }
    }

    private fun navigateToDashboard(role: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("selected_role", role)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
