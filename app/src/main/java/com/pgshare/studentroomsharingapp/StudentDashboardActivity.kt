package com.pgshare.studentroomsharingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pgshare.studentroomsharingapp.Fragments.ExploreFragment
import com.pgshare.studentroomsharingapp.Fragments.InboxFragment
import com.pgshare.studentroomsharingapp.Fragments.ProfileFragment

import com.pgshare.studentroomsharingapp.databinding.ActivityStudentDashboardBinding

class StudentDashboardActivity : AppCompatActivity() {


    lateinit var binding : ActivityStudentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Load the ExploreFragment by default when the activity starts
        if (savedInstanceState == null) {
            loadFragment(ExploreFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_explore
        }

        val target = intent.getStringExtra("TARGET_FRAGMENT")



        // Handle Bottom Navigation item clicks
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    loadFragment(ExploreFragment())
                    true
                }
                R.id.nav_saved -> {
                    // Replace with your actual SavedFragment
                    loadFragment(SavedFragment())
                    true
                }
                R.id.nav_inbox -> {
                    // Replace with your actual InboxFragment
                    loadFragment(InboxFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        if (target == "INBOX") {
            // Select the tab and load the fragment
            binding.bottomNavigation.selectedItemId = R.id.nav_inbox

            // Option B: If doing it manually
            //supportFragmentManager.beginTransaction().replace(R.id.fragment_container, InboxFragment()).commit()

        }
    }



    /**
     * Helper function to swap fragments in the container
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}


