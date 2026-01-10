package com.scottapps.devistagram.model

import com.google.gson.annotations.SerializedName

data class MessagesResponse(
    @SerializedName("has_more")
    val hasMore: Boolean,
    
    @SerializedName("cursor")
    val cursor: String?,
    
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
    val originator: Author?
)

data class MessageSubject(
    @SerializedName("profile")
    val profile: Author?
)
