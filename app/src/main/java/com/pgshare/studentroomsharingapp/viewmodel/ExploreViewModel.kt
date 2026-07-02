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
    val error: String? = null,
    val ownerNames: Map<String, String> = emptyMap(),
    val savedRoomKeys: Set<String> = emptySet()
)

class ExploreViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val currentUserId = repository.getCurrentUserId()

    init {
        observeRooms()
        observeSavedKeys()
    }

    private fun observeRooms() {
        viewModelScope.launch {
            repository.observeRooms().collect { rooms ->
                val ownerIds = rooms.mapNotNull { it.userId }.distinct()
                val names = if (ownerIds.isNotEmpty()) fetchOwnerNames(ownerIds) else emptyMap()
                _uiState.value = _uiState.value.copy(
                    allRooms = rooms,
                    displayRooms = if (_uiState.value.displayRooms.isEmpty() || _uiState.value.displayRooms === _uiState.value.allRooms) rooms else _uiState.value.displayRooms,
                    isLoading = false,
                    ownerNames = names
                )
            }
        }
    }

    private suspend fun fetchOwnerNames(userIds: List<String>): Map<String, String> {
        return coroutineScope {
            userIds.map { uid ->
                async {
                    val name = repository.getUserName(uid)
                    uid to (name ?: "Unknown User")
                }
            }.associate { it.await() }
        }
    }

    private fun observeSavedKeys() {
        if (currentUserId == null) return
        viewModelScope.launch {
            repository.observeFavorites(currentUserId).collect { favorites ->
                val keys = favorites.mapNotNull { room ->
                    room.roomName?.replace(Regex("[.#$\\[\\]]"), "")
                }.toSet()
                _uiState.value = _uiState.value.copy(savedRoomKeys = keys)
            }
        }
    }

    fun applyFilter(criteria: String) {
        val all = _uiState.value.allRooms
        val filtered = when (criteria) {
            "Under ₹8,000" -> all.filter {
                val price = it.price?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
                price in 1..8000
            }
            "Private Room" -> all.filter {
                it.description?.contains("Private Room", ignoreCase = true) == true
            }
            "AC" -> all.filter {
                it.description?.contains("AC", ignoreCase = true) == true
            }
            "Student Friendly" -> all.filter {
                it.description?.contains("Student", ignoreCase = true) == true
            }
            else -> all
        }
        _uiState.value = _uiState.value.copy(displayRooms = filtered)
    }

    fun clearFilter() {
        _uiState.value = _uiState.value.copy(displayRooms = _uiState.value.allRooms)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExploreViewModel() as T
        }
    }
}
