package com.scottapps.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.scottapps.devistagram.model.Deviation
import com.scottapps.devistagram.model.GalleryFolder
import com.scottapps.devistagram.model.UserProfile
import com.scottapps.devistagram.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _watchersCount = MutableLiveData<Int>()
    val watchersCount: LiveData<Int> = _watchersCount

    private val _friendsCount = MutableLiveData<Int>()
    val friendsCount: LiveData<Int> = _friendsCount

    private val _deviations = MutableLiveData<List<Deviation>>()
    val deviations: LiveData<List<Deviation>> = _deviations

    private val _deviationsCount = MutableLiveData<Int>()
    val deviationsCount: LiveData<Int> = _deviationsCount

    private val _galleryFolders = MutableLiveData<List<GalleryFolder>>()
    val galleryFolders: LiveData<List<GalleryFolder>> = _galleryFolders

    private val _selectedGalleryDeviations = MutableLiveData<List<Deviation>>()
    val selectedGalleryDeviations: LiveData<List<Deviation>> = _selectedGalleryDeviations

    private val _collectionFolders = MutableLiveData<List<GalleryFolder>>()
    val collectionFolders: LiveData<List<GalleryFolder>> = _collectionFolders

    private val _selectedCollectionDeviations = MutableLiveData<List<Deviation>>()
    val selectedCollectionDeviations: LiveData<List<Deviation>> = _selectedCollectionDeviations

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getCurrentUserProfile().fold(
                onSuccess = { userProfile ->
                    _profile.value = userProfile
                    
                    // Use the stats from the profile
                    _deviationsCount.value = userProfile.stats?.userDeviations ?: 0
                    
                    _isLoading.value = false

                    // Load additional profile data
                    val username = userProfile.actualUsername
                    if (username != null) {
                        loadProfileData(username)
                    }
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load profile"
                    _isLoading.value = false
                }
            )
        }
    }

    private fun loadProfileData(username: String) {
        viewModelScope.launch {
            // Scrape the profile page for actual watchers/watching counts
            repository.scrapeProfileCounts(username).fold(
                onSuccess = { (watchers, watching) ->
                    _watchersCount.value = watchers
                    _friendsCount.value = watching
                },
                onFailure = {
                    // Fallback to API counts if scraping fails
                    loadWatchersFallback(username)
                    loadFriendsFallback(username)
                }
            )

            // Load user deviations (gallery grid)
            repository.getUserDeviations(username).fold(
                onSuccess = { deviations ->
                    _deviations.value = deviations
                },
                onFailure = {
                    _deviations.value = emptyList()
                }
            )
        }
    }
    
    private suspend fun loadWatchersFallback(username: String) {
        repository.getUserWatchersCount(username).fold(
            onSuccess = { count ->
                _watchersCount.value = count
            },
            onFailure = {
                _watchersCount.value = 0
            }
        )
    }
    
    private suspend fun loadFriendsFallback(username: String) {
        repository.getUserFriendsCount(username).fold(
            onSuccess = { count ->
                _friendsCount.value = count
            },
            onFailure = {
                _friendsCount.value = 0
            }
        )
    }

    fun clearError() {
        _error.value = null
    }

    fun loadGalleryFolders(username: String) {
        viewModelScope.launch {
            repository.getGalleryFolders(username).fold(
                onSuccess = { folders ->
                    _galleryFolders.value = folders
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load gallery folders"
                    _galleryFolders.value = emptyList()
                }
            )
        }
    }

    fun loadGalleryFolder(username: String, folderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getGalleryFolderDeviations(username, folderId).fold(
                onSuccess = { deviations ->
                    _selectedGalleryDeviations.value = deviations
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load gallery"
                    _selectedGalleryDeviations.value = emptyList()
                    _isLoading.value = false
                }
            )
        }
    }

    fun loadCollectionFolders(username: String) {
        viewModelScope.launch {
            repository.getCollectionFolders(username).fold(
                onSuccess = { folders ->
                    _collectionFolders.value = folders
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load collection folders"
                    _collectionFolders.value = emptyList()
                }
            )
        }
    }

    fun loadCollectionFolder(username: String, folderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCollectionFolderDeviations(username, folderId).fold(
                onSuccess = { deviations ->
                    _selectedCollectionDeviations.value = deviations
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load collection"
                    _selectedCollectionDeviations.value = emptyList()
                    _isLoading.value = false
                }
            )
        }
    }
}