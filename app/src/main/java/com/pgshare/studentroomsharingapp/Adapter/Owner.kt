package com.pgshare.studentroomsharingapp.Adapter

class Owner {
    var ownerId: String? = null
    var ownerName: String? = null
    var ownerEmail: String? = null
    var ownerPhone: String? = null
    var userType: String? = null
    var imageUrl: String? = null
    var gender: String? = null

    constructor()

    constructor(
        ownerId: String?,
        ownerName: String?,
        ownerPhone: String?,
        gender: String?,
        imageUrl: String?,
        userType: String?
    ) {
        this.ownerId = ownerId
        this.ownerName = ownerName
        this.ownerPhone = ownerPhone
        this.userType = userType
        this.gender = gender
        this.imageUrl = imageUrl
    }

    constructor(ownerId: String?, ownerEmail: String?) {
        this.ownerId = ownerId
        this.ownerEmail = ownerEmail
    }

    constructor(ownerId: String?, ownerName: String?, ownerEmail: String?, ownerPhone: String?) {
        this.ownerId = ownerId
        this.ownerName = ownerName
        this.ownerEmail = ownerEmail
        this.ownerPhone = ownerPhone
    }
}
