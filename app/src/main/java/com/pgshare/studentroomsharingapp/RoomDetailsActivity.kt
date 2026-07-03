package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.PagerAdapter
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
            binding.tvRoomTitle.text = room.roomName
            binding.tvDescription.text = room.description ?: "No description provided."

            val formattedPrice = "\u20B9${room.price}"
            binding.tvPrice.text = formattedPrice

            observeOwnerName()
            viewModel.loadOwnerName(room.userId)

            binding.btnChat.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("RECEIVER_ID", room.userId)
                intent.putExtra("ROOM_ID", room.id)
                startActivity(intent)
            }

            setupImagePager(room.imageUrls)
        }
    }

    private fun setupImagePager(imageUrls: List<String?>?) {
        val images = imageUrls?.filter { !it.isNullOrEmpty() } ?: emptyList()

        if (images.isEmpty()) {
            binding.viewpagerRoomImages.visibility = View.GONE
            binding.tabLayoutImageIndicator.visibility = View.GONE
            return
        }

        val decodedBitmaps = images.mapNotNull { base64 ->
            try {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (decodedBitmaps.isEmpty()) {
            binding.viewpagerRoomImages.visibility = View.GONE
            binding.tabLayoutImageIndicator.visibility = View.GONE
            return
        }

        binding.viewpagerRoomImages.adapter = object : PagerAdapter() {
            override fun getCount() = decodedBitmaps.size

            override fun isViewFromObject(view: View, `object`: Any) = view === `object`

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val imageView = ImageView(this@RoomDetailsActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(decodedBitmaps[position])
                }
                container.addView(imageView)
                return imageView
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }
        }

        binding.tabLayoutImageIndicator.setupWithViewPager(binding.viewpagerRoomImages, true)
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
