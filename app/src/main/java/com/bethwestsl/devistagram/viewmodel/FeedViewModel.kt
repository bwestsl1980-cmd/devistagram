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
    
    init {
        loadDeviations()
    }
    
    fun setBrowseType(browseType: String) {
        if (currentBrowseType != browseType) {
            currentBrowseType = browseType
            loadDeviations(refresh = true)
        }
    }
    
    fun loadDeviations(refresh: Boolean = false) {
        if (refresh) {
            currentOffset = null
            _deviations.value = emptyList()
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getBrowseContent(
                browseType = currentBrowseType,
                offset = currentOffset
            ).fold(
                onSuccess = { newDeviations ->
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
}
