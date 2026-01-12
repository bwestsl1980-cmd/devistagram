package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

// The API returns the deviation directly as the response
data class DeviationDetailResponse(
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

    @SerializedName("is_mature")
    val isMature: Boolean,

    @SerializedName("is_deleted")
    val isDeleted: Boolean,

    @SerializedName("excerpt")
    val excerpt: String?,

    @SerializedName("description")
    val description: String?
) {
    // Convert to Deviation model
    fun toDeviation(): Deviation {
        return Deviation(
            deviationId = deviationId,
            title = title,
            url = url,
            author = author,
            stats = stats,
            publishedTime = publishedTime,
            allowsComments = allowsComments,
            preview = preview,
            content = content,
            thumbs = thumbs,
            category = category,
            categoryPath = categoryPath,
            isFavourited = isFavourited,
            isMature = isMature,
            isDeleted = isDeleted,
            excerpt = excerpt,
            description = description
        )
    }
}
