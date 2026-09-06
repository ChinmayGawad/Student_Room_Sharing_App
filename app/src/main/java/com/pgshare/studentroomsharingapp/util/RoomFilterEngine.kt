package com.pgshare.studentroomsharingapp.util

import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.viewmodel.FilterOptions

object RoomFilterEngine {

    fun filter(
        rooms: List<Room>,
        query: String = "",
        criteria: String? = null,
        options: FilterOptions = FilterOptions()
    ): List<Room> {
        val trimmedQuery = query.lowercase().trim()

        var filtered = rooms.filter { room ->
            if (trimmedQuery.isEmpty()) return@filter true
            room.location?.lowercase()?.contains(trimmedQuery) == true ||
                    room.roomName?.lowercase()?.contains(trimmedQuery) == true ||
                    room.description?.lowercase()?.contains(trimmedQuery) == true ||
                    room.amenities?.any { it.lowercase().contains(trimmedQuery) } == true
        }

        // Quick criteria chips
        if (criteria != null) {
            filtered = when (criteria) {
                "Under ₹8,000" -> filtered.filter {
                    val price = parsePrice(it.price)
                    price in 1..8000
                }
                "Private Room" -> filtered.filter {
                    it.description?.contains("Private Room", ignoreCase = true) == true
                }
                "AC" -> filtered.filter {
                    it.amenities?.any { a -> a.contains("AC", ignoreCase = true) || a.contains("Air Conditioning", ignoreCase = true) } == true ||
                            it.description?.contains("AC", ignoreCase = true) == true
                }
                "Student Friendly" -> filtered.filter {
                    it.description?.contains("Student", ignoreCase = true) == true
                }
                "Room" -> filtered.filter {
                    it.description?.contains("Room", ignoreCase = true) == true
                }
                "PG" -> filtered.filter {
                    it.description?.contains("PG", ignoreCase = true) == true
                }
                "Flat" -> filtered.filter {
                    it.description?.contains("Flat", ignoreCase = true) == true
                }
                else -> filtered
            }
        }

        // Detailed Filter Options
        if (!options.isDefault) {
            filtered = filtered.filter { room ->
                // Price filter
                val roomPrice = parsePrice(room.price)
                if (roomPrice !in options.minPrice..options.maxPrice) {
                    return@filter false
                }

                // Room Type filter
                if (options.roomType != "Any" && options.roomType.isNotBlank()) {
                    val matchesType = room.description?.contains(options.roomType, ignoreCase = true) == true ||
                            room.roomName?.contains(options.roomType, ignoreCase = true) == true
                    if (!matchesType) return@filter false
                }

                // Availability filter
                if (options.onlyAvailable && room.isRoomBooked) {
                    return@filter false
                }

                // Required Amenities filter
                if (options.amenities.isNotEmpty()) {
                    val roomAmenities = room.amenities?.map { it.lowercase() } ?: emptyList()
                    val roomDesc = room.description?.lowercase() ?: ""
                    for (required in options.amenities) {
                        val reqLower = required.lowercase()
                        val hasAmenity = roomAmenities.any { it.contains(reqLower) } ||
                                roomDesc.contains(reqLower)
                        if (!hasAmenity) return@filter false
                    }
                }

                true
            }
        }

        return filtered
    }

    fun parsePrice(priceStr: String?): Int {
        if (priceStr.isNullOrBlank()) return 0
        return priceStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
    }
}
