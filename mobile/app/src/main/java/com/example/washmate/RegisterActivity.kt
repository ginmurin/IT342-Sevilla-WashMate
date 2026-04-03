package com.example.washmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.SyncRequest
import com.example.washmate.auth.SupabaseManager
import com.example.washmate.databinding.ActivityRegisterBinding
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize if not already done
        SupabaseManager.init(this)
        RetrofitClient.init(this)

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvLogin.setOnClickListener {
            finish() // Goes back to Login screen
        }
    }

    private fun performRegistration() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                // Step 1: Sign up with Supabase using Email provider
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                        data = buildJsonObject {
                            put("first_name", firstName)
                            put("last_name", lastName)
                            put("username", username)
                            put("phone", phone)
                        }
                    }
                }

                val user = SupabaseManager.client.auth.currentUserOrNull()

                if (user == null) {
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                binding.btnRegister.isEnabled = true

                // Navigate to OTP verification (Supabase will send OTP to email)
                val intent = Intent(this@RegisterActivity, OtpVerificationActivity::class.java).apply {
                    putExtra(OtpVerificationActivity.EMAIL_EXTRA, email)
                    putExtra(OtpVerificationActivity.FIRST_NAME_EXTRA, firstName)
                    putExtra(OtpVerificationActivity.LAST_NAME_EXTRA, lastName)
                    putExtra(OtpVerificationActivity.USERNAME_EXTRA, username)
                    putExtra(OtpVerificationActivity.PHONE_EXTRA, phone)
                    putExtra(OtpVerificationActivity.PASSWORD_EXTRA, password)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } catch (e: RestException) {
                binding.btnRegister.isEnabled = true
                Log.e("RegisterActivity", "Signup error: ${e.message}", e)

                val errorMessage = when {
                    e.message?.contains("already registered") == true -> "Email already registered"
                    e.message?.contains("invalid") == true -> "Invalid credentials"
                    else -> e.message ?: "Registration failed"
                }

                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.btnRegister.isEnabled = true
                Log.e("RegisterActivity", "Registration error: ${e.message}", e)
                Toast.makeText(
                    this@RegisterActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
