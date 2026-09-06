package com.pgshare.studentroomsharingapp.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.ItemRoomBinding
import com.pgshare.studentroomsharingapp.model.Room

class RoomListAdapter(
    private val clickListener: OnRoomClickListener
) : ListAdapter<Room, RoomListAdapter.RoomViewHolder>(RoomDiffCallback()) {

    private var ownerNames: Map<String, String> = emptyMap()
    private var savedRoomKeys: Set<String> = emptySet()

    fun setOwnerNames(names: Map<String, String>) {
        ownerNames = names
    }

    fun setSavedRoomKeys(keys: Set<String>) {
        if (savedRoomKeys != keys) {
            savedRoomKeys = keys
            notifyDataSetChanged()
        }
    }

    interface OnRoomClickListener {
        fun onRoomClick(room: Room)
        fun onSaveClick(room: Room)
    }

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.roomTitle.text = room.roomName

            val rawDesc = room.description ?: ""
            val type = if (rawDesc.startsWith("Type:")) {
                rawDesc.substringAfter("Type:").substringBefore("\n").trim().uppercase()
            } else {
                "ROOM"
            }
            val loc = room.location?.trim()?.uppercase() ?: "LOCATION NOT SPECIFIED"
            binding.roomDistance.text = "$type \u2022 $loc"

            binding.roomPrice.text = room.formatPrice

            if (room.isRoomBooked) {
                binding.chipRoomStatus.text = "BOOKED"
                binding.chipRoomStatus.chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
            } else {
                binding.chipRoomStatus.text = "AVAILABLE"
                binding.chipRoomStatus.chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981"))
            }

            val firstImg = room.imageUrls?.firstOrNull()
            if (!firstImg.isNullOrEmpty()) {
                if (firstImg.startsWith("http://") || firstImg.startsWith("https://")) {
                    com.bumptech.glide.Glide.with(binding.roomImage.context)
                        .load(firstImg)
                        .placeholder(R.drawable.placeholder_room)
                        .error(R.drawable.placeholder_room)
                        .into(binding.roomImage)
                } else {
                    try {
                        val cleanBase64 = if (firstImg.contains(",")) firstImg.substringAfter(",") else firstImg
                        val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        if (bitmap != null) {
                            binding.roomImage.setImageBitmap(bitmap)
                        } else {
                            binding.roomImage.setImageResource(R.drawable.placeholder_room)
                        }
                    } catch (e: Exception) {
                        binding.roomImage.setImageResource(R.drawable.placeholder_room)
                    }
                }
            } else {
                binding.roomImage.setImageResource(R.drawable.placeholder_room)
            }

            bindAmenities(room.amenities)

            val isSaved = room.id?.let { savedRoomKeys.contains(it) } ?: false

            updateHeartIcon(isSaved)

            binding.btnBookmark.setOnClickListener {
                clickListener.onSaveClick(room)
            }

            binding.root.setOnClickListener {
                clickListener.onRoomClick(room)
            }
        }

        private fun bindAmenities(amenities: List<String>?) {
            binding.layoutAmenitiesContainer.removeAllViews()
            val list = amenities?.filter { it.isNotBlank() } ?: emptyList()
            if (list.isEmpty()) {
                binding.layoutAmenitiesContainer.visibility = android.view.View.GONE
                return
            }
            binding.layoutAmenitiesContainer.visibility = android.view.View.VISIBLE
            val context = binding.root.context
            val displayList = list.take(3)

            for (amenity in displayList) {
                val iconRes = getAmenityIcon(amenity)
                val itemLayout = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = (12 * context.resources.displayMetrics.density).toInt()
                    }
                }

                val iconView = android.widget.ImageView(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        (16 * context.resources.displayMetrics.density).toInt(),
                        (16 * context.resources.displayMetrics.density).toInt()
                    )
                    setImageResource(iconRes)
                    setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.onSurfaceVariant))
                }

                val textView = android.widget.TextView(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = (4 * context.resources.displayMetrics.density).toInt()
                    }
                    text = getShortAmenityName(amenity)
                    setTextAppearance(R.style.TextAppearance_BodySmall)
                    setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.onSurfaceVariant))
                }

                itemLayout.addView(iconView)
                itemLayout.addView(textView)
                binding.layoutAmenitiesContainer.addView(itemLayout)
            }
        }

        private fun getShortAmenityName(amenity: String): String {
            return when {
                amenity.contains("WiFi", ignoreCase = true) -> "WiFi"
                amenity.contains("Air Conditioning", ignoreCase = true) || amenity.equals("AC", ignoreCase = true) -> "AC"
                amenity.contains("Furnished", ignoreCase = true) -> "Furnished"
                amenity.contains("Bath", ignoreCase = true) -> "Attached Bath"
                amenity.contains("Washing", ignoreCase = true) || amenity.contains("Laundry", ignoreCase = true) -> "Laundry"
                amenity.contains("Balcony", ignoreCase = true) -> "Balcony"
                amenity.contains("Kitchen", ignoreCase = true) -> "Kitchen"
                else -> amenity.take(12)
            }
        }

        private fun getAmenityIcon(amenity: String): Int {
            return when {
                amenity.contains("WiFi", ignoreCase = true) -> R.drawable.baseline_wifi_24
                amenity.contains("Air Conditioning", ignoreCase = true) || amenity.equals("AC", ignoreCase = true) -> R.drawable.baseline_ac_unit_24
                amenity.contains("Furnished", ignoreCase = true) || amenity.contains("Kitchen", ignoreCase = true) -> R.drawable.baseline_kitchen_24
                amenity.contains("Bath", ignoreCase = true) -> R.drawable.baseline_security_24
                amenity.contains("Washing", ignoreCase = true) || amenity.contains("Laundry", ignoreCase = true) -> R.drawable.baseline_local_laundry_service_24
                amenity.contains("Balcony", ignoreCase = true) -> R.drawable.baseline_pool_24
                else -> R.drawable.baseline_check_circle_24
            }
        }

        private fun updateHeartIcon(isSaved: Boolean) {
            if (isSaved) {
                binding.btnBookmark.setImageResource(R.drawable.baseline_favorite_24)
                binding.btnBookmark.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.colorError)
                )
            } else {
                binding.btnBookmark.setImageResource(R.drawable.baseline_favorite_border_24)
                binding.btnBookmark.setColorFilter(android.graphics.Color.WHITE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class RoomDiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
            return oldItem == newItem
        }
    }
}
