package com.bethwestsl.devistagram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.format.DateUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bethwestsl.devistagram.databinding.ActivityNoteDetailBinding
import com.bethwestsl.devistagram.model.NoteFolder
import com.bethwestsl.devistagram.repository.NotesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var repository: NotesRepository
    private var noteId: String? = null
    private var folders: List<NoteFolder> = emptyList()

    companion object {
        private const val EXTRA_NOTE_ID = "note_id"

        fun start(context: Context, noteId: String) {
            val intent = Intent(context, NoteDetailActivity::class.java)
            intent.putExtra(EXTRA_NOTE_ID, noteId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = NotesRepository(this)
        noteId = intent.getStringExtra(EXTRA_NOTE_ID)

        // Handle window insets for toolbar
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams
            layoutParams.height = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material) + insets.top
            v.layoutParams = layoutParams
            v.setPadding(0, insets.top, 0, 0)
            windowInsets
        }

        setupToolbar()
        loadNote()
        loadFolders()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    showDeleteConfirmDialog()
                    true
                }
                R.id.action_move_to_folder -> {
                    showMoveToFolderDialog()
                    true
                }
                R.id.action_reply -> {
                    Toast.makeText(this, "Reply feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadNote() {
        val id = noteId ?: return

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            repository.getNote(id).fold(
                onSuccess = { note ->
                    binding.progressBar.visibility = View.GONE

                    // Sender
                    binding.senderTextView.text = "From: ${note.originator?.username ?: "Unknown"}"

                    // Timestamp
                    binding.timestampTextView.text = formatTimestamp(note.timestamp)

                    // Subject
                    binding.subjectTextView.text = note.subject ?: "(No Subject)"

                    // Body - use HTML if available, otherwise plain text
                    if (note.html != null) {
                        binding.bodyTextView.text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(note.html, Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            @Suppress("DEPRECATION")
                            Html.fromHtml(note.html)
                        }
                    } else {
                        binding.bodyTextView.text = note.body ?: ""
                    }
                },
                onFailure = { exception ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@NoteDetailActivity,
                        "Failed to load note: ${exception.message}",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            )
        }
    }

    private fun loadFolders() {
        lifecycleScope.launch {
            repository.getNoteFolders().fold(
                onSuccess = { folderList ->
                    folders = folderList
                },
                onFailure = { exception ->
                    // Silently fail - user can still delete the note
                }
            )
        }
    }

    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp == null) return ""

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(timestamp)

            if (date != null) {
                DateUtils.getRelativeTimeSpanString(
                    date.time,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } else {
                timestamp
            }
        } catch (e: Exception) {
            timestamp
        }
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNote()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNote() {
        val id = noteId ?: return

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            repository.deleteNotes(listOf(id)).fold(
                onSuccess = {
                    Toast.makeText(this@NoteDetailActivity, "Note deleted", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = { exception ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@NoteDetailActivity,
                        "Failed to delete note: ${exception.message}",
                        Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun showMoveToFolderDialog() {
        val id = noteId ?: return

        if (folders.isEmpty()) {
            Toast.makeText(this, "No folders available. Create a folder first.", Toast.LENGTH_SHORT).show()
            return
        }

        val folderNames = folders.map { it.folderName ?: "Unnamed" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Move to Folder")
            .setItems(folderNames) { _, which ->
                val folder = folders[which]
                folder.folderId?.let { folderId ->
                    moveNote(id, folderId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun moveNote(noteId: String, folderId: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            repository.moveNotes(listOf(noteId), folderId).fold(
                onSuccess = {
                    Toast.makeText(this@NoteDetailActivity, "Note moved", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onFailure = { exception ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@NoteDetailActivity,
                        "Failed to move note: ${exception.message}",
                        Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

