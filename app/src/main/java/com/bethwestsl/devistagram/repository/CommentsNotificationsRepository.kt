package com.bethwestsl.devistagram.repository

import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.network.RetrofitClient
import android.content.Context

class CommentsNotificationsRepository(context: Context) {

    private val api = RetrofitClient.contentApi
    private val tokenManager = TokenManager(context)
    private var nextOffset: Int? = null
    private var hasMore = true

    suspend fun getCommentsNotifications(refresh: Boolean = false): Result<List<Message>> {
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

            Log.d("CommentsRepo", "Fetching comments/replies with offset: $nextOffset")

            // Fetch both comments and replies
            val commentsResponse = api.getMessagesFeedback(
                authorization = "Bearer $token",
                type = "comments",
                offset = nextOffset,
                limit = 50,
                matureContent = true
            )

            val repliesResponse = api.getMessagesFeedback(
                authorization = "Bearer $token",
                type = "replies",
                offset = nextOffset,
                limit = 50,
                matureContent = true
            )

            Log.d("CommentsRepo", "Comments response code: ${commentsResponse.code()}, Replies response code: ${repliesResponse.code()}")

            if (commentsResponse.isSuccessful || repliesResponse.isSuccessful) {
                val commentsBody = commentsResponse.body()
                val repliesBody = repliesResponse.body()

                // Combine both results
                val allNotifications = mutableListOf<Message>()
                allNotifications.addAll(commentsBody?.results ?: emptyList())
                allNotifications.addAll(repliesBody?.results ?: emptyList())

                // Sort by timestamp (most recent first)
                allNotifications.sortByDescending { it.timestamp }

                // Update pagination (use the one with more results)
                nextOffset = commentsBody?.nextOffset ?: repliesBody?.nextOffset
                hasMore = (commentsBody?.hasMore == true) || (repliesBody?.hasMore == true)

                Log.d("CommentsRepo", "Loaded ${allNotifications.size} notifications (${commentsBody?.results?.size ?: 0} comments, ${repliesBody?.results?.size ?: 0} replies), has_more: $hasMore")
                Result.success(allNotifications)
            } else {
                val errorBody = commentsResponse.errorBody()?.string() ?: repliesResponse.errorBody()?.string()
                Log.e("CommentsRepo", "API Error: ${commentsResponse.code()} - $errorBody")
                Result.failure(Exception("Failed to load comments: ${commentsResponse.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("CommentsRepo", "Exception loading comments", e)
            Result.failure(e)
        }
    }

    fun resetPagination() {
        nextOffset = null
        hasMore = true
    }
}

