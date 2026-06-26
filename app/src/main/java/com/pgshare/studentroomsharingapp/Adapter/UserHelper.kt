package com.pgshare.studentroomsharingapp.Adapter

class UserHelper {
    var userId: String? = null
    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var gender: String? = null

    // Required default constructor
    constructor()

    constructor(userId: String?, name: String?, email: String?, phone: String?, gender: String?) {
        this.userId = userId
        this.name = name
        this.email = email
        this.phone = phone
        this.gender = gender
    }
}
