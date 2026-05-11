package com.example.washmate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.washmate.databinding.ActivityScheduleAddressBinding
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleAddressBinding
    private val calendar = Calendar.getInstance()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve extras
        val serviceIds = intent.getLongArrayExtra("SERVICE_IDS") ?: longArrayOf()
        val quantities = intent.getDoubleArrayExtra("QUANTITIES") ?: doubleArrayOf()
        val variantIds = intent.getLongArrayExtra("VARIANT_IDS") ?: longArrayOf()
        val specialInstructions = intent.getStringExtra("SPECIAL_INSTRUCTIONS") ?: ""
        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)

        // Setup Location
        binding.tvUseCurrentLocation.setOnClickListener {
            checkLocationPermissionAndGetLocation()
        }

        // Setup Date Pickers
        binding.etPickupDate.setOnClickListener {
            showDatePicker { date ->
                binding.etPickupDate.setText(date)
            }
        }

        binding.etDeliveryDate.setOnClickListener {
            showDatePicker { date ->
                binding.etDeliveryDate.setText(date)
            }
        }

        // Setup Time Spinners
        val timeSlots = arrayOf(
            "08:00 AM – 10:00 AM",
            "10:00 AM – 12:00 PM",
            "02:00 PM – 04:00 PM",
            "04:00 PM – 06:00 PM"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, timeSlots)
        binding.spinnerPickupTime.setAdapter(adapter)
        binding.spinnerDeliveryTime.setAdapter(adapter)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContinueToPayment.setOnClickListener {
            val pickupDate = binding.etPickupDate.text.toString()
            val pickupTime = binding.spinnerPickupTime.text.toString()
            val deliveryDate = binding.etDeliveryDate.text.toString()
            val deliveryTime = binding.spinnerDeliveryTime.text.toString()
            val address = binding.etAddress.text.toString()
            val phone = binding.etPhone.text.toString()

            if (pickupDate.isEmpty() || pickupTime.isEmpty() || deliveryDate.isEmpty() || deliveryTime.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Open PaymentReviewActivity (Step 3)
            val intent = Intent(this, PaymentReviewActivity::class.java).apply {
                putExtra("SERVICE_IDS", serviceIds)
                putExtra("QUANTITIES", quantities)
                putExtra("VARIANT_IDS", variantIds)
                putExtra("SPECIAL_INSTRUCTIONS", specialInstructions)
                putExtra("TOTAL_AMOUNT", totalAmount)
                putExtra("PICKUP_DATE", pickupDate)
                putExtra("PICKUP_TIME", pickupTime)
                putExtra("DELIVERY_DATE", deliveryDate)
                putExtra("DELIVERY_TIME", deliveryTime)
                putExtra("ADDRESS", address)
                putExtra("PHONE", phone)
            }
            startActivity(intent)
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            calendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(sdf.format(calendar.time))
        }, year, month, day)

        // Set min date to tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -1) // Reset

        datePickerDialog.show()
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val locationManager = getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        val location = if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
        } else {
            locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
        }

        if (location != null) {
            val geocoder = android.location.Geocoder(this, java.util.Locale.getDefault())
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1, object : android.location.Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: List<android.location.Address>) {
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0].getAddressLine(0)
                                runOnUiThread {
                                    binding.etAddress.setText(address)
                                }
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            runOnUiThread {
                                Toast.makeText(this@ScheduleAddressActivity, "Geocoder error: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                } else {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        binding.etAddress.setText(address)
                    } else {
                        Toast.makeText(this@ScheduleAddressActivity, "Unable to find address for this location", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Geocoder error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Unable to get current location. Make sure GPS is on.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
