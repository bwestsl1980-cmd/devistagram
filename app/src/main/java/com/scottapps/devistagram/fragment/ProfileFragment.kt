package com.scottapps.devistagram.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.scottapps.devistagram.LoginActivity
import com.scottapps.devistagram.adapter.DeviationGridAdapter
import com.scottapps.devistagram.auth.OAuthManager
import com.scottapps.devistagram.databinding.FragmentProfileBinding
import com.scottapps.devistagram.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var oAuthManager: OAuthManager
    private lateinit var deviationsAdapter: DeviationGridAdapter

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
        setupButtons()
    }

    private fun setupRecyclerView() {
        deviationsAdapter = DeviationGridAdapter { deviation ->
            // TODO: Open deviation detail view
            Toast.makeText(requireContext(), "Clicked: ${deviation.title}", Toast.LENGTH_SHORT).show()
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
    }

    private fun setupButtons() {
        binding.editProfileButton.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.shareProfileButton.setOnClickListener {
            Toast.makeText(requireContext(), "Share Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
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
        oAuthManager.logout()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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