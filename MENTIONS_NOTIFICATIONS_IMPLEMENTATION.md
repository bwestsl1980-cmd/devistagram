# Mentions Notifications Implementation

## Overview
Implemented the Mentions tab in the Notifications section to display mention notifications from the DeviantArt API.

## Implementation Date
January 12, 2026

## API Integration

### Endpoint
`GET /messages/mentions`

### Parameters Used
- `offset`: For pagination
- `limit`: 10 (number of items per page)
- `mature_content`: true

### Response Format
Returns a `FeedbackMessagesResponse` with:
- `has_more`: Boolean indicating if there are more results
- `next_offset`: Integer for the next page
- `results`: Array of `Message` objects

Each `Message` contains:
- `originator`: The user who mentioned you
- `subject.title`: The title of the comment/deviation where you were mentioned
- `is_new`: Boolean indicating if unread
- `ts`: Timestamp

## Display Format

```
{username} has mentioned you in {title}
```

**Example:**
```
john_artist has mentioned you in Amazing Artwork Discussion
```

**Clickable elements:**
- `username` → Navigate to user profile (TODO)
- `title` → Navigate to the mention location (TODO)

## Files Created

### 1. Repository
**File:** `MentionsNotificationsRepository.kt`
- Fetches mentions from `/messages/mentions`
- Manages offset-based pagination
- Handles authentication and error logging

### 2. ViewModel
**File:** `MentionsNotificationsViewModel.kt`
- Manages UI state for mention notifications
- Handles refresh and load more functionality
- Exposes LiveData for the fragment

### 3. Layout Files

**File:** `fragment_mentions_notifications.xml`
- RecyclerView for displaying notifications
- SwipeRefreshLayout for pull-to-refresh
- Progress bar and error states

**File:** `item_mention_notification.xml`
- User avatar (circular)
- New indicator badge
- Notification text with clickable links
- Timestamp

### 4. Fragment
**File:** `MentionsNotificationsFragment.kt`
- Full implementation with clickable links
- Displays "{username} has mentioned you in {title}"
- Proper text formatting with SpannableString

### 5. API
**File:** `DeviantArtApi.kt` (updated)
- Added `getMessagesMentions()` endpoint

## Clickable Links Implementation

### Username
- Position: First word in the sentence
- Click action: Shows toast (TODO: Navigate to user profile)
- Color: Blue link color (`#0095F6` / `#3897F0`)

### Title
- Position: End of sentence after "in"
- Click action: Shows toast (TODO: Navigate to mention location)
- Color: Blue link color (`#0095F6` / `#3897F0`)

### Visual Styling
- No underlines for clean appearance
- Uses SpannableString with ClickableSpan
- Maintains text selectability

## Technical Details

### Data Source
- Uses `subject.title` from the API response
- Falls back to "a comment" if title is null

### Pagination
- Offset-based (same as Feedback and Comments tabs)
- Starts with `offset=null`
- Uses `next_offset` from response for subsequent calls

### Error Handling
- Logs all API calls and responses
- Shows user-friendly error messages
- Handles network failures gracefully

## Features

✅ Fetches mention notifications
✅ Pull-to-refresh
✅ Infinite scroll pagination
✅ Loading states
✅ Error handling
✅ Empty state
✅ New notification indicator
✅ User avatars with Coil
✅ Relative timestamps ("2h ago")
✅ Clickable links:
  - Username (placeholder)
  - Title (placeholder)

## Next Steps

TODO items for future implementation:
1. Navigate to user profile when username is clicked
2. Navigate to the mention location (comment/deviation) when title is clicked
3. Consider showing more context about where the mention occurred

## Complete Notifications Implementation

With the Mentions tab complete, all three notification tabs are now functional:

1. **Feedback** - Collection additions (`type=activity`)
2. **Comments** - Comments and replies (`type=comments` and `type=replies`)
3. **Mentions** - User mentions (separate endpoint)

All tabs feature:
- Pull-to-refresh
- Infinite scroll
- Clickable links
- Real-time loading states
- Error handling

