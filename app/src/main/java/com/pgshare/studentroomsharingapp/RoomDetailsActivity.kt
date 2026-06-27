package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.pgshare.studentroomsharingapp.Adapter.ImagePagerAdapter // Make sure this imports your ViewPager2 adapter
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.databinding.ActivityRoomDetailsBinding

class RoomDetailsActivity : AppCompatActivity() {

    private var room: Room? = null
    private var isRoomBooked = false
    private lateinit var binding: ActivityRoomDetailsBinding
    private lateinit var imageAdapter: ImagePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide default action bar since we are using our custom CollapsingToolbar
        supportActionBar?.hide()

        binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the Room object from the intent
        room = intent.getParcelableExtra("Rooms")

        if (room != null) {
            // 1. Populate Text Data
            binding.tvDetailTitle.text = room!!.roomName
            binding.tvDescriptionBody.text = room!!.description

            // Set price in both the description breakdown AND the bottom floating sheet
            binding.tvRentAmount.text = room!!.formatPrice
            binding.tvCtaPrice.text = room!!.formatPrice

            // Note: If you have a deposit field in your Room model, you can set it here:
//             binding.tvDepositAmount.text = room!!.depositAmount

            // 2. Setup ViewPager2 and TabLayout for Swipeable Images
            val imageUrls: List<String> = room?.imageUrls?.filterNotNull() ?: emptyList()
            if (imageUrls.isNotEmpty()) {
                // Initialize the adapter with the URLs
                imageAdapter = ImagePagerAdapter(imageUrls)
                binding.viewpagerRoomImages.adapter = imageAdapter

                // Attach the dots to the swiping action
                TabLayoutMediator(binding.tabLayoutImageIndicator, binding.viewpagerRoomImages) { _, _ ->
                    // Empty lambda because our dots don't have text labels
                }.attach()
            }

            // 3. Handle Booking Status
            isRoomBooked = room!!.isRoomBooked
            if (isRoomBooked) {
                // Update our new MaterialButton CTA instead of the old 'bookRoomButton'
                binding.btnChatOwner.text = "Room Booked"
                binding.btnChatOwner.isEnabled = false
            }

            // 4. Setup Click Listeners
            binding.btnChatOwner.setOnClickListener {
                if (!isRoomBooked) {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("roomId", room!!.id)
                    startActivity(intent)
                }
            }

            // Setup back button behavior on the CollapsingToolbar
            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

        } else {
            // Handle case where room object is null gracefully
            Toast.makeText(this, "Failed to load room details", Toast.LENGTH_SHORT).show()
            finish() // Close the activity so the user isn't stuck on a blank screen
        }
    }
}
