package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

// Response for GET /comments/deviation/{deviationid}
data class CommentsResponse(
    @SerializedName("thread")
    val thread: List<Comment>?,

    @SerializedName("has_more")
    val hasMore: Boolean,

    @SerializedName("next_offset")
    val nextOffset: Int?,

    @SerializedName("total")
    val total: Int?
)

data class Comment(
    @SerializedName("commentid")
    val commentId: String,

    @SerializedName("parentid")
    val parentId: String?,

    @SerializedName("posted")
    val posted: String,

    @SerializedName("text")
    val text: String?,

    @SerializedName("body")
    val body: String?,

    @SerializedName("user")
    val user: CommentUser,

    @SerializedName("is_featured")
    val isFeatured: Boolean = false,

    @SerializedName("is_liked")
    val isLiked: Boolean = false,

    @SerializedName("likes")
    val likes: Int = 0,

    @SerializedName("replies")
    val replies: Int = 0,

    @SerializedName("hidden")
    val hidden: String? = null,

    // For nested replies
    var repliesList: List<Comment>? = null,
    var isExpanded: Boolean = false,
    var isLoadingReplies: Boolean = false
)

data class CommentUser(
    @SerializedName("userid")
    val userId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("usericon")
    val userIcon: String,

    @SerializedName("type")
    val type: String
)

// Response for GET /comments/{commentid}/siblings
data class CommentSiblingsResponse(
    @SerializedName("thread")
    val thread: List<Comment>?,

    @SerializedName("has_more")
    val hasMore: Boolean,

    @SerializedName("next_offset")
    val nextOffset: Int?
)

// Request for POST /comments/post/deviation/{deviationid}
data class PostCommentRequest(
    @SerializedName("body")
    val body: String,

    @SerializedName("commentid")
    val commentId: String? = null  // For replies
)

// Response for POST /comments/post/deviation/{deviationid}
data class PostCommentResponse(
    @SerializedName("commentid")
    val commentId: String,

    @SerializedName("posted")
    val posted: String,

    @SerializedName("text")
    val text: String
)

