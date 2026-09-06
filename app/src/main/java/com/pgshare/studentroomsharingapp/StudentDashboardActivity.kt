package com.pgshare.studentroomsharingapp

import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import com.pgshare.studentroomsharingapp.Fragments.ExploreFragment
import com.pgshare.studentroomsharingapp.Fragments.InboxFragment
import com.pgshare.studentroomsharingapp.Fragments.ProfileFragment
import com.pgshare.studentroomsharingapp.Fragments.SavedFragment

import com.pgshare.studentroomsharingapp.databinding.ActivityStudentDashboardBinding

class StudentDashboardActivity : AppCompatActivity() {

    lateinit var binding: ActivityStudentDashboardBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FCMService.createNotificationChannels(this)
        FirebaseMessaging.getInstance().subscribeToTopic("rooms_all")

        requestNotificationPermission()

        val target = intent.getStringExtra("TARGET_FRAGMENT")

        if (savedInstanceState == null) {
            val defaultFragment = when (target) {
                "INBOX" -> InboxFragment()
                "PROFILE" -> ProfileFragment()
                else -> ExploreFragment()
            }
            loadFragment(defaultFragment)
            binding.bottomNavigation.selectedItemId = when (target) {
                "INBOX" -> R.id.navigation_inbox
                "PROFILE" -> R.id.navigation_profile
                else -> R.id.navigation_explore
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_explore -> {
                    loadFragment(ExploreFragment()); true
                }
                R.id.navigation_saved -> {
                    loadFragment(SavedFragment()); true
                }
                R.id.navigation_inbox -> {
                    loadFragment(InboxFragment()); true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment()); true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}


