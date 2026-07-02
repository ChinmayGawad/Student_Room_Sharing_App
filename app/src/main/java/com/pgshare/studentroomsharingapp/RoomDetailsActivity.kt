package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.pgshare.studentroomsharingapp.Adapter.ImagePagerAdapter
import com.pgshare.studentroomsharingapp.databinding.ActivityRoomDetailsBinding
import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.viewmodel.RoomDetailViewModel
import kotlinx.coroutines.launch

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailsBinding
    private val viewModel = RoomDetailViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val room = intent.getParcelableExtra<Room>("Rooms")

        if (room != null) {
            val imageUrls = room.imageUrls ?: arrayListOf()
            val imagePagerAdapter = ImagePagerAdapter(imageUrls)
            binding.viewpagerRoomImages.adapter = imagePagerAdapter

            TabLayoutMediator(binding.tabLayoutImageIndicator, binding.viewpagerRoomImages) { _, _ ->
            }.attach()

            binding.tvDetailTitle.text = room.roomName
            binding.tvDescriptionBody.text = room.description ?: "No description provided."

            val formattedPrice = "\u20B9${room.price}"
            binding.tvRentAmount.text = formattedPrice
            binding.tvCtaPrice.text = formattedPrice

            val formattedDeposit = "\u20B9${room.deposit}"
            binding.tvDepositAmount.text = formattedDeposit

            observeOwnerName()
            viewModel.loadOwnerName(room.userId)

            binding.btnChatOwner.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("RECEIVER_ID", room.userId)
                intent.putExtra("ROOM_ID", room.id)
                startActivity(intent)
            }
        }
    }

    private fun observeOwnerName() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvOwnerName.text = "Listed by ${state.ownerName}"
                }
            }
        }
    }
}
