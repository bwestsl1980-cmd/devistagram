package com.bethwestsl.devistagram.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bethwestsl.devistagram.DeviationDetailActivity
import com.bethwestsl.devistagram.R
import com.bethwestsl.devistagram.adapter.DeviationAdapter
import com.bethwestsl.devistagram.databinding.FragmentFeedBinding
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.util.ArtistFilterManager
import com.bethwestsl.devistagram.viewmodel.FeedViewModel

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FeedViewModel
    private lateinit var adapter: DeviationAdapter
    private lateinit var filterManager: ArtistFilterManager
    private var allDeviations: List<Deviation> = emptyList()
    private var hasLoadedContent = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        android.util.Log.d("FeedFragment", "════════════════════════════════════")
        android.util.Log.d("FeedFragment", "onCreateView - Fragment is being created")
        android.util.Log.d("FeedFragment", "════════════════════════════════════")
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("FeedFragment", "onViewCreated - Starting setup")

        filterManager = ArtistFilterManager(requireContext())

        // Check toggle state immediately
        val toggleState = filterManager.isFavoritesFilterEnabled()
        val favoriteCount = filterManager.getFavoriteArtists().size
        android.util.Log.d("FeedFragment", "onViewCreated - Toggle state: $toggleState")
        android.util.Log.d("FeedFragment", "onViewCreated - Favorites count: $favoriteCount")

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams
            layoutParams.height = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material) + insets.top
            v.layoutParams = layoutParams
            v.setPadding(0, insets.top, 0, 0)
            windowInsets
        }

        android.util.Log.d("FeedFragment", "onViewCreated - Setting up RecyclerView")
        setupRecyclerView()

        android.util.Log.d("FeedFragment", "onViewCreated - Setting up ViewModel")
        setupViewModel()

        android.util.Log.d("FeedFragment", "onViewCreated - Setting up SwipeRefresh")
        setupSwipeRefresh()

        android.util.Log.d("FeedFragment", "onViewCreated - Setting up FilterToggle")
        setupFilterToggle()

        android.util.Log.d("FeedFragment", "onViewCreated - Setup complete, hasLoadedContent: $hasLoadedContent")
    }

    private fun setupRecyclerView() {
        adapter = DeviationAdapter(
            onDeviationClick = { deviation ->
                DeviationDetailActivity.start(requireContext(), deviation)
            },
            onAuthorClick = { username ->
                com.bethwestsl.devistagram.OtherUserProfileActivity.start(requireContext(), username)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FeedFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupViewModel() {
        android.util.Log.d("FeedFragment", "setupViewModel - Creating/Getting ViewModel")
        viewModel = ViewModelProvider(this)[FeedViewModel::class.java]

        // Clear any stale data from previous fragment instances
        android.util.Log.d("FeedFragment", "setupViewModel - Clearing stale ViewModel data")
        viewModel.clearData()

        android.util.Log.d("FeedFragment", "setupViewModel - Setting up deviations observer")
        viewModel.deviations.observe(viewLifecycleOwner) { deviations ->
            android.util.Log.d("FeedFragment", "────────────────────────────────────")
            android.util.Log.d("FeedFragment", "OBSERVER FIRED: deviations changed")
            android.util.Log.d("FeedFragment", "Received ${deviations?.size ?: 0} deviations")
            android.util.Log.d("FeedFragment", "Current toggle state: ${filterManager.isFavoritesFilterEnabled()}")
            android.util.Log.d("FeedFragment", "hasLoadedContent: $hasLoadedContent")

            // ONLY process if we've explicitly loaded content
            // This prevents stale ViewModel data from being displayed
            if (deviations != null && hasLoadedContent) {
                android.util.Log.d("FeedFragment", "→ Processing deviations (content was loaded)")
                allDeviations = deviations
                applyFilter()
            } else if (deviations != null && !hasLoadedContent) {
                android.util.Log.d("FeedFragment", "→ IGNORING deviations (content not loaded yet - stale data)")
            }
            android.util.Log.d("FeedFragment", "────────────────────────────────────")
        }

        android.util.Log.d("FeedFragment", "setupViewModel - Setting up loading observer")
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            android.util.Log.d("FeedFragment", "Loading state changed: $isLoading")
            binding.progressBar.visibility = if (isLoading && adapter.itemCount == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.swipeRefreshLayout.isRefreshing = isLoading && adapter.itemCount > 0
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorTextView.text = error
                binding.errorTextView.visibility = View.VISIBLE
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadContent()
        }
    }

    private fun setupFilterToggle() {
        // Restore saved filter state
        binding.favoritesFilterSwitch.isChecked = filterManager.isFavoritesFilterEnabled()
        binding.safeModeSwitch.isChecked = filterManager.isSafeModeEnabled()
        
        binding.favoritesFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterManager.setFavoritesFilterEnabled(isChecked)
            loadContent()
        }
        
        binding.safeModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterManager.setSafeModeEnabled(isChecked)
            applyFilter()
        }
    }

    private fun loadContent() {
        val showFavoritesOnly = filterManager.isFavoritesFilterEnabled()

        android.util.Log.d("FeedFragment", "▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼")
        android.util.Log.d("FeedFragment", "loadContent() CALLED")
        android.util.Log.d("FeedFragment", "showFavoritesOnly: $showFavoritesOnly")
        android.util.Log.d("FeedFragment", "Setting hasLoadedContent = true")
        hasLoadedContent = true

        if (showFavoritesOnly) {
            android.util.Log.d("FeedFragment", "→ Path: FAVORITES FEED")

            val favoriteUsernames = filterManager.getFavoriteArtists()
            android.util.Log.d("FeedFragment", "→ Favorites count: ${favoriteUsernames.size}")
            android.util.Log.d("FeedFragment", "→ Favorites list: $favoriteUsernames")

            if (favoriteUsernames.isEmpty()) {
                android.util.Log.d("FeedFragment", "→ No favorites - showing EMPTY STATE")
                binding.recyclerView.visibility = View.GONE
                binding.errorTextView.text = "No favorite artists yet. Visit a user's profile and tap ⭐ to add them to favorites!"
                binding.errorTextView.visibility = View.VISIBLE
                allDeviations = emptyList()
                adapter.submitList(emptyList())
                android.util.Log.d("FeedFragment", "→ Empty state displayed")
            } else {
                android.util.Log.d("FeedFragment", "→ Loading favorites feed from API for ${favoriteUsernames.size} users")
                viewModel.loadFavoritesFeed(favoriteUsernames)
            }
        } else {
            android.util.Log.d("FeedFragment", "→ Path: REGULAR FEED")
            android.util.Log.d("FeedFragment", "→ Setting browse type: deviantsyouwatch")
            viewModel.setBrowseType("deviantsyouwatch")
            android.util.Log.d("FeedFragment", "→ Calling loadDeviations(refresh=true)")
            viewModel.loadDeviations(refresh = true)
        }
        android.util.Log.d("FeedFragment", "▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲")
    }

    private fun applyFilter() {
        val safeModeEnabled = filterManager.isSafeModeEnabled()
        
        android.util.Log.d("FeedFragment", "┌──────────────────────────────────┐")
        android.util.Log.d("FeedFragment", "│ applyFilter() CALLED             │")
        android.util.Log.d("FeedFragment", "│ Input deviations: ${allDeviations.size.toString().padEnd(14)}│")
        android.util.Log.d("FeedFragment", "│ Safe mode: ${safeModeEnabled.toString().padEnd(20)}│")

        // Only apply safe mode filter (favorites are now handled by loadContent)
        val filtered = allDeviations.filter { deviation ->
            // If safe mode is on, hide mature content
            if (safeModeEnabled && deviation.isMature == true) {
                return@filter false
            }
            true
        }

        android.util.Log.d("FeedFragment", "│ Output deviations: ${filtered.size.toString().padEnd(14)}│")
        android.util.Log.d("FeedFragment", "└──────────────────────────────────┘")

        adapter.submitList(filtered.toList()) {
            android.util.Log.d("FeedFragment", "List submitted to adapter - ${filtered.size} items")
        }

        if (filtered.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        } else {
            val message = "No posts available"
            binding.errorTextView.text = message
            binding.errorTextView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("FeedFragment", "════════════════════════════════════")
        android.util.Log.d("FeedFragment", "onResume - Fragment becoming visible")
        android.util.Log.d("FeedFragment", "hasLoadedContent: $hasLoadedContent")
        android.util.Log.d("FeedFragment", "Toggle state: ${filterManager.isFavoritesFilterEnabled()}")
        android.util.Log.d("FeedFragment", "Favorites count: ${filterManager.getFavoriteArtists().size}")
        android.util.Log.d("FeedFragment", "Current deviations count: ${allDeviations.size}")

        // Load content when fragment becomes visible, but only once per instance
        if (!hasLoadedContent) {
            android.util.Log.d("FeedFragment", "onResume - Content NOT loaded yet, calling loadContent()")
            loadContent()
        } else {
            android.util.Log.d("FeedFragment", "onResume - Content ALREADY loaded, skipping loadContent()")
        }
        android.util.Log.d("FeedFragment", "════════════════════════════════════")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
