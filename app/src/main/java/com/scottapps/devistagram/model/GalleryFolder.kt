package com.scottapps.devistagram.model

import com.google.gson.annotations.SerializedName

data class GalleryFoldersResponse(
    @SerializedName("has_more")
    val hasMore: Boolean?,
    
    @SerializedName("next_offset")
    val nextOffset: Int?,
    
    @SerializedName("results")
    val results: List<GalleryFolder>?
)

data class GalleryFolder(
    @SerializedName("folderid")
    val folderId: String?,
    
    @SerializedName("parent")
    val parent: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("size")
    val size: Int?,
    
    @SerializedName("thumb")
    val thumb: Thumbnail?
)
