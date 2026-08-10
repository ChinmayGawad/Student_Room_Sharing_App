package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.chip.Chip
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
            bindRoomData(room)

            observeOwnerName()

            viewModel.loadOwnerName(room.userId)

            binding.btnChat.setOnClickListener {
                startChat(room)
            }
            binding.btnChatBottom.setOnClickListener {
                startChat(room)
            }
            binding.btnBookNow.setOnClickListener {
                startPayment(room)
            }

            setupImagePager(room.imageUrls)
        }
    }

    companion object {
        private val AMENITY_ICONS = mapOf(
            "High-Speed WiFi" to R.drawable.baseline_wifi_24,
            "Air Conditioning" to R.drawable.baseline_ac_unit_24,
            "Attached Bath" to R.drawable.baseline_security_24,
            "Fully Furnished" to R.drawable.baseline_kitchen_24,
            "Washing Machine" to R.drawable.baseline_local_laundry_service_24,
            "Balcony" to R.drawable.baseline_pool_24
        )
    }

    private fun bindRoomData(room: Room) {
        val formattedPrice = "\u20B9${room.price}"
        binding.tvRoomTitle.text = room.roomName
        binding.tvPrice.text = formattedPrice
        binding.tvBottomPrice.text = formattedPrice
        binding.tvMonthlyRent.text = formattedPrice
        binding.tvLocation.text = room.location ?: "Location not specified"

        if (!room.deposit.isNullOrEmpty()) {
            binding.tvDeposit.text = "\u20B9${room.deposit}"
        } else {
            binding.tvDeposit.text = "N/A"
        }

        val roomDescription = room.description
        if (roomDescription != null && roomDescription.startsWith("Type:")) {
            val parts = roomDescription.split("\n\n", limit = 2)
            val roomType = parts[0].removePrefix("Type:").trim()
            binding.chipPropertyType.text = roomType
            val userText = if (parts.size > 1) parts[1].trim() else ""
            if (userText.isNotEmpty()) {
                binding.tvDescription.text = userText
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }
        } else {
            binding.chipPropertyType.text = "Room"
            binding.tvDescription.text = roomDescription ?: "No description provided."
        }

        setupAmenities(room.amenities)
    }

    private fun setupAmenities(amenityNames: List<String>?) {
        if (amenityNames.isNullOrEmpty()) {
            binding.layoutAmenities.visibility = View.GONE
            return
        }

        var currentRow: LinearLayout? = null
        var chipCount = 0

        for (label in amenityNames) {
            val iconRes = AMENITY_ICONS[label] ?: R.drawable.baseline_check_circle_24
            if (chipCount % 2 == 0) {
                currentRow = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    if (chipCount > 0) {
                        (layoutParams as LinearLayout.LayoutParams).topMargin =
                            resources.getDimensionPixelSize(R.dimen.spacing_8)
                    }
                }
                binding.layoutAmenities.addView(currentRow)
            }

            val chip = LayoutInflater.from(this).inflate(
                R.layout.item_amenity_chip,
                binding.layoutAmenities,
                false
            ) as Chip

            chip.apply {
                val iconDrawable = ContextCompat.getDrawable(this@RoomDetailsActivity, iconRes)
                chipIcon = iconDrawable
                text = label
                isChecked = true
                isClickable = false
                isCheckable = false
            }

            val lp = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            if (chipCount % 2 == 0) {
                lp.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_8)
            } else {
                lp.marginStart = resources.getDimensionPixelSize(R.dimen.spacing_8)
            }
            chip.layoutParams = lp

            currentRow?.addView(chip)
            chipCount++
        }
    }

    private fun startChat(room: Room) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("RECEIVER_ID", room.userId)
        intent.putExtra("ROOM_ID", room.id)
        startActivity(intent)
    }

    private fun startPayment(room: Room) {
        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("ROOM_ID", room.id)
            putExtra("ROOM_NAME", room.roomName)
            putExtra("ROOM_PRICE", room.price)
            putExtra("ROOM_DEPOSIT", room.deposit)
        }
        startActivity(intent)
    }

    private fun setupImagePager(imageUrls: List<String?>?) {
        val images = imageUrls?.filterNotNull()?.filter { it.isNotEmpty() } ?: emptyList()

        if (images.isEmpty()) {
            binding.viewpagerRoomImages.visibility = View.GONE
            binding.tabLayoutImageIndicator.visibility = View.GONE
            return
        }

        binding.viewpagerRoomImages.adapter = object : PagerAdapter() {
            override fun getCount() = images.size

            override fun isViewFromObject(view: View, `object`: Any) = view === `object`

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val imageView = ImageView(this@RoomDetailsActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                val item = images[position]
                if (item.startsWith("http://") || item.startsWith("https://")) {
                    com.bumptech.glide.Glide.with(this@RoomDetailsActivity)
                        .load(item)
                        .placeholder(R.drawable.placeholder_room)
                        .into(imageView)
                } else {
                    try {
                        val bytes = Base64.decode(item, Base64.DEFAULT)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.setImageBitmap(bmp)
                    } catch (e: Exception) {
                        imageView.setImageResource(R.drawable.placeholder_room)
                    }
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
