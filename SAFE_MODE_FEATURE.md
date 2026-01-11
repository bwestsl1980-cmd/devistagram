# Safe Mode Feature - Implementation Summary

## Feature Overview
Users can enable "Safe Mode" to hide all mature content, making the app safe for viewing at work or in public. This is a global setting that persists across the entire app and app restarts.

## Use Case
"When I am at work I want to be able to look at DeviantArt without looking over my shoulder"

## Implementation

### 1. ArtistFilterManager Updates
**New Methods:**
- `isSafeModeEnabled(): Boolean` - Check if safe mode is active
- `setSafeModeEnabled(enabled: Boolean)` - Toggle safe mode

**Storage:**
- Key: `safe_mode_enabled`
- Type: Boolean
- Location: SharedPreferences
- Default: `false` (show all content)

### 2. UI Changes

**fragment_feed.xml:**
- Added "🔒 Safe Mode (Hide Mature)" switch below "Show Favorites Only"
- Lock emoji provides visual indicator
- Clear labeling: "(Hide Mature)"

**Layout:**
```
[Toolbar: Feed]
[Switch: Show Favorites Only]
[Switch: 🔒 Safe Mode (Hide Mature)]
[Feed Content]
```

### 3. Filtering Logic

**Filter Priority (all applied):**
1. **Blocked artists** - Always hidden
2. **Safe Mode** - Hide all `isMature == true` content
3. **Favorites Only** - Show only favorited artists (if enabled)

**Code:**
```kotlin
val filtered = allDeviations.filter { deviation ->
    // Always hide blocked
    if (blockedArtists.contains(username)) return@filter false
    
    // Hide mature if safe mode is on
    if (safeModeEnabled && deviation.isMature == true) return@filter false
    
    // Show only favorites if filter is on
    if (showFavoritesOnly) return@filter favoriteArtists.contains(username)
    
    // Show everything else
    true
}
```

### 4. Deviation Model

The `Deviation` model has an `isMature` property that indicates if content is marked as mature on DeviantArt:
```kotlin
data class Deviation(
    ...
    val isMature: Boolean?,
    ...
)
```

This field is populated by the DeviantArt API.

## User Flow

1. **User opens app at work**
2. **Taps "🔒 Safe Mode (Hide Mature)" toggle ON**
3. **All mature content disappears from feed**
4. **Toggle state persists across:**
   - Tab switches
   - App restarts
   - Theme changes
5. **User goes home, turns Safe Mode OFF**
6. **All content (including mature) shows again**

## Benefits

✅ **Work-safe browsing** - Hide all mature content with one tap
✅ **Global setting** - Applies everywhere (can be extended to other fragments)
✅ **Persistent** - Remembers your choice across app restarts
✅ **Instant** - No API calls, pure client-side filtering
✅ **Clear visual indicator** - 🔒 lock emoji shows it's active
✅ **No cost** - Local SharedPreferences storage

## Extending to Other Fragments

To apply Safe Mode to other parts of the app (Discover, Tagged, etc.):

```kotlin
// In any fragment
val filterManager = ArtistFilterManager(requireContext())
if (filterManager.isSafeModeEnabled()) {
    // Filter out mature content
    filteredList = deviations.filter { it.isMature != true }
}
```

## Testing Checklist

- [ ] Safe Mode toggle appears on Feed page
- [ ] Toggle starts OFF by default
- [ ] Turning ON hides all mature-flagged deviations
- [ ] Turning OFF shows all content again
- [ ] Safe Mode state persists across app restarts
- [ ] Safe Mode works with Favorites filter (both can be ON)
- [ ] Safe Mode works with blocked artists (both filters apply)
- [ ] Lock emoji (🔒) is visible in both light/dark themes

## Future Enhancements

- Apply Safe Mode to Discover, Tagged, Profile tabs
- Add "Safe Mode Active" indicator in app bar when enabled
- Statistics: Show how many posts were filtered
- Quick toggle in notification shade or widget
