package com.pgshare.studentroomsharingapp.Adapter

class Message {
    var message: String? = null
        private set
    var isSentByUser: Boolean = false
        private set
    var timestamp: String? = null
        private set

    var username: String? = null
        private set

    var email: String? = null
        private set

    val roomId: String? = null

    constructor()


    constructor(message: String?, sentByUser: Boolean, username: String?, email: String?) {
        this.message = message
        this.isSentByUser = sentByUser
        // Set timestamp when the message is created
        // Example timestamp, you can use your own logic to set the timestamp
        this.email = email
        this.username = username
    }
}
