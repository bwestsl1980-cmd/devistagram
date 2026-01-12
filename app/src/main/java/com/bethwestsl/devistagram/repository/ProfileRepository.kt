package com.bethwestsl.devistagram.repository

import android.content.Context
import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.model.GalleryFolder
import com.bethwestsl.devistagram.model.UserProfile
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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

    suspend fun getUserProfile(username: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching profile for user: $username")

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
    
    suspend fun getGalleryFolders(username: String): Result<List<GalleryFolder>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching gallery folders for: $username")

            val response = api.getGalleryFolders(
                authorization = "Bearer $accessToken",
                username = username,
                calculateSize = true,
                offset = null,
                limit = 50
            )

            Log.d("ProfileRepo", "Gallery folders Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val folders = response.body()!!.results ?: emptyList()
                Log.d("ProfileRepo", "Loaded ${folders.size} gallery folders")
                Result.success(folders)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Gallery folders API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch gallery folders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching gallery folders", e)
            Result.failure(e)
        }
    }
    
    suspend fun getGalleryFolderDeviations(username: String, folderId: String): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching gallery folder $folderId for: $username")

            val response = api.getGalleryFolder(
                folderId = folderId,
                authorization = "Bearer $accessToken",
                username = username,
                offset = null,
                limit = 24,
                matureContent = true
            )

            Log.d("ProfileRepo", "Gallery folder Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val deviations = response.body()!!.results
                    ?.filter { deviation ->
                        deviation.content?.src != null ||
                                deviation.preview?.src != null ||
                                deviation.thumbs?.firstOrNull()?.src != null
                    }
                    ?: emptyList()
                Log.d("ProfileRepo", "Loaded ${deviations.size} deviations from folder")
                Result.success(deviations)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Gallery folder API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch gallery folder: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching gallery folder", e)
            Result.failure(e)
        }
    }
    
    /**
     * Scrapes the profile HTML page to get actual watcher/watching counts
     * Returns Pair(watchers, watching)
     */
    suspend fun scrapeProfileCounts(username: String): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProfileRepo", "Scraping profile page for: $username")
            
            var watchersCount = 0
            var watchingCount = 0
            
            // Scrape watchers count from main profile page
            val profileUrl = "https://www.deviantart.com/$username"
            val profileDoc: Document = Jsoup.connect(profileUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            // DeviantArt structure: <div class="mTBhrk">8.3K</div> followed by text "watchers"
            profileDoc.select("span").forEach { span ->
                val spanText = span.text().lowercase()
                
                if (spanText.contains("watchers")) {
                    // Look for a div with class mTBhrk before this span
                    val countDiv = span.previousElementSibling()?.select("div.mTBhrk")?.first()
                        ?: span.parent()?.select("div.mTBhrk")?.first()
                    
                    countDiv?.let {
                        val countText = it.text()
                        watchersCount = parseCountFromText(countText)
                        Log.d("ProfileRepo", "Found watchers: $countText -> $watchersCount")
                    }
                }
            }
            
            // Scrape watching count from about page
            val aboutUrl = "https://www.deviantart.com/$username/about"
            val aboutDoc: Document = Jsoup.connect(aboutUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            // Find div with class nGC9Z7 that contains the watching count
            aboutDoc.select("div.nGC9Z7").forEach { div ->
                val divText = div.text()
                // Check if this div is near text that says "watching"
                val parent = div.parent()
                val parentText = parent?.text()?.lowercase() ?: ""
                
                if (parentText.contains("watching")) {
                    watchingCount = parseCountFromText(divText)
                    Log.d("ProfileRepo", "Found watching: $divText -> $watchingCount")
                }
            }
            
            Log.d("ProfileRepo", "Final scraped counts - Watchers: $watchersCount, Watching: $watchingCount")
            
            Result.success(Pair(watchersCount, watchingCount))
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception scraping profile", e)
            Result.failure(e)
        }
    }
    
    private fun parseCountFromText(text: String): Int {
        return try {
            // Extract first number from text
            val numberPattern = """([0-9,.]+[KMk]?)""".toRegex()
            val match = numberPattern.find(text)
            
            if (match != null) {
                val numStr = match.groupValues[1].replace(",", "")
                when {
                    numStr.endsWith("K", ignoreCase = true) -> {
                        (numStr.dropLast(1).toFloat() * 1000).toInt()
                    }
                    numStr.endsWith("M", ignoreCase = true) -> {
                        (numStr.dropLast(1).toFloat() * 1000000).toInt()
                    }
                    else -> numStr.toInt()
                }
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error parsing count from: $text", e)
            0
        }
    }

    suspend fun getCollectionFolders(username: String): Result<List<GalleryFolder>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching collection folders for: $username")

            // Create an "All" folder first
            val allFolder = GalleryFolder(
                folderId = "all",
                name = "All",
                parent = null,
                size = null,
                thumb = null
            )

            val response = api.getCollectionFolders(
                authorization = "Bearer $accessToken",
                username = username
            )

            Log.d("ProfileRepo", "Collection folders Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val foldersResponse = response.body()!!
                val folders = foldersResponse.results ?: emptyList()
                
                // Put "All" at the beginning
                val allFolders = listOf(allFolder) + folders
                
                Log.d("ProfileRepo", "Loaded ${allFolders.size} collection folders (including All)")
                Result.success(allFolders)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Collection folders API Error: ${response.code()} - $errorBody")
                // Return just the "All" folder if API fails
                Result.success(listOf(allFolder))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching collection folders", e)
            Result.failure(e)
        }
    }

    suspend fun getCollectionFolderDeviations(username: String, folderId: String): Result<List<Deviation>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("ProfileRepo", "Fetching collection folder deviations for: $username, folder: $folderId")

            val response = if (folderId == "all") {
                // Use /collections/all endpoint
                api.getAllCollections(
                    authorization = "Bearer $accessToken",
                    username = username,
                    matureContent = true
                )
            } else {
                // Use /collections/{folderid} endpoint
                api.getCollectionFolder(
                    folderId = folderId,
                    authorization = "Bearer $accessToken",
                    username = username,
                    matureContent = true
                )
            }

            Log.d("ProfileRepo", "Collection folder Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val deviationsResponse = response.body()!!
                val deviations = deviationsResponse.results ?: emptyList()
                Log.d("ProfileRepo", "Loaded ${deviations.size} deviations from collection")
                Result.success(deviations)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ProfileRepo", "Collection folder API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch collection: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Exception fetching collection folder", e)
            Result.failure(e)
        }
    }

    // Convenience method aliases for ViewModel
    suspend fun getGalleryFolderContent(username: String, folderId: String): Result<List<Deviation>> {
        return getGalleryFolderDeviations(username, folderId)
    }

    suspend fun getCollectionFolderContent(username: String, folderId: String): Result<List<Deviation>> {
        return getCollectionFolderDeviations(username, folderId)
    }
}
