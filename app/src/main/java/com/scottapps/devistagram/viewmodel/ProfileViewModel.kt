package com.scottapps.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.scottapps.devistagram.model.Deviation
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
                    _isLoading.value = false

                    // Load all profile data
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
            // Load watchers count
            repository.getUserWatchersCount(username).fold(
                onSuccess = { count ->
                    _watchersCount.value = count
                },
                onFailure = {
                    _watchersCount.value = 0
                }
            )

            // Load friends count
            repository.getUserFriendsCount(username).fold(
                onSuccess = { count ->
                    _friendsCount.value = count
                },
                onFailure = {
                    _friendsCount.value = 0
                }
            )

            // Load user deviations
            repository.getUserDeviations(username).fold(
                onSuccess = { deviations ->
                    _deviations.value = deviations
                    _deviationsCount.value = deviations.size
                },
                onFailure = {
                    _deviations.value = emptyList()
                    _deviationsCount.value = 0
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}