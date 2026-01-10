package com.scottapps.devistagram.repository

import android.content.Context
import android.util.Log
import com.scottapps.devistagram.auth.TokenManager
import com.scottapps.devistagram.model.Deviation
import com.scottapps.devistagram.model.UserProfile
import com.scottapps.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.contentApi

    suspend fun getCurrentUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching current user profile")

            // First get the current user to get their username
            val whoamiResponse = api.getCurrentUser(
                authorization = "Bearer $accessToken"
            )

            Log.d("ProfileRepo", "Whoami Response code: ${whoamiResponse.code()}")

            if (whoamiResponse.isSuccessful && whoamiResponse.body() != null) {
                val basicProfile = whoamiResponse.body()!!
                val username = basicProfile.actualUsername

                if (username == null) {
                    Log.e("ProfileRepo", "No username found in whoami response")
                    return@withContext Result.failure(Exception("No username found"))
                }

                Log.d("ProfileRepo", "Got username: $username, now fetching full profile")

                // Now fetch the full profile with stats
                val response = api.getUserProfile(
                    username = username,
                    authorization = "Bearer $accessToken"
                )

                Log.d("ProfileRepo", "Profile Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    Log.d("ProfileRepo", "Loaded profile for ${profile.actualUsername}")
                    Log.d("ProfileRepo", "Stats: ${profile.stats}")
                    Result.success(profile)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileRepo", "Profile API Error: ${response.code()} - $errorBody")
                    Result.failure(Exception("Failed to fetch profile: ${response.code()} - $errorBody"))
                }
            } else {
                val errorBody = whoamiResponse.errorBody()?.string()
                Log.e("ProfileRepo", "Whoami API Error: ${whoamiResponse.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch user info: ${whoamiResponse.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching profile", e)
            Result.failure(e)
        }
    }

    suspend fun getUserWatchersCount(username: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching watchers for: $username")

            val response = api.getUserWatchers(
                username = username,
                authorization = "Bearer $accessToken",
                offset = null,
                limit = 50
            )

            Log.d("ProfileRepo", "Watchers Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val watchersResponse = response.body()!!
                val count = watchersResponse.results?.size ?: 0
                Log.d("ProfileRepo", "Watchers count: $count")
                Result.success(count)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Watchers API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch watchers: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching watchers", e)
            Result.failure(e)
        }
    }

    suspend fun getUserFriendsCount(username: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching friends for: $username")

            val response = api.getUserFriends(
                username = username,
                authorization = "Bearer $accessToken",
                offset = null,
                limit = 50
            )

            Log.d("ProfileRepo", "Friends Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val friendsResponse = response.body()!!
                val count = friendsResponse.results?.size ?: 0
                Log.d("ProfileRepo", "Friends count: $count")
                Result.success(count)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Friends API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch friends: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching friends", e)
            Result.failure(e)
        }
    }

    suspend fun getUserDeviations(username: String): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching deviations for: $username")

            val response = api.getUserGallery(
                authorization = "Bearer $accessToken",
                username = username,
                offset = null,
                limit = 24,
                matureContent = true
            )

            Log.d("ProfileRepo", "Deviations Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val deviations = response.body()!!.results
                    ?.filter { deviation ->
                        deviation.content?.src != null ||
                                deviation.preview?.src != null ||
                                deviation.thumbs?.firstOrNull()?.src != null
                    }
                    ?: emptyList()
                Log.d("ProfileRepo", "Loaded ${deviations.size} deviations")
                Result.success(deviations)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Deviations API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch deviations: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching deviations", e)
            Result.failure(e)
        }
    }
}