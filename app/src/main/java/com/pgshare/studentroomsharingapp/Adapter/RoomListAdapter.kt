package com.pgshare.studentroomsharingapp.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.ItemRoomBinding

class RoomListAdapter(
    private var roomList: List<Room>,
    private val clickListener: OnRoomClickListener
) : RecyclerView.Adapter<RoomListAdapter.RoomViewHolder>() {

    // Initialize Firebase references at the adapter level
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val favoritesRef = FirebaseDatabase.getInstance().getReference("Users")
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    // Interface to handle clicks cleanly
    interface OnRoomClickListener {
        fun onRoomClick(room: Room)
        fun onSaveClick(room: Room) // Keeping your original interface intact!
    }

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            // 1. Bind Text Data
            binding.tvRoomTitle.text = room.roomName

            // Format location and room type
            val type = room.description?.replace("Type: ", "")?.uppercase() ?: "ROOM"
            binding.tvRoomLocation.text = "$type • ${room.location?.uppercase()}"

            // Your custom Room model getter
            binding.tvRoomPrice.text = room.formatPrice

            // 2. Fetch and display Owner Name
            val ownerId = room.userId
            if (!ownerId.isNullOrEmpty()) {
                usersRef.child(ownerId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!binding.root.isAttachedToWindow) return // Prevent RecyclerView recycling issues

                        val username = snapshot.child("username").value as? String
                        val email = snapshot.child("email").value as? String
                        val displayName = getDisplayName(username, email)
                        binding.tvRoomOwner.text = "Listed by $displayName"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (binding.root.isAttachedToWindow) {
                            binding.tvRoomOwner.text = "Listed by Unknown User"
                        }
                    }
                })
            } else {
                binding.tvRoomOwner.text = "Listed by Unknown User"
            }

            // 3. Decode the Base64 Image back to a Bitmap
            val base64String = room.imageUrls?.firstOrNull()
            if (!base64String.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imgRoomThumbnail.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.imgRoomThumbnail.setImageResource(R.drawable.imageplaceholder)
                }
            } else {
                binding.imgRoomThumbnail.setImageResource(R.drawable.imageplaceholder)
            }



            // 3. Handle Firebase Save Logic & UI Toggle
            // Sanitize the roomName so Firebase accepts it as a valid database key
            val rawKey = room.roomName ?: "UnknownRoom"
            val roomKey = rawKey.replace(Regex("[.#$\\[\\]]"), "")
            var isSaved = false

            // Only attempt to fetch/write if a user is logged in
            if (currentUserId != null) {
                val userFavoritesRef = favoritesRef.child(currentUserId).child("favorites").child(roomKey)

                // Check initial state from Firebase when the view binds
                userFavoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isSaved = snapshot.exists()
                        updateHeartIcon(isSaved)
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                // Handle Save Button Click
                binding.btnSaveRoom.setOnClickListener {
                    isSaved = !isSaved
                    updateHeartIcon(isSaved) // Optimistic UI update for instant feedback

                    // Sync the change to Firebase Database
                    if (isSaved) {
                        userFavoritesRef.setValue(room)
                    } else {
                        userFavoritesRef.removeValue()
                    }

                    // Still trigger your original interface callback!
                    clickListener.onSaveClick(room)
                }
            } else {
                // Failsafe for logged out users
                updateHeartIcon(false)
                binding.btnSaveRoom.setOnClickListener {
                    // Triggers the interface so you can show a "Please login" Toast in your Fragment
                    clickListener.onSaveClick(room)
                }
            }

            // 4. Handle Entire Card Click
            binding.root.setOnClickListener {
                clickListener.onRoomClick(room)
            }
        }

        // Helper function inside the ViewHolder to swap the drawables
        private fun updateHeartIcon(isSaved: Boolean) {
            if (isSaved) {
                binding.btnSaveRoom.setImageResource(R.drawable.baseline_favorite_24)
            } else {
                binding.btnSaveRoom.setImageResource(R.drawable.baseline_favorite_border_24)
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
    // Helper function for name extraction (same logic as InboxAdapter)
    fun getDisplayName(username: String?, email: String?): String {
        return if (!username.isNullOrBlank()) username
        else if (!email.isNullOrBlank()) email.substringBefore("@")
        else "Unknown User"
    }
}