package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.model.SearchUser
import com.bethwestsl.devistagram.repository.DeviantArtRepository
import kotlinx.coroutines.launch

class UserSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DeviantArtRepository(application)

    private val _users = MutableLiveData<List<SearchUser>>()
    val users: LiveData<List<SearchUser>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _users.value = emptyList()
            _error.value = "Search for users to follow"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.searchUsers(query = query).fold(
                onSuccess = { users ->
                    _users.value = users

                    if (users.isEmpty()) {
                        _error.value = "No users found for \"$query\""
                    }

                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to search users"
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}

