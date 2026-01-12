package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.launch

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _deviations = MutableLiveData<List<Deviation>>()
    val deviations: LiveData<List<Deviation>> = _deviations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentOffset: Int? = null
    private var hasMore = true
    private val allDeviations = mutableListOf<Deviation>()

    init {
        loadDeviations()
    }

    fun loadDeviations(refresh: Boolean = false) {
        if (_isLoading.value == true) return

        if (refresh) {
            currentOffset = null
            hasMore = true
            allDeviations.clear()
        }

        if (!hasMore && !refresh) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    _error.value = "Not authenticated"
                    _isLoading.value = false
                    return@launch
                }

                val response = RetrofitClient.contentApi.getBrowseContent(
                    browseType = "home",
                    authorization = "Bearer $token",
                    offset = currentOffset,
                    limit = 24,
                    matureContent = true
                )

                _isLoading.value = false

                if (response.isSuccessful) {
                    val results = response.body()?.results ?: emptyList()
                    val offset = response.body()?.nextOffset

                    if (refresh) {
                        allDeviations.clear()
                    }

                    allDeviations.addAll(results)
                    _deviations.value = allDeviations.toList()

                    currentOffset = offset
                    hasMore = response.body()?.hasMore == true
                } else {
                    _error.value = "Failed to load discover content: ${response.code()}"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun loadMore() {
        if (!hasMore || _isLoading.value == true) return
        loadDeviations(refresh = false)
    }

    fun clearError() {
        _error.value = null
    }
}

