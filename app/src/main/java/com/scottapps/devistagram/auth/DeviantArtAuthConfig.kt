package com.scottapps.devistagram.auth

object DeviantArtAuthConfig {
    const val CLIENT_ID = "59601"
    const val CLIENT_SECRET = "ccd16fd197588957c0bd74939057fa9d"
    const val REDIRECT_URI = "com.scottapps.devistagram://oauth2callback"
    const val AUTHORIZATION_URL = "https://www.deviantart.com/oauth2/authorize"
    const val TOKEN_URL = "https://www.deviantart.com/oauth2/token"
    const val SCOPE = "browse message user" // Browse, messages, and user profile access
    
    // API Base URL
    const val API_BASE_URL = "https://www.deviantart.com/api/v1/oauth2/"
}
