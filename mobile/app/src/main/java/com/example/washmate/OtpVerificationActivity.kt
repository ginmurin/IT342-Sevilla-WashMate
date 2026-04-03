package com.example.washmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.washmate.api.RetrofitClient
import com.example.washmate.api.SyncRequest
import com.example.washmate.auth.SupabaseManager
import com.example.washmate.databinding.ActivityOtpVerificationBinding
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.gotrue.OtpType

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    private var resendCountdownTimer: CountDownTimer? = null
    private var resendCooldown = 0

    companion object {
        const val EMAIL_EXTRA = "email"
        const val FIRST_NAME_EXTRA = "firstName"
        const val LAST_NAME_EXTRA = "lastName"
        const val USERNAME_EXTRA = "username"
        const val PHONE_EXTRA = "phone"
        const val PASSWORD_EXTRA = "password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra(EMAIL_EXTRA) ?: ""
        val firstName = intent.getStringExtra(FIRST_NAME_EXTRA) ?: ""
        val lastName = intent.getStringExtra(LAST_NAME_EXTRA) ?: ""
        val username = intent.getStringExtra(USERNAME_EXTRA) ?: ""
        val phone = intent.getStringExtra(PHONE_EXTRA) ?: ""
        val password = intent.getStringExtra(PASSWORD_EXTRA) ?: ""

        // Display masked email
        binding.tvEmailAddress.text = maskEmail(email)

        // Set up OTP input
        setupOtpInput()

        // Verify button
        binding.btnVerify.setOnClickListener {
            val otp = binding.otpInput.getOtp()
            if (otp.length == 6) {
                verifyOtp(email, otp, firstName, lastName, username, phone, password)
            } else {
                Toast.makeText(this, "Please enter a 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend button
        binding.btnResendCode.setOnClickListener {
            if (resendCooldown <= 0) {
                resendOtp(email)
            }
        }

        // Start resend cooldown
        startResendCooldown()
    }

    private fun setupOtpInput() {
        binding.otpInput.setOtpChangedListener { isComplete ->
            binding.btnVerify.isEnabled = isComplete
        }
    }

    private fun verifyOtp(email: String, otp: String, firstName: String, lastName: String, username: String,phone: String, password: String) {
        binding.btnVerify.isEnabled = false
        binding.otpInput.setEnabled(false)

        lifecycleScope.launch {
            try {
                // Verify OTP with Supabase
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.verifyEmailOtp(
                        email = email,
                        token = otp,
                        type = OtpType.Email.SIGNUP
                    )
                }

                val session = SupabaseManager.client.auth.currentSessionOrNull()
                val user = SupabaseManager.client.auth.currentUserOrNull()

                if (session == null || user == null) {
                    binding.btnVerify.isEnabled = true
                    binding.otpInput.setEnabled(true)
                    Toast.makeText(this@OtpVerificationActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Sync with backend
                val syncRequest = SyncRequest(
                    email = user.email ?: email,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phone.ifEmpty { null },
                    username = username,
                    role = "CUSTOMER",
                    oauthId = user.id ?: "", // Supabase user ID
                    oauthProvider = "SUPABASE"
                )

                val syncResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sync(syncRequest)
                }

                if (syncResponse.isSuccessful && syncResponse.body() != null) {
                    val authData = syncResponse.body()!!

                    val sharedPref = getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("JWT_TOKEN", session.accessToken)
                        putString("USER_EMAIL", authData.email)
                        putString("USER_ROLE", authData.role)
                        putString("USER_ID", authData.userId.toString())
                        apply()
                    }

                    Toast.makeText(this@OtpVerificationActivity, "Email verified! Registration complete.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@OtpVerificationActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    binding.btnVerify.isEnabled = true
                    binding.otpInput.setEnabled(true)
                    Toast.makeText(this@OtpVerificationActivity, "Sync failed: ${syncResponse.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnVerify.isEnabled = true
                binding.otpInput.setEnabled(true)
                Log.e("OtpVerificationActivity", "OTP verification error: ${e.message}", e)

                // Helpful tip: Supabase exceptions often contain detailed messages
                val errorMsg = if (e is RestException) "Invalid or expired code" else "An error occurred"
                Toast.makeText(this@OtpVerificationActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOtp(email: String) {
        binding.btnResendCode.isEnabled = false

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.resendEmail(
                        email = email,
                        type = OtpType.Email.SIGNUP
                    )
                }
                Toast.makeText(this@OtpVerificationActivity, "OTP code sent", Toast.LENGTH_SHORT).show()
                startResendCooldown()
            } catch (e: Exception) {
                binding.btnResendCode.isEnabled = true
                Log.e("OtpVerificationActivity", "Resend OTP error: ${e.message}", e)
                Toast.makeText(this@OtpVerificationActivity, "Failed to resend code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startResendCooldown() {
        resendCountdownTimer?.cancel()
        resendCooldown = 60

        resendCountdownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendCooldown = (millisUntilFinished / 1000).toInt()
                binding.btnResendCode.text = "Resend code in ${resendCooldown}s"
                binding.btnResendCode.isEnabled = false
            }

            override fun onFinish() {
                resendCooldown = 0
                binding.btnResendCode.text = "Resend code"
                binding.btnResendCode.isEnabled = true
            }
        }.start()
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email

        val localPart = parts[0]
        val domain = parts[1]

        return if (localPart.length > 2) {
            "${localPart.substring(0, 2)}${"*".repeat(localPart.length - 2)}@$domain"
        } else {
            "${"*".repeat(localPart.length)}@$domain"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resendCountdownTimer?.cancel()
    }
}
