package com.bethwestsl.devistagram.fragment

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
import com.bethwestsl.devistagram.adapter.DeviationGridAdapter
import com.bethwestsl.devistagram.databinding.FragmentDiscoverBinding
import com.bethwestsl.devistagram.viewmodel.FeedViewModel

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FeedViewModel
    private lateinit var adapter: DeviationGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    private fun setupRecyclerView() {
        adapter = DeviationGridAdapter { deviation ->
            DeviationDetailActivity.start(requireContext(), deviation)
        }

        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3) // 3 columns
            adapter = this@DiscoverFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.GridLayoutManager
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

        viewModel.deviations.observe(viewLifecycleOwner) { deviations ->
            if (deviations != null) {
                adapter.submitList(deviations)
                if (deviations.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
