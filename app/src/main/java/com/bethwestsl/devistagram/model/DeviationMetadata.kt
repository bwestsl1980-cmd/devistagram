package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

data class DeviationMetadataResponse(
    @SerializedName("metadata")
    val metadata: List<DeviationMetadata>?
)

data class DeviationMetadata(
    @SerializedName("deviationid")
    val deviationId: String,

    @SerializedName("title")
    val title: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("tags")
    val tags: List<Tag>?,

    @SerializedName("is_mature")
    val isMature: Boolean,

    @SerializedName("mature_level")
    val matureLevel: String?,

    @SerializedName("license")
    val license: String?
)

data class Tag(
    @SerializedName("tag_name")
    val tagName: String,

    @SerializedName("sponsored")
    val sponsored: Boolean
)

