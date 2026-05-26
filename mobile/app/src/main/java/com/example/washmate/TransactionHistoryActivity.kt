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
import com.example.washmate.databinding.ActivityTransactionHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        loadTransactions()
    }

    private fun loadTransactions() {
        binding.pbLoader.visibility = View.VISIBLE
        binding.llEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getWalletTransactions()
                }

                binding.pbLoader.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val transactions = response.body()!!
                    if (transactions.isEmpty()) {
                        binding.llEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvTransactions.adapter = TransactionAdapter(transactions)
                    }
                } else {
                    Toast.makeText(this@TransactionHistoryActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                    binding.llEmptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.pbLoader.visibility = View.GONE
                Toast.makeText(this@TransactionHistoryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.llEmptyState.visibility = View.VISIBLE
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
                holder.tvAmount.setTextColor(Color.parseColor("#10B981"))
                holder.vIndicator.setBackgroundColor(Color.parseColor("#10B981"))
            } else {
                holder.tvAmount.text = "- ₱${String.format("%.2f", tx.amount)}"
                holder.tvAmount.setTextColor(Color.parseColor("#EF4444"))
                holder.vIndicator.setBackgroundColor(Color.parseColor("#EF4444"))
            }

            holder.tvStatus.text = tx.status.uppercase()
            when (tx.status.uppercase()) {
                "COMPLETED" -> {
                    holder.tvStatus.setTextColor(Color.parseColor("#0F766E"))
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#CCFBF1"))
                }
                "PENDING" -> {
                    holder.tvStatus.setTextColor(Color.parseColor("#B45309"))
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#FEF3C7"))
                }
                else -> {
                    holder.tvStatus.setTextColor(Color.parseColor("#B91C1C"))
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#FEE2E2"))
                }
            }
        }

        override fun getItemCount() = transactions.size
    }
}
