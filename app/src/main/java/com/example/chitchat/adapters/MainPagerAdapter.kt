package com.example.chitchat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(
    private val fragmentsList: List<Fragment>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return fragmentsList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentsList[position]
    }

}