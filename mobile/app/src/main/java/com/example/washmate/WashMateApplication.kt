package com.example.washmate

import android.app.Application
import com.example.washmate.api.RetrofitClient

class WashMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
