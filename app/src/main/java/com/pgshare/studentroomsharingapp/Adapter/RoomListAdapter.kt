package com.pgshare.studentroomsharingapp.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.ItemRoomBinding
import com.pgshare.studentroomsharingapp.model.Room

class RoomListAdapter(
    private var roomList: List<Room>,
    private val clickListener: OnRoomClickListener
) : RecyclerView.Adapter<RoomListAdapter.RoomViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val favoritesRef = FirebaseDatabase.getInstance().getReference("Users")

    private var ownerNames: Map<String, String> = emptyMap()
    private var savedRoomKeys: Set<String> = emptySet()

    fun setOwnerNames(names: Map<String, String>) {
        ownerNames = names
        notifyDataSetChanged()
    }

    fun setSavedRoomKeys(keys: Set<String>) {
        savedRoomKeys = keys
        notifyDataSetChanged()
    }

    interface OnRoomClickListener {
        fun onRoomClick(room: Room)
        fun onSaveClick(room: Room)
    }

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.roomTitle.text = room.roomName

            val type = room.description?.replace("Type: ", "")?.uppercase() ?: "ROOM"
            binding.roomDistance.text = "$type • ${room.location?.uppercase()}"

            binding.roomPrice.text = room.formatPrice

            val base64String = room.imageUrls?.firstOrNull()
            if (!base64String.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.roomImage.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.roomImage.setImageResource(R.drawable.placeholder_room)
                }
            } else {
                binding.roomImage.setImageResource(R.drawable.placeholder_room)
            }

            val rawKey = room.roomName ?: "UnknownRoom"
            val roomKey = rawKey.replace(Regex("[.#$\\[\\]]"), "")
            val isSaved = savedRoomKeys.contains(roomKey)

            updateHeartIcon(isSaved)

            if (currentUserId != null) {
                val userFavoritesRef = favoritesRef.child(currentUserId).child("favorites").child(roomKey)

                binding.btnBookmark.setOnClickListener {
                    val newSaved = !isSaved
                    updateHeartIcon(newSaved)
                    if (newSaved) {
                        userFavoritesRef.setValue(room)
                    } else {
                        userFavoritesRef.removeValue()
                    }
                    clickListener.onSaveClick(room)
                }
            } else {
                binding.btnBookmark.setOnClickListener {
                    clickListener.onSaveClick(room)
                }
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
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int = roomList.size

    fun updateData(newRooms: List<Room>) {
        roomList = newRooms
        notifyDataSetChanged()
    }
}
