package com.example.washmate.auth

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import io.github.jan.supabase.gotrue.providers.builtin.IDToken

object GoogleAuthHelper {
    private var googleSignInClient: GoogleSignInClient? = null
    private var webClientId: String = ""

    /**
     * Initialize Google Sign-In with web client ID
     * @param context Application context
     * @param webClientIdValue Web client ID from Google Cloud Console (NOT Android client ID)
     */
    fun initialize(context: Context, webClientIdValue: String) {
        webClientId = webClientIdValue
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId) // Ensure this is the WEB Client ID from Cloud Console
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
    }

    /**
     * Generate cryptographically secure nonce for PKCE flow
     * @return Pair of (raw nonce for Supabase, hashed nonce for Google)
     */
    fun generateNonce(): String {
        val rawNonce = ByteArray(32)
        SecureRandom().nextBytes(rawNonce)
        return Base64.encodeToString(rawNonce, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    fun hashNonce(rawNonce: String): String {
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * Parse Google's full_name into first and last names
     * @param fullName Full name from Google profile (e.g., "John Doe")
     * @return Pair of (firstName, lastName)
     */
    fun parseGoogleName(fullName: String?): Pair<String, String> {
        val name = fullName?.trim() ?: ""
        if (name.isEmpty()) return "" to ""

        val parts = name.split("\\s+".toRegex())
        val firstName = parts.firstOrNull() ?: ""
        val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""
        return firstName to lastName
    }

    /**
     * Initiate Google Sign-In flow and get ID token
     * Used by Activities via ActivityResultContracts
     * @param activity Current activity context
     * @return SignInIntentLauncher to launch in Activity
     */
    fun getSignInIntent() = googleSignInClient?.signInIntent

    /**
     * Extract ID token from Google Sign-In result
     * @param task Google Sign-In task result
     * @return ID token string or null if failed
     * @throws ApiException if sign-in failed
     */
    fun getIdTokenFromResult(task: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>?): String? {
        return try {
            val account = task?.getResult(ApiException::class.java)
            account?.idToken
        } catch (e: ApiException) {
            Log.e("GoogleAuthHelper", "Sign-in failed: ${e.statusCode}")
            null
        }
    }

    /**
     * Exchange Google ID token with Supabase for session
     * @param idToken Google ID token
     * @param rawNonce Raw nonce used in token generation
     * @return Supabase session with user info
     */
    suspend fun exchangeTokenWithSupabase(
        idToken: String,
        //rawNonce: String? = null
    ): io.github.jan.supabase.gotrue.user.UserInfo? = withContext(Dispatchers.IO) {
        try {
            // Use the idToken function inside the Google provider block
            SupabaseManager.client.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
            SupabaseManager.client.auth.currentUserOrNull()
        } catch (e: Exception) {
            android.util.Log.e("GoogleAuthHelper", "Supabase Exchange Error: ${e.message}")
            throw e
        }
    }

    /**
     * Sign out from Google and clear cached account
     * Must be called on app logout
     * @param context Application context
     */
    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        googleSignInClient?.signOut()?.addOnCompleteListener {
            onComplete()
        }
    }
}
