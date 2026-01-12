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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterManager = ArtistFilterManager(requireContext())

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
        setupSwipeRefresh()
        setupFilterToggle()
    }

    private fun setupRecyclerView() {
        adapter = DeviationAdapter(
            onDeviationClick = { deviation ->
                DeviationDetailActivity.start(requireContext(), deviation)
            },
            onFavoriteClick = { deviation ->
                val added = filterManager.toggleFavorite(deviation.author.username)
                Toast.makeText(
                    requireContext(),
                    if (added) "Added ${deviation.author.username} to favorites"
                    else "Removed ${deviation.author.username} from favorites",
                    Toast.LENGTH_SHORT
                ).show()
                applyFilter()
            },
            onBlockClick = { deviation ->
                val added = filterManager.toggleBlocked(deviation.author.username)
                Toast.makeText(
                    requireContext(),
                    if (added) "Blocked ${deviation.author.username}"
                    else "Unblocked ${deviation.author.username}",
                    Toast.LENGTH_SHORT
                ).show()
                applyFilter()
            },
            isFavorite = { username -> filterManager.isFavorite(username) },
            isBlocked = { username -> filterManager.isBlocked(username) }
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
        viewModel = ViewModelProvider(this)[FeedViewModel::class.java]

        // Set browse type to "deviantsyouwatch" for the feed
        viewModel.setBrowseType("deviantsyouwatch")

        viewModel.deviations.observe(viewLifecycleOwner) { deviations ->
            if (deviations != null) {
                allDeviations = deviations
                applyFilter()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
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
            viewModel.loadDeviations(refresh = true)
        }
    }

    private fun setupFilterToggle() {
        // Restore saved filter state
        binding.favoritesFilterSwitch.isChecked = filterManager.isFavoritesFilterEnabled()
        binding.safeModeSwitch.isChecked = filterManager.isSafeModeEnabled()
        
        binding.favoritesFilterSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterManager.setFavoritesFilterEnabled(isChecked)
            applyFilter()
        }
        
        binding.safeModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterManager.setSafeModeEnabled(isChecked)
            applyFilter()
        }
    }

    private fun applyFilter() {
        val blockedArtists = filterManager.getBlockedArtists()
        val favoriteArtists = filterManager.getFavoriteArtists()
        val showFavoritesOnly = filterManager.isFavoritesFilterEnabled()
        val safeModeEnabled = filterManager.isSafeModeEnabled()
        
        val filtered = allDeviations.filter { deviation ->
            val username = deviation.author.username
            
            // Always hide blocked artists
            if (blockedArtists.contains(username)) {
                return@filter false
            }
            
            // If safe mode is on, hide mature content
            if (safeModeEnabled && deviation.isMature == true) {
                return@filter false
            }
            
            // If favorites filter is on, only show favorites
            if (showFavoritesOnly) {
                return@filter favoriteArtists.contains(username)
            }
            
            // Otherwise show all (except blocked and mature if safe mode)
            true
        }
        
        adapter.submitList(filtered)
        
        if (filtered.isNotEmpty()) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        } else {
            val message = when {
                showFavoritesOnly && favoriteArtists.isEmpty() -> "No favorite artists yet. Tap ⭐ on posts to add favorites!"
                showFavoritesOnly -> "No posts from favorite artists"
                else -> "No posts from artists you watch yet"
            }
            binding.errorTextView.text = message
            binding.errorTextView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
