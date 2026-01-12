# Comments Notifications Implementation

## Overview
Implemented the Comments tab in the Notifications section to display comment and reply notifications from the DeviantArt API.

## Implementation Date
January 12, 2026

## API Integration

### Endpoints
Two separate API calls are made and combined:

1. `GET /messages/feedback?type=comments` - For comment notifications
2. `GET /messages/feedback?type=replies` - For reply notifications

### Parameters Used
- `type`: "comments" or "replies"
- `offset`: For pagination
- `limit`: 10 (number of items per page)
- `mature_content`: true

### Response Handling
- Both API calls are made simultaneously
- Results are combined into a single list
- Sorted by timestamp (most recent first)
- Pagination uses the offset from either response

## Display Formats

### For Comments
```
{username} has commented on your deviation {deviation title}
```

**Example:**
```
john_artist has commented on your deviation Sunset Painting
```

**Clickable elements:**
- `username` → Navigate to user profile (TODO)
- `deviation title` → Opens DeviationDetailActivity ✅

### For Replies
```
{username} has replied to your comment
```

**Example:**
```
jane_user has replied to your comment
```

**Clickable elements:**
- `username` → Navigate to user profile (TODO)
- `comment` → Navigate to comment view (TODO)

## Data Model Updates

### MessageSubject
Added new fields:
- `comment`: MessageComment object for reply notifications
- `title`: String for deviation title in comment notifications

### MessageComment (new)
```kotlin
data class MessageComment(
    val commentId: String?,
    val body: String?
)
```

## Files Created

### 1. Repository
**File:** `CommentsNotificationsRepository.kt`
- Fetches both comments and replies
- Combines and sorts results by timestamp
- Manages offset-based pagination

### 2. ViewModel
**File:** `CommentsNotificationsViewModel.kt`
- Manages UI state for comment notifications
- Handles refresh and load more functionality
- Exposes LiveData for the fragment

### 3. Layout Files

**File:** `fragment_comments_notifications.xml`
- RecyclerView for displaying notifications
- SwipeRefreshLayout for pull-to-refresh
- Progress bar and error states

**File:** `item_comment_notification.xml`
- User avatar (circular)
- New indicator badge
- Notification text with clickable links
- Timestamp

### 4. Fragment
**File:** `CommentsNotificationsFragment.kt`
- Full implementation with clickable links
- Differentiates between comments and replies
- Proper text formatting for each type

## Clickable Links Implementation

### Comment Type
- **Username**: First word - click shows toast (TODO: profile navigation)
- **Deviation title**: End of sentence - navigates to DeviationDetailActivity ✅

### Reply Type  
- **Username**: First word - click shows toast (TODO: profile navigation)
- **"comment" word**: Click shows toast with commentId (TODO: comment navigation)

### Visual Styling
- Link color: `#0095F6` (light theme), `#3897F0` (dark theme)
- No underlines for clean appearance
- Uses SpannableString with ClickableSpan

## Technical Details

### Type Detection
```kotlin
val isReply = message.type.contains("reply", ignoreCase = true)
```

### Data Sources
- Comments: Uses `subject.title` or `subject.deviation.title`
- Replies: Uses `subject.comment.commentId` for navigation

### Helper Classes
```kotlin
private data class ClickableRange(
    val start: Int,
    val end: Int,
    val type: ClickType,
    val data: String
)

private enum class ClickType {
    USER, DEVIATION, COMMENT
}
```

## Features

✅ Fetches both comments and replies
✅ Combined and sorted display
✅ Pull-to-refresh
✅ Infinite scroll pagination
✅ Loading states
✅ Error handling
✅ Empty state
✅ New notification indicator
✅ User avatars with Coil
✅ Relative timestamps
✅ Clickable links:
  - Username (placeholder)
  - Deviation title (functional)
  - Comment word in replies (placeholder)

## Next Steps

TODO items for future implementation:
1. Navigate to user profile when username is clicked
2. Navigate to comment view when "comment" is clicked in reply notifications
3. Consider adding preview of the comment text in the notification

