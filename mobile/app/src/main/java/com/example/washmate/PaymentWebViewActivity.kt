package com.example.washmate

import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentWebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_web_view)

        val url = intent.getStringExtra("URL") ?: ""
        val webView = findViewById<WebView>(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val currentUrl = request?.url.toString()
                android.util.Log.d("PaymentWebView", "shouldOverrideUrlLoading: $currentUrl")
                return handleUrlDirectly(currentUrl)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                android.util.Log.d("PaymentWebView", "onPageStarted: $url")
                if (url != null) {
                    handleUrlDirectly(url)
                }
            }
        }

        webView.loadUrl(url)
    }

    private fun handleUrlDirectly(currentUrl: String): Boolean {
        if (currentUrl.contains("payment/success")) {
            val uri = android.net.Uri.parse(currentUrl)
            val orderId = uri.getQueryParameter("orderId")?.toLongOrNull()
            val paymentId = uri.getQueryParameter("paymentId") ?: ""
            
            if (orderId != null && paymentId.isNotEmpty()) {
                Toast.makeText(this, "Confirming payment...", Toast.LENGTH_SHORT).show()
                
                lifecycleScope.launch {
                    try {
                        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "GCASH"
                        val response = withContext(Dispatchers.IO) {
                            com.example.washmate.api.RetrofitClient.instance.confirmOrderPayment(orderId, paymentId, paymentMethod)
                        }
                        
                        if (response.isSuccessful) {
                            Toast.makeText(this@PaymentWebViewActivity, "Payment Confirmed!", Toast.LENGTH_LONG).show()
                            
                            val intent = Intent(this@PaymentWebViewActivity, OrderSuccessActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PaymentWebViewActivity, "Failed to confirm payment", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this@PaymentWebViewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            return true
        } else if (currentUrl.contains("wallet/payment-success")) {
            val uri = android.net.Uri.parse(currentUrl)
            val paymentId = uri.getQueryParameter("paymentId") ?: ""
            
            if (paymentId.isNotEmpty()) {
                Toast.makeText(this, "Confirming top-up...", Toast.LENGTH_SHORT).show()
                
                lifecycleScope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            com.example.washmate.api.RetrofitClient.instance.confirmWalletTopup(paymentId, emptyMap())
                        }
                        
                        if (response.isSuccessful) {
                            Toast.makeText(this@PaymentWebViewActivity, "Top-up Successful!", Toast.LENGTH_LONG).show()
                            
                            val intent = Intent(this@PaymentWebViewActivity, OrderSuccessActivity::class.java)
                            intent.putExtra("IS_WALLET", true)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PaymentWebViewActivity, "Failed to confirm top-up", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this@PaymentWebViewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            return true
        } else if (currentUrl.contains("subscription/upgrade-success")) {
            val uri = android.net.Uri.parse(currentUrl)
            val userSubscriptionId = uri.getQueryParameter("userSubscriptionId")?.toLongOrNull()
            val paymentId = uri.getQueryParameter("paymentId") ?: ""
            
            if (userSubscriptionId != null && paymentId.isNotEmpty()) {
                Toast.makeText(this, "Confirming subscription upgrade...", Toast.LENGTH_SHORT).show()
                
                lifecycleScope.launch {
                    try {
                        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "GCASH"
                        val response = withContext(Dispatchers.IO) {
                            com.example.washmate.api.RetrofitClient.instance.confirmSubscriptionUpgrade(userSubscriptionId, paymentId, paymentMethod)
                        }
                        
                        if (response.isSuccessful) {
                            Toast.makeText(this@PaymentWebViewActivity, "Subscription Upgraded Successfully!", Toast.LENGTH_LONG).show()
                            
                            val intent = Intent(this@PaymentWebViewActivity, DashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PaymentWebViewActivity, "Failed to confirm upgrade", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this@PaymentWebViewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            return true
        } else if (currentUrl.contains("payment/error") || currentUrl.contains("wallet/payment-error") || currentUrl.contains("subscription/upgrade-error")) {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
            finish()
            return true
        }
        return false
    }
}
