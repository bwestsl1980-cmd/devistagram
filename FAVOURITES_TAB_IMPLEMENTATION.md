# Favourites Tab Implementation - Collections API

## Overview
Implemented the Favourites tab to work exactly like the Galleries tab, but using the DeviantArt Collections API instead. The default collection is "All" which shows all favourited items.

## Implementation

### 1. API Endpoints Added (DeviantArtApi.kt)
```kotlin
@GET("collections/folders")
suspend fun getCollectionFolders(
    @Header("Authorization") authorization: String,
    @Query("username") username: String,
    @Query("offset") offset: Int? = null,
    @Query("limit") limit: Int = 50
): Response<com.scottapps.devistagram.model.GalleryFoldersResponse>

@GET("collections/{folderid}")
suspend fun getCollectionFolder(
    @retrofit2.http.Path("folderid") folderId: String,
    @Header("Authorization") authorization: String,
    @Query("username") username: String,
    @Query("offset") offset: Int? = null,
    @Query("limit") limit: Int = 24,
    @Query("mature_content") matureContent: Boolean = true
): Response<DailyDeviationsResponse>

@GET("collections/all")
suspend fun getAllCollections(
    @Header("Authorization") authorization: String,
    @Query("username") username: String,
    @Query("offset") offset: Int? = null,
    @Query("limit") limit: Int = 24,
    @Query("mature_content") matureContent: Boolean = true
): Response<DailyDeviationsResponse>
```

### 2. Repository Methods (ProfileRepository.kt)
```kotlin
suspend fun getCollectionFolders(username: String): Result<List<GalleryFolder>>
- Fetches collection folders from /collections/folders
- Automatically prepends "All" folder (folderId = "all") to the list
- Returns "All" folder even if API fails (graceful fallback)

suspend fun getCollectionFolderDeviations(username: String, folderId: String): Result<List<Deviation>>
- If folderId == "all": Uses /collections/all endpoint
- Otherwise: Uses /collections/{folderid} endpoint
- Returns list of deviations from the collection
```

### 3. ViewModel Methods (ProfileViewModel.kt)
```kotlin
private val _collectionFolders = MutableLiveData<List<GalleryFolder>>()
val collectionFolders: LiveData<List<GalleryFolder>>

private val _selectedCollectionDeviations = MutableLiveData<List<Deviation>>()
val selectedCollectionDeviations: LiveData<List<Deviation>>

fun loadCollectionFolders(username: String)
fun loadCollectionFolder(username: String, folderId: String)
```

### 4. Fragment Implementation (ProfileFragment.kt)
```kotlin
- Added collectionFolders variable
- Added observers for collectionFolders and selectedCollectionDeviations
- Implemented setupCollectionSpinner() (mirrors setupGallerySpinner())
- Updated Favourites tab (position 2) to:
  1. Show spinner
  2. Load collection folders if empty
  3. Auto-select "All" collection when loaded
  4. Display collection deviations below spinner
```

## User Flow

### First time entering Favourites tab:
1. Shows "Loading collections..."
2. Fetches collection folders from API
3. Prepends "All" to the folder list
4. Spinner shows "All" as selected
5. Auto-loads all favourited items from /collections/all
6. Displays deviations below spinner

### Switching collections:
1. User taps spinner
2. Sees dropdown: "All", "Folder 1", "Folder 2", etc.
3. Selects a collection
4. Collection deviations load and display
5. Spinner shows selected collection name

### Switching tabs and returning:
1. Collections already loaded
2. Shows last selected collection and its items
3. Spinner displays currently selected collection

## Key Features
✅ Mirrors Galleries tab functionality exactly
✅ Default collection is "All" (shows all favourites)
✅ Uses same Spinner styling (white text, dark background)
✅ Same layout and UX as Galleries tab
✅ Graceful error handling (shows "All" even if API fails)
✅ Auto-loads default collection on first visit
✅ Remembers selected collection when switching tabs

## API Structure
- **GET /collections/folders** - List all collection folders
- **GET /collections/all** - Get all favourited items across all folders
- **GET /collections/{folderid}** - Get items from specific collection folder

## Testing Checklist
- [ ] Favourites tab loads and shows spinner
- [ ] "All" collection auto-loads on first visit
- [ ] Spinner displays "All" as selected
- [ ] Can switch between collections via spinner
- [ ] Deviations display correctly for each collection
- [ ] Empty collections show appropriate message
- [ ] Switching tabs preserves selection
- [ ] Spinner text is visible (white on dark background)

## Files Modified
1. DeviantArtApi.kt - Added 3 collections endpoints
2. ProfileRepository.kt - Added getCollectionFolders() and getCollectionFolderDeviations()
3. ProfileViewModel.kt - Added collection LiveData and load methods
4. ProfileFragment.kt - Added collection logic and setupCollectionSpinner()
