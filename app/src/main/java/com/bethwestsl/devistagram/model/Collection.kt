package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

// Response for GET /collections/folders
data class CollectionFoldersResponse(
    @SerializedName("results")
    val results: List<CollectionFolder>?,

    @SerializedName("has_more")
    val hasMore: Boolean,

    @SerializedName("next_offset")
    val nextOffset: Int?
)

data class CollectionFolder(
    @SerializedName("folderid")
    val folderId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("size")
    val size: Int = 0,

    @SerializedName("thumb")
    val thumb: Thumbnail? = null
)

// Request for POST /collections/fave
data class FaveDeviationRequest(
    @SerializedName("deviationid")
    val deviationId: String,

    @SerializedName("folderid")
    val folderId: String? = null
)

// Response for POST /collections/fave
data class FaveDeviationResponse(
    @SerializedName("success")
    val success: Boolean
)

// Response for POST /collections/unfave
data class UnfaveDeviationResponse(
    @SerializedName("success")
    val success: Boolean
)

// Request for POST /collections/folders/create
data class CreateCollectionFolderRequest(
    @SerializedName("folder")
    val folder: String
)

// Response for POST /collections/folders/create
data class CreateCollectionFolderResponse(
    @SerializedName("folderid")
    val folderId: String,

    @SerializedName("name")
    val name: String
)

