package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.repository.CommentsNotificationsRepository
import kotlinx.coroutines.launch

class CommentsNotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CommentsNotificationsRepository(application)

    private val _notifications = MutableLiveData<List<Message>>()
    val notifications: LiveData<List<Message>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val currentNotifications = mutableListOf<Message>()
    private var isLoadingMore = false

    init {
        loadNotifications()
    }

    fun loadNotifications(refresh: Boolean = false) {
        if (_isLoading.value == true && !refresh) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            if (refresh) {
                currentNotifications.clear()
                repository.resetPagination()
            }

            repository.getCommentsNotifications(refresh).fold(
                onSuccess = { newNotifications ->
                    if (refresh) {
                        currentNotifications.clear()
                    }
                    currentNotifications.addAll(newNotifications)
                    _notifications.value = currentNotifications.toList()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load comment notifications"
                    _isLoading.value = false
                }
            )
        }
    }

    fun loadMore() {
        if (isLoadingMore || _isLoading.value == true) return

        isLoadingMore = true
        viewModelScope.launch {
            repository.getCommentsNotifications(refresh = false).fold(
                onSuccess = { newNotifications ->
                    currentNotifications.addAll(newNotifications)
                    _notifications.value = currentNotifications.toList()
                    isLoadingMore = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load more notifications"
                    isLoadingMore = false
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}

