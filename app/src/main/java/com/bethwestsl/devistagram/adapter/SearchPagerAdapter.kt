package com.bethwestsl.devistagram.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SearchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = mutableListOf<Fragment>()
    private val titles = mutableListOf<String>()

    fun setFragments(fragmentList: List<Fragment>, titleList: List<String>) {
        fragments.clear()
        titles.clear()
        fragments.addAll(fragmentList)
        titles.addAll(titleList)
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getPageTitle(position: Int): String = titles[position]
}

