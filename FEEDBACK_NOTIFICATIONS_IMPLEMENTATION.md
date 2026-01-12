# Feedback Notifications Implementation

## Overview
Implemented the Feedback tab in the Notifications section to display activity notifications from the DeviantArt API.

## Implementation Date
January 12, 2026

## API Integration

### Endpoint
`GET /messages/feedback`

### Parameters Used
- `type`: "activity" (filters for activity-type feedback - collection additions)
- `offset`: Integer for pagination (starts at null, then uses `next_offset` from response)
- `limit`: 10 (number of items per page)
- `mature_content`: true

### Response Format
The API returns a `FeedbackMessagesResponse` with:
- `has_more`: Boolean indicating if there are more results
- `next_offset`: Integer for the next page (null if no more results)
- `results`: Array of `Message` objects

Each `Message` contains:
- `originator`: The user who performed the action
- `subject.deviation`: The deviation that was added to a collection
- `collection`: The collection/folder where it was added (with `folderid` and `name`)
- `type`: "feedback.collect" for collection additions
- `is_new`: Boolean indicating if the notification is unread
- `ts`: Timestamp in ISO format

### Pagination
Uses offset-based pagination:
1. First request: `offset=null` 
2. Subsequent requests: `offset={next_offset}` from previous response
3. Stop when `has_more=false`

## Troubleshooting

### 400 Error Fix
**Problem:** Initial implementation used incorrect parameters causing 400 Bad Request error.

**Root Cause:** 
1. Used `stack="activity"` parameter instead of `type="activity"`
2. Used `cursor` for pagination instead of `offset`
3. Wrong response model (used `MessagesResponse` instead of `FeedbackMessagesResponse`)

**Solution:** 
1. Changed parameter from `stack` to `type` with value "activity"
2. Changed pagination from cursor-based to offset-based
3. Created new `FeedbackMessagesResponse` model with `next_offset` field
4. Removed client-side filtering (API now returns only activity notifications)

**Verified in Postman:**
```
GET https://www.deviantart.com/api/v1/oauth2/messages/feedback?type=activity
Headers: Authorization: Bearer {token}
```

**Logs to check:**
- `FeedbackRepo`: Shows API calls, response codes, offset values, and result counts
- Check Logcat for "FeedbackRepo" tag to see detailed API responses

## Files Created

### 1. Repository
**File:** `FeedbackNotificationsRepository.kt`
- Handles API calls to fetch feedback notifications
- Manages pagination with cursor
- Uses TokenManager for authentication

### 2. ViewModel
**File:** `FeedbackNotificationsViewModel.kt`
- Manages UI state (loading, error, data)
- Handles refresh and load more functionality
- Exposes LiveData for the fragment to observe

### 3. Layout Files

**File:** `fragment_feedback_notifications.xml`
- RecyclerView for displaying notifications
- SwipeRefreshLayout for pull-to-refresh
- Progress bar for loading state
- Error/empty state TextView

**File:** `item_feedback_notification.xml`
- User avatar (circular)
- New indicator badge
- Notification text
- Deviation thumbnail
- Timestamp

### 4. Fragment
**File:** `FeedbackNotificationsFragment.kt` (Updated)
- Replaced placeholder with full implementation
- RecyclerView with adapter
- Infinite scroll pagination
- Pull-to-refresh functionality

## Files Modified

### 1. Message Model
**File:** `Message.kt`
- Added `collection` field to Message
- Added `MessageCollection` data class (folderId, name)
- Added `MessageDeviation` data class (deviationId, title, url, thumbs)
- Updated `MessageSubject` to include deviation

### 2. DeviantArt API
**File:** `DeviantArtApi.kt`
- Added `getMessagesFeedback()` endpoint

## Display Format

Each notification displays:
```
{username} has added {deviation title} to their folder {collection name}
```

Examples:
```
john_artist has added Sunset Painting to their folder Featured
user123 has added Cool Art to their folder Favourites
```

**Collection Logic:**
- If the `collection` block exists in the response → Display the folder name from `collection.name`
  - Example: `"collection": { "name": "Featured" }` → displays "Featured"
- If the `collection` block is null/missing → Display "Favourites" (deviation added to favorites, not a specific folder)
  - When there's no collection block, the user favorited the deviation without putting it in a specific collection folder

## Clickable Links Implementation

### How It Works
The notification text uses `SpannableString` with `ClickableSpan` to make specific parts of the text clickable:

1. **Username (originator)**: First word in the sentence
   - Click action: TODO - Will navigate to user profile
   - Current: Shows toast message

2. **Deviation Title**: Middle part of the sentence  
   - Click action: Opens `DeviationDetailActivity` with the deviation ID
   - Creates a minimal `Deviation` object with just the ID for navigation

3. **Collection Name**: Last part of the sentence (if collection exists)
   - Click action: TODO - Will navigate to collection folder view
   - Current: Shows toast message with collection name and folder ID
   - Only clickable if `collection` block exists in the API response

### Visual Styling
- Link color: `#0095F6` (light theme), `#3897F0` (dark theme)
- No underlines (cleaner Instagram-like appearance)
- Text remains selectable and accessible

### Code Structure
```kotlin
// Create spannable text with clickable regions
SpannableString with ClickableSpan objects
LinkMovementMethod.getInstance() enables click handling
```

## Features

✅ Pull-to-refresh
✅ Infinite scroll pagination
✅ Loading states
✅ Error handling
✅ Empty state
✅ New notification indicator
✅ User avatars with Coil image loading
✅ Deviation thumbnails
✅ Relative timestamps (e.g., "2h ago")

## Next Steps

The Comments and Mentions tabs can be implemented similarly using:
- Comments: `GET /messages/feedback` with `stack=comments`
- Mentions: `GET /messages/feedback` with `stack=replies`

