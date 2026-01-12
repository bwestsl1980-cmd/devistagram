package com.bethwestsl.devistagram.auth

import com.bethwestsl.devistagram.BuildConfig

object DeviantArtAuthConfig {
    val CLIENT_ID: String = BuildConfig.DEVIANTART_CLIENT_ID
    val CLIENT_SECRET: String = BuildConfig.DEVIANTART_CLIENT_SECRET
    const val REDIRECT_URI = "com.bethwestsl.devistagram://oauth2callback"
    const val AUTHORIZATION_URL = "https://www.deviantart.com/oauth2/authorize"
    const val TOKEN_URL = "https://www.deviantart.com/oauth2/token"
    const val SCOPE = "browse message note user collection comment.post user.manage" // Full access for browsing, messages, notes, user profile, collections, commenting, and user management

    // API Base URL
    const val API_BASE_URL = "https://www.deviantart.com/api/v1/oauth2/"
}
