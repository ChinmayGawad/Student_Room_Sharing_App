package com.pgshare.studentroomsharingapp.model

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    var id: String? = null,
    var userId: String? = null,
    var roomName: String? = null,
    var location: String? = null,
    var description: String? = null,
    var price: String? = null,
    var deposit: String? = null,
    var imageUrls: ArrayList<String?>? = ArrayList(),
    var imageResourceId: Int = 0,

    var amenities: ArrayList<String>? = ArrayList(),

    @get:PropertyName("roomBooked")
    @set:PropertyName("roomBooked")
    var isRoomBooked: Boolean = false
) : Parcelable {

    // Computed property for UI formatting (ignored by Firebase automatically)
    val formatPrice: String
        get() = "₹$price"
}