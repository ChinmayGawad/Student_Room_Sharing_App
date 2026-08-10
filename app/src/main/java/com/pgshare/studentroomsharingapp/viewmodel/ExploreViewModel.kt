package com.pgshare.studentroomsharingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExploreUiState(
    val allRooms: List<Room> = emptyList(),
    val displayRooms: List<Room> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val ownerNames: Map<String, String> = emptyMap(),
    val savedRoomKeys: Set<String> = emptySet(),
    val searchQuery: String = "",
    val filterCriteria: String? = null
)

class ExploreViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val currentUserId = repository.getCurrentUserId()

    private val ownerNameCache = mutableMapOf<String, String>()

    init {
        observeRooms()
        observeSavedKeys()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
    }

    private fun observeRooms() {
        viewModelScope.launch {
            repository.observeRooms().collect { rooms ->
                if (rooms.isEmpty()) {
                    repository.seedDemoRooms()
                }
                val ownerIds = rooms.mapNotNull { it.userId }.distinct()
                val names = if (ownerIds.isNotEmpty()) fetchOwnerNames(ownerIds) else emptyMap()
                _uiState.value = _uiState.value.copy(
                    allRooms = rooms,
                    isLoading = false,
                    isRefreshing = false,
                    ownerNames = names
                )
                filterAndSearch()
            }
        }
    }

    private suspend fun fetchOwnerNames(userIds: List<String>): Map<String, String> {
        val uncached = userIds.filter { it !in ownerNameCache }
        if (uncached.isNotEmpty()) {
            val fetched = coroutineScope {
                uncached.map { uid ->
                    async {
                        val name = repository.getUserName(uid)
                        uid to (name ?: "Unknown User")
                    }
                }.associate { it.await() }
            }
            ownerNameCache.putAll(fetched)
        }
        return ownerNameCache.filterKeys { it in userIds }
    }

    fun toggleFavorite(room: Room) {
        val userId = currentUserId ?: return
        val roomId = room.id ?: return
        viewModelScope.launch {
            val isSaved = _uiState.value.savedRoomKeys.contains(roomId)
            if (isSaved) {
                repository.removeFavorite(userId, roomId)
            } else {
                repository.addFavorite(userId, room)
            }
        }
    }

    private fun observeSavedKeys() {
        if (currentUserId == null) return
        viewModelScope.launch {
            repository.observeFavorites(currentUserId).collect { favorites ->
                val keys = favorites.mapNotNull { it.id }.toSet()
                _uiState.value = _uiState.value.copy(savedRoomKeys = keys)
            }
        }
    }

    private fun filterAndSearch() {
        val all = _uiState.value.allRooms
        val query = _uiState.value.searchQuery.lowercase()
        val criteria = _uiState.value.filterCriteria

        var filtered = all.filter { room ->
            val matchesSearch = room.location?.lowercase()?.contains(query) == true ||
                    room.roomName?.lowercase()?.contains(query) == true ||
                    room.description?.lowercase()?.contains(query) == true
            matchesSearch
        }

        if (criteria != null) {
            filtered = when (criteria) {
                "Under ₹8,000" -> filtered.filter {
                    val price = it.price?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
                    price in 1..8000
                }
                "Private Room" -> filtered.filter {
                    it.description?.contains("Private Room", ignoreCase = true) == true
                }
                "AC" -> filtered.filter {
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
        _uiState.value = _uiState.value.copy(displayRooms = filtered)
    }

    fun applyFilter(criteria: String) {
        _uiState.value = _uiState.value.copy(filterCriteria = criteria)
        filterAndSearch()
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterAndSearch()
    }

    fun clearFilter() {
        _uiState.value = _uiState.value.copy(filterCriteria = null)
        filterAndSearch()
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExploreViewModel() as T
        }
    }
}
