package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

data class MessagesResponse(
    @SerializedName("has_more")
    val hasMore: Boolean,
    
    @SerializedName("cursor")
    val cursor: String?,
    
    @SerializedName("results")
    val results: List<Message>?
)

data class FeedbackMessagesResponse(
    @SerializedName("has_more")
    val hasMore: Boolean,

    @SerializedName("next_offset")
    val nextOffset: Int?,

    @SerializedName("results")
    val results: List<Message>?
)

data class Message(
    @SerializedName("messageid")
    val messageId: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("subject")
    val subject: MessageSubject?,
    
    @SerializedName("preview")
    val preview: String?,
    
    @SerializedName("ts")
    val timestamp: String,
    
    @SerializedName("is_new")
    val isNew: Boolean,
    
    @SerializedName("originator")
    val originator: Author?,

    @SerializedName("collection")
    val collection: MessageCollection?
)

data class MessageSubject(
    @SerializedName("profile")
    val profile: Author?,

    @SerializedName("deviation")
    val deviation: MessageDeviation?,

    @SerializedName("comment")
    val comment: MessageComment?,

    @SerializedName("title")
    val title: String?
)

data class MessageComment(
    @SerializedName("commentid")
    val commentId: String?,

    @SerializedName("body")
    val body: String?
)

data class MessageCollection(
    @SerializedName("folderid")
    val folderId: String?,

    @SerializedName("name")
    val name: String?
)

data class MessageDeviation(
    @SerializedName("deviationid")
    val deviationId: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("url")
    val url: String?,

    @SerializedName("thumbs")
    val thumbs: List<Preview>?,

    @SerializedName("text_content")
    val textContent: MessageTextContent?
)

data class MessageTextContent(
    @SerializedName("excerpt")
    val excerpt: String?
)
