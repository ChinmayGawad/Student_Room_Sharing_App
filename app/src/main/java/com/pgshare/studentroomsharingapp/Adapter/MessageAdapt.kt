package com.pgshare.studentroomsharingapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.R
import java.util.Locale

class MessageAdapter(
    private val messages: ArrayList<Message>,
    private val currentUserId: String // CHANGED: Now expects a UID instead of an email
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        // CHANGED: Compare the message's senderId to the current user's UID
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

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(message: Message) {
            messageTextView.text = message.message ?: ""
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val textViewUserInitial: TextView = itemView.findViewById(R.id.textViewUserInitial)
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)

        fun bind(message: Message) {
            messageTextView.text = message.message ?: ""

            // Safely handle the username and initial (Defaults to "Unknown" if null)
            val username = message.username ?: "Unknown"
            textViewUserName.text = username

            if (username.isNotEmpty()) {
                val initial = username.substring(0, 1).uppercase(Locale.getDefault())
                textViewUserInitial.text = initial
            }
        }
    }
}