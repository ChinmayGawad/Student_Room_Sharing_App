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
        savedRoomKeys = keys
    }

    interface OnRoomClickListener {
        fun onRoomClick(room: Room)
        fun onSaveClick(room: Room)
    }

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.roomTitle.text = room.roomName

            val type = room.description?.replace("Type: ", "")?.uppercase() ?: "ROOM"
            binding.roomDistance.text = "$type \u2022 ${room.location?.uppercase()}"

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

            val isSaved = room.id?.let { savedRoomKeys.contains(it) } ?: false

            updateHeartIcon(isSaved)

            binding.btnBookmark.setOnClickListener {
                clickListener.onSaveClick(room)
            }

            binding.root.setOnClickListener {
                clickListener.onRoomClick(room)
            }
        }

        private fun updateHeartIcon(isSaved: Boolean) {
            if (isSaved) {
                binding.btnBookmark.setImageResource(R.drawable.baseline_favorite_24)
            } else {
                binding.btnBookmark.setImageResource(R.drawable.baseline_favorite_border_24)
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
