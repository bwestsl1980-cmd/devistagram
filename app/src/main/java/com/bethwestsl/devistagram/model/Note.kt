package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

data class Note(
    @SerializedName("noteid")
    val noteId: String?,

    @SerializedName("ts")
    val timestamp: String?,

    @SerializedName("subject")
    val subject: String?,

    @SerializedName("body")
    val body: String?,

    @SerializedName("html")
    val html: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("originator")
    val originator: User?,

    @SerializedName("is_read")
    val isRead: Boolean?,

    @SerializedName("folderid")
    val folderId: String?,

    @SerializedName("stack_count")
    val stackCount: Int?
)

data class NotesResponse(
    @SerializedName("results")
    val results: List<Note>?,

    @SerializedName("has_more")
    val hasMore: Boolean?,

    @SerializedName("next_offset")
    val nextOffset: Int?
)

data class NoteFolder(
    @SerializedName("folderid")
    val folderId: String?,

    @SerializedName("folder")
    val folderName: String?,

    @SerializedName("size")
    val size: Int?,

    @SerializedName("has_subfolders")
    val hasSubfolders: Boolean?
)

data class NoteFoldersResponse(
    @SerializedName("results")
    val results: List<NoteFolder>?,

    @SerializedName("has_more")
    val hasMore: Boolean?
)

data class DeleteNoteResponse(
    @SerializedName("success")
    val success: Boolean?
)

data class CreateFolderResponse(
    @SerializedName("folderid")
    val folderId: String?,

    @SerializedName("folder")
    val folderName: String?
)

data class MoveNotesResponse(
    @SerializedName("success")
    val success: Boolean?
)

data class SendNoteResponse(
    @SerializedName("success")
    val success: Boolean?
)

