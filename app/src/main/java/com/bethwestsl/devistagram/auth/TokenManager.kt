package com.bethwestsl.devistagram.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "deviantart_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveTokens(accessToken: String, refreshToken: String?, expiresIn: Int) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L)
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_EXPIRY_TIME, expiryTime)
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun isTokenExpired(): Boolean {
        val expiryTime = sharedPreferences.getLong(KEY_EXPIRY_TIME, 0)
        return System.currentTimeMillis() >= expiryTime
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && !isTokenExpired()
    }
    
    fun clearTokens() {
        android.util.Log.d("TokenManager", "CLEARING TOKENS - Before clear")
        android.util.Log.d("TokenManager", "Access token before clear: ${getAccessToken()}")
        sharedPreferences.edit().clear().apply()
        android.util.Log.d("TokenManager", "CLEARING TOKENS - After clear")
        android.util.Log.d("TokenManager", "Access token after clear: ${getAccessToken()}")
    }
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRY_TIME = "expiry_time"
    }
}
