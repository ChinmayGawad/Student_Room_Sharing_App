package com.pgshare.studentroomsharingapp.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.databinding.ItemRoomBinding

class RoomListAdapter(
    private var roomList: List<Room>,
    private val clickListener: OnRoomClickListener
) : RecyclerView.Adapter<RoomListAdapter.RoomViewHolder>() {

    // Interface to handle clicks cleanly
    interface OnRoomClickListener {
        fun onRoomClick(room: Room)
        fun onSaveClick(room: Room)
    }

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            // 1. Bind Text Data
            binding.tvRoomTitle.text = room.roomName

            // Format location and room type (description holds the room type from our previous step)
            val type = room.description?.replace("Type: ", "")?.uppercase() ?: "ROOM"
            binding.tvRoomLocation.text = "$type • ${room.location?.uppercase()}"

            // Your Room model already has a formatPrice getter!
            binding.tvRoomPrice.text = room.formatPrice

            // 2. Decode the Base64 Image back to a Bitmap
            val base64String = room.imageUrls?.firstOrNull() // Grab the first photo for the thumbnail
            if (!base64String.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imgRoomThumbnail.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Optional: Set a fallback drawable here if decoding fails
                    // binding.imgRoomThumbnail.setImageResource(R.drawable.imageplaceholder)
                }
            } else {
                // If the user uploaded no images somehow, use a placeholder
                // binding.imgRoomThumbnail.setImageResource(R.drawable.imageplaceholder)
            }

            // 3. Handle Clicks
            binding.root.setOnClickListener {
                clickListener.onRoomClick(room)
            }

            binding.btnSaveRoom.setOnClickListener {
                clickListener.onSaveClick(room)
                // We can add logic to swap the heart icon to a filled heart here later
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int = roomList.size

    // Helper function to update the list when Firebase pushes new data
    fun updateData(newRooms: List<Room>) {
        roomList = newRooms
        notifyDataSetChanged()
    }
}