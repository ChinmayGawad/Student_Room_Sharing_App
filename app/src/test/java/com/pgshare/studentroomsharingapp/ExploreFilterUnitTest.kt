package com.pgshare.studentroomsharingapp

import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.util.RoomFilterEngine
import com.pgshare.studentroomsharingapp.viewmodel.FilterOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExploreFilterUnitTest {

    private lateinit var sampleRooms: List<Room>

    @Before
    fun setup() {
        sampleRooms = listOf(
            Room(
                id = "1",
                roomName = "Cozy Shared PG for Students",
                location = "Metro Station West",
                price = "6000",
                deposit = "12000",
                description = "Type: PG\nAffordable shared accommodation for students near metro.",
                amenities = arrayListOf("WiFi", "Laundry", "Attached Bath"),
                isRoomBooked = false
            ),
            Room(
                id = "2",
                roomName = "Premium Private Single Room",
                location = "Tech Park Sector 5",
                price = "12500",
                deposit = "25000",
                description = "Type: Private Room\nSpacious air-conditioned private room with balcony.",
                amenities = arrayListOf("WiFi", "AC", "Balcony", "Furnished"),
                isRoomBooked = false
            ),
            Room(
                id = "3",
                roomName = "Modern Female Student Flat",
                location = "University North Gate",
                price = "9000",
                deposit = "18000",
                description = "Type: Flat\nFully furnished 2BHK flat share near college campus.",
                amenities = arrayListOf("WiFi", "AC", "Furnished", "Laundry"),
                isRoomBooked = true // Already booked
            ),
            Room(
                id = "4",
                roomName = "Budget Double Sharing Room",
                location = "South Campus Avenue",
                price = "4500",
                deposit = "9000",
                description = "Type: Shared Room\nDouble sharing student room with mess facilities.",
                amenities = arrayListOf("WiFi"),
                isRoomBooked = false
            )
        )
    }

    @Test
    fun defaultFilter_returnsAllRooms() {
        val result = RoomFilterEngine.filter(sampleRooms)
        assertEquals(4, result.size)
    }

    @Test
    fun searchQuery_matchesTitle() {
        val result = RoomFilterEngine.filter(sampleRooms, query = "Premium")
        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
    }

    @Test
    fun searchQuery_matchesLocation() {
        val result = RoomFilterEngine.filter(sampleRooms, query = "Metro Station")
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun searchQuery_matchesAmenity() {
        val result = RoomFilterEngine.filter(sampleRooms, query = "Balcony")
        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
    }

    @Test
    fun priceFilter_withinRange() {
        val options = FilterOptions(minPrice = 5000, maxPrice = 10000)
        val result = RoomFilterEngine.filter(sampleRooms, options = options)
        // Room 1 (6000), Room 3 (9000) match
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "3" })
    }

    @Test
    fun roomTypeFilter_filtersCorrectType() {
        val options = FilterOptions(roomType = "Private Room")
        val result = RoomFilterEngine.filter(sampleRooms, options = options)
        assertEquals(1, result.size)
        assertEquals("2", result[0].id)
    }

    @Test
    fun availabilityFilter_excludesBookedRooms() {
        val options = FilterOptions(onlyAvailable = true)
        val result = RoomFilterEngine.filter(sampleRooms, options = options)
        // Room 3 is booked, so should only return 3 rooms
        assertEquals(3, result.size)
        assertFalse(result.any { it.isRoomBooked })
    }

    @Test
    fun requiredAmenities_matchesAllSelected() {
        val options = FilterOptions(amenities = setOf("WiFi", "AC", "Furnished"))
        val result = RoomFilterEngine.filter(sampleRooms, options = options)
        // Room 2 and Room 3 have WiFi, AC, Furnished
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "2" })
        assertTrue(result.any { it.id == "3" })
    }

    @Test
    fun compoundFilter_combinesPriceTypeAvailabilityAndAmenities() {
        val options = FilterOptions(
            minPrice = 5000,
            maxPrice = 15000,
            roomType = "Flat",
            onlyAvailable = true // Room 3 is Flat but booked
        )
        val result = RoomFilterEngine.filter(sampleRooms, options = options)
        // Room 3 matches type and price but is booked, so result should be empty
        assertTrue(result.isEmpty())
    }

    @Test
    fun quickCriteriaChip_under8000() {
        val result = RoomFilterEngine.filter(sampleRooms, criteria = "Under ₹8,000")
        // Room 1 (6000) and Room 4 (4500)
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "4" })
    }

    @Test
    fun parsePrice_handlesFormattedCurrencies() {
        assertEquals(8500, RoomFilterEngine.parsePrice("₹8,500"))
        assertEquals(12000, RoomFilterEngine.parsePrice("12000/mo"))
        assertEquals(0, RoomFilterEngine.parsePrice(""))
        assertEquals(0, RoomFilterEngine.parsePrice(null))
    }
}
