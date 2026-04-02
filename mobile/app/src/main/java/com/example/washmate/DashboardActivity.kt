package com.example.washmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.washmate.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve saved user info (just an example of getting the email/name)
        val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("USER_EMAIL", "User")
        
        binding.tvWelcomeMessage.text = "Welcome back,\n$email!"

        binding.btnLogout.setOnClickListener {
            // Clear preferences and go back to Login
            sharedPref.edit().clear().apply()
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
