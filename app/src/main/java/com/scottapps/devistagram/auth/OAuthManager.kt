package com.scottapps.devistagram.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.scottapps.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class OAuthManager(private val context: Context) {
    
    private val tokenManager = TokenManager(context)
    private var pendingState: String? = null
    
    /**
     * Starts the OAuth flow by opening DeviantArt authorization in Custom Tabs
     */
    fun startOAuthFlow() {
        // Generate unique state for CSRF protection
        pendingState = UUID.randomUUID().toString()
        
        val authUri = Uri.parse(DeviantArtAuthConfig.AUTHORIZATION_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", DeviantArtAuthConfig.CLIENT_ID)
            .appendQueryParameter("redirect_uri", DeviantArtAuthConfig.REDIRECT_URI)
            .appendQueryParameter("scope", DeviantArtAuthConfig.SCOPE)
            .appendQueryParameter("state", pendingState)
            .build()
        
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        
        customTabsIntent.launchUrl(context, authUri)
    }
    
    /**
     * Handles the callback from DeviantArt with the authorization code
     */
    suspend fun handleAuthCallback(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verify state parameter
            val state = uri.getQueryParameter("state")
            if (state != pendingState) {
                return@withContext Result.failure(Exception("Invalid state parameter"))
            }
            
            // Check for error
            val error = uri.getQueryParameter("error")
            if (error != null) {
                val errorDescription = uri.getQueryParameter("error_description") ?: error
                return@withContext Result.failure(Exception(errorDescription))
            }
            
            // Get authorization code
            val code = uri.getQueryParameter("code")
                ?: return@withContext Result.failure(Exception("No authorization code received"))
            
            // Exchange code for access token
            exchangeCodeForToken(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Exchanges authorization code for access token
     */
    private suspend fun exchangeCodeForToken(code: String): Result<String> {
        return try {
            val response = RetrofitClient.authApi.getAccessToken(
                grantType = "authorization_code",
                clientId = DeviantArtAuthConfig.CLIENT_ID,
                clientSecret = DeviantArtAuthConfig.CLIENT_SECRET,
                redirectUri = DeviantArtAuthConfig.REDIRECT_URI,
                code = code
            )
            
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenManager.saveTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresIn = tokenResponse.expiresIn
                )
                Result.success("Login successful")
            } else {
                Result.failure(Exception("Failed to get access token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refreshes the access token using the refresh token
     */
    suspend fun refreshAccessToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return@withContext Result.failure(Exception("No refresh token available"))
            
            val response = RetrofitClient.authApi.refreshAccessToken(
                grantType = "refresh_token",
                clientId = DeviantArtAuthConfig.CLIENT_ID,
                clientSecret = DeviantArtAuthConfig.CLIENT_SECRET,
                refreshToken = refreshToken
            )
            
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenManager.saveTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken ?: refreshToken,
                    expiresIn = tokenResponse.expiresIn
                )
                Result.success("Token refreshed")
            } else {
                Result.failure(Exception("Failed to refresh token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    
    fun logout() {
        tokenManager.clearTokens()
        pendingState = null
    }
}
