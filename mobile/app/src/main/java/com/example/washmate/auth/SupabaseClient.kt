package com.example.washmate.auth

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import com.example.washmate.BuildConfig

object SupabaseManager {
    private lateinit var _client: SupabaseClient

    val client: SupabaseClient
        get() {
            if (!this::_client.isInitialized) {
                throw IllegalStateException("SupabaseClient not initialized. Call init() first.")
            }
            return _client
        }

    fun init(context: Context) {
        if (this::_client.isInitialized) {
            return // Already initialized
        }

        _client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            httpEngine = io.ktor.client.engine.android.Android.create()
            install(Auth)
            install(Postgrest)
        }
    }
}
