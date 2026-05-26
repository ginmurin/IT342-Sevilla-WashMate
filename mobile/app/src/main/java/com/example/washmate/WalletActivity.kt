package com.example.washmate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.WalletTransactionDTO
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
                    val intent = Intent(this, OrdersActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_wallet -> true
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
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
            selectedPaymentMethod = "PAYMAYA"
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
        // Set layout manager
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)

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

                // Fetch Transactions
                val txResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getWalletTransactions()
                }
                if (txResponse.isSuccessful && txResponse.body() != null) {
                    val transactions = txResponse.body()!!
                    binding.rvTransactions.adapter = TransactionAdapter(transactions)
                } else {
                    Toast.makeText(this@WalletActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WalletActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTimestamp(isoString: String): String {
        return try {
            val parts = isoString.split("T")
            if (parts.size == 2) {
                val date = parts[0]
                val time = parts[1].substringBefore(".")
                "$date $time"
            } else {
                isoString
            }
        } catch (e: Exception) {
            isoString
        }
    }

    private inner class TransactionAdapter(private val transactions: List<WalletTransactionDTO>) :
        RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

        inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val vIndicator: View = view.findViewById(R.id.vIndicator)
            val tvDescription: TextView = view.findViewById(R.id.tvDescription)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val tvAmount: TextView = view.findViewById(R.id.tvAmount)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
            return TransactionViewHolder(view)
        }

        override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
            val tx = transactions[position]
            holder.tvDescription.text = tx.description ?: "Transaction"
            holder.tvDate.text = formatTimestamp(tx.createdAt)
            
            val isCredit = tx.transactionType.uppercase() == "CREDIT"
            if (isCredit) {
                holder.tvAmount.text = "+ ₱${String.format("%.2f", tx.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#10B981")) // green-500
                holder.vIndicator.setBackgroundColor(Color.parseColor("#10B981"))
            } else {
                holder.tvAmount.text = "- ₱${String.format("%.2f", tx.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#EF4444")) // red-500
                holder.vIndicator.setBackgroundColor(Color.parseColor("#EF4444"))
            }

            holder.tvStatus.text = tx.status.uppercase()
            when (tx.status.uppercase()) {
                "COMPLETED" -> {
                    holder.tvStatus.setTextColor(Color.parseColor("#0F766E")) // teal-700
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#CCFBF1")) // teal-100
                }
                "PENDING" -> {
                    holder.tvStatus.setTextColor(Color.parseColor("#B45309")) // amber-700
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7")) // amber-100
                }
                else -> { // FAILED
                    holder.tvStatus.setTextColor(Color.parseColor("#B91C1C")) // red-700
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#FEE2E2")) // red-100
                }
            }
        }

        override fun getItemCount() = transactions.size
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
            "PAYMAYA" -> {
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
