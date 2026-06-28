package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.pgshare.studentroomsharingapp.Adapter.ImagePagerAdapter
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.databinding.ActivityRoomDetailsBinding

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Handle Back Button in Toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Retrieve the Parcelable Room object passed from the Explore feed
        val room = intent.getParcelableExtra<Room>("Rooms")

        if (room != null) {
            // 3. Initialize the Image Gallery
            // Safely grab the image list, defaulting to an empty list if null
            val imageUrls = room.imageUrls ?: arrayListOf()

            // Pass the Base64 strings to the adapter we just updated
            val imagePagerAdapter = ImagePagerAdapter(imageUrls)
            binding.viewpagerRoomImages.adapter = imagePagerAdapter

            // 4. Synchronize Pagination Dots
            // This connects the TabLayout dots to the ViewPager swiping action
            TabLayoutMediator(binding.tabLayoutImageIndicator, binding.viewpagerRoomImages) { _, _ ->
                // Leave empty: the custom visual behavior is handled by your tab_indicator_selector.xml
            }.attach()

            // 5. Populate the Text Views with Firebase Data
            binding.tvDetailTitle.text = room.roomName
            binding.tvDescriptionBody.text = room.description ?: "No description provided."

            // Format the pricing string securely
            val formattedPrice = "₹${room.price}"
            binding.tvRentAmount.text = formattedPrice

            // Push the same price to the persistent bottom CTA
            binding.tvCtaPrice.text = formattedPrice

            val formattedDeposit = "₹${room.deposit}"
            binding.tvDepositAmount.text = formattedDeposit


            // 6. Handle Chat Button Navigation
            binding.btnChatOwner.setOnClickListener {
                // When you are ready to link the Chat UI, uncomment this!
                // val intent = Intent(this, ChatActivity::class.java)
                // startActivity(intent)
                Toast.makeText(this,"Soon will Work ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}