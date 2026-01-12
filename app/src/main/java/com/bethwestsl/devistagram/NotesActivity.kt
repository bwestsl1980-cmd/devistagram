package com.bethwestsl.devistagram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bethwestsl.devistagram.adapter.NotesAdapter
import com.bethwestsl.devistagram.databinding.ActivityNotesBinding
import com.bethwestsl.devistagram.model.Note
import com.bethwestsl.devistagram.model.NoteFolder
import com.bethwestsl.devistagram.viewmodel.NotesViewModel

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var notesAdapter: NotesAdapter
    private var folders: List<NoteFolder> = emptyList()
    private val selectedNotes = mutableSetOf<String>()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NotesActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        setupRecyclerView()
        setupViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            if (selectedNotes.isNotEmpty()) {
                clearSelection()
            } else {
                finish()
            }
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_manage_folders -> {
                    showManageFoldersDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onNoteClick = { note ->
                // Open note detail
                note.noteId?.let { noteId ->
                    NoteDetailActivity.start(this, noteId)
                }
            },
            onNoteLongClick = { note ->
                // Start selection mode
                note.noteId?.let { noteId ->
                    toggleNoteSelection(noteId)
                    updateToolbarForSelection()
                }
                true
            },
            isSelectionMode = { selectedNotes.isNotEmpty() },
            isNoteSelected = { note -> selectedNotes.contains(note.noteId) },
            onSelectionToggle = { note ->
                note.noteId?.let { noteId ->
                    toggleNoteSelection(noteId)
                    updateToolbarForSelection()
                }
            }
        )

        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotesActivity)
            adapter = notesAdapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        viewModel.notes.observe(this) { notes ->
            if (notes.isNotEmpty()) {
                notesAdapter.submitList(notes)
                binding.notesRecyclerView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.GONE
            } else {
                binding.notesRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.VISIBLE
            }
        }

        viewModel.folders.observe(this) { folderList ->
            folders = folderList
            setupFolderSpinner()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.operationSuccess.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.clearOperationSuccess()
                clearSelection()
            }
        }
    }

    private fun setupFolderSpinner() {
        // Add "Inbox" as first item
        val folderNames = mutableListOf("Inbox")
        folderNames.addAll(folders.map { it.folderName ?: "Unnamed" })

        val adapter = ArrayAdapter(this, R.layout.spinner_item_toolbar, folderNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.folderSpinner.adapter = adapter

        binding.folderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Inbox - no folder ID
                    viewModel.selectFolder(null)
                } else {
                    // Custom folder
                    val folder = folders[position - 1]
                    viewModel.selectFolder(folder)
                }
                clearSelection()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun toggleNoteSelection(noteId: String) {
        if (selectedNotes.contains(noteId)) {
            selectedNotes.remove(noteId)
        } else {
            selectedNotes.add(noteId)
        }
        notesAdapter.notifyDataSetChanged()
    }

    private fun clearSelection() {
        selectedNotes.clear()
        notesAdapter.notifyDataSetChanged()
        updateToolbarForSelection()
    }

    private fun updateToolbarForSelection() {
        if (selectedNotes.isNotEmpty()) {
            binding.toolbar.title = "${selectedNotes.size} selected"
            binding.toolbar.menu.clear()
            binding.toolbar.inflateMenu(R.menu.notes_selection_menu)
            binding.toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        showDeleteNotesConfirmDialog()
                        true
                    }
                    R.id.action_move_to_folder -> {
                        showMoveToFolderDialog()
                        true
                    }
                    else -> false
                }
            }
        } else {
            binding.toolbar.title = "Mail"
            binding.toolbar.menu.clear()
            binding.toolbar.inflateMenu(R.menu.notes_menu)
            binding.toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_manage_folders -> {
                        showManageFoldersDialog()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onBackPressed() {
        if (selectedNotes.isNotEmpty()) {
            clearSelection()
        } else {
            super.onBackPressed()
        }
    }

    private fun showManageFoldersDialog() {
        val options = arrayOf("Create Folder", "Rename Folder", "Delete Folder")

        AlertDialog.Builder(this)
            .setTitle("Manage Folders")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showCreateFolderDialog()
                    1 -> showRenameFolderDialog()
                    2 -> showDeleteFolderDialog()
                }
            }
            .show()
    }

    private fun showCreateFolderDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Folder name"

        AlertDialog.Builder(this)
            .setTitle("Create Folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    viewModel.createFolder(folderName)
                } else {
                    Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameFolderDialog() {
        if (folders.isEmpty()) {
            Toast.makeText(this, "No folders to rename", Toast.LENGTH_SHORT).show()
            return
        }

        val folderNames = folders.map { it.folderName ?: "Unnamed" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Folder to Rename")
            .setItems(folderNames) { _, which ->
                val folder = folders[which]
                showRenameInputDialog(folder)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameInputDialog(folder: NoteFolder) {
        val input = android.widget.EditText(this)
        input.hint = "New folder name"
        input.setText(folder.folderName)

        AlertDialog.Builder(this)
            .setTitle("Rename Folder")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    folder.folderId?.let { folderId ->
                        viewModel.renameFolder(folderId, newName)
                    }
                } else {
                    Toast.makeText(this, "Please enter a folder name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteFolderDialog() {
        if (folders.isEmpty()) {
            Toast.makeText(this, "No folders to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val folderNames = folders.map { it.folderName ?: "Unnamed" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Folder to Delete")
            .setItems(folderNames) { _, which ->
                val folder = folders[which]
                showDeleteConfirmDialog(folder)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(folder: NoteFolder) {
        AlertDialog.Builder(this)
            .setTitle("Delete Folder")
            .setMessage("Are you sure you want to delete '${folder.folderName}'? Notes in this folder will be moved to Inbox.")
            .setPositiveButton("Delete") { _, _ ->
                folder.folderId?.let { folderId ->
                    viewModel.deleteFolder(folderId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteNotesConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Notes")
            .setMessage("Are you sure you want to delete ${selectedNotes.size} note(s)?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNotes(selectedNotes.toList())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoveToFolderDialog() {
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
                    viewModel.moveNotes(selectedNotes.toList(), folderId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

