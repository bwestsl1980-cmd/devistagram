# Fix: 403 Error When Fetching Notes

## Problem
Getting a 403 Forbidden error when trying to fetch notes from the DeviantArt API.

## Root Causes Identified

### 1. Missing OAuth Scope (Primary Issue)
The OAuth scope was missing the `note` permission. The Notes API requires the `note` scope to access user notes/mail.

### 2. Incorrect API Endpoint Formats (Secondary Issue)
Several Notes API endpoints were using incorrect parameter types (Form fields instead of Query parameters), which could cause 400 errors after fixing the 403.

## Solutions Applied

### Solution 1: Added `note` OAuth Scope
Updated `DeviantArtAuthConfig.kt` to include the `note` scope:

**Before:**
```kotlin
const val SCOPE = "browse message user collection comment.post user.manage"
```

**After:**
```kotlin
const val SCOPE = "browse message note user collection comment.post user.manage"
```

### Solution 2: Fixed API Endpoint Parameter Types
Corrected the following endpoints to use Query parameters instead of Form fields:

1. **DELETE Notes** - Changed from `@Field` to `@Query` for `noteids[]`
2. **CREATE Folder** - Changed from `@Field` to `@Query` for `folder`
3. **DELETE Folder** - Removed unnecessary `@FormUrlEncoded`
4. **RENAME Folder** - Changed from `@Field` to `@Query` for `folder`
5. **MOVE Notes** - Changed from `@Field` to `@Query` for `noteids[]` and `folderid`

These changes align with the DeviantArt API specification which expects these POST endpoints to use query parameters, not form-encoded bodies.

## Important: You Must Re-authenticate

⚠️ **CRITICAL:** Since the OAuth scope has changed, your existing access token does NOT have the `note` permission.

### Steps to Fix:

1. **Logout from the app** (Profile → Menu → Logout)
2. **Login again** - this will request a new token with the `note` scope included
3. **Try accessing Mail again** - Both 403 and potential 400 errors should now be resolved

### Why This is Necessary:
- OAuth scopes are "baked in" to the access token when it's created
- Changing the scope in code doesn't affect existing tokens
- You need to get a new token that includes the `note` scope
- This is done automatically when you login after the scope change

## Testing After Re-login:

1. ✅ Logout from the app
2. ✅ Login again (new token will have `note` scope)
3. ✅ Go to Profile tab
4. ✅ Click Mail icon
5. ✅ Notes should now load without 403 error

## DeviantArt API Scopes Reference:

The following scopes are now included:
- `browse` - Browse deviations
- `message` - Access messages/notifications feed
- `note` - **Access notes/mail** ← This was missing!
- `user` - Access user profile information
- `collection` - Access favorites/collections
- `comment.post` - Post comments
- `user.manage` - Manage user settings (watch/unwatch)

## If You Still Get 403 After Re-login:

1. Check the DeviantArt developer console to ensure your app registration includes the `note` scope
2. Verify you're using the correct Client ID and Secret
3. Check the network request to confirm the scope parameter includes "note"
4. Try revoking access at https://www.deviantart.com/settings/applications and login again

## Files Modified:
- ✅ `DeviantArtAuthConfig.kt` - Added `note` to SCOPE constant
- ✅ `DeviantArtApi.kt` - Fixed Notes API endpoints to use correct parameter types (Query vs Form fields)

## Summary of Fixes:

**Authentication:**
- Added `note` scope for OAuth authorization

**API Endpoints (now use Query parameters instead of Form fields):**
- `/notes/delete` - Delete notes
- `/notes/folders/create` - Create folder
- `/notes/folders/remove/{folderid}` - Delete folder  
- `/notes/folders/rename/{folderid}` - Rename folder
- `/notes/move` - Move notes to folder

These changes should resolve both 403 (Forbidden) and potential 400 (Bad Request) errors!

