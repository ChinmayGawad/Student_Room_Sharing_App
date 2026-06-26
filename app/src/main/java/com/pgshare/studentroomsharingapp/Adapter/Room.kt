package com.pgshare.studentroomsharingapp.Adapter

import android.os.Parcel
import android.os.Parcelable

class Room : Parcelable {
    var id: String? = null
    @JvmField
    var roomName: String? = null
    @JvmField
    var location: String? = null
    var description: String? = null
    private var price: String? = null
    @JvmField
    var imageUrls: ArrayList<String?>? = ArrayList<String?>()
    var imageResourceId: Int = 0
    var isRoomBooked: Boolean = false // Add new field for booking status

    // Getters and setters
    val owner: Owner? = null

    // Default constructor with no arguments (required by Firebase)
    constructor()

    constructor(
        id: String?,
        roomName: String?,
        location: String?,
        description: String?,
        price: String?,
        imageUrls: ArrayList<String?>?,
        imageResourceId: Int
    ) {
        this.id = id
        this.roomName = roomName
        this.location = location
        this.description = description
        this.price = price
        this.imageUrls = imageUrls
        this.imageResourceId = imageResourceId
        this.isRoomBooked = false // Initialize booked status to false
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        roomName = `in`.readString()
        location = `in`.readString()
        description = `in`.readString()
        price = `in`.readString()
        imageUrls = ArrayList<String?>() // Initialize the ArrayList
        `in`.readStringList(imageUrls!!)
        imageResourceId = `in`.readInt()
        isRoomBooked = `in`.readByte().toInt() != 0 // Read booking status from Parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(id)
        out.writeString(roomName)
        out.writeString(location)
        out.writeString(description)
        out.writeString(price)
        out.writeStringList(imageUrls)
        out.writeInt(imageResourceId)
        out.writeByte((if (isRoomBooked) 1 else 0).toByte()) // Write booking status to Parcel
    }

    fun getPrice(): String {
        return price!!
    }

    fun setPrice(price: String?) {
        this.price = price
    }

    val formatPrice: String
        get() =// Format price to display with two decimal places and currency symbol
            "₹" + getPrice()

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Room?> = object : Parcelable.Creator<Room?> {
            override fun createFromParcel(`in`: Parcel): Room {
                return Room(`in`)
            }

            override fun newArray(size: Int): Array<Room?> {
                return arrayOfNulls<Room>(size)
            }
        }
    }
}