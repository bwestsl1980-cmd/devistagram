# Default Gallery Selection Feature

## Overview
This feature allows you to select which gallery displays in the Feed tab for everyone viewing your profile. A spinner above the tabs lets you choose your default gallery.

## Implementation Details

### User Experience

#### On Your Own Profile (ProfileFragment)
1. **Default Gallery Spinner**: Located above the Feed/Galleries/Favourites tabs
2. **Label**: "Default Gallery"
3. **Description**: "This gallery will be shown in your Feed tab"
4. **Selection**: Choose any of your galleries from the dropdown
5. **Auto-save**: Your selection is automatically saved
6. **Feed Tab**: Shows a spinner with your selected default gallery pre-selected
7. **Galleries Tab**: Shows a spinner starting with "Featured" gallery (independent)
8. **Favourites Tab**: Shows collections as before

#### When Others View Your Profile (OtherUserProfileActivity)
1. **Feed Tab**: Shows a spinner with your selected default gallery pre-selected
2. **Galleries Tab**: Shows a spinner starting with "Featured"
3. **Favourites Tab**: Shows collections as before
4. If you haven't set a default, the Feed tab spinner will start with "Featured"

### Technical Implementation

#### Files Modified

**1. fragment_profile.xml**
- Added `defaultGalleryLabel` TextView - "Default Gallery" title
- Added `defaultGalleryDescription` TextView - explains the feature
- Added `defaultGallerySpinner` Spinner - positioned above the tabs for gallery selection

**2. ProfileFragment.kt**
- Added `defaultGalleryFolders` variable to store galleries for the selector
- Modified gallery folders observer to populate both spinners
- Added `setupDefaultGallerySpinner()` method:
  - Populates the default gallery spinner above tabs
  - Loads saved preference and pre-selects it
  - Saves selection to SharedPreferences when changed
  - Updates the Feed tab spinner when selection changes
- Modified `setupFeedGallerySpinner()` method:
  - Loads the saved default gallery preference
  - Pre-selects it in the Feed tab spinner
  - Allows browsing all galleries
- Feed tab spinner reflects the default gallery selection
- Galleries tab spinner always starts with "Featured"

**3. OtherUserProfileActivity.kt**
- `setupFeedGallerySpinner()` loads the profile owner's default gallery
- Feed tab spinner shows the saved default
- Galleries tab works independently

### Storage Mechanism

The feature uses Android's SharedPreferences to save your default gallery selection:

**Key**: `app_prefs` (shared preferences file)

**Values**:
- `default_gallery_id`: The folder ID of your selected default gallery
- `default_gallery_username`: Your username (to ensure the correct profile loads the correct default)

### Flow Diagram

```
User Opens Their Profile
    ↓
See "Default Gallery" spinner above tabs
    ↓
Spinner shows currently saved default (or "Featured" if first time)
    ↓
User selects a different gallery from the default gallery spinner
    ↓
Save to SharedPreferences:
  - default_gallery_id = selected gallery's folderId
  - default_gallery_username = current user's username
    ↓
Feed tab spinner updates to show the new default gallery
    ↓
When user clicks Feed tab:
    ↓
Feed tab spinner shows the default gallery selected
Gallery content loads and displays
    ↓
When another user views this profile:
    ↓
Feed tab opens
    ↓
Check SharedPreferences for this user's default gallery
    ↓
If saved: Feed tab spinner shows that gallery selected
If not saved: Feed tab spinner shows "Featured" selected
    ↓
Galleries tab always shows "Featured" first (independent of default)
```

### Benefits

1. **Clear Control**: Dedicated spinner above tabs makes it obvious where to set your default
2. **Universal**: Your selection applies to everyone viewing your Feed tab (including you)
3. **Simple**: One place to set, one place to see the result
4. **Flexible**: Can be changed anytime from the default gallery spinner
5. **Persistent**: The setting is saved across app sessions

### Future Enhancements

Potential improvements for future versions:
- Allow selecting different defaults for different contexts (logged-in vs. logged-out viewers)
- Option to set a default collection as well
- UI to show which gallery is currently set as default
- Sync this preference to a backend server for multi-device support

## Testing

To test this feature:

1. **Login to your account**
2. **Go to Profile tab**
3. **See the "Default Gallery" spinner above the Feed/Galleries/Favourites tabs**
4. **Current default is shown** (or "Featured" if first time)
5. **Select a different gallery** from this default gallery spinner (e.g., "Scraps")
6. **Click on the Feed tab** (or it's already selected)
7. **Verify the Feed tab spinner shows "Scraps" selected**
8. **Verify the gallery content loaded**
9. **Click on Galleries tab**
10. **Verify it shows "Featured" selected** (independent)
11. **Click back to Feed tab**
12. **Verify "Scraps" is still selected**
13. **Close and reopen the app**
14. **Go to Profile**
15. **Verify the default gallery spinner shows "Scraps" selected**
16. **Verify the Feed tab shows "Scraps" selected**
17. **View your profile as another user**
18. **Verify Feed tab shows "Scraps" selected**

## Files Changed

- `app/src/main/res/layout/fragment_profile.xml` - Added default gallery selector UI above tabs
- `app/src/main/java/com/scottapps/devistagram/fragment/ProfileFragment.kt` - Logic for saving/loading default
- `app/src/main/java/com/scottapps/devistagram/OtherUserProfileActivity.kt` - Feed tab loads saved selection

## Dependencies

No new dependencies required - uses existing Android SharedPreferences API.

