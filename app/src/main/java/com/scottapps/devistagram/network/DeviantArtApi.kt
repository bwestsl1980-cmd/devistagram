package com.scottapps.devistagram.network

import com.scottapps.devistagram.model.DailyDeviationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DeviantArtApi {

    @GET("browse/{browseType}")
    suspend fun getBrowseContent(
        @retrofit2.http.Path("browseType") browseType: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean? = null
    ): Response<DailyDeviationsResponse>

    @GET("browse/tags")
    suspend fun browseByTag(
        @Header("Authorization") authorization: String,
        @Query("tag") tag: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean? = null
    ): Response<DailyDeviationsResponse>

    @GET("messages/feed")
    suspend fun getMessagesFeed(
        @Header("Authorization") authorization: String,
        @Query("cursor") cursor: String? = null,
        @Query("stack") stack: Boolean = true
    ): Response<com.scottapps.devistagram.model.MessagesResponse>

    @GET("user/profile/{username}")
    suspend fun getUserProfile(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String
    ): Response<com.scottapps.devistagram.model.UserProfile>

    @GET("user/watchers/{username}")
    suspend fun getUserWatchers(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.scottapps.devistagram.model.WatchersResponse>

    @GET("user/friends/{username}")
    suspend fun getUserFriends(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.scottapps.devistagram.model.FriendsResponse>

    @GET("gallery/all")
    suspend fun getUserGallery(
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<DailyDeviationsResponse>

    @GET("user/whoami")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<com.scottapps.devistagram.model.UserProfile>
}