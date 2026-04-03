package com.example.washmate.api

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.washmate.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response

class HttpInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip token injection only for public auth endpoints (login, email lookup)
        // DO inject token for /register so backend knows who the user is
        val url = originalRequest.url.toString()
        val shouldInjectToken = !(url.contains("/api/auth/login") || url.contains("/api/auth/email-by-username"))

        // Build new request with token if needed
        val requestBuilder = originalRequest.newBuilder()

        if (shouldInjectToken) {
            val sharedPref = context.getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("JWT_TOKEN", null)

            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d("HttpInterceptor", "Token injected for: ${originalRequest.url}")
            }
        }

        val request = requestBuilder.build()

        // Log request
        Log.d("HttpInterceptor", "→ ${request.method} ${request.url}")

        try {
            val response = chain.proceed(request)

            // Log response
            Log.d("HttpInterceptor", "← ${response.code} ${response.message} (${request.url})")

            // Handle 401 - Clear token and redirect to login
            if (response.code == 401) {
                Log.w("HttpInterceptor", "401 Unauthorized - clearing token and redirecting to login")
                val sharedPref = context.getSharedPreferences("WashMatePrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove("JWT_TOKEN")
                    remove("USER_EMAIL")
                    remove("USER_ROLE")
                    apply()
                }

                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }

            return response
        } catch (e: Exception) {
            Log.e("HttpInterceptor", "Request failed: ${e.message}", e)
            throw e
        }
    }
}
