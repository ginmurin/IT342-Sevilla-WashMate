package com.example.washmate.api

import android.content.Context
import com.example.washmate.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var _instance: AuthApi? = null
    private var _okHttpClient: OkHttpClient? = null

    fun init(context: Context) {
        if (_okHttpClient == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            _okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpInterceptor(context))
                .addInterceptor(loggingInterceptor)
                .build()
        }
    }

    val instance: AuthApi
        get() {
            if (_instance == null) {
                if (_okHttpClient == null) {
                    throw IllegalStateException("RetrofitClient not initialized. Call init(context) first.")
                }

                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(_okHttpClient!!)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                _instance = retrofit.create(AuthApi::class.java)
            }
            return _instance!!
        }
}
