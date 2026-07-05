package com.pgshare.studentroomsharingapp.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pgshare.studentroomsharingapp.ChatActivity
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.model.RecentChat
import java.text.SimpleDateFormat
import java.util.*

data class InboxUserInfo(
    val displayName: String,
    val profileImageUrl: String?
)

class InboxAdapter : ListAdapter<RecentChat, InboxAdapter.InboxViewHolder>(InboxDiffCallback()) {

    private var userProfiles: Map<String, InboxUserInfo> = emptyMap()

    fun setUserProfiles(profiles: Map<String, InboxUserInfo>) {
        userProfiles = profiles
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(getItem(position), userProfiles)
    }

    class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvAvatarInitial: TextView = itemView.findViewById(R.id.tvAvatarInitial)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)

        fun bind(chat: RecentChat, profiles: Map<String, InboxUserInfo>) {
            tvLastMessage.text = chat.lastMessage

            val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(chat.timestamp))

            val info = profiles[chat.targetUserId]

            if (info != null) {
                tvUserName.text = info.displayName
                if (!info.profileImageUrl.isNullOrEmpty()) {
                    tvAvatarInitial.visibility = View.GONE
                    ivAvatar.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(info.profileImageUrl)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivAvatar)
                } else {
                    tvAvatarInitial.visibility = View.VISIBLE
                    ivAvatar.visibility = View.GONE
                    tvAvatarInitial.text =
                        if (info.displayName.isNotEmpty()) info.displayName.take(1).uppercase(Locale.getDefault())
                        else "?"
                }
            } else {
                tvUserName.text = itemView.context.getString(R.string.loading_placeholder)
                tvAvatarInitial.text = "?"
                tvAvatarInitial.visibility = View.VISIBLE
                ivAvatar.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java).apply {
                    putExtra("RECEIVER_ID", chat.targetUserId)
                    putExtra("ROOM_ID", chat.roomId)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    private class InboxDiffCallback : DiffUtil.ItemCallback<RecentChat>() {
        override fun areItemsTheSame(oldItem: RecentChat, newItem: RecentChat): Boolean {
            return oldItem.chatRoomId == newItem.chatRoomId
        }

        override fun areContentsTheSame(oldItem: RecentChat, newItem: RecentChat): Boolean {
            return oldItem.lastMessage == newItem.lastMessage &&
                    oldItem.timestamp == newItem.timestamp
        }
    }
}
