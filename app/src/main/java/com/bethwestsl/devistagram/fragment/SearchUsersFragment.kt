package com.bethwestsl.devistagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bethwestsl.devistagram.adapter.UserSearchAdapter
import com.bethwestsl.devistagram.databinding.FragmentSearchUsersBinding
import com.bethwestsl.devistagram.viewmodel.UserSearchViewModel

class SearchUsersFragment : Fragment() {

    private var _binding: FragmentSearchUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UserSearchViewModel
    private lateinit var adapter: UserSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViewModel()
        setupSearchField()

        binding.errorTextView.text = "Search for users to follow"
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        adapter = UserSearchAdapter { user ->
            // TODO: Navigate to user profile when profile activity is implemented
            Toast.makeText(requireContext(), "View ${user.username}'s profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchUsersFragment.adapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[UserSearchViewModel::class.java]

        viewModel.users.observe(viewLifecycleOwner) { users ->
            if (users != null) {
                adapter.submitList(users)
                if (users.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                } else if (binding.searchEditText.text.toString().isNotEmpty()) {
                    binding.errorTextView.text = "No users found"
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorTextView.text = error
                binding.errorTextView.visibility = View.VISIBLE
                if (error != "Search for users to follow") {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
                viewModel.clearError()
            }
        }
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
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            hideKeyboard()
            viewModel.searchUsers(query)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



