package com.example.washmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.SyncRequest
import com.example.washmate.auth.GoogleAuthHelper
import com.example.washmate.auth.SupabaseManager
import com.example.washmate.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
    private var isAuthenticating = false

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isAuthenticating = false // Reset flag here
        binding.btnRegister.isEnabled = true
        binding.btnGoogleSignUp.isEnabled = true

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    performGoogleSignUp(idToken)
                }
            } catch (e: ApiException) {
                // FIXED: You had a 'when' block here that wasn't doing anything.
                // Now it shows a Toast with the actual error.
                val errorMsg = when (e.statusCode) {
                    7 -> "Network Error: Check your Wi-Fi"
                    10 -> "Developer Error: SHA-1 or Package Name mismatch"
                    12500 -> "Sign-in failed: Check Google Play Services/Consent Screen"
                    else -> "Error Code: ${e.statusCode}"
                }
                Log.e("GoogleSignIn", "Status Code: ${e.statusCode}")
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) { // Add this line
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize if not already done
        SupabaseManager.init(this)
        RetrofitClient.init(this)

        // Initialize Google Auth Helper with web client ID
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        GoogleAuthHelper.initialize(this, webClientId)

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvLogin.setOnClickListener {
            finish() // Goes back to Login screen
        }

        // Google Sign-Up button
        binding.btnGoogleSignUp.setOnClickListener {
            if (!isAuthenticating) {
                performGoogleSignUpIntent()
            }
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

    private fun performGoogleSignUpIntent() {
        if (isAuthenticating) return

        val signInIntent = GoogleAuthHelper.getSignInIntent()

        if (signInIntent != null) {
            // Only proceed if the intent is actually created
            isAuthenticating = true
            binding.btnRegister.isEnabled = false
            binding.btnGoogleSignUp.isEnabled = false

            googleSignInLauncher.launch(signInIntent)
        } else {
            // This happens if GoogleAuthHelper.initialize() wasn't called yet
            Toast.makeText(this, "Google Sign-In not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performGoogleSignUp(idToken: String) {
        lifecycleScope.launch {
            try {
                // 1. Exchange token with Supabase
                val user = GoogleAuthHelper.exchangeTokenWithSupabase(idToken)

                if (user == null) {
                    resetLoadingState()
                    Toast.makeText(this@RegisterActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Get Session and Provider Info
                val session = SupabaseManager.client.auth.currentSessionOrNull()

                // For Google signup, provider should always be "google"
                val provider = user.appMetadata?.get("provider")?.jsonPrimitive?.content

                // 3. Sync with your backend
                val fullName = user.userMetadata?.get("full_name")?.jsonPrimitive?.content
                val (firstName, lastName) = GoogleAuthHelper.parseGoogleName(fullName)

                val syncRequest = SyncRequest(
                    email = user.email ?: "",
                    firstName = firstName,
                    lastName = lastName,
                    username = user.email?.substringBefore("@"),
                    phoneNumber = null,
                    role = "CUSTOMER",
                    oauthId = user.id,  // Supabase UUID
                    oauthProvider = if (provider == "google") "GOOGLE" else null
                )

                val syncResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sync(syncRequest)
                }

                if (syncResponse.isSuccessful && syncResponse.body() != null && session != null) {
                    val authData = syncResponse.body()!!

                    getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE).edit().apply {
                        putString("JWT_TOKEN", session.accessToken)
                        putString("USER_EMAIL", authData.email)
                        putString("USER_ROLE", authData.role)
                        putString("USER_ID", authData.userId.toString())
                        apply()
                    }

                    startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    resetLoadingState()
                    Toast.makeText(this@RegisterActivity, "Sync failed: ${syncResponse.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                resetLoadingState()
                Log.e("RegisterActivity", "Google Sign-Up error: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Auth error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resetLoadingState() {
        isAuthenticating = false
        binding.btnRegister.isEnabled = true
        binding.btnGoogleSignUp.isEnabled = true
    }
}
