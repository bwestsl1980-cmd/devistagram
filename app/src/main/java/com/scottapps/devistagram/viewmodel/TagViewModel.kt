package com.scottapps.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.scottapps.devistagram.model.Deviation
import com.scottapps.devistagram.repository.DeviantArtRepository
import kotlinx.coroutines.launch

class TagViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = DeviantArtRepository(application)
    
    private val _deviations = MutableLiveData<List<Deviation>>()
    val deviations: LiveData<List<Deviation>> = _deviations
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var currentTag: String? = null
    private var currentOffset: Int? = null
    private var isLoadingMore = false
    private var showMature: Boolean = true
    
    fun searchTag(tag: String, showMature: Boolean = true) {
        if (tag.isBlank()) {
            _deviations.value = emptyList()
            _error.value = "Search for tags to see deviations"
            return
        }
        
        currentTag = tag
        this.showMature = showMature
        currentOffset = null
        _deviations.value = emptyList()
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.browseByTag(tag = tag, offset = currentOffset).fold(
                onSuccess = { newDeviations ->
                    val filteredDeviations = filterMatureContent(newDeviations)
                    _deviations.value = filteredDeviations
                    
                    if (newDeviations.isNotEmpty()) {
                        currentOffset = (currentOffset ?: 0) + newDeviations.size
                    }
                    
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to search tag"
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun loadMore() {
        if (isLoadingMore || _isLoading.value == true || currentTag == null) return
        
        isLoadingMore = true
        viewModelScope.launch {
            repository.browseByTag(tag = currentTag!!, offset = currentOffset).fold(
                onSuccess = { newDeviations ->
                    val currentList = _deviations.value ?: emptyList()
                    val filteredDeviations = filterMatureContent(newDeviations)
                    _deviations.value = currentList + filteredDeviations
                    
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
    
    private fun filterMatureContent(deviations: List<Deviation>): List<Deviation> {
        return if (showMature) {
            deviations
        } else {
            deviations.filter { !it.isMature }
        }
    }
}
