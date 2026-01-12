# Watch Button Implementation

## Summary
Implemented full Watch/Unwatch functionality for the deviation detail view. The Watch button now:
- Checks if the user is already watching the artist when the deviation loads
- Displays "Watch" or "Watching" based on current status
- Allows users to toggle their watch status by clicking the button
- Shows loading state while API calls are in progress
- Displays toast notifications for success/error feedback

## Files Created

### 1. UserWatchRepository.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/repository/UserWatchRepository.kt`

**Purpose:** Repository class to handle all user watch-related API calls

**Methods:**
- `isWatchingUser(username, accessToken)` - Check if currently watching a user
- `watchUser(username, accessToken)` - Watch a user
- `unwatchUser(username, accessToken)` - Unwatch a user
- `toggleWatchUser(username, accessToken, currentlyWatching)` - Toggle watch status

## Files Modified

### 1. WatchersAndFriends.kt
**Added:**
- `WatchUserResponse` - Response model for watch API
- `UnwatchUserResponse` - Response model for unwatch API
- `WatchingStatusResponse` - Response model for checking watch status

### 2. DeviantArtApi.kt
**Added API Endpoints:**
- `POST /user/friends/watch/{username}` - Watch a user
- `DELETE /user/friends/unwatch/{username}` - Unwatch a user
- `GET /user/friends/watching/{username}` - Check if watching a user

### 3. DeviationDetailActivity.kt
**Added:**
- `userWatchRepository: UserWatchRepository` - Repository instance
- `isWatchingArtist: Boolean` - State tracking if watching the artist
- `isLoadingWatchStatus: Boolean` - State tracking if API call in progress

**New Methods:**
- `checkWatchingStatus(username, accessToken)` - Called when deviation loads to check watch status
- `toggleWatchArtist()` - Handles watch button clicks
- `updateWatchButtonState()` - Updates button text ("Watch"/"Watching"/"Loading...")

**Modified:**
- `onCreate()` - Initialize UserWatchRepository
- `loadDeviationDetails()` - Added call to checkWatchingStatus after loading deviation
- `setupButtons()` - Changed followButton click handler to call toggleWatchArtist()

## API Endpoints Used

### Check Watch Status
```
GET /user/friends/watching/{username}
Authorization: Bearer {access_token}

Response:
{
  "watching": true/false
}
```

### Watch User
```
POST /user/friends/watch/{username}
Authorization: Bearer {access_token}
Body: {
  "watch[friend]": true,
  "watch[deviations]": true
}

Response:
{
  "success": true/false
}
```

### Unwatch User
```
DELETE /user/friends/unwatch/{username}
Authorization: Bearer {access_token}

Response:
{
  "success": true/false
}
```

## User Experience Flow

1. **User opens a deviation:**
   - Deviation details load
   - Watch button shows "Loading..." (disabled)
   - API call checks if already watching the artist
   - Button updates to "Watch" or "Watching" (enabled)

2. **User clicks Watch button:**
   - Button shows "Loading..." (disabled)
   - API call to watch/unwatch the artist
   - On success: Button updates to new state, toast shows confirmation
   - On error: Button reverts to previous state, toast shows error

3. **Button States:**
   - "Loading..." - API call in progress (disabled)
   - "Watch" - Not currently watching (enabled, clickable)
   - "Watching" - Currently watching (enabled, clickable to unwatch)

## Error Handling

- Network errors show toast with error message
- Failed API calls log errors and show user-friendly messages
- Button state is properly restored on errors
- Prevents multiple simultaneous API calls

## Notes

- The watch API accepts optional parameters for what to watch (friend, deviations, journals, etc.)
- Currently configured to watch both friend status and deviations
- All API calls are async and don't block the UI
- Uses Kotlin coroutines for async operations
- Follows existing repository pattern in the app

