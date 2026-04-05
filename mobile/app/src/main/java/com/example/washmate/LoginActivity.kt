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
import com.example.washmate.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonPrimitive

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

        // Initialize Supabase and Retrofit
        SupabaseManager.init(this)
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

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                // Step 1: Authenticate with Supabase using Email provider
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.signInWith(Email) {
                        email = emailOrUsername
                        this.password = password
                    }
                }

                val session = SupabaseManager.client.auth.currentSessionOrNull()
                val user = SupabaseManager.client.auth.currentUserOrNull()

                if (session == null || user == null) {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val provider = user.appMetadata?.get("provider")?.jsonPrimitive?.content
                val isGoogleAuth = provider == "google"

                // Step 2: Sync with backend
                val syncRequest = SyncRequest(
                    email = user.email ?: emailOrUsername,
                    firstName = user.userMetadata?.get("first_name")?.jsonPrimitive?.content ?: "",
                    lastName = user.userMetadata?.get("last_name")?.jsonPrimitive?.content ?: "",
                    username = user.userMetadata?.get("username")?.jsonPrimitive?.content,
                    phoneNumber = user.userMetadata?.get("phone")?.jsonPrimitive?.content,
                    oauthId = user.id,  // Supabase user UUID
                    oauthProvider = if (isGoogleAuth) "GOOGLE" else "SUPABASE",
                    role = "CUSTOMER"
                )

                val syncResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sync(syncRequest)
                }

                binding.btnLogin.isEnabled = true

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

                    val userRole = authData.role.uppercase()

                    // Navigate based on role
                    if (userRole == "CUSTOMER") {
                        startDashboard()
                    } else {
                        // Show role selection for SHOPOWNER/ADMIN
                        val intent = Intent(this@LoginActivity, RoleSelectActivity::class.java)
                        intent.putExtra("user_role", userRole)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Sync failed: ${syncResponse.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: RestException) {
                binding.btnLogin.isEnabled = true
                Log.e("LoginActivity", "Authentication error: ${e.message}", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Invalid credentials. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
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
                // Generate only the raw nonce string as required by your helper
                //val rawNonce = GoogleAuthHelper.generateNonce()

                // Exchange token with Supabase
                val user = withContext(Dispatchers.IO) {
                    GoogleAuthHelper.exchangeTokenWithSupabase(idToken)//rawNonce
                }

                val session = SupabaseManager.client.auth.currentSessionOrNull()

                if (user == null || session == null) {
                    resetUiState()
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Parse user info
                val fullName = user.userMetadata?.get("full_name")?.jsonPrimitive?.content ?: ""
                val (firstName, lastName) = GoogleAuthHelper.parseGoogleName(fullName)

                // Sync with backend
                val syncRequest = SyncRequest(
                    email = user.email ?: "",
                    firstName = firstName,
                    lastName = lastName,
                    username = user.email?.substringBefore("@"),
                    phoneNumber = null,
                    oauthId = user.id,  // Google auth - store the Supabase UUID
                    oauthProvider = "GOOGLE",
                    role = "CUSTOMER"
                )

                val syncResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sync(syncRequest)
                }

                if (syncResponse.isSuccessful && syncResponse.body() != null) {
                    val authData = syncResponse.body()!!

                    // Save to Prefs
                    getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE).edit().apply {
                        putString("JWT_TOKEN", session.accessToken)
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
                    Toast.makeText(this@LoginActivity, "Sync failed: ${syncResponse.code()}", Toast.LENGTH_SHORT).show()
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
