package com.bethwestsl.devistagram.repository

import android.content.Context
import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviantArtRepository(context: Context) {
    
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.contentApi
    
    suspend fun getBrowseContent(
        browseType: String,
        offset: Int? = null
    ): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))
            
            Log.d("DeviantArtRepo", "Fetching $browseType with offset: $offset")
            
            val response = api.getBrowseContent(
                browseType = browseType,
                authorization = "Bearer $accessToken",
                offset = offset,
                limit = 24,
                matureContent = true
            )
            
            Log.d("DeviantArtRepo", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val deviations = response.body()!!.results
                    ?.filter { deviation ->
                        // Only include deviations that have at least one image
                        deviation.content?.src != null || 
                        deviation.preview?.src != null || 
                        deviation.thumbs?.firstOrNull()?.src != null
                    }
                    ?: emptyList()
                Log.d("DeviantArtRepo", "Loaded ${deviations.size} deviations")
                Result.success(deviations)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DeviantArtRepo", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch deviations: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("DeviantArtRepo", "Exception fetching deviations", e)
            Result.failure(e)
        }
    }
    
    suspend fun browseByTag(
        tag: String,
        offset: Int? = null
    ): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))
            
            Log.d("DeviantArtRepo", "Browsing tag: $tag with offset: $offset")
            
            val response = api.browseByTag(
                authorization = "Bearer $accessToken",
                tag = tag,
                offset = offset,
                limit = 24,
                matureContent = true
            )
            
            Log.d("DeviantArtRepo", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val deviations = response.body()!!.results
                    ?.filter { deviation ->
                        deviation.content?.src != null || 
                        deviation.preview?.src != null || 
                        deviation.thumbs?.firstOrNull()?.src != null
                    }
                    ?: emptyList()
                Log.d("DeviantArtRepo", "Loaded ${deviations.size} deviations for tag: $tag")
                Result.success(deviations)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DeviantArtRepo", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch deviations: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("DeviantArtRepo", "Exception fetching tag deviations", e)
            Result.failure(e)
        }
    }

    suspend fun searchUsers(
        query: String
    ): Result<List<com.bethwestsl.devistagram.model.SearchUser>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("DeviantArtRepo", "Searching users: $query")

            val response = api.searchUsers(
                authorization = "Bearer $accessToken",
                query = query
            )

            Log.d("DeviantArtRepo", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!.results ?: emptyList()
                Log.d("DeviantArtRepo", "Found ${users.size} users for query: $query")
                Result.success(users)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DeviantArtRepo", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to search users: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("DeviantArtRepo", "Exception searching users", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches a feed of deviations from favorited users.
     * For each favorited username:
     * 1. Gets their profile
     * 2. Gets their gallery folders
     * 3. Fetches the 4 most recent deviations from their main gallery
     * 4. Combines and sorts all deviations by timestamp
     */
    suspend fun getFavoritesFeed(
        favoriteUsernames: Set<String>
    ): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            if (favoriteUsernames.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            Log.d("DeviantArtRepo", "Fetching feed for ${favoriteUsernames.size} favorited users")

            val allDeviations = mutableListOf<Deviation>()

            // Fetch deviations for each favorited user
            favoriteUsernames.forEach { username ->
                try {
                    Log.d("DeviantArtRepo", "Fetching gallery for: $username")

                    // Use getUserGallery to get the latest deviations (limit 4)
                    val galleryResponse = api.getUserGallery(
                        authorization = "Bearer $accessToken",
                        username = username,
                        offset = null,
                        limit = 4,
                        matureContent = true
                    )

                    if (galleryResponse.isSuccessful && galleryResponse.body() != null) {
                        val deviations = galleryResponse.body()!!.results
                            ?.filter { deviation ->
                                deviation.content?.src != null ||
                                deviation.preview?.src != null ||
                                deviation.thumbs?.firstOrNull()?.src != null
                            }
                            ?: emptyList()

                        allDeviations.addAll(deviations)
                        Log.d("DeviantArtRepo", "Added ${deviations.size} deviations from $username")
                    } else {
                        Log.w("DeviantArtRepo", "Failed to fetch gallery for $username: ${galleryResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("DeviantArtRepo", "Error fetching gallery for $username", e)
                    // Continue with next user even if one fails
                }
            }

            // Sort by published time (newest first)
            val sortedDeviations = allDeviations.sortedByDescending { deviation ->
                deviation.publishedTime ?: ""
            }

            Log.d("DeviantArtRepo", "Loaded ${sortedDeviations.size} total deviations from favorites")
            Result.success(sortedDeviations)

        } catch (e: Exception) {
            Log.e("DeviantArtRepo", "Exception fetching favorites feed", e)
            Result.failure(e)
        }
    }
}
