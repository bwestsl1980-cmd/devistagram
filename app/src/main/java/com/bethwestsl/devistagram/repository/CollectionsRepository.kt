package com.bethwestsl.devistagram.repository

import android.util.Log
import com.bethwestsl.devistagram.model.CollectionFolder
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectionsRepository {
    private val api = RetrofitClient.contentApi

    /**
     * Get user's collection folders
     */
    suspend fun getCollectionFolders(
        accessToken: String,
        username: String? = null
    ): Result<List<CollectionFolder>> = withContext(Dispatchers.IO) {
        try {
            Log.d("CollectionsRepo", "Fetching collection folders")
            val response = api.getCollectionFolders(
                authorization = "Bearer $accessToken",
                username = username,
                calculateSize = true
            )

            if (response.isSuccessful && response.body() != null) {
                val folders = response.body()!!.results ?: emptyList()
                Log.d("CollectionsRepo", "Fetched ${folders.size} folders")
                Result.success(folders)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CollectionsRepo", "Failed to fetch folders: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch collection folders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CollectionsRepo", "Exception fetching folders", e)
            Result.failure(e)
        }
    }

    /**
     * Add a deviation to favorites/collection
     */
    suspend fun faveDeviation(
        deviationId: String,
        accessToken: String,
        folderId: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("CollectionsRepo", "Favoriting deviation: '$deviationId' (length: ${deviationId.length}) to folder: $folderId")
            Log.d("CollectionsRepo", "DeviationId isEmpty: ${deviationId.isEmpty()}, isBlank: ${deviationId.isBlank()}")
            val response = api.faveDeviation(
                authorization = "Bearer $accessToken",
                deviationId = deviationId,
                folderId = folderId
            )

            if (response.isSuccessful && response.body() != null) {
                val success = response.body()!!.success
                Log.d("CollectionsRepo", "Fave deviation success: $success")
                Result.success(success)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CollectionsRepo", "Failed to fave: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to favorite deviation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CollectionsRepo", "Exception favoriting deviation", e)
            Result.failure(e)
        }
    }

    /**
     * Remove a deviation from favorites
     */
    suspend fun unfaveDeviation(
        deviationId: String,
        accessToken: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("CollectionsRepo", "Unfavoriting deviation: $deviationId")
            val response = api.unfaveDeviation(
                authorization = "Bearer $accessToken",
                deviationId = deviationId
            )

            if (response.isSuccessful && response.body() != null) {
                val success = response.body()!!.success
                Log.d("CollectionsRepo", "Unfave deviation success: $success")
                Result.success(success)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CollectionsRepo", "Failed to unfave: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to unfavorite deviation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CollectionsRepo", "Exception unfavoriting deviation", e)
            Result.failure(e)
        }
    }

    /**
     * Create a new collection folder
     */
    suspend fun createCollectionFolder(
        folderName: String,
        accessToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("CollectionsRepo", "Creating collection folder: $folderName")
            val response = api.createCollectionFolder(
                authorization = "Bearer $accessToken",
                folderName = folderName
            )

            if (response.isSuccessful && response.body() != null) {
                val folderId = response.body()!!.folderId
                Log.d("CollectionsRepo", "Created folder with ID: $folderId")
                Result.success(folderId)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CollectionsRepo", "Failed to create folder: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to create collection folder: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CollectionsRepo", "Exception creating folder", e)
            Result.failure(e)
        }
    }
}

