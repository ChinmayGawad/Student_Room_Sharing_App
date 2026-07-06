package com.pgshare.studentroomsharingapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.model.Message
import java.util.Locale

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private var receiverName: String = "Unknown"
    var receiverProfileImageUrl: String? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    fun setReceiverName(name: String) {
        receiverName = name
    }

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
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
        val message = getItem(position)
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

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

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.message == newItem.message &&
                    oldItem.timestamp == newItem.timestamp
        }
    }
}
