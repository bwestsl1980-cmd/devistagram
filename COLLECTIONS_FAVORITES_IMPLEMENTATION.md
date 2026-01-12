# Collections/Favorites Feature Implementation

## Summary
Implemented full collections (favorites) functionality for the deviation detail view, allowing users to:
- Add deviations to their collection folders
- View their collection folders
- Create new collection folders
- Remove deviations from favorites
- See favorite status on deviations

## Files Created

### 1. Collection.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/model/Collection.kt`

**Purpose:** Data models for collections/favorites API

**Models:**
- `CollectionFoldersResponse` - Response for fetching user's collection folders
- `CollectionFolder` - Individual collection folder data
- `FaveDeviationRequest` - Request body for favoriting (unused, using query params)
- `FaveDeviationResponse` - Response after favoriting
- `UnfaveDeviationResponse` - Response after unfavoriting
- `CreateCollectionFolderRequest` - Request for creating folder (unused, using query params)
- `CreateCollectionFolderResponse` - Response after creating folder

### 2. CollectionsRepository.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/repository/CollectionsRepository.kt`

**Purpose:** Repository class to handle all collection-related API calls

**Methods:**
- `getCollectionFolders(accessToken, username)` - Fetch user's collection folders
- `faveDeviation(deviationId, accessToken, folderId)` - Add deviation to favorites/collection
- `unfaveDeviation(deviationId, accessToken)` - Remove deviation from favorites
- `createCollectionFolder(folderName, accessToken)` - Create new collection folder

### 3. CollectionFolderAdapter.kt
**Path:** `app/src/main/java/com/scottapps/devistagram/adapter/CollectionFolderAdapter.kt`

**Purpose:** RecyclerView adapter for displaying collection folders in selection dialog

**Features:**
- Display folder thumbnail
- Show folder name and item count
- Clickable items for folder selection

### 4. item_collection_folder.xml
**Path:** `app/src/main/res/layout/item_collection_folder.xml`

**Purpose:** Layout for collection folder list items

**UI Components:**
- Folder thumbnail (56x56dp)
- Folder name
- Item count (e.g., "42 items")

## Files Modified

### 1. DeviantArtApi.kt
**Added API Endpoints:**
- `GET /collections/folders` - Fetch user's collection folders
- `POST /collections/fave` - Add deviation to favorites/collection
- `POST /collections/unfave` - Remove deviation from favorites
- `POST /collections/folders/create` - Create new collection folder

### 2. DeviationDetailActivity.kt
**Added:**
- `collectionsRepository: CollectionsRepository` - Repository instance
- `isFavorited: Boolean` - Track if deviation is favorited

**New Methods:**
- `showFavoriteFolderDialog()` - Main entry point for favorite action
- `showFolderSelectionDialog(folders)` - Show folder picker dialog
- `showCreateFolderDialog()` - Show create folder dialog
- `createFolderAndFavorite(folderName)` - Create folder and add to it
- `favoriteDeviation(folderId)` - Add deviation to collection
- `unfavoriteDeviation()` - Remove deviation from favorites
- `updateFavoriteButton()` - Update button text based on favorite status

**Modified:**
- `onCreate()` - Initialize CollectionsRepository
- `setupButtons()` - Changed favorite button to call showFavoriteFolderDialog()
- `displayDeviation()` - Set initial favorite status and update button

## API Endpoints Used

### Get Collection Folders
```
GET /collections/folders
Authorization: Bearer {access_token}
Query Parameters:
  - username: String (optional, defaults to current user)
  - calculate_size: Boolean (default: true)
  - offset: Int (optional)
  - limit: Int (default: 50)

Response:
{
  "results": [CollectionFolder],
  "has_more": boolean,
  "next_offset": int
}
```

### Add to Favorites
```
POST /collections/fave
Authorization: Bearer {access_token}
Query Parameters:
  - deviationid: String (required)
  - folderid: String (optional, defaults to main favorites)

Response:
{
  "success": boolean
}
```

### Remove from Favorites
```
POST /collections/unfave
Authorization: Bearer {access_token}
Query Parameters:
  - deviationid: String (required)

Response:
{
  "success": boolean
}
```

### Create Collection Folder
```
POST /collections/folders/create
Authorization: Bearer {access_token}
Query Parameters:
  - folder: String (folder name)

Response:
{
  "folderid": "uuid",
  "name": "folder name"
}
```

## User Experience Flow

### Favoriting a Deviation

1. **User clicks "Favorite" button:**
   - If already favorited: Shows confirmation dialog to remove
   - If not favorited: Shows loading dialog while fetching folders

2. **Loading folders:**
   - Fetches user's collection folders from API
   - If no folders exist: Shows "Create Folder" dialog
   - If folders exist: Shows folder selection dialog

3. **Folder selection dialog:**
   - Lists all collection folders with thumbnails and item counts
   - Option at bottom: "Create New Folder..."
   - User can select existing folder or create new one

4. **Adding to collection:**
   - Sends API request to add deviation to selected folder
   - Shows success toast: "Added to favorites!"
   - Updates button text to "Favorited"

### Creating New Folder

5. **User selects "Create New Folder...":**
   - Shows dialog with text input
   - User enters folder name
   - Clicks "Create"

6. **Creating folder:**
   - API creates new collection folder
   - Shows success toast: "Folder created!"
   - Automatically adds deviation to new folder
   - Shows success toast: "Added to favorites!"

### Unfavoriting

7. **User clicks "Favorited" button:**
   - Shows confirmation dialog: "Remove from Favorites"
   - User confirms removal

8. **Removing from favorites:**
   - API removes deviation from all collections
   - Shows success toast: "Removed from favorites"
   - Updates button text back to "Favorite"

## Button States

The favorite button displays different text based on state:
- **"Favorite"** - Deviation is not in favorites (default)
- **"Favorited"** - Deviation is already in favorites

## Initial State

When the deviation loads:
1. The `isFavourited` field from the Deviation model is checked
2. The button text is set accordingly
3. User sees current favorite status immediately

## Dialog Flow

```
Click "Favorite"
    |
    ├─ Already favorited?
    |   └─ Show unfavorite confirmation
    |       └─ Confirm → Unfavorite → Update UI
    |
    └─ Not favorited
        └─ Load folders
            ├─ No folders
            |   └─ Show create folder dialog
            |       └─ Create folder → Favorite to new folder
            |
            └─ Has folders
                └─ Show folder selection
                    ├─ Select folder → Favorite to folder
                    └─ "Create New..." → Create dialog → Favorite to new folder
```

## Error Handling

- **Failed to load folders:** Toast shows "Failed to load folders"
- **Failed to favorite:** Toast shows "Failed to favorite"
- **Failed to unfavorite:** Toast shows "Failed to unfavorite"
- **Failed to create folder:** Toast shows "Failed to create folder"
- **Empty folder name:** Toast shows "Folder name cannot be empty"
- All errors are logged to LogCat for debugging

## UI Features

- **Folder Thumbnails:** Shows preview of folder contents
- **Item Counts:** Displays number of deviations in each folder
- **Loading Indicators:** Dialog shows "Loading folders..." while fetching
- **Confirmation Dialogs:** Asks for confirmation before removing favorites
- **Auto-close:** Dialogs automatically close after successful actions
- **Responsive:** Button updates immediately after favoriting/unfavoriting

## Notes

- Users can favorite to specific folders or default favorites
- Creating a folder automatically adds the deviation to it
- Unfavoriting removes from ALL folders (DeviantArt API limitation)
- The `isFavourited` field indicates if deviation is in any collection
- Folder thumbnails are loaded with Coil image library
- Empty state handled with "Create New Folder" option
- All operations use coroutines for async execution

## Future Enhancements

Possible improvements:
- Move deviation between folders
- View which folder(s) contain the deviation
- Bulk favorite/unfavorite operations
- Folder management (rename, delete)
- Custom folder ordering
- Recently used folders at top
- Search folders by name

## Integration

The favorite feature is now fully integrated into the deviation detail view:
1. Favorite button is always visible
2. Status is automatically loaded with deviation
3. One-click favoriting with folder selection
4. Visual feedback for all actions
5. Error handling for edge cases

