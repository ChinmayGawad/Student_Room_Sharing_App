package com.pgshare.studentroomsharingapp.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.ChatActivity
import com.pgshare.studentroomsharingapp.R
import java.text.SimpleDateFormat
import java.util.*

class InboxAdapter(
    private val inboxList: ArrayList<RecentChat>
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val recentChat = inboxList[position]
        holder.bind(recentChat)
    }

    override fun getItemCount(): Int = inboxList.size

    class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvAvatarInitial: TextView = itemView.findViewById(R.id.tvAvatarInitial)

        fun bind(chat: RecentChat) {
            Log.d("InboxDebug", "Trying to load User ID: '${chat.targetUserId}' for chat: ${chat.lastMessage}")
            tvLastMessage.text = chat.lastMessage

            // Format the timestamp
            val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(chat.timestamp))

            // 1. PREVENT RECYCLING BUG: Reset the UI
            tvUserName.text = "Loading..."
            tvAvatarInitial.text = "?"

            // 2. Look ONLY at your official "Users" node
            val usersRef =
                FirebaseDatabase.getInstance().getReference("Users").child(chat.targetUserId)

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // --- START DATA INSPECTOR ---
                    if (snapshot.exists()) {
                        Log.d(
                            "InboxDebug",
                            "--- Inspecting Data for User ID: ${chat.targetUserId} ---"
                        )
                        for (child in snapshot.children) {
                            Log.d(
                                "InboxDebug",
                                "Found Key: '${child.key}', Value: '${child.value}'"
                            )
                        }
                        Log.d("InboxDebug", "--- End Inspection ---")
                    } else {
                        Log.d(
                            "InboxDebug",
                            "User snapshot does not exist for ID: ${chat.targetUserId}!"
                        )
                    }
                    // --- END DATA INSPECTOR ---

                    val username = snapshot.child("username").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)

                    // 3. Smart Name Extraction
                    val finalName = when {
                        !username.isNullOrEmpty() -> username
                        !email.isNullOrEmpty() -> {
                            val extracted = email.substringBefore("@")
                            extracted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        }

                        else -> "Unknown User"
                    }

                    tvUserName.text = finalName
                    tvAvatarInitial.text =
                        if (finalName != "Unknown User" && finalName.isNotEmpty()) {
                            finalName.take(1).uppercase(Locale.getDefault())
                        } else {
                            "?"
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("InboxDebug", "Fetch failed: ${error.message}")
                    tvUserName.text = "Unknown User"
                    tvAvatarInitial.text = "?"
                }
            })

            // Handle clicking the row to open the Chat
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java).apply {
                    putExtra("RECEIVER_ID", chat.targetUserId)
                    putExtra("ROOM_ID", chat.roomId)
                }
                itemView.context.startActivity(intent)
            }
        }



        // Helper function to extract the best possible name
        private fun extractAndSetUser(snapshot: DataSnapshot) {
            val username = snapshot.child("username").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)

            val finalName = when {
                !username.isNullOrEmpty() -> username
                !email.isNullOrEmpty() -> {
                    // Extract name from email (e.g., "john.doe@email.com" -> "John.doe")
                    val extracted = email.substringBefore("@")
                    extracted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
                else -> "Unknown User"
            }

            tvUserName.text = finalName
            tvAvatarInitial.text = if (finalName != "Unknown User") finalName.substring(0, 1).uppercase(Locale.getDefault()) else "?"
        }

        // Helper function for missing users
        private fun setUnknownUser() {
            tvUserName.text = "Unknown User"
            tvAvatarInitial.text = "?"
        }
    }
}