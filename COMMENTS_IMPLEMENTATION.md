# Comments Feature Implementation

## Summary
Implemented full comments functionality for the deviation detail view, including:
- Display main comments on deviations
- Support for nested comments (replies to comments)
- View/hide replies with expandable UI
- Post new comments
- Reply to existing comments
- Automatic comment reloading after posting

## Files Created

### 1. Comment.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/model/Comment.kt`

**Purpose:** Data models for comments API

**Models:**
- `CommentsResponse` - Response for fetching deviation comments
- `Comment` - Individual comment data with nested reply support
- `CommentUser` - User who posted the comment
- `CommentSiblingsResponse` - Response for fetching comment replies
- `PostCommentRequest` - Request body for posting comments
- `PostCommentResponse` - Response after posting a comment

**Key Fields:**
- `repliesList: List<Comment>?` - Cached list of loaded replies
- `isExpanded: Boolean` - Track if replies are shown
- `isLoadingReplies: Boolean` - Loading state for replies

### 2. CommentsRepository.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/repository/CommentsRepository.kt`

**Purpose:** Repository class to handle all comment-related API calls

**Methods:**
- `getDeviationComments(deviationId, accessToken, offset, limit)` - Fetch main comments
- `getCommentReplies(commentId, accessToken, offset, limit)` - Fetch replies to a comment
- `postComment(deviationId, commentText, accessToken, parentCommentId)` - Post new comment or reply

### 3. CommentsAdapter.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/adapter/CommentsAdapter.kt`

**Purpose:** RecyclerView adapter for displaying comments with nested replies

**Features:**
- Display user avatar, username, timestamp
- Show comment text
- Display like count if > 0
- Show reply count with expand/collapse functionality
- Nested RecyclerView for replies
- Loading indicators for reply loading
- Callbacks for user interactions

**Callbacks:**
- `onReplyClick(Comment)` - When user clicks reply button
- `onLoadReplies(Comment)` - When user clicks to view/hide replies

### 4. item_comment.xml
**Path:** `app/src/main/res/layout/item_comment.xml`

**Purpose:** Layout for individual comment items

**UI Components:**
- User avatar (circular, 40dp)
- Username and timestamp
- Comment text
- Like count (if any)
- Reply button
- View/Hide replies button (if comment has replies)
- Nested RecyclerView for replies (indented 52dp)
- Loading indicator for replies

## Files Modified

### 1. DeviantArtApi.kt
**Added API Endpoints:**
- `GET /comments/deviation/{deviationid}` - Fetch comments on a deviation
- `GET /comments/{commentid}/siblings` - Fetch replies to a comment
- `POST /comments/post/deviation/{deviationid}` - Post a comment on deviation

### 2. activity_deviation_detail.xml
**Added UI Components:**
- Comments section divider
- Comments header with count
- "Add Comment" button
- Comments RecyclerView
- Comments loading indicator
- "No comments" message

### 3. DeviationDetailActivity.kt
**Added:**
- `commentsRepository: CommentsRepository` - Repository instance
- `commentsAdapter: CommentsAdapter` - RecyclerView adapter
- `comments: MutableList<Comment>` - Cached comments list

**New Methods:**
- `setupCommentsRecyclerView()` - Initialize adapter with callbacks
- `loadComments(deviationId, accessToken)` - Load main comments
- `loadReplies(comment)` - Load/toggle replies for a comment
- `showAddCommentDialog(parentComment)` - Show dialog to add comment/reply
- `showReplyDialog(comment)` - Show reply dialog
- `postComment(commentText, parentComment)` - Post comment to API

**Modified:**
- `onCreate()` - Initialize comments repository and RecyclerView
- `setupButtons()` - Added "Add Comment" button handler
- `loadDeviationDetails()` - Added call to loadComments()

## API Endpoints Used

### Get Deviation Comments
```
GET /comments/deviation/{deviationid}
Authorization: Bearer {access_token}
Query Parameters:
  - offset: Int (optional)
  - limit: Int (default: 50)
  - maxdepth: Int (default: 5)

Response:
{
  "thread": [Comment],
  "has_more": boolean,
  "next_offset": int,
  "total": int
}
```

### Get Comment Replies (Siblings)
```
GET /comments/{commentid}/siblings
Authorization: Bearer {access_token}
Query Parameters:
  - offset: Int (optional)
  - limit: Int (default: 50)

Response:
{
  "thread": [Comment],
  "has_more": boolean,
  "next_offset": int
}
```

### Post Comment on Deviation
```
POST /comments/post/deviation/{deviationid}
Authorization: Bearer {access_token}
Body:
{
  "body": "comment text",
  "commentid": "parent_comment_id" // optional, for replies
}

Response:
{
  "commentid": "new_comment_id",
  "posted": "timestamp",
  "text": "comment text"
}
```

## User Experience Flow

### Viewing Comments

1. **User opens deviation:**
   - Comments load automatically after deviation loads
   - Shows "Comments (count)" header
   - Displays list of main comments
   - Shows "No comments yet" if empty

2. **Each comment shows:**
   - User avatar (circular)
   - Username
   - Time ago (e.g., "2h ago", "3d ago")
   - Comment text
   - Like count (if > 0)
   - Reply button
   - "View X replies" button (if comment has replies)

### Loading Replies

3. **User clicks "View X replies":**
   - Button changes to loading state
   - Fetches replies from API
   - Displays nested replies indented below comment
   - Button text changes to "Hide X replies"
   - Nested comments show same UI as main comments

4. **User clicks "Hide X replies":**
   - Collapses nested replies
   - Button text changes back to "View X replies"

### Posting Comments

5. **User clicks "Add Comment":**
   - Dialog appears with text input
   - User types comment
   - Clicks "Post"
   - Comment is posted to API
   - Comments list automatically refreshes
   - Toast shows "Comment posted!"

6. **User clicks "Reply" on a comment:**
   - Dialog appears with text input
   - Shows "Reply to {username}"
   - User types reply
   - Clicks "Post"
   - Reply is posted as nested comment
   - Comments list automatically refreshes

## Comment Structure

Comments support unlimited nesting through the recursive structure:

```
Main Comment 1
  └─ Reply 1.1
      └─ Reply 1.1.1
  └─ Reply 1.2
Main Comment 2
  └─ Reply 2.1
  └─ Reply 2.2
      └─ Reply 2.2.1
          └─ Reply 2.2.1.1
```

Each level of replies is fetched on-demand when the user clicks "View replies".

## Timestamp Formatting

Comments display relative timestamps:
- Just now (< 1 minute)
- Xm ago (< 1 hour)
- Xh ago (< 1 day)
- Xd ago (< 1 week)
- "MMM d, yyyy" (older than 1 week)

## Error Handling

- Network errors show toast with error message
- Failed comment loads show "Failed to load comments"
- Failed reply loads show "Failed to load replies"
- Failed comment posts show error toast
- Empty state handled with "No comments yet" message

## UI Features

- **Nested RecyclerViews:** Replies use nested RecyclerViews for proper scrolling
- **Loading States:** Progress indicators for comment and reply loading
- **Expandable UI:** Smooth expand/collapse for replies
- **Clickable Actions:** Reply and view replies buttons with visual feedback
- **Avatar Images:** Circular user avatars loaded with Coil
- **Text Wrapping:** Comment text properly wraps for long content

## Notes

- Comments are loaded automatically when deviation loads
- Replies are loaded on-demand to reduce initial load time
- Posting a comment refreshes the entire comments list
- The adapter supports recursive nesting through nested RecyclerViews
- Comment text is plain text (HTML not rendered currently)
- Like functionality is displayed but not interactive (future enhancement)
- Dialog-based comment input (could be enhanced to bottom sheet later)

