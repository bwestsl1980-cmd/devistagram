package com.bethwestsl.devistagram.network

import com.bethwestsl.devistagram.model.DailyDeviationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DeviantArtApi {

    @GET("browse/{browseType}")
    suspend fun getBrowseContent(
        @retrofit2.http.Path("browseType") browseType: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean? = null
    ): Response<DailyDeviationsResponse>

    @GET("browse/tags")
    suspend fun browseByTag(
        @Header("Authorization") authorization: String,
        @Query("tag") tag: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean? = null
    ): Response<DailyDeviationsResponse>

    @GET("messages/feed")
    suspend fun getMessagesFeed(
        @Header("Authorization") authorization: String,
        @Query("cursor") cursor: String? = null,
        @Query("stack") stack: Boolean = true
    ): Response<com.bethwestsl.devistagram.model.MessagesResponse>

    @GET("messages/feedback")
    suspend fun getMessagesFeedback(
        @Header("Authorization") authorization: String,
        @Query("type") type: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<com.bethwestsl.devistagram.model.FeedbackMessagesResponse>

    @GET("messages/mentions")
    suspend fun getMessagesMentions(
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<com.bethwestsl.devistagram.model.FeedbackMessagesResponse>

    @GET("user/profile/{username}")
    suspend fun getUserProfile(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.UserProfile>

    @GET("user/watchers/{username}")
    suspend fun getUserWatchers(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.WatchersResponse>

    @GET("user/friends/{username}")
    suspend fun getUserFriends(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.FriendsResponse>

    @GET("gallery/all")
    suspend fun getUserGallery(
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<DailyDeviationsResponse>

    @GET("user/whoami")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.UserProfile>

    @GET("gallery/folders")
    suspend fun getGalleryFolders(
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("calculate_size") calculateSize: Boolean = true,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.GalleryFoldersResponse>

    @GET("gallery/{folderid}")
    suspend fun getGalleryFolder(
        @retrofit2.http.Path("folderid") folderId: String,
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<DailyDeviationsResponse>

    @GET("collections/folders")
    suspend fun getCollectionFolders(
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.GalleryFoldersResponse>

    @GET("collections/{folderid}")
    suspend fun getCollectionFolder(
        @retrofit2.http.Path("folderid") folderId: String,
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<DailyDeviationsResponse>

    @GET("collections/all")
    suspend fun getAllCollections(
        @Header("Authorization") authorization: String,
        @Query("username") username: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean = true
    ): Response<DailyDeviationsResponse>

    @GET("deviation/{deviationid}")
    suspend fun getDeviation(
        @retrofit2.http.Path("deviationid") deviationId: String,
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.DeviationDetailResponse>

    @GET("user/friends/search")
    suspend fun searchUsers(
        @Header("Authorization") authorization: String,
        @Query("query") query: String
    ): Response<com.bethwestsl.devistagram.model.UserSearchResponse>

    @GET("deviation/metadata")
    suspend fun getDeviationMetadata(
        @Header("Authorization") authorization: String,
        @Query("deviationids[]") deviationIds: List<String>
    ): Response<com.bethwestsl.devistagram.model.DeviationMetadataResponse>

    @retrofit2.http.POST("user/friends/watch/{username}")
    suspend fun watchUser(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String,
        @retrofit2.http.Body watch: Map<String, Boolean>? = null
    ): Response<com.bethwestsl.devistagram.model.WatchUserResponse>

    @retrofit2.http.DELETE("user/friends/unwatch/{username}")
    suspend fun unwatchUser(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.UnwatchUserResponse>

    @GET("user/friends/watching/{username}")
    suspend fun getWatchingStatus(
        @retrofit2.http.Path("username") username: String,
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.WatchingStatusResponse>

    @GET("comments/deviation/{deviationid}")
    suspend fun getDeviationComments(
        @retrofit2.http.Path("deviationid") deviationId: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50,
        @Query("maxdepth") maxDepth: Int = 5
    ): Response<com.bethwestsl.devistagram.model.CommentsResponse>

    @GET("comments/{commentid}/siblings")
    suspend fun getCommentSiblings(
        @retrofit2.http.Path("commentid") commentId: String,
        @Header("Authorization") authorization: String,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.CommentSiblingsResponse>

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("comments/post/deviation/{deviationid}")
    suspend fun postCommentOnDeviation(
        @retrofit2.http.Path("deviationid") deviationId: String,
        @Header("Authorization") authorization: String,
        @retrofit2.http.Field("body") body: String,
        @retrofit2.http.Field("commentid") commentId: String? = null
    ): Response<com.bethwestsl.devistagram.model.PostCommentResponse>

    @GET("collections/folders")
    suspend fun getCollectionFolders(
        @Header("Authorization") authorization: String,
        @Query("username") username: String? = null,
        @Query("calculate_size") calculateSize: Boolean = true,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.CollectionFoldersResponse>

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("collections/fave")
    suspend fun faveDeviation(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Field("deviationid") deviationId: String,
        @retrofit2.http.Field("folderid") folderId: String? = null
    ): Response<com.bethwestsl.devistagram.model.FaveDeviationResponse>

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("collections/unfave")
    suspend fun unfaveDeviation(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Field("deviationid") deviationId: String
    ): Response<com.bethwestsl.devistagram.model.UnfaveDeviationResponse>

    @retrofit2.http.POST("collections/folders/create")
    suspend fun createCollectionFolder(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("folder") folderName: String
    ): Response<com.bethwestsl.devistagram.model.CreateCollectionFolderResponse>

    // Notes API endpoints
    @GET("notes")
    suspend fun getNotes(
        @Header("Authorization") authorization: String,
        @Query("folderid") folderId: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<com.bethwestsl.devistagram.model.NotesResponse>

    @GET("notes/{noteid}")
    suspend fun getNote(
        @retrofit2.http.Path("noteid") noteId: String,
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.Note>

    @retrofit2.http.POST("notes/delete")
    suspend fun deleteNotes(
        @Header("Authorization") authorization: String,
        @Query("noteids[]") noteIds: List<String>
    ): Response<com.bethwestsl.devistagram.model.DeleteNoteResponse>

    @GET("notes/folders")
    suspend fun getNoteFolders(
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.NoteFoldersResponse>

    @retrofit2.http.POST("notes/folders/create")
    suspend fun createNoteFolder(
        @Header("Authorization") authorization: String,
        @Query("folder") folderName: String
    ): Response<com.bethwestsl.devistagram.model.CreateFolderResponse>

    @retrofit2.http.POST("notes/folders/remove/{folderid}")
    suspend fun deleteNoteFolder(
        @retrofit2.http.Path("folderid") folderId: String,
        @Header("Authorization") authorization: String
    ): Response<com.bethwestsl.devistagram.model.DeleteNoteResponse>

    @retrofit2.http.POST("notes/folders/rename/{folderid}")
    suspend fun renameNoteFolder(
        @retrofit2.http.Path("folderid") folderId: String,
        @Header("Authorization") authorization: String,
        @Query("folder") newName: String
    ): Response<com.bethwestsl.devistagram.model.CreateFolderResponse>

    @retrofit2.http.POST("notes/move")
    suspend fun moveNotes(
        @Header("Authorization") authorization: String,
        @Query("noteids[]") noteIds: List<String>,
        @Query("folderid") folderId: String
    ): Response<com.bethwestsl.devistagram.model.MoveNotesResponse>

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("notes/send")
    suspend fun sendNote(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Field("to") recipients: List<String>,
        @retrofit2.http.Field("subject") subject: String,
        @retrofit2.http.Field("body") body: String
    ): Response<com.bethwestsl.devistagram.model.SendNoteResponse>
}
