package com.example.washmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.ChangePasswordRequest
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.TwoFactorEnableRequest
import com.example.washmate.api.UpdateUserRequest
import com.example.washmate.databinding.ActivitySettingsBinding
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var is2FAEnabled = false
    private var is2FAVerificationVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupListeners()
        loadUserProfile()
        validateNewPassword("")
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_wallet -> {
                    startActivity(Intent(this, WalletActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.btnSaveAccount.setOnClickListener {
            saveAccountInformation()
        }

        binding.btnUpdatePassword.setOnClickListener {
            updatePassword()
        }

        binding.etNewPassword.doAfterTextChanged { text ->
            validateNewPassword(text?.toString() ?: "")
        }

        binding.btnToggle2FA.setOnClickListener {
            if (is2FAEnabled) {
                disable2FA()
            } else {
                if (!is2FAVerificationVisible) {
                    send2FACode()
                } else {
                    enable2FA()
                }
            }
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getMe()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        binding.etUsername.setText(user.username)
                        binding.etFirstName.setText(user.firstName)
                        binding.etLastName.setText(user.lastName)
                        binding.etEmail.setText(user.email)
                        binding.etPhone.setText(user.phoneNumber)
                        
                        val isCustomer = user.role.uppercase() == "CUSTOMER"
                        binding.etUsername.isEnabled = isCustomer
                        if (!isCustomer) {
                            binding.tilUsername.hint = "Username (Read-only)"
                        } else {
                            binding.tilUsername.hint = "Username"
                        }
                        
                        is2FAEnabled = user.twoFactorEnabled
                        update2FAUI()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveAccountInformation() {
        val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
        val username = binding.etUsername.text.toString()
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val phone = binding.etPhone.text.toString()

        val request = UpdateUserRequest(username, firstName, lastName, phone)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.updateMe(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Account updated successfully", Toast.LENGTH_SHORT).show()
                        sharedPref.edit().putString("USER_FIRST_NAME", firstName).apply()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: ""
                        if (errorMsg.contains("Username is already taken")) {
                            Toast.makeText(this@SettingsActivity, "Error: Username is already taken", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@SettingsActivity, "Failed to update account", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateNewPassword(newPassword)) {
            Toast.makeText(this, "New password does not meet complexity requirements", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ChangePasswordRequest(currentPassword, newPassword)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.changePassword(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        binding.etCurrentPassword.setText("")
                        binding.etNewPassword.setText("")
                        binding.etConfirmPassword.setText("")
                        validateNewPassword("")
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: ""
                        if (errorMsg.contains("Password must be at least 8 characters")) {
                            Toast.makeText(this@SettingsActivity, "Failed: Password must contain uppercase and digit.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@SettingsActivity, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateNewPassword(password: String): Boolean {
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }

        if (hasMinLength) {
            binding.tvReqLength.text = "✔️ At least 8 characters"
            binding.tvReqLength.setTextColor(android.graphics.Color.parseColor("#10B981"))
        } else {
            binding.tvReqLength.text = "❌ At least 8 characters"
            binding.tvReqLength.setTextColor(android.graphics.Color.parseColor("#EF4444"))
        }

        if (hasUppercase) {
            binding.tvReqUppercase.text = "✔️ At least one uppercase letter"
            binding.tvReqUppercase.setTextColor(android.graphics.Color.parseColor("#10B981"))
        } else {
            binding.tvReqUppercase.text = "❌ At least one uppercase letter"
            binding.tvReqUppercase.setTextColor(android.graphics.Color.parseColor("#EF4444"))
        }

        if (hasDigit) {
            binding.tvReqNumber.text = "✔️ At least one number"
            binding.tvReqNumber.setTextColor(android.graphics.Color.parseColor("#10B981"))
        } else {
            binding.tvReqNumber.text = "❌ At least one number"
            binding.tvReqNumber.setTextColor(android.graphics.Color.parseColor("#EF4444"))
        }

        return hasMinLength && hasUppercase && hasDigit
    }

    private fun send2FACode() {
        binding.btnToggle2FA.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.sendTwoFactorCode()
                withContext(Dispatchers.Main) {
                    binding.btnToggle2FA.isEnabled = true
                    if (response.isSuccessful) {
                        is2FAVerificationVisible = true
                        binding.ll2FAVerification.visibility = View.VISIBLE
                        binding.btnToggle2FA.text = "Verify and Enable"
                        Toast.makeText(this@SettingsActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Failed to send code", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnToggle2FA.isEnabled = true
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enable2FA() {
        val code = binding.et2FACode.text.toString()
        if (code.length != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnToggle2FA.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.enableTwoFactor(TwoFactorEnableRequest(code))
                withContext(Dispatchers.Main) {
                    binding.btnToggle2FA.isEnabled = true
                    if (response.isSuccessful) {
                        is2FAEnabled = true
                        is2FAVerificationVisible = false
                        binding.et2FACode.setText("")
                        update2FAUI()
                        Toast.makeText(this@SettingsActivity, "2FA Enabled", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Invalid code or error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnToggle2FA.isEnabled = true
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun disable2FA() {
        binding.btnToggle2FA.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.disableTwoFactor()
                withContext(Dispatchers.Main) {
                    binding.btnToggle2FA.isEnabled = true
                    if (response.isSuccessful) {
                        is2FAEnabled = false
                        update2FAUI()
                        Toast.makeText(this@SettingsActivity, "2FA Disabled", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Failed to disable 2FA", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnToggle2FA.isEnabled = true
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun update2FAUI() {
        if (is2FAEnabled) {
            binding.tv2FAStatus.text = "Enabled"
            binding.tv2FAStatus.setTextColor(android.graphics.Color.parseColor("#059669")) // Emerald 600
            binding.btnToggle2FA.text = "Disable Two-Factor Authentication"
            binding.btnToggle2FA.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DC2626")) // Red 600
            binding.ll2FAVerification.visibility = View.GONE
            is2FAVerificationVisible = false
        } else {
            binding.tv2FAStatus.text = "Disabled"
            binding.tv2FAStatus.setTextColor(android.graphics.Color.parseColor("#64748B")) // Slate 500
            binding.btnToggle2FA.text = "Enable Two-Factor Authentication"
            binding.btnToggle2FA.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9333EA")) // Purple 600
            
            if (!is2FAVerificationVisible) {
                binding.ll2FAVerification.visibility = View.GONE
            }
        }
    }
}
