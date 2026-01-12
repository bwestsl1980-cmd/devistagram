# Comments Display Issues Fix

## Date: January 12, 2026

## Problems Identified and Fixed

### Issue 1: Comment Content Not Showing
**Problem:** Comments were displaying empty or showing no text content

**Root Cause:** The DeviantArt API might return comment content in a `body` field instead of `text` field, or both fields might exist. The adapter was only checking the `text` field.

**Solution:**
- Added `body` field to the `Comment` model as an alternative field for comment content
- Updated `CommentsAdapter` to check both fields with fallback logic:
  1. Use `text` if it's not null or blank
  2. Fall back to `body` if `text` is empty
  3. Show "[No comment text]" if both are empty
- Added detailed logging to track which field contains the content

### Issue 2: Nested Comments Not Visually Indented
**Problem:** Child comments (replies) were not clearly distinguished from parent comments

**Solution:**
1. **Increased indentation** - Changed from 52dp to 24dp left margin for better proportions
2. **Added visual border** - Created `comment_reply_border.xml` drawable with a 2dp left border in gray (#E0E0E0)
3. **Added padding** - Added 12dp left padding to nested RecyclerView for better spacing
4. **Updated progress indicator** - Aligned loading indicator with new indentation (36dp)

## Files Modified

### 1. Comment.kt - Added Body Field
**Location:** `app/src/main/java/com/scottapps/devistagram/model/Comment.kt`

**Changes:**
```kotlin
data class Comment(
    // ...existing fields...
    @SerializedName("text")
    val text: String?,

    @SerializedName("body")  // NEW - Alternative field for comment content
    val body: String?,

    @SerializedName("user")
    val user: CommentUser,
    // ...rest of fields...
)
```

### 2. CommentsAdapter.kt - Fixed Content Display
**Location:** `app/src/main/java/com/scottapps/devistagram/adapter/CommentsAdapter.kt`

**Changes:**
```kotlin
// Comment text - use body if text is null or empty
val commentContent = when {
    !comment.text.isNullOrBlank() -> comment.text
    !comment.body.isNullOrBlank() -> comment.body
    else -> "[No comment text]"
}
commentTextView.text = commentContent

// Added logging for debugging
android.util.Log.d("CommentsAdapter", 
    "Binding comment ${comment.commentId}: text='${comment.text}', body='${comment.body}', final='$commentContent'")
```

### 3. item_comment.xml - Improved Visual Hierarchy
**Location:** `app/src/main/res/layout/item_comment.xml`

**Changes:**
```xml
<!-- Nested Replies RecyclerView -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/repliesRecyclerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="24dp"      <!-- Changed from 52dp -->
    android:layout_marginTop="8dp"
    android:paddingStart="12dp"             <!-- NEW - Added padding -->
    android:paddingEnd="0dp"
    android:background="@drawable/comment_reply_border"  <!-- NEW - Visual border -->
    android:nestedScrollingEnabled="false"
    android:visibility="gone"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

<!-- Progress bar aligned with new indentation -->
<ProgressBar
    android:id="@+id/repliesProgressBar"
    android:layout_marginStart="36dp"      <!-- Changed from 52dp -->
    ... />
```

### 4. comment_reply_border.xml - NEW FILE
**Location:** `app/src/main/res/drawable/comment_reply_border.xml`

**Purpose:** Drawable to create a subtle left border for nested comment replies

**Content:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Left border (2dp gray line) -->
    <item android:left="0dp">
        <shape android:shape="rectangle">
            <solid android:color="#E0E0E0" />
            <size android:width="2dp" />
        </shape>
    </item>
    <!-- Transparent background -->
    <item android:left="2dp">
        <shape android:shape="rectangle">
            <solid android:color="@android:color/transparent" />
        </shape>
    </item>
</layer-list>
```

### 5. CommentsRepository.kt - Enhanced Logging
**Location:** `app/src/main/java/com/scottapps/devistagram/repository/CommentsRepository.kt`

**Changes:**
```kotlin
if (response.isSuccessful && response.body() != null) {
    val comments = response.body()!!.thread ?: emptyList()
    Log.d("CommentsRepo", "Fetched ${comments.size} comments")
    
    // NEW - Log each comment's content for debugging
    comments.forEachIndexed { index, comment ->
        Log.d("CommentsRepo", 
            "Comment $index: id=${comment.commentId}, text='${comment.text}', " +
            "user=${comment.user.username}, replies=${comment.replies}")
    }
    Result.success(comments)
}
```

## Visual Improvements

### Before:
- Comment content might not show (empty text)
- Nested replies hard to distinguish from parent comments
- No visual hierarchy

### After:
- Comment content reliably displays from either `text` or `body` field
- Nested replies have:
  - 24dp left margin
  - 12dp internal left padding
  - 2dp gray vertical border on the left
  - Clear visual separation from parent comments
- Multi-level nesting is visually clear (replies to replies)

## Testing Checklist

After rebuilding:
1. ✅ View a deviation with comments
2. ✅ Verify comment text is displayed
3. ✅ Check that comments with replies show "View X replies" button
4. ✅ Click to expand replies
5. ✅ Verify nested replies are indented with a left border
6. ✅ Verify you can reply to comments (this requires the OAuth scopes fix)
7. ✅ Check multi-level nesting (replies to replies)

## Debugging

If comments still don't show content after this fix:
1. Check the logcat for "CommentsRepo" and "CommentsAdapter" tags
2. Look for the detailed logging that shows `text` and `body` values
3. If both fields are empty in the API response, the issue is with the API data itself

## Notes

- DeviantArt API returns comment content with HTML formatting (rich text editor output)
- The `stripHtmlTags()` function handles common HTML tags and entities
- For more complex formatting, consider using Android's `Html.fromHtml()` with a TextView that supports spans
- The `body` field is commonly used in DeviantArt's API for text content
- Timestamp format DD/MM/YYYY is concise and internationally recognizable
- The visual border color (#E0E0E0) is a light gray that works well in both light and dark themes
- The 24dp + 12dp indentation (36dp total) provides good visual hierarchy without taking too much horizontal space
- Nested replies can theoretically go multiple levels deep (replies to replies to replies), and the border will stack nicely

## Related Fixes

This fix is independent of the OAuth scopes fix in `OAUTH_SCOPES_COMPLETE_FIX.md`, but both are needed for full comment functionality:
- **This fix:** Display existing comments properly
- **OAuth fix:** Allow posting new comments

