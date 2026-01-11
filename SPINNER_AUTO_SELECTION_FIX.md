# Spinner Auto-Selection Fix

## Problem
When landing on the Galleries tab, the Spinner was automatically selecting the first gallery ("Featured") and loading its deviations, instead of showing a prompt.

## Root Cause
Android Spinners automatically select the first item (position 0) when the adapter is populated. Since "Featured" was the first gallery folder, it was being auto-selected and triggering the `onItemSelected` callback.

## Solution
Added a prompt item "Select a gallery..." at position 0, so when the Spinner is first displayed:
1. It shows "Select a gallery..." (position 0)
2. The empty state message is shown
3. No gallery deviations are loaded automatically

## Code Changes

### setupGallerySpinner() - Added Prompt Item
```kotlin
// BEFORE:
val folderNames = galleryFolders.map { it.name ?: "Unnamed" }

// AFTER:
val folderNames = mutableListOf("Select a gallery...")
folderNames.addAll(galleryFolders.map { it.name ?: "Unnamed" })
```

### onItemSelected() - Skip Prompt Item
```kotlin
override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    // Skip the first item (prompt)
    if (position == 0) {
        // Show prompt message
        binding.emptyStateTextView.text = "Select a gallery from the dropdown above"
        binding.emptyStateTextView.visibility = View.VISIBLE
        binding.deviationsRecyclerView.visibility = View.GONE
        return
    }
    
    // Adjust index to account for prompt item
    val folderIndex = position - 1
    val selectedFolder = galleryFolders[folderIndex]
    currentUsername?.let { username ->
        selectedFolder.folderId?.let { folderId ->
            viewModel.loadGalleryFolder(username, folderId)
        }
    }
}
```

## Result
✅ Spinner now shows "Select a gallery..." on first display
✅ Empty state message is shown: "Select a gallery from the dropdown above"
✅ No gallery is auto-loaded
✅ When user selects an actual gallery (positions 1+), it loads correctly
✅ Index is adjusted (-1) to match the actual gallery folder list

## User Flow
1. User taps "Galleries" tab
2. Spinner appears showing "Select a gallery..."
3. Empty state shows: "Select a gallery from the dropdown above"
4. User taps spinner and sees dropdown list:
   - Select a gallery...
   - Featured
   - What the Subscribers Saw - 2025 1st Half
   - Beth's Out of Court Settlement
   - etc.
5. User selects "Featured"
6. Gallery deviations load and display below the spinner
7. Spinner continues to show "Featured" as selected
