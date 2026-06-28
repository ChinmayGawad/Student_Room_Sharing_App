package com.pgshare.studentroomsharingapp.Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pgshare.studentroomsharingapp.AddRoomStep2Fragment
import com.pgshare.studentroomsharingapp.Fragments.AddRoomStep1Fragment
import com.pgshare.studentroomsharingapp.fragments.AddRoomStep3Fragment

class WizardPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    // Right now we have 3 steps built.
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AddRoomStep1Fragment() // The Basics
            1 -> AddRoomStep2Fragment() // The Photos
            2 -> AddRoomStep3Fragment() // We will add pricing here later
            else -> AddRoomStep1Fragment()
        }
    }
}

