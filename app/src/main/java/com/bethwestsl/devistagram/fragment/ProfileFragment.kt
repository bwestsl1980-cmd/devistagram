package com.bethwestsl.devistagram.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.DeviationDetailActivity
import com.bethwestsl.devistagram.LoginActivity
import com.bethwestsl.devistagram.NotesActivity
import com.bethwestsl.devistagram.R
import com.bethwestsl.devistagram.adapter.DeviationGridAdapter
import com.bethwestsl.devistagram.auth.OAuthManager
import com.bethwestsl.devistagram.databinding.FragmentProfileBinding
import com.bethwestsl.devistagram.model.GalleryFolder
import com.bethwestsl.devistagram.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var oAuthManager: OAuthManager
    private lateinit var deviationsAdapter: DeviationGridAdapter
    private var currentUsername: String? = null
    private var galleryFolders: List<GalleryFolder> = emptyList()
    private var collectionFolders: List<GalleryFolder> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        oAuthManager = OAuthManager(requireContext())

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams
            layoutParams.height = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material) + insets.top
            v.layoutParams = layoutParams
            v.setPadding(0, insets.top, 0, 0)
            windowInsets
        }

        setupRecyclerView()
        setupViewModel()
        setupMenu()
        setupTabs()

        // Initialize default tab state (Feed tab is selected by default)
        binding.galleryFoldersSpinner.visibility = View.VISIBLE
        binding.emptyStateTextView.text = "Loading galleries..."
        binding.emptyStateTextView.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        deviationsAdapter = DeviationGridAdapter { deviation ->
            DeviationDetailActivity.start(requireContext(), deviation)
        }

        binding.deviationsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = deviationsAdapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                // Store username for gallery loading
                currentUsername = profile.actualUsername

                // Load gallery folders for default gallery selection
                currentUsername?.let { username ->
                    viewModel.loadGalleryFolders(username)
                }

                // Avatar
                binding.avatarImageView.load(profile.actualUserIcon) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(android.R.drawable.ic_menu_myplaces)
                    error(android.R.drawable.ic_menu_myplaces)
                }

                // Username
                binding.usernameTextView.text = profile.actualUsername

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
        viewModel.deviationsCount.observe(viewLifecycleOwner) { count ->
            binding.deviationsCountTextView.text = formatCount(count)
        }

        // Observe watchers count
        viewModel.watchersCount.observe(viewLifecycleOwner) { count ->
            binding.watchersCountTextView.text = formatCount(count)
        }

        // Observe friends count
        viewModel.friendsCount.observe(viewLifecycleOwner) { count ->
            binding.watchingCountTextView.text = formatCount(count)
        }

        // Observe deviations
        viewModel.deviations.observe(viewLifecycleOwner) { deviations ->
            if (deviations.isNotEmpty()) {
                deviationsAdapter.submitList(deviations)
                binding.deviationsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateTextView.visibility = View.GONE
            } else {
                binding.deviationsRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe gallery folders
        viewModel.galleryFolders.observe(viewLifecycleOwner) { folders ->
            galleryFolders = folders

            // Setup spinner based on current tab
            when (binding.profileTabLayout.selectedTabPosition) {
                0 -> {
                    // Feed tab - setup Feed spinner with default gallery
                    setupFeedGallerySpinner()
                }
                1 -> {
                    // Galleries tab - setup Galleries spinner
                    setupGallerySpinner()
                }
                // Position 2 is Favourites, doesn't use gallery folders
            }
        }

        // Observe selected gallery deviations
        viewModel.selectedGalleryDeviations.observe(viewLifecycleOwner) { deviations ->
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
        viewModel.collectionFolders.observe(viewLifecycleOwner) { folders ->
            collectionFolders = folders
            setupCollectionSpinner()
        }

        // Observe selected collection deviations
        viewModel.selectedCollectionDeviations.observe(viewLifecycleOwner) { deviations ->
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

    private fun setupMenu() {
        // Set initial theme toggle state
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        binding.toolbar.menu.findItem(R.id.action_theme_toggle)?.isChecked = isDarkMode
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_mail -> {
                    // Open mail/notes activity
                    NotesActivity.start(requireContext())
                    true
                }
                R.id.action_theme_toggle -> {
                    // Toggle theme
                    val newMode = if (menuItem.isChecked) {
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    } else {
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    }
                    // Save preference
                    requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putInt("night_mode", newMode)
                        .apply()
                    // Apply theme change (this will recreate the activity)
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newMode)
                    true
                }
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
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
                            
                            // Load gallery folders (will auto-select Featured when loaded)
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
                            
                            // Load collection folders (will auto-select All when loaded)
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
        
        // Just use the gallery names directly
        val folderNames = galleryFolders.map { it.name ?: "Unnamed" }
        
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_toolbar, folderNames)
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
        if (galleryFolders.isEmpty()) return

        val folderNames = galleryFolders.map { it.name ?: "Unnamed" }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_toolbar, folderNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.galleryFoldersSpinner.adapter = adapter

        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

        binding.galleryFoldersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFolder = galleryFolders[position]
                currentUsername?.let { username ->
                    selectedFolder.folderId?.let { folderId ->
                        // Load the gallery
                        viewModel.loadGalleryFolder(username, folderId)

                        // Save as default for Feed tab
                        prefs.edit()
                            .putString("default_gallery_id", folderId)
                            .putString("default_gallery_username", username)
                            .apply()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Load the saved default or use Featured (first gallery)
        val savedGalleryId = prefs.getString("default_gallery_id", null)
        var selectedIndex = 0
        if (savedGalleryId != null) {
            val index = galleryFolders.indexOfFirst { it.folderId == savedGalleryId }
            if (index >= 0) {
                selectedIndex = index
            }
        }

        // Set the selection
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
        
        // Use the collection folder names
        val folderNames = collectionFolders.map { it.name ?: "Unnamed" }
        
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_toolbar, folderNames)
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

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Revoke token with API and clear local data
            oAuthManager.logout()
            
            // Navigate to login immediately
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            loginIntent.putExtra("LOGGED_OUT", true)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            requireActivity().finish()
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
            count >= 1000 -> String.format("%.1fK", count / 1000.0)
            else -> count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
