package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.model.GalleryFolder
import com.bethwestsl.devistagram.model.UserProfile
import com.bethwestsl.devistagram.repository.ProfileRepository
import com.bethwestsl.devistagram.repository.UserWatchRepository
import com.bethwestsl.devistagram.auth.TokenManager
import kotlinx.coroutines.launch

class OtherUserProfileViewModel(application: Application, private val username: String) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository(application)
    private val watchRepository = UserWatchRepository()
    private val tokenManager = TokenManager(application)

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

    private val _isWatching = MutableLiveData<Boolean>()
    val isWatching: LiveData<Boolean> = _isWatching

    init {
        loadProfile()
        checkWatchStatus()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            profileRepository.getUserProfile(username).fold(
                onSuccess = { userProfile ->
                    _profile.value = userProfile

                    // Use the stats from the profile
                    _deviationsCount.value = userProfile.stats?.userDeviations ?: 0

                    _isLoading.value = false

                    // Load additional profile data
                    loadProfileData(username)
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
            profileRepository.scrapeProfileCounts(username).fold(
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
            profileRepository.getUserDeviations(username).fold(
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
        profileRepository.getUserWatchersCount(username).fold(
            onSuccess = { count ->
                _watchersCount.value = count
            },
            onFailure = {
                _watchersCount.value = 0
            }
        )
    }

    private suspend fun loadFriendsFallback(username: String) {
        profileRepository.getUserFriendsCount(username).fold(
            onSuccess = { count ->
                _friendsCount.value = count
            },
            onFailure = {
                _friendsCount.value = 0
            }
        )
    }

    private fun checkWatchStatus() {
        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken() ?: return@launch

            watchRepository.isWatchingUser(username, accessToken).fold(
                onSuccess = { isWatching ->
                    _isWatching.value = isWatching
                },
                onFailure = {
                    _isWatching.value = false
                }
            )
        }
    }

    fun toggleWatch() {
        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken() ?: return@launch
            val currentWatchStatus = _isWatching.value ?: false

            watchRepository.toggleWatchUser(username, accessToken, currentWatchStatus).fold(
                onSuccess = { success ->
                    if (success) {
                        _isWatching.value = !currentWatchStatus
                    }
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to update watch status"
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun loadGalleryFolders(username: String) {
        viewModelScope.launch {
            profileRepository.getGalleryFolders(username).fold(
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
            profileRepository.getGalleryFolderContent(username, folderId).fold(
                onSuccess = { deviations ->
                    _selectedGalleryDeviations.value = deviations
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load gallery"
                    _selectedGalleryDeviations.value = emptyList()
                }
            )
        }
    }

    fun loadCollectionFolders(username: String) {
        viewModelScope.launch {
            profileRepository.getCollectionFolders(username).fold(
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
            profileRepository.getCollectionFolderContent(username, folderId).fold(
                onSuccess = { deviations ->
                    _selectedCollectionDeviations.value = deviations
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load collection"
                    _selectedCollectionDeviations.value = emptyList()
                }
            )
        }
    }
}

// ViewModelFactory for passing username parameter
class OtherUserProfileViewModelFactory(
    private val application: Application,
    private val username: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OtherUserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OtherUserProfileViewModel(application, username) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

