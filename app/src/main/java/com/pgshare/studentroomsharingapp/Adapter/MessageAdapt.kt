package com.pgshare.studentroomsharingapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.R
import java.util.Locale

class MessageAdapter(
    private val messages: ArrayList<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var receiverName: String = "Unknown"
    private var receiverProfileImageUrl: String? = null
    private var receiverId: String? = null

    fun setReceiverName(name: String) {
        this.receiverName = name
        notifyDataSetChanged()
    }

    fun setReceiverId(id: String) {
        this.receiverId = id
        fetchReceiverProfileImage()
    }

    private fun fetchReceiverProfileImage() {
        receiverId?.let { uid ->
            FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            receiverProfileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                            notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val view = inflater.inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message) {
            messageTextView.text = message.message ?: ""
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val textViewUserInitial: TextView = itemView.findViewById(R.id.textViewUserInitial)
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val cardAvatarInitial: View = itemView.findViewById(R.id.cardAvatarInitial)
        private val ivMessageAvatar: ImageView = itemView.findViewById(R.id.ivMessageAvatar)

        fun bind(message: Message) {
            messageTextView.text = message.message ?: ""
            textViewUserName.text = receiverName

            if (receiverName.isNotEmpty() && receiverName != "Unknown") {
                val initial = receiverName.substring(0, 1).uppercase(Locale.getDefault())
                textViewUserInitial.text = initial
            } else {
                textViewUserInitial.text = "?"
            }

            // Load profile image or show initial
            if (!receiverProfileImageUrl.isNullOrEmpty()) {
                cardAvatarInitial.visibility = View.GONE
                ivMessageAvatar.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(receiverProfileImageUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivMessageAvatar)
            } else {
                cardAvatarInitial.visibility = View.VISIBLE
                ivMessageAvatar.visibility = View.GONE
            }
        }
    }
}