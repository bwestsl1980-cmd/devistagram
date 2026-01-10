package com.scottapps.devistagram.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("token_type")
    val tokenType: String,
    
    @SerializedName("refresh_token")
    val refreshToken: String?,
    
    @SerializedName("scope")
    val scope: String
)

data class TokenError(
    @SerializedName("error")
    val error: String,
    
    @SerializedName("error_description")
    val errorDescription: String
)
