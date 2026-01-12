package com.bethwestsl.devistagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bethwestsl.devistagram.DeviationDetailActivity
import com.bethwestsl.devistagram.adapter.DeviationGridAdapter
import com.bethwestsl.devistagram.databinding.FragmentSearchTagsBinding
import com.bethwestsl.devistagram.viewmodel.TagViewModel

class SearchTagsFragment : Fragment() {

    private var _binding: FragmentSearchTagsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TagViewModel
    private lateinit var adapter: DeviationGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchTagsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchField()
        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
    }

    private fun setupSearchField() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.matureContentSwitch.setOnCheckedChangeListener { _, _ ->
            val tag = binding.searchEditText.text.toString().trim()
            if (tag.isNotEmpty()) {
                performSearch()
            }
        }
    }

    private fun performSearch() {
        val tag = binding.searchEditText.text.toString().trim()
        val showMature = binding.matureContentSwitch.isChecked

        if (tag.isNotEmpty()) {
            viewModel.searchTag(tag, showMature)
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun setupRecyclerView() {
        adapter = DeviationGridAdapter { deviation ->
            DeviationDetailActivity.start(requireContext(), deviation)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = this@SearchTagsFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 && firstVisibleItemPosition >= 0) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[TagViewModel::class.java]

        viewModel.deviations.observe(viewLifecycleOwner) { deviations ->
            if (deviations != null) {
                adapter.submitList(deviations)
                if (deviations.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                } else if (binding.searchEditText.text.toString().isNotEmpty()) {
                    binding.errorTextView.text = "No results found for this tag"
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading && adapter.itemCount > 0
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorTextView.text = error
                binding.errorTextView.visibility = View.VISIBLE
                if (error != "Search for tags to see deviations") {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
                viewModel.clearError()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            performSearch()
            if (binding.searchEditText.text.toString().trim().isEmpty()) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

