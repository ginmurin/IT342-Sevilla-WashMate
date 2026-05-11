package com.example.washmate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.WashServiceDTO
import com.example.washmate.databinding.ActivityServicesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServicesActivity : AppCompatActivity() {

    private val selectedServices = mutableSetOf<Long>()
    private val selectedVariants = mutableMapOf<Long, Long>() // serviceId -> variantId
    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            val serviceIds = selectedServices.toLongArray()
            val variantIds = serviceIds.map { selectedVariants[it] ?: 0L }.toLongArray()

            val intent = Intent(this, PlaceOrderActivity::class.java).apply {
                putExtra("SELECTED_SERVICES", serviceIds)
                putExtra("SELECTED_VARIANTS", variantIds)
            }
            startActivity(intent)
        }

        // Fetch services from backend
        fetchServices()
    }

    private fun fetchServices() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getServices()
                }

                if (response.isSuccessful && response.body() != null) {
                    displayServices(response.body()!!)
                } else {
                    Toast.makeText(this@ServicesActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ServicesActivity, "Error loading services: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayServices(services: List<WashServiceDTO>) {
        binding.tvLoading.visibility = View.GONE
        binding.llServices.removeAllViews()

        if (services.isEmpty()) {
            val emptyTv = TextView(this)
            emptyTv.text = "No services available at the moment."
            emptyTv.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyTv.setPadding(0, 32, 0, 0)
            binding.llServices.addView(emptyTv)
            return
        }

        for (service in services) {
            val itemView = layoutInflater.inflate(R.layout.item_service, null)
            
            val tvServiceName = itemView.findViewById<TextView>(R.id.tvServiceName)
            val tvServicePrice = itemView.findViewById<TextView>(R.id.tvServicePrice)
            val tvServiceDescription = itemView.findViewById<TextView>(R.id.tvServiceDescription)
            val tvUnitType = itemView.findViewById<TextView>(R.id.tvUnitType)

            tvServiceName.text = service.serviceName
            
            if (service.hasVariants || service.basePricePerUnit == 0.0) {
                tvServicePrice.visibility = View.GONE
            } else {
                tvServicePrice.visibility = View.VISIBLE
                tvServicePrice.text = "₱${String.format("%.2f", service.basePricePerUnit)}"
            }
            
            tvServiceDescription.text = service.description ?: "No description available"
            tvUnitType.text = "Per ${service.unitType.lowercase()}"

            val rgVariants = itemView.findViewById<android.widget.RadioGroup>(R.id.rgVariants)
            if (service.hasVariants && !service.variants.isNullOrEmpty()) {
                rgVariants.visibility = View.VISIBLE
                rgVariants.removeAllViews()
                for (variant in service.variants) {
                    val rbVariant = android.widget.RadioButton(this).apply {
                        text = "${variant.variantName} - ₱${String.format("%.2f", variant.variantPrice)}"
                        textSize = 14f
                        setTextColor(Color.parseColor("#64748B"))
                        setPadding(8, 4, 0, 4)
                        id = variant.variantId.toInt() // Use variant ID as view ID
                    }
                    rgVariants.addView(rbVariant)
                }
                
                rgVariants.setOnCheckedChangeListener { _, checkedId ->
                    selectedVariants[service.serviceId] = checkedId.toLong()
                }
            } else {
                rgVariants.visibility = View.GONE
            }

            val cardView = itemView as com.google.android.material.card.MaterialCardView
            
            itemView.setOnClickListener {
                if (selectedServices.contains(service.serviceId)) {
                    selectedServices.remove(service.serviceId)
                    cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                    cardView.strokeWidth = 0
                } else {
                    selectedServices.add(service.serviceId)
                    cardView.setCardBackgroundColor(Color.parseColor("#F0FDFA")) // Teal 50
                    cardView.strokeColor = Color.parseColor("#0D9488")
                    cardView.strokeWidth = 4
                }
                
                // Show/hide continue button
                if (selectedServices.isNotEmpty()) {
                    binding.btnContinue.visibility = View.VISIBLE
                } else {
                    binding.btnContinue.visibility = View.GONE
                }
            }

            binding.llServices.addView(itemView)
        }
    }
}
