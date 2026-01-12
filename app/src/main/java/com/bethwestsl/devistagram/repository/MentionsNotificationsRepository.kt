package com.bethwestsl.devistagram.repository

import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.network.RetrofitClient
import android.content.Context

class MentionsNotificationsRepository(context: Context) {

    private val api = RetrofitClient.contentApi
    private val tokenManager = TokenManager(context)
    private var nextOffset: Int? = null
    private var hasMore = true

    suspend fun getMentionsNotifications(refresh: Boolean = false): Result<List<Message>> {
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

            Log.d("MentionsRepo", "Fetching mentions with offset: $nextOffset")

            val response = api.getMessagesMentions(
                authorization = "Bearer $token",
                offset = nextOffset,
                limit = 50,
                matureContent = true
            )

            Log.d("MentionsRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                nextOffset = body?.nextOffset
                hasMore = body?.hasMore ?: false
                val results = body?.results ?: emptyList()

                Log.d("MentionsRepo", "Loaded ${results.size} mentions, has_more: $hasMore, next_offset: $nextOffset")
                Result.success(results)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("MentionsRepo", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to load mentions: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("MentionsRepo", "Exception loading mentions", e)
            Result.failure(e)
        }
    }

    fun resetPagination() {
        nextOffset = null
        hasMore = true
    }
}

