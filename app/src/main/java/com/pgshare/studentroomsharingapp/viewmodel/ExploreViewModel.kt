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

data class FilterOptions(
    val minPrice: Int = 2000,
    val maxPrice: Int = 30000,
    val roomType: String = "Any",
    val amenities: Set<String> = emptySet(),
    val onlyAvailable: Boolean = false
) {
    val isDefault: Boolean
        get() = minPrice <= 2000 && maxPrice >= 30000 && (roomType == "Any" || roomType.isEmpty()) && amenities.isEmpty() && !onlyAvailable
}

data class ExploreUiState(
    val allRooms: List<Room> = emptyList(),
    val displayRooms: List<Room> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val ownerNames: Map<String, String> = emptyMap(),
    val savedRoomKeys: Set<String> = emptySet(),
    val searchQuery: String = "",
    val filterCriteria: String? = null,
    val filterOptions: FilterOptions = FilterOptions()
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
        viewModelScope.launch {
            try {
                if (_uiState.value.allRooms.isEmpty()) {
                    repository.seedDemoRooms()
                }
                val ownerIds = _uiState.value.allRooms.mapNotNull { it.userId }.distinct()
                if (ownerIds.isNotEmpty()) {
                    val names = fetchOwnerNames(ownerIds)
                    _uiState.value = _uiState.value.copy(ownerNames = names)
                }
                kotlinx.coroutines.delay(600)
            } catch (e: Exception) {
                android.util.Log.w("ExploreViewModel", "Refresh error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    private fun observeRooms() {
        viewModelScope.launch {
            repository.observeRooms().collect { rooms ->
                if (rooms.isEmpty()) {
                    viewModelScope.launch { repository.seedDemoRooms() }
                }
                _uiState.value = _uiState.value.copy(
                    allRooms = rooms,
                    isLoading = false,
                    isRefreshing = false
                )
                filterAndSearch()

                val ownerIds = rooms.mapNotNull { it.userId }.distinct()
                if (ownerIds.isNotEmpty()) {
                    viewModelScope.launch {
                        val names = fetchOwnerNames(ownerIds)
                        _uiState.value = _uiState.value.copy(ownerNames = names)
                    }
                }
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
        val filtered = com.pgshare.studentroomsharingapp.util.RoomFilterEngine.filter(
            rooms = _uiState.value.allRooms,
            query = _uiState.value.searchQuery,
            criteria = _uiState.value.filterCriteria,
            options = _uiState.value.filterOptions
        )
        _uiState.value = _uiState.value.copy(displayRooms = filtered)
    }

    fun applyFilterOptions(options: FilterOptions) {
        _uiState.value = _uiState.value.copy(filterOptions = options)
        filterAndSearch()
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
        _uiState.value = _uiState.value.copy(filterCriteria = null, filterOptions = FilterOptions())
        filterAndSearch()
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExploreViewModel() as T
        }
    }
}
