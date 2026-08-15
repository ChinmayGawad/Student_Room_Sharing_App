package com.pgshare.studentroomsharingapp

import com.pgshare.studentroomsharingapp.model.Room
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomModelUnitTest {

    @Test
    fun roomPriceFormatting_returnsCorrectFormattedCurrency() {
        val room = Room(
            id = "room_1",
            roomName = "Luxury Shared PG",
            price = "7500",
            deposit = "15000"
        )

        assertEquals("₹7500", room.formatPrice)
    }

    @Test
    fun defaultRoomBookingStatus_isFalse() {
        val room = Room(
            id = "room_2",
            roomName = "Cozy Studio Flat",
            price = "12000"
        )

        assertFalse(room.isRoomBooked)
    }

    @Test
    fun roomBookingStatus_canBeUpdated() {
        val room = Room(
            id = "room_3",
            roomName = "Single AC Room",
            price = "9000",
            isRoomBooked = true
        )

        assertTrue(room.isRoomBooked)
    }
}
