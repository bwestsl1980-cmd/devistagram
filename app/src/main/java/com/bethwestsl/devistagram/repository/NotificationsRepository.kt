package com.bethwestsl.devistagram.repository

import android.content.Context
import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationsRepository(context: Context) {
    
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.contentApi
    
    suspend fun getMessagesFeed(cursor: String? = null): Result<Pair<List<Message>, String?>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))
            
            Log.d("NotificationsRepo", "Fetching messages with cursor: $cursor")
            
            val response = api.getMessagesFeed(
                authorization = "Bearer $accessToken",
                cursor = cursor,
                stack = false
            )
            
            Log.d("NotificationsRepo", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!.results ?: emptyList()
                val nextCursor = response.body()!!.cursor
                Log.d("NotificationsRepo", "Loaded ${messages.size} messages")
                Result.success(Pair(messages, nextCursor))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotificationsRepo", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch messages: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("NotificationsRepo", "Exception fetching messages", e)
            Result.failure(e)
        }
    }
}
