package com.bethwestsl.devistagram.repository

import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.network.RetrofitClient
import android.content.Context

class FeedbackNotificationsRepository(context: Context) {

    private val api = RetrofitClient.contentApi
    private val tokenManager = TokenManager(context)
    private var nextOffset: Int? = null
    private var hasMore = true

    suspend fun getFeedbackNotifications(refresh: Boolean = false): Result<List<Message>> {
        return try {
            if (refresh) {
                nextOffset = null
                hasMore = true
            }

            if (!hasMore) {
                return Result.success(emptyList())
            }

            val token = tokenManager.getAccessToken() ?: return Result.failure(
                Exception("Not authenticated")
            )

            Log.d("FeedbackRepo", "Fetching feedback with offset: $nextOffset")

            val response = api.getMessagesFeedback(
                authorization = "Bearer $token",
                type = "activity",
                offset = nextOffset,
                limit = 50,
                matureContent = true
            )

            Log.d("FeedbackRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                nextOffset = body?.nextOffset
                hasMore = body?.hasMore ?: false
                val results = body?.results ?: emptyList()

                Log.d("FeedbackRepo", "Loaded ${results.size} activity notifications, has_more: $hasMore, next_offset: $nextOffset")
                Result.success(results)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FeedbackRepo", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to load feedback: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FeedbackRepo", "Exception loading feedback", e)
            Result.failure(e)
        }
    }

    fun resetPagination() {
        nextOffset = null
        hasMore = true
    }
}

