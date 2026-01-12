package com.bethwestsl.devistagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.bethwestsl.devistagram.adapter.SearchPagerAdapter
import com.bethwestsl.devistagram.databinding.FragmentSearchBinding

class TaggedFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
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

        setupViewPager()
    }

    private fun setupViewPager() {
        val pagerAdapter = SearchPagerAdapter(this)

        val fragments = listOf(
            SearchUsersFragment(),
            SearchTagsFragment()
        )
        val titles = listOf("Users", "Tags")

        pagerAdapter.setFragments(fragments, titles)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
            tab.icon = when (position) {
                0 -> requireContext().getDrawable(com.bethwestsl.devistagram.R.drawable.ic_user)
                1 -> requireContext().getDrawable(com.bethwestsl.devistagram.R.drawable.ic_tag)
                else -> null
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
