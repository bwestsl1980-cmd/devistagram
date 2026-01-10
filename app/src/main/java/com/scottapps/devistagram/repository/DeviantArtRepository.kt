package com.scottapps.devistagram.repository

import android.content.Context
import android.util.Log
import com.scottapps.devistagram.auth.TokenManager
import com.scottapps.devistagram.model.Deviation
import com.scottapps.devistagram.network.RetrofitClient
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
}
