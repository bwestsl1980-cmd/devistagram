package com.scottapps.devistagram.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("user")
    val user: User?,
    
    @SerializedName("profile_url")
    val profileUrl: String?,
    
    @SerializedName("tagline")
    val tagline: String?,
    
    @SerializedName("country")
    val country: String?,
    
    @SerializedName("stats")
    val stats: ProfileStats?,
    
    // Legacy fields for backward compatibility with /user/whoami
    @SerializedName("userid")
    val userId: String?,
    
    @SerializedName("username")
    val username: String?,
    
    @SerializedName("usericon")
    val userIcon: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("details")
    val details: UserDetails?
) {
    // Helper properties to access user data regardless of structure
    val actualUserId: String?
        get() = userId ?: user?.userId
    
    val actualUsername: String?
        get() = username ?: user?.username
    
    val actualUserIcon: String?
        get() = userIcon ?: user?.userIcon
    
    val actualType: String?
        get() = type ?: user?.type
    
    val actualTagline: String?
        get() = tagline ?: details?.tagline
    
    val actualCountry: String?
        get() = country ?: details?.country
}

data class User(
    @SerializedName("userid")
    val userId: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("usericon")
    val userIcon: String,
    
    @SerializedName("type")
    val type: String
)

data class ProfileStats(
    @SerializedName("user_deviations")
    val userDeviations: Int,
    
    @SerializedName("user_favourites")
    val userFavourites: Int,
    
    @SerializedName("user_comments")
    val userComments: Int,
    
    @SerializedName("profile_pageviews")
    val profilePageviews: Int,
    
    @SerializedName("profile_comments")
    val profileComments: Int,
    
    // Legacy watchers/friends (not provided by /user/profile endpoint)
    @SerializedName("watchers")
    val watchers: Int = 0,
    
    @SerializedName("friends")
    val friends: Int = 0
)

data class UserDetails(
    @SerializedName("tagline")
    val tagline: String?,
    
    @SerializedName("countryid")
    val countryId: Int?,
    
    @SerializedName("country")
    val country: String?
)
