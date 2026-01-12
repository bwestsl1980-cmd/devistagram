package com.bethwestsl.devistagram.repository

import android.util.Log
import com.bethwestsl.devistagram.model.Comment
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentsRepository {
    private val api = RetrofitClient.contentApi

    /**
     * Fetch comments for a deviation
     */
    suspend fun getDeviationComments(
        deviationId: String,
        accessToken: String,
        offset: Int? = null,
        limit: Int = 50
    ): Result<List<Comment>> = withContext(Dispatchers.IO) {
        try {
            Log.d("CommentsRepo", "Fetching comments for deviation: $deviationId")
            val response = api.getDeviationComments(
                deviationId = deviationId,
                authorization = "Bearer $accessToken",
                offset = offset,
                limit = limit
            )

            if (response.isSuccessful && response.body() != null) {
                val comments = response.body()!!.thread ?: emptyList()
                Log.d("CommentsRepo", "Fetched ${comments.size} comments")
                comments.forEachIndexed { index, comment ->
                    Log.d("CommentsRepo", "Comment $index: id=${comment.commentId}, text='${comment.text}', user=${comment.user.username}, replies=${comment.replies}")
                }
                Result.success(comments)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CommentsRepo", "Failed to fetch comments: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch comments: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CommentsRepo", "Exception fetching comments", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch replies to a comment (siblings)
     */
    suspend fun getCommentReplies(
        commentId: String,
        accessToken: String,
        offset: Int? = null,
        limit: Int = 50
    ): Result<List<Comment>> = withContext(Dispatchers.IO) {
        try {
            Log.d("CommentsRepo", "Fetching replies for comment: $commentId")
            val response = api.getCommentSiblings(
                commentId = commentId,
                authorization = "Bearer $accessToken",
                offset = offset,
                limit = limit
            )

            if (response.isSuccessful && response.body() != null) {
                val replies = response.body()!!.thread ?: emptyList()
                Log.d("CommentsRepo", "Fetched ${replies.size} replies")
                Result.success(replies)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CommentsRepo", "Failed to fetch replies: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch replies: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CommentsRepo", "Exception fetching replies", e)
            Result.failure(e)
        }
    }

    /**
     * Post a comment on a deviation
     */
    suspend fun postComment(
        deviationId: String,
        commentText: String,
        accessToken: String,
        parentCommentId: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("CommentsRepo", "Posting comment on deviation: $deviationId")
            Log.d("CommentsRepo", "Comment text: '$commentText', parent: $parentCommentId")

            val response = api.postCommentOnDeviation(
                deviationId = deviationId,
                authorization = "Bearer $accessToken",
                body = commentText,
                commentId = parentCommentId
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d("CommentsRepo", "Comment posted successfully")
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CommentsRepo", "Failed to post comment: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to post comment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CommentsRepo", "Exception posting comment", e)
            Result.failure(e)
        }
    }
}

