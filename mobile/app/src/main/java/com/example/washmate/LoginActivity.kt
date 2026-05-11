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
import com.example.washmate.api.LoginRequest
import com.example.washmate.api.GoogleIdTokenRequest
import com.example.washmate.auth.GoogleAuthHelper
import com.example.washmate.BuildConfig
import com.example.washmate.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isAuthenticating = false

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    performGoogleSignIn(idToken)
                } else {
                    isAuthenticating = false
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Failed to get Google ID token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                isAuthenticating = false
                binding.btnLogin.isEnabled = true
                when (e.statusCode) {
                    12501 -> Log.d("GoogleSignIn", "User cancelled sign-in")
                    else -> Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isAuthenticating = false
            binding.btnLogin.isEnabled = true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Retrofit
        RetrofitClient.init(this)

        // Initialize Google Auth Helper with web client ID
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        GoogleAuthHelper.initialize(this, webClientId)

        // Check if already logged in
        val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("JWT_TOKEN", null)
        if (token != null) {
            startDashboard()
            return
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Google Sign-In button
        binding.btnGoogleSignIn.setOnClickListener {
            if (!isAuthenticating) {
                performGoogleSignInIntent()
            }
        }
    }

    private fun performLogin() {
        val emailOrUsername = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/username and password", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Call backend login directly
                val loginResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.login(LoginRequest(emailOrUsername = emailOrUsername, password = password))
                }

                binding.btnLogin.isEnabled = true

                if (loginResponse.isSuccessful && loginResponse.body() != null) {
                    val authData = loginResponse.body()!!

                    // Save token and user info from our backend
                    val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("JWT_TOKEN", authData.accessToken)
                        putString("USER_EMAIL", authData.email)
                        putString("USER_ROLE", authData.role)
                        putString("USER_ID", authData.userId.toString())
                        apply()
                    }

                    val userRole = authData.role.uppercase()

                    // Navigate based on role
                    if (userRole == "CUSTOMER") {
                        startDashboard()
                    } else {
                        val intent = Intent(this@LoginActivity, RoleSelectActivity::class.java)
                        intent.putExtra("user_role", userRole)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${loginResponse.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.btnLogin.isEnabled = true
                Log.e("LoginActivity", "Login error: ${e.message}", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun performGoogleSignInIntent() {
        if (isAuthenticating) return

        val signInIntent = GoogleAuthHelper.getSignInIntent()
        if (signInIntent != null) {
            isAuthenticating = true
            binding.btnLogin.isEnabled = false
            binding.btnGoogleSignIn.isEnabled = false
            googleSignInLauncher.launch(signInIntent)
        } else {
            Toast.makeText(this, "Google Sign-In not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performGoogleSignIn(idToken: String) {
        lifecycleScope.launch {
            try {
                // Call backend google/mobile directly
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.googleMobile(GoogleIdTokenRequest(idToken))
                }

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    // Save to Prefs
                    getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE).edit().apply {
                        putString("JWT_TOKEN", authData.accessToken)
                        putString("USER_EMAIL", authData.email)
                        putString("USER_ROLE", authData.role)
                        putString("USER_ID", authData.userId.toString())
                        apply()
                    }

                    if (authData.role.uppercase() == "CUSTOMER") {
                        startDashboard()
                    } else {
                        val intent = Intent(this@LoginActivity, RoleSelectActivity::class.java)
                        intent.putExtra("user_role", authData.role.uppercase())
                        startActivity(intent)
                        finish()
                    }
                } else {
                    resetUiState()
                    Toast.makeText(this@LoginActivity, "Google Sign-In failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                resetUiState()
                Log.e("LoginActivity", "Google Login error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Auth error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun resetUiState() {
        isAuthenticating = false
        binding.btnLogin.isEnabled = true
        binding.btnGoogleSignIn.isEnabled = true
    }
}
