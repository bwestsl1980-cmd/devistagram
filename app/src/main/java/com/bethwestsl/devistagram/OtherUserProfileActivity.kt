package com.bethwestsl.devistagram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.adapter.DeviationGridAdapter
import com.bethwestsl.devistagram.databinding.ActivityOtherUserProfileBinding
import com.bethwestsl.devistagram.model.GalleryFolder
import com.bethwestsl.devistagram.viewmodel.OtherUserProfileViewModel
import com.bethwestsl.devistagram.viewmodel.OtherUserProfileViewModelFactory

class OtherUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtherUserProfileBinding
    private lateinit var viewModel: OtherUserProfileViewModel
    private lateinit var deviationsAdapter: DeviationGridAdapter
    private var galleryFolders: List<GalleryFolder> = emptyList()
    private var collectionFolders: List<GalleryFolder> = emptyList()
    private var currentUsername: String? = null

    companion object {
        private const val EXTRA_USERNAME = "username"

        fun start(context: Context, username: String) {
            val intent = Intent(context, OtherUserProfileActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username == null) {
            Toast.makeText(this, "Error: Username not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUsername = username

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
        setupViewModel(username)
        setupTabs()
        setupWatchButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        deviationsAdapter = DeviationGridAdapter { deviation ->
            DeviationDetailActivity.start(this, deviation)
        }

        binding.deviationsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@OtherUserProfileActivity, 3)
            adapter = deviationsAdapter
        }
    }

    private fun setupViewModel(username: String) {
        val factory = OtherUserProfileViewModelFactory(application, username)
        viewModel = ViewModelProvider(this, factory)[OtherUserProfileViewModel::class.java]

        viewModel.profile.observe(this) { profile ->
            if (profile != null) {
                // Avatar
                binding.avatarImageView.load(profile.actualUserIcon) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(android.R.drawable.ic_menu_myplaces)
                    error(android.R.drawable.ic_menu_myplaces)
                }

                // Username
                binding.usernameTextView.text = profile.actualUsername
                binding.toolbar.title = profile.actualUsername

                // Tagline
                val tagline = profile.actualTagline
                if (tagline != null && tagline.isNotEmpty()) {
                    binding.taglineTextView.text = tagline
                    binding.taglineTextView.visibility = View.VISIBLE
                } else {
                    binding.taglineTextView.visibility = View.GONE
                }
            }
        }

        // Observe deviations count
        viewModel.deviationsCount.observe(this) { count ->
            binding.deviationsCountTextView.text = formatCount(count)
        }

        // Observe watchers count
        viewModel.watchersCount.observe(this) { count ->
            binding.watchersCountTextView.text = formatCount(count)
        }

        // Observe friends count
        viewModel.friendsCount.observe(this) { count ->
            binding.watchingCountTextView.text = formatCount(count)
        }

        // Observe deviations
        viewModel.deviations.observe(this) { deviations ->
            if (deviations.isNotEmpty()) {
                deviationsAdapter.submitList(deviations)
                binding.deviationsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.GONE
            } else {
                binding.deviationsRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.VISIBLE
            }
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

        // Observe gallery folders
        viewModel.galleryFolders.observe(this) { folders ->
            galleryFolders = folders
            setupGallerySpinner()
            // Setup the Feed tab spinner on initial load
            setupFeedGallerySpinner()
        }

        // Observe selected gallery deviations
        viewModel.selectedGalleryDeviations.observe(this) { deviations ->
            if (deviations.isNotEmpty()) {
                deviationsAdapter.submitList(deviations)
                binding.deviationsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.GONE
            } else {
                binding.deviationsRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.text = "No deviations in this gallery"
                binding.emptyStateTextView.visibility = View.VISIBLE
            }
        }

        // Observe collection folders
        viewModel.collectionFolders.observe(this) { folders ->
            collectionFolders = folders
            setupCollectionSpinner()
        }

        // Observe selected collection deviations
        viewModel.selectedCollectionDeviations.observe(this) { deviations ->
            if (deviations.isNotEmpty()) {
                deviationsAdapter.submitList(deviations)
                binding.deviationsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.GONE
            } else {
                binding.deviationsRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.text = "No items in this collection"
                binding.emptyStateTextView.visibility = View.VISIBLE
            }
        }

        // Observe watch status
        viewModel.isWatching.observe(this) { isWatching ->
            updateWatchButton(isWatching)
        }
    }

    private fun setupWatchButton() {
        binding.watchButton.setOnClickListener {
            viewModel.toggleWatch()
        }
    }

    private fun updateWatchButton(isWatching: Boolean) {
        if (isWatching) {
            binding.watchButton.text = "Watching"
            binding.watchButton.setIconResource(R.drawable.ic_check)
        } else {
            binding.watchButton.text = "Watch"
            binding.watchButton.setIconResource(R.drawable.ic_add)
        }
    }

    private fun setupTabs() {
        binding.profileTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Feed tab - show spinner with default gallery selected
                        binding.galleryFoldersSpinner.visibility = View.VISIBLE
                        binding.emptyStateTextView.text = "No deviations yet"

                        // If we already have folders loaded, show them
                        if (galleryFolders.isEmpty()) {
                            binding.deviationsRecyclerView.visibility = View.GONE
                            binding.emptyStateTextView.text = "Loading galleries..."
                            binding.emptyStateTextView.visibility = View.VISIBLE

                            // Load gallery folders first
                            currentUsername?.let { username ->
                                viewModel.loadGalleryFolders(username)
                            }
                        } else {
                            // Setup the spinner for Feed tab and load default gallery
                            setupFeedGallerySpinner()
                        }
                    }
                    1 -> {
                        // Galleries tab - show spinner
                        binding.galleryFoldersSpinner.visibility = View.VISIBLE

                        // If we already have folders loaded, show them
                        if (galleryFolders.isEmpty()) {
                            binding.deviationsRecyclerView.visibility = View.GONE
                            binding.emptyStateTextView.text = "Loading galleries..."
                            binding.emptyStateTextView.visibility = View.VISIBLE

                            // Load gallery folders
                            currentUsername?.let { username ->
                                viewModel.loadGalleryFolders(username)
                            }
                        } else {
                            // Galleries already loaded, show current selection or default to Featured
                            viewModel.selectedGalleryDeviations.value?.let { deviations ->
                                if (deviations.isNotEmpty()) {
                                    deviationsAdapter.submitList(deviations)
                                    binding.deviationsRecyclerView.visibility = View.VISIBLE
                                    binding.emptyStateTextView.visibility = View.GONE
                                } else {
                                    binding.deviationsRecyclerView.visibility = View.GONE
                                    binding.emptyStateTextView.text = "No deviations in this gallery"
                                    binding.emptyStateTextView.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    2 -> {
                        // Favourites tab - show spinner for collections
                        binding.galleryFoldersSpinner.visibility = View.VISIBLE

                        // If we already have collection folders loaded, show them
                        if (collectionFolders.isEmpty()) {
                            binding.deviationsRecyclerView.visibility = View.GONE
                            binding.emptyStateTextView.text = "Loading collections..."
                            binding.emptyStateTextView.visibility = View.VISIBLE

                            // Load collection folders
                            currentUsername?.let { username ->
                                viewModel.loadCollectionFolders(username)
                            }
                        } else {
                            // Collections already loaded, show current selection
                            viewModel.selectedCollectionDeviations.value?.let { deviations ->
                                if (deviations.isNotEmpty()) {
                                    deviationsAdapter.submitList(deviations)
                                    binding.deviationsRecyclerView.visibility = View.VISIBLE
                                    binding.emptyStateTextView.visibility = View.GONE
                                } else {
                                    binding.deviationsRecyclerView.visibility = View.GONE
                                    binding.emptyStateTextView.text = "No items in this collection"
                                    binding.emptyStateTextView.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupGallerySpinner() {
        if (galleryFolders.isEmpty()) {
            binding.emptyStateTextView.text = "No galleries found"
            binding.emptyStateTextView.visibility = View.VISIBLE
            binding.deviationsRecyclerView.visibility = View.GONE
            return
        }

        val folderNames = galleryFolders.map { it.name ?: "Unnamed" }

        val adapter = ArrayAdapter(this, R.layout.spinner_item_toolbar, folderNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.galleryFoldersSpinner.adapter = adapter

        binding.galleryFoldersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFolder = galleryFolders[position]
                currentUsername?.let { username ->
                    selectedFolder.folderId?.let { folderId ->
                        viewModel.loadGalleryFolder(username, folderId)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Auto-select the first gallery (Featured) for the Galleries tab
        if (galleryFolders.isNotEmpty()) {
            binding.galleryFoldersSpinner.setSelection(0)
        }
    }

    private fun setupFeedGallerySpinner() {
        if (galleryFolders.isEmpty()) {
            binding.emptyStateTextView.text = "No galleries found"
            binding.emptyStateTextView.visibility = View.VISIBLE
            binding.deviationsRecyclerView.visibility = View.GONE
            return
        }

        val folderNames = galleryFolders.map { it.name ?: "Unnamed" }

        val adapter = ArrayAdapter(this, R.layout.spinner_item_toolbar, folderNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.galleryFoldersSpinner.adapter = adapter

        binding.galleryFoldersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFolder = galleryFolders[position]
                currentUsername?.let { username ->
                    selectedFolder.folderId?.let { folderId ->
                        viewModel.loadGalleryFolder(username, folderId)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Load the default gallery if one is saved, otherwise default to Featured (first gallery)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedUsername = prefs.getString("default_gallery_username", null)
        val savedGalleryId = prefs.getString("default_gallery_id", null)

        var selectedIndex = 0

        // If this is the user who has a default gallery set, use it
        if (savedUsername == currentUsername && savedGalleryId != null) {
            val index = galleryFolders.indexOfFirst { it.folderId == savedGalleryId }
            if (index >= 0) {
                selectedIndex = index
            }
        }

        // Set the selection and load the gallery
        if (galleryFolders.isNotEmpty()) {
            binding.galleryFoldersSpinner.setSelection(selectedIndex)
        }
    }

    private fun setupCollectionSpinner() {
        if (collectionFolders.isEmpty()) {
            binding.emptyStateTextView.text = "No collections found"
            binding.emptyStateTextView.visibility = View.VISIBLE
            binding.deviationsRecyclerView.visibility = View.GONE
            return
        }

        val folderNames = collectionFolders.map { it.name ?: "Unnamed" }

        val adapter = ArrayAdapter(this, R.layout.spinner_item_toolbar, folderNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.galleryFoldersSpinner.adapter = adapter

        binding.galleryFoldersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFolder = collectionFolders[position]
                currentUsername?.let { username ->
                    selectedFolder.folderId?.let { folderId ->
                        viewModel.loadCollectionFolder(username, folderId)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Auto-select the first collection (All) and load it
        if (collectionFolders.isNotEmpty()) {
            binding.galleryFoldersSpinner.setSelection(0)
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
            count >= 1000 -> String.format("%.1fK", count / 1000.0)
            else -> count.toString()
        }
    }
}

