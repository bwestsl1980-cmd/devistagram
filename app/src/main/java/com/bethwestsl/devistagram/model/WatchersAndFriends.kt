package com.bethwestsl.devistagram.model

import com.google.gson.annotations.SerializedName

// Response for /user/watchers/{username}
data class WatchersResponse(
    @SerializedName("has_more")
    val hasMore: Boolean,

    @SerializedName("next_offset")
    val nextOffset: Int?,

    @SerializedName("results")
    val results: List<Watcher>?
)

data class Watcher(
    @SerializedName("user")
    val user: WatcherUser,

    @SerializedName("is_watching")
    val isWatching: Boolean,

    @SerializedName("lastvisit")
    val lastVisit: String?,

    @SerializedName("watch")
    val watch: WatchInfo?
)

data class WatcherUser(
    @SerializedName("userid")
    val userId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("usericon")
    val userIcon: String,

    @SerializedName("type")
    val type: String
)

data class WatchInfo(
    @SerializedName("friend")
    val friend: Boolean,

    @SerializedName("deviations")
    val deviations: Boolean,

    @SerializedName("journals")
    val journals: Boolean,

    @SerializedName("forum_threads")
    val forumThreads: Boolean,

    @SerializedName("critiques")
    val critiques: Boolean,

    @SerializedName("scraps")
    val scraps: Boolean,

    @SerializedName("activity")
    val activity: Boolean,

    @SerializedName("collections")
    val collections: Boolean
)

// Response for /user/friends/{username}
data class FriendsResponse(
    @SerializedName("has_more")
    val hasMore: Boolean,

    @SerializedName("next_offset")
    val nextOffset: Int?,

    @SerializedName("results")
    val results: List<Friend>?
)

data class Friend(
    @SerializedName("user")
    val user: WatcherUser,

    @SerializedName("is_watching")
    val isWatching: Boolean,

    @SerializedName("lastvisit")
    val lastVisit: String?,

    @SerializedName("watch")
    val watch: WatchInfo?
)

// Response for /user/friends/search
data class UserSearchResponse(
    @SerializedName("results")
    val results: List<SearchUser>?
)

data class SearchUser(
    @SerializedName("userid")
    val userId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("usericon")
    val userIcon: String,

    @SerializedName("type")
    val type: String
)

// Response for POST /user/friends/watch/{username}
data class WatchUserResponse(
    @SerializedName("success")
    val success: Boolean
)

// Response for DELETE /user/friends/unwatch/{username}
data class UnwatchUserResponse(
    @SerializedName("success")
    val success: Boolean
)

// Response for GET /user/friends/watching/{username}
data class WatchingStatusResponse(
    @SerializedName("watching")
    val watching: Boolean
)

