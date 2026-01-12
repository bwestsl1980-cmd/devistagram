package com.bethwestsl.devistagram.repository

import android.content.Context
import android.util.Log
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.model.*
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.contentApi

    suspend fun getNotes(
        folderId: String? = null,
        offset: Int? = null
    ): Result<NotesResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Fetching notes for folder: $folderId, offset: $offset")

            val response = api.getNotes(
                authorization = "Bearer $accessToken",
                folderId = folderId,
                offset = offset,
                limit = 50
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d("NotesRepository", "Loaded ${response.body()!!.results?.size ?: 0} notes")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch notes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception fetching notes", e)
            Result.failure(e)
        }
    }

    suspend fun getNote(noteId: String): Result<Note> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            val response = api.getNote(
                noteId = noteId,
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch note: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception fetching note", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNotes(noteIds: List<String>): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Deleting ${noteIds.size} notes")

            val response = api.deleteNotes(
                authorization = "Bearer $accessToken",
                noteIds = noteIds
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d("NotesRepository", "Notes deleted successfully")
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to delete notes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception deleting notes", e)
            Result.failure(e)
        }
    }

    suspend fun getNoteFolders(): Result<List<NoteFolder>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Fetching note folders")

            val response = api.getNoteFolders(
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val folders = response.body()!!.results ?: emptyList()
                Log.d("NotesRepository", "Loaded ${folders.size} folders")
                Result.success(folders)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to fetch folders: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception fetching folders", e)
            Result.failure(e)
        }
    }

    suspend fun createFolder(folderName: String): Result<CreateFolderResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Creating folder: $folderName")

            val response = api.createNoteFolder(
                authorization = "Bearer $accessToken",
                folderName = folderName
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d("NotesRepository", "Folder created successfully")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to create folder: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception creating folder", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFolder(folderId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Deleting folder: $folderId")

            val response = api.deleteNoteFolder(
                folderId = folderId,
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d("NotesRepository", "Folder deleted successfully")
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to delete folder: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception deleting folder", e)
            Result.failure(e)
        }
    }

    suspend fun renameFolder(folderId: String, newName: String): Result<CreateFolderResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Renaming folder: $folderId to $newName")

            val response = api.renameNoteFolder(
                folderId = folderId,
                authorization = "Bearer $accessToken",
                newName = newName
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d("NotesRepository", "Folder renamed successfully")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to rename folder: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception renaming folder", e)
            Result.failure(e)
        }
    }

    suspend fun moveNotes(noteIds: List<String>, folderId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Moving ${noteIds.size} notes to folder: $folderId")

            val response = api.moveNotes(
                authorization = "Bearer $accessToken",
                noteIds = noteIds,
                folderId = folderId
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d("NotesRepository", "Notes moved successfully")
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to move notes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception moving notes", e)
            Result.failure(e)
        }
    }

    suspend fun sendNote(
        recipients: List<String>,
        subject: String,
        body: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("Not logged in"))

            Log.d("NotesRepository", "Sending note to ${recipients.size} recipients")

            val response = api.sendNote(
                authorization = "Bearer $accessToken",
                recipients = recipients,
                subject = subject,
                body = body
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d("NotesRepository", "Note sent successfully")
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("NotesRepository", "API Error: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to send note: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("NotesRepository", "Exception sending note", e)
            Result.failure(e)
        }
    }
}

