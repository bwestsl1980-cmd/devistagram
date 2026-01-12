# OAuth Scopes Complete Fix

## Date: January 12, 2026

## Problems Identified and Fixed

### Issue 1: Favoriting Deviations Failed (403 Forbidden)
**Error:** `insufficient_scope` - missing `collection` scope

### Issue 2: Favoriting API Format Wrong (400 Bad Request)
**Error:** `deviationid is required` - parameters sent as query instead of form body

### Issue 3: Posting Comments Failed (403 Forbidden)
**Error:** `insufficient_scope` - missing `comment.post` scope

### Issue 4: Posting Comments Failed (400 Bad Request)
**Error:** `body is required` - parameters sent as JSON body instead of form body

## All Changes Made

### 1. DeviantArtAuthConfig.kt - OAuth Scopes Updated
**Before:**
```kotlin
const val SCOPE = "browse message user"
```

**After:**
```kotlin
const val SCOPE = "browse message user collection comment.post user.manage"
```

**Scopes Breakdown:**
- `browse` - Browse public content
- `message` - Access messages
- `user` - Access user profile information
- `collection` - **NEW** - Add/remove favorites, manage collections
- `comment.post` - **NEW** - Post comments on deviations
- `user.manage` - **NEW** - Watch/unwatch users (preemptive fix)

### 2. DeviantArtApi.kt - API Request Format Fixed
**Changed:** `faveDeviation()`, `unfaveDeviation()`, and `postCommentOnDeviation()` endpoints

**Favorite Endpoints - Before:**
```kotlin
@retrofit2.http.POST("collections/fave")
suspend fun faveDeviation(
    @Header("Authorization") authorization: String,
    @retrofit2.http.Query("deviationid") deviationId: String,
    @retrofit2.http.Query("folderid") folderId: String? = null
)
```

**Favorite Endpoints - After:**
```kotlin
@retrofit2.http.FormUrlEncoded
@retrofit2.http.POST("collections/fave")
suspend fun faveDeviation(
    @Header("Authorization") authorization: String,
    @retrofit2.http.Field("deviationid") deviationId: String,
    @retrofit2.http.Field("folderid") folderId: String? = null
)
```

**Comment Endpoint - Before:**
```kotlin
@retrofit2.http.POST("comments/post/deviation/{deviationid}")
suspend fun postCommentOnDeviation(
    @retrofit2.http.Path("deviationid") deviationId: String,
    @Header("Authorization") authorization: String,
    @retrofit2.http.Body body: PostCommentRequest
)
```

**Comment Endpoint - After:**
```kotlin
@retrofit2.http.FormUrlEncoded
@retrofit2.http.POST("comments/post/deviation/{deviationid}")
suspend fun postCommentOnDeviation(
    @retrofit2.http.Path("deviationid") deviationId: String,
    @Header("Authorization") authorization: String,
    @retrofit2.http.Field("body") body: String,
    @retrofit2.http.Field("commentid") commentId: String? = null
)
```

**Why:** DeviantArt's POST endpoints expect form-encoded body data, not JSON or query parameters.

### 3. Added Debug Logging
**Files:**
- `CollectionsRepository.kt` - Added detailed logging for deviation ID validation
- `DeviationDetailActivity.kt` - Added logging for favorite action tracking

## REQUIRED ACTION ⚠️

**YOU MUST LOG OUT AND LOG BACK IN** for the scope changes to take effect!

### Steps:
1. Build and install the app
2. Open the app
3. Go to **Profile** tab (bottom navigation)
4. Scroll down and tap **"Logout"**
5. **Log in again** with your DeviantArt account

### Why?
OAuth tokens are issued with specific scopes at authorization time. Your existing token has the old scopes (`browse message user`). To get a new token with all the required scopes (`browse message user collection comment.post user.manage`), you must re-authorize the app.

## What Should Work After Re-Login

✅ **Favoriting deviations** - Add to favorites/collections  
✅ **Unfavoriting deviations** - Remove from favorites  
✅ **Posting comments** - Comment on deviations  
✅ **Watching users** - Follow/unfollow artists (should work without scope errors)

## Technical Details

### OAuth Scope Errors
When an API endpoint requires a scope that wasn't requested during authorization:
- HTTP Status: `403 Forbidden`
- Error: `insufficient_scope`
- Solution: Add the required scope to `SCOPE` constant and re-authorize

### Form-Encoded POST Requests
DeviantArt API POST endpoints expect `application/x-www-form-urlencoded` format:
- Use `@FormUrlEncoded` annotation on the API method
- Use `@Field` instead of `@Query` for parameters
- Retrofit automatically handles the encoding

## Files Modified

1. `app/src/main/java/com/scottapps/devistagram/auth/DeviantArtAuthConfig.kt`
2. `app/src/main/java/com/scottapps/devistagram/network/DeviantArtApi.kt`
3. `app/src/main/java/com/scottapps/devistagram/repository/CollectionsRepository.kt`
4. `app/src/main/java/com/scottapps/devistagram/repository/CommentsRepository.kt`
5. `app/src/main/java/com/scottapps/devistagram/DeviationDetailActivity.kt`

## Future Scope Considerations

If you add new features that interact with DeviantArt API, check the API documentation for required scopes:
- `stash` - Access Sta.sh storage
- `publish` - Submit deviations
- `note` - Access notes/messages
- `gallery` - Manage gallery folders

Add them to `SCOPE` constant before implementing the features.

