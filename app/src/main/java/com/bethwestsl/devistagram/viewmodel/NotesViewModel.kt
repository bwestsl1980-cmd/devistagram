package com.bethwestsl.devistagram.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bethwestsl.devistagram.model.Note
import com.bethwestsl.devistagram.model.NoteFolder
import com.bethwestsl.devistagram.repository.NotesRepository
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(application)

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _folders = MutableLiveData<List<NoteFolder>>()
    val folders: LiveData<List<NoteFolder>> = _folders

    private val _selectedFolder = MutableLiveData<NoteFolder?>()
    val selectedFolder: LiveData<NoteFolder?> = _selectedFolder

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    init {
        loadFolders()
        loadNotes(null) // Load inbox by default
    }

    fun loadNotes(folderId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getNotes(folderId).fold(
                onSuccess = { response ->
                    _notes.value = response.results ?: emptyList()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load notes"
                    _notes.value = emptyList()
                    _isLoading.value = false
                }
            )
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            repository.getNoteFolders().fold(
                onSuccess = { folderList ->
                    _folders.value = folderList
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load folders"
                    _folders.value = emptyList()
                }
            )
        }
    }

    fun selectFolder(folder: NoteFolder?) {
        _selectedFolder.value = folder
        loadNotes(folder?.folderId)
    }

    fun deleteNotes(noteIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.deleteNotes(noteIds).fold(
                onSuccess = {
                    _operationSuccess.value = "Notes deleted successfully"
                    // Reload notes after deletion
                    loadNotes(_selectedFolder.value?.folderId)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to delete notes"
                    _isLoading.value = false
                }
            )
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.createFolder(folderName).fold(
                onSuccess = {
                    _operationSuccess.value = "Folder created successfully"
                    loadFolders()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to create folder"
                    _isLoading.value = false
                }
            )
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.deleteFolder(folderId).fold(
                onSuccess = {
                    _operationSuccess.value = "Folder deleted successfully"
                    _selectedFolder.value = null
                    loadFolders()
                    loadNotes(null) // Return to inbox
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to delete folder"
                    _isLoading.value = false
                }
            )
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.renameFolder(folderId, newName).fold(
                onSuccess = {
                    _operationSuccess.value = "Folder renamed successfully"
                    loadFolders()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to rename folder"
                    _isLoading.value = false
                }
            )
        }
    }

    fun moveNotes(noteIds: List<String>, folderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.moveNotes(noteIds, folderId).fold(
                onSuccess = {
                    _operationSuccess.value = "Notes moved successfully"
                    // Reload notes after moving
                    loadNotes(_selectedFolder.value?.folderId)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to move notes"
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearOperationSuccess() {
        _operationSuccess.value = null
    }
}

