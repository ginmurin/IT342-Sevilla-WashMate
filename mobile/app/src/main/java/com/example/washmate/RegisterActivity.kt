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
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
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
                            put("phone", phone)
                        }
                    }
                }

                val session = SupabaseManager.client.auth.currentSessionOrNull()
                val user = SupabaseManager.client.auth.currentUserOrNull()

                if (session == null || user == null) {
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Step 2: Sync with backend
                val syncRequest = SyncRequest(
                    email = user.email ?: email,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phone.ifEmpty { null },
                    username = user.userMetadata?.get("username")?.jsonPrimitive?.content,
                    role = "CUSTOMER"
                )

                val syncResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sync(syncRequest)
                }

                binding.btnRegister.isEnabled = true

                if (syncResponse.isSuccessful && syncResponse.body() != null) {
                    val authData = syncResponse.body()!!

                    // Save token and user info
                    val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("JWT_TOKEN", session.accessToken)
                        putString("USER_EMAIL", authData.email)
                        putString("USER_ROLE", authData.role)
                        putString("USER_ID", authData.userId.toString())
                        apply()
                    }

                    Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to Dashboard
                    val intent = Intent(this@RegisterActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Sync failed: ${syncResponse.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
