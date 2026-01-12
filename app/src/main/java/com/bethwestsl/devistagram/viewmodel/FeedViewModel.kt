package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.repository.DeviantArtRepository
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = DeviantArtRepository(application)
    
    private val _deviations = MutableLiveData<List<Deviation>>()
    val deviations: LiveData<List<Deviation>> = _deviations
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var currentOffset: Int? = null
    private var isLoadingMore = false
    private var currentBrowseType = "home"
    private var isFavoritesMode = false

    // No init block - let fragment control when to load

    fun setBrowseType(browseType: String) {
        android.util.Log.d("FeedViewModel", "setBrowseType() called")
        android.util.Log.d("FeedViewModel", "  Current type: $currentBrowseType")
        android.util.Log.d("FeedViewModel", "  New type: $browseType")

        if (currentBrowseType != browseType) {
            android.util.Log.d("FeedViewModel", "  → Type CHANGED - will reload")
            currentBrowseType = browseType
            loadDeviations(refresh = true)
        } else {
            android.util.Log.d("FeedViewModel", "  → Type SAME - no reload")
        }
    }
    
    fun loadDeviations(refresh: Boolean = false) {
        android.util.Log.d("FeedViewModel", "═══════════════════════════════════")
        android.util.Log.d("FeedViewModel", "loadDeviations() called")
        android.util.Log.d("FeedViewModel", "  Refresh: $refresh")
        android.util.Log.d("FeedViewModel", "  Browse type: $currentBrowseType")
        android.util.Log.d("FeedViewModel", "  Current offset: $currentOffset")

        // Reset favorites mode when loading regular feed
        isFavoritesMode = false

        if (refresh) {
            android.util.Log.d("FeedViewModel", "  → Resetting offset and clearing list")
            currentOffset = null
            _deviations.value = emptyList()
        }
        
        viewModelScope.launch {
            android.util.Log.d("FeedViewModel", "  → Starting API call...")
            _isLoading.value = true
            _error.value = null
            
            repository.getBrowseContent(
                browseType = currentBrowseType,
                offset = currentOffset
            ).fold(
                onSuccess = { newDeviations ->
                    android.util.Log.d("FeedViewModel", "  → API SUCCESS: Received ${newDeviations.size} deviations")
                    val currentList = _deviations.value ?: emptyList()
                    _deviations.value = if (refresh) {
                        newDeviations
                    } else {
                        currentList + newDeviations
                    }
                    
                    // Update offset for pagination (assuming 24 items per page)
                    if (newDeviations.isNotEmpty()) {
                        currentOffset = (currentOffset ?: 0) + newDeviations.size
                    }
                    
                    android.util.Log.d("FeedViewModel", "  → Setting _deviations.value (will trigger observer)")
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    android.util.Log.e("FeedViewModel", "  → API FAILURE: ${exception.message}")
                    _error.value = exception.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
            )
        }
        android.util.Log.d("FeedViewModel", "═══════════════════════════════════")
    }
    
    fun loadMore() {
        // Don't load more when in favorites mode - favorites feed loads all at once
        if (isFavoritesMode) {
            android.util.Log.d("FeedViewModel", "loadMore() - Skipping, in favorites mode")
            return
        }

        if (isLoadingMore || _isLoading.value == true) return
        
        isLoadingMore = true
        viewModelScope.launch {
            repository.getBrowseContent(
                browseType = currentBrowseType,
                offset = currentOffset
            ).fold(
                onSuccess = { newDeviations ->
                    val currentList = _deviations.value ?: emptyList()
                    _deviations.value = currentList + newDeviations
                    
                    if (newDeviations.isNotEmpty()) {
                        currentOffset = (currentOffset ?: 0) + newDeviations.size
                    }
                    
                    isLoadingMore = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load more"
                    isLoadingMore = false
                }
            )
        }
    }
    
    fun clearError() {
        _error.value = null
    }

    fun loadFavoritesFeed(favoriteUsernames: Set<String>) {
        android.util.Log.d("FeedViewModel", "═══════════════════════════════════")
        android.util.Log.d("FeedViewModel", "loadFavoritesFeed() called")
        android.util.Log.d("FeedViewModel", "  Favorites count: ${favoriteUsernames.size}")
        android.util.Log.d("FeedViewModel", "  Favorites: $favoriteUsernames")

        // Set favorites mode to prevent loadMore from fetching regular feed
        isFavoritesMode = true

        viewModelScope.launch {
            android.util.Log.d("FeedViewModel", "  → Starting favorites API calls...")
            _isLoading.value = true
            _error.value = null

            repository.getFavoritesFeed(favoriteUsernames).fold(
                onSuccess = { deviations ->
                    android.util.Log.d("FeedViewModel", "  → FAVORITES SUCCESS: Received ${deviations.size} deviations")
                    android.util.Log.d("FeedViewModel", "  → Setting _deviations.value (will trigger observer)")
                    _deviations.value = deviations
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    android.util.Log.e("FeedViewModel", "  → FAVORITES FAILURE: ${exception.message}")
                    _error.value = exception.message ?: "Failed to load favorites feed"
                    _isLoading.value = false
                }
            )
        }
        android.util.Log.d("FeedViewModel", "═══════════════════════════════════")
    }

    fun clearData() {
        android.util.Log.d("FeedViewModel", "clearData() - Clearing all ViewModel data")
        _deviations.value = emptyList()
        _error.value = null
        currentOffset = null
        isFavoritesMode = false
    }
}
