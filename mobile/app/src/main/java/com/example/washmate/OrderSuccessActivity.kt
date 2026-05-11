package com.example.washmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OrderSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_success)

        val isWallet = intent.getBooleanExtra("IS_WALLET", false)
        if (isWallet) {
            val tvSuccessTitle = findViewById<android.widget.TextView>(R.id.tvSuccessTitle)
            val tvSuccessSubtitle = findViewById<android.widget.TextView>(R.id.tvSuccessSubtitle)
            tvSuccessTitle.text = "Wallet Recharged Successfully!"
            tvSuccessSubtitle.text = "Your wallet balance has been updated."
        }

        val btnGoHome = findViewById<Button>(R.id.btnGoHome)
        btnGoHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
