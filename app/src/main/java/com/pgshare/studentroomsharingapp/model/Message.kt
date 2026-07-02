package com.pgshare.studentroomsharingapp.model

data class Message(
    var messageId: String? = null,
    var message: String? = null,
    var senderId: String? = null,    // Replaces isSentByUser
    var username: String? = null,
    var email: String? = null,
    var roomId: String? = null,
    var timestamp: Long = System.currentTimeMillis() // Long is better for chronological sorting
)
