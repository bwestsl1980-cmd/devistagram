package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.repository.NotificationsRepository
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = NotificationsRepository(application)
    
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount
    
    private var currentCursor: String? = null
    private var isLoadingMore = false
    
    init {
        loadMessages()
    }
    
    fun loadMessages(refresh: Boolean = false) {
        if (refresh) {
            currentCursor = null
            _messages.value = emptyList()
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getMessagesFeed(cursor = currentCursor).fold(
                onSuccess = { (newMessages, nextCursor) ->
                    val currentList = _messages.value ?: emptyList()
                    
                    // Deduplicate by stackid to avoid showing the same notification multiple times
                    val allMessages = if (refresh) {
                        newMessages
                    } else {
                        currentList + newMessages
                    }
                    
                    val uniqueMessages = allMessages.distinctBy { it.messageId }
                    _messages.value = uniqueMessages
                    
                    // Update unread count
                    val unreadCount = uniqueMessages.count { !it.isNew }
                    android.util.Log.d("NotificationsViewModel", "Total messages: ${uniqueMessages.size}, Unread: $unreadCount")
                    _unreadCount.value = unreadCount
                    
                    currentCursor = nextCursor
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun loadMore() {
        if (isLoadingMore || _isLoading.value == true || currentCursor == null) return
        
        isLoadingMore = true
        viewModelScope.launch {
            repository.getMessagesFeed(cursor = currentCursor).fold(
                onSuccess = { (newMessages, nextCursor) ->
                    val currentList = _messages.value ?: emptyList()
                    val allMessages = currentList + newMessages
                    
                    // Deduplicate by messageId
                    val uniqueMessages = allMessages.distinctBy { it.messageId }
                    _messages.value = uniqueMessages
                    
                    // Update unread count
                    _unreadCount.value = uniqueMessages.count { !it.isNew }
                    
                    currentCursor = nextCursor
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
}
