package com.pgshare.studentroomsharingapp.Adapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecentChat(
    var chatRoomId : String ="",
    var roomId: String = "",
    var targetUserId: String = "", // The UID of the person they are talking to
    var lastMessage: String = "",
    var timestamp: Long = 0L
    ) : Parcelable

