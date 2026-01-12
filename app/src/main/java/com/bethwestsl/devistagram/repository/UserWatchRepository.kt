package com.bethwestsl.devistagram.repository

import android.util.Log
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserWatchRepository {
    private val api = RetrofitClient.contentApi

    /**
     * Check if the current user is watching a specific username
     */
    suspend fun isWatchingUser(username: String, accessToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserWatchRepo", "Checking if watching user: $username")
            val response = api.getWatchingStatus(
                username = username,
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val isWatching = response.body()!!.watching
                Log.d("UserWatchRepo", "Is watching $username: $isWatching")
                Result.success(isWatching)
            } else {
                Log.e("UserWatchRepo", "Failed to check watching status: ${response.code()}")
                Result.failure(Exception("Failed to check watching status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserWatchRepo", "Exception checking watching status", e)
            Result.failure(e)
        }
    }

    /**
     * Watch a user
     */
    suspend fun watchUser(username: String, accessToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserWatchRepo", "Watching user: $username")
            val response = api.watchUser(
                username = username,
                authorization = "Bearer $accessToken",
                watch = mapOf("watch[friend]" to true, "watch[deviations]" to true)
            )

            if (response.isSuccessful && response.body() != null) {
                val success = response.body()!!.success
                Log.d("UserWatchRepo", "Watch user $username success: $success")
                Result.success(success)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserWatchRepo", "Failed to watch user: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to watch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserWatchRepo", "Exception watching user", e)
            Result.failure(e)
        }
    }

    /**
     * Unwatch a user
     */
    suspend fun unwatchUser(username: String, accessToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserWatchRepo", "Unwatching user: $username")
            val response = api.unwatchUser(
                username = username,
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val success = response.body()!!.success
                Log.d("UserWatchRepo", "Unwatch user $username success: $success")
                Result.success(success)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserWatchRepo", "Failed to unwatch user: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to unwatch user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserWatchRepo", "Exception unwatching user", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle watch status for a user
     */
    suspend fun toggleWatchUser(username: String, accessToken: String, currentlyWatching: Boolean): Result<Boolean> {
        return if (currentlyWatching) {
            unwatchUser(username, accessToken)
        } else {
            watchUser(username, accessToken)
        }
    }
}

