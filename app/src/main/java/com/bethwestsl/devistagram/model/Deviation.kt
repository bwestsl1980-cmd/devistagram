package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

data class DailyDeviationsResponse(
    @SerializedName("results")
    val results: List<Deviation>?,  // Results are directly Deviation objects
    
    @SerializedName("has_more")
    val hasMore: Boolean,
    
    @SerializedName("next_offset")
    val nextOffset: Int?
)

data class Deviation(
    @SerializedName("deviationid")
    val deviationId: String,
    
    @SerializedName("title")
    val title: String?,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("author")
    val author: Author,
    
    @SerializedName("stats")
    val stats: Stats?,
    
    @SerializedName("published_time")
    val publishedTime: String?,
    
    @SerializedName("allows_comments")
    val allowsComments: Boolean,
    
    @SerializedName("preview")
    val preview: Preview?,
    
    @SerializedName("content")
    val content: Content?,
    
    @SerializedName("thumbs")
    val thumbs: List<Thumbnail>?,
    
    @SerializedName("category")
    val category: String?,
    
    @SerializedName("category_path")
    val categoryPath: String?,
    
    @SerializedName("is_favourited")
    val isFavourited: Boolean,
    
    @SerializedName("is_deleted")
    val isDeleted: Boolean,
    
    @SerializedName("is_mature")
    val isMature: Boolean = false,
    
    @SerializedName("excerpt")
    val excerpt: String?,
    
    @SerializedName("description")
    val description: String?
)

data class Author(
    @SerializedName("userid")
    val userId: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("usericon")
    val userIcon: String,
    
    @SerializedName("type")
    val type: String
)

data class Stats(
    @SerializedName("comments")
    val comments: Int,
    
    @SerializedName("favourites")
    val favourites: Int,
    
    @SerializedName("views")
    val views: Int?
)

data class Preview(
    @SerializedName("src")
    val src: String,
    
    @SerializedName("height")
    val height: Int,
    
    @SerializedName("width")
    val width: Int,
    
    @SerializedName("transparency")
    val transparency: Boolean
)

data class Content(
    @SerializedName("src")
    val src: String,
    
    @SerializedName("height")
    val height: Int,
    
    @SerializedName("width")
    val width: Int,
    
    @SerializedName("transparency")
    val transparency: Boolean,
    
    @SerializedName("filesize")
    val filesize: Int
)

data class Thumbnail(
    @SerializedName("src")
    val src: String,
    
    @SerializedName("height")
    val height: Int,
    
    @SerializedName("width")
    val width: Int,
    
    @SerializedName("transparency")
    val transparency: Boolean
)
