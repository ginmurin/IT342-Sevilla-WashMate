package com.example.washmate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.OrderRequest
import com.example.washmate.api.OrderServiceInput
import com.example.washmate.api.WashServiceDTO
import com.example.washmate.databinding.ActivityPlaceOrderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaceOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceOrderBinding
    private val quantities = mutableMapOf<Long, Double>()
    private val selectedServicesList = mutableListOf<WashServiceDTO>()
    private val selectedVariantsMap = mutableMapOf<Long, Long>() // serviceId -> variantId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedIds = intent.getLongArrayExtra("SELECTED_SERVICES") ?: longArrayOf()
        val selectedVariantIds = intent.getLongArrayExtra("SELECTED_VARIANTS") ?: longArrayOf()

        for (i in selectedIds.indices) {
            if (i < selectedVariantIds.size) {
                selectedVariantsMap[selectedIds[i]] = selectedVariantIds[i]
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSubmitOrder.setOnClickListener {
            submitOrder()
        }

        fetchAndDisplayServices(selectedIds)
    }

    private fun fetchAndDisplayServices(selectedIds: LongArray) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getServices()
                }

                if (response.isSuccessful && response.body() != null) {
                    val allServices = response.body()!!
                    val filtered = allServices.filter { it.serviceId in selectedIds }
                    selectedServicesList.addAll(filtered)
                    displaySelectedServices()
                } else {
                    Toast.makeText(this@PlaceOrderActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlaceOrderActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySelectedServices() {
        binding.llSelectedServices.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (service in selectedServicesList) {
            val itemView = inflater.inflate(R.layout.item_selected_service, binding.llSelectedServices, false)
            val tvServiceName = itemView.findViewById<TextView>(R.id.tvServiceName)
            val tvServicePrice = itemView.findViewById<TextView>(R.id.tvServicePrice)
            val etQuantity = itemView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etQuantity)

            val variantId = selectedVariantsMap[service.serviceId]
            val variant = service.variants?.find { it.variantId == variantId }

            if (variant != null) {
                tvServiceName.text = "${service.serviceName} (${variant.variantName})"
                tvServicePrice.text = "₱${String.format("%.2f", variant.variantPrice)} per ${service.unitType.lowercase()}"
            } else {
                tvServiceName.text = service.serviceName
                tvServicePrice.text = "₱${String.format("%.2f", service.basePricePerUnit)} per ${service.unitType.lowercase()}"
            }

            etQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val qtyStr = s.toString()
                    val qty = qtyStr.toDoubleOrNull() ?: 0.0
                    quantities[service.serviceId] = qty
                    calculateTotal()
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.llSelectedServices.addView(itemView)
        }
    }

    private fun calculateTotalAmount(): Double {
        var total = 0.0
        for (service in selectedServicesList) {
            val qty = quantities[service.serviceId] ?: 0.0
            val variantId = selectedVariantsMap[service.serviceId]
            val variant = service.variants?.find { it.variantId == variantId }
            
            val price = variant?.variantPrice ?: service.basePricePerUnit
            total += qty * price
        }
        return total
    }

    private fun calculateTotal() {
        val total = calculateTotalAmount()
        binding.tvTotalAmount.text = "₱${String.format("%.2f", total)}"
    }

    private fun submitOrder() {
        if (quantities.isEmpty() || quantities.values.all { it <= 0 }) {
            Toast.makeText(this, "Please enter quantity for at least one service", Toast.LENGTH_SHORT).show()
            return
        }

        val specialInstructions = binding.etSpecialInstructions.text.toString()

        val serviceIds = mutableListOf<Long>()
        val qtys = mutableListOf<Double>()
        val variantIds = mutableListOf<Long>()

        for (service in selectedServicesList) {
            val qty = quantities[service.serviceId] ?: 0.0
            if (qty > 0) {
                serviceIds.add(service.serviceId)
                qtys.add(qty)
                variantIds.add(selectedVariantsMap[service.serviceId] ?: 0L)
            }
        }

        val intent = android.content.Intent(this, ScheduleAddressActivity::class.java).apply {
            putExtra("SERVICE_IDS", serviceIds.toLongArray())
            putExtra("QUANTITIES", qtys.toDoubleArray())
            putExtra("VARIANT_IDS", variantIds.toLongArray())
            putExtra("SPECIAL_INSTRUCTIONS", specialInstructions)
            putExtra("TOTAL_AMOUNT", calculateTotalAmount())
        }
        startActivity(intent)
    }
}
