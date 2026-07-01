package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.ImagePagerAdapter
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.databinding.ActivityRoomDetailsBinding

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailsBinding
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

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
            val imageUrls = room.imageUrls ?: arrayListOf()
            val imagePagerAdapter = ImagePagerAdapter(imageUrls)
            binding.viewpagerRoomImages.adapter = imagePagerAdapter

            // 4. Synchronize Pagination Dots
            TabLayoutMediator(binding.tabLayoutImageIndicator, binding.viewpagerRoomImages) { _, _ ->
            }.attach()

            // 5. Populate the Text Views with Firebase Data
            binding.tvDetailTitle.text = room.roomName
            binding.tvDescriptionBody.text = room.description ?: "No description provided."

            val formattedPrice = "₹${room.price}"
            binding.tvRentAmount.text = formattedPrice
            binding.tvCtaPrice.text = formattedPrice

            val formattedDeposit = "₹${room.deposit}"
            binding.tvDepositAmount.text = formattedDeposit

            // 6. Fetch and display Owner Name
            fetchAndDisplayOwnerName(room.userId)

            // 7. Handle Chat Button Navigation (FIXED)
            binding.btnChatOwner.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)

                // Assuming your Room data class uses 'userId' for the owner and 'roomId' for the key.
                // If your variables are named differently in Room.kt (like ownerId or roomKey), update them here!
                intent.putExtra("RECEIVER_ID", room.userId)
                intent.putExtra("ROOM_ID", room.id)

                // You are already inside an Activity, so you just call startActivity() directly
                startActivity(intent)
            }
        }
    }

    private fun fetchAndDisplayOwnerName(userId: String?) {
        userId?.let { uid ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        binding.tvOwnerName.text = "Listed by Unknown User"
                        return
                    }

                    val username = snapshot.child("username").value as? String
                    val email = snapshot.child("email").value as? String

                    val displayName = if (!username.isNullOrBlank()) {
                        username
                    } else if (!email.isNullOrBlank()) {
                        email.substringBefore("@")
                    } else {
                        "Unknown User"
                    }

                    binding.tvOwnerName.text = "Listed by $displayName"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("RoomDetailsActivity", "Failed to load owner name", error.toException())
                    binding.tvOwnerName.text = "Listed by Unknown User"
                }
            })
        } ?: run {
            binding.tvOwnerName.text = "Listed by Unknown User"
        }
    }
}