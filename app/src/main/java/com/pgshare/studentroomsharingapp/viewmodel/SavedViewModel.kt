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

data class SavedUiState(
    val savedRooms: List<Room> = emptyList(),
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val ownerNames: Map<String, String> = emptyMap()
)

class SavedViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        val userId = repository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = SavedUiState(isLoading = false, isLoggedIn = false)
            return
        }
        viewModelScope.launch {
            repository.observeFavorites(userId).collect { rooms ->
                val ownerIds = rooms.mapNotNull { it.userId }.distinct()
                val names = if (ownerIds.isNotEmpty()) fetchOwnerNames(ownerIds) else emptyMap()
                _uiState.value = SavedUiState(
                    savedRooms = rooms,
                    isLoading = false,
                    isLoggedIn = true,
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

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SavedViewModel() as T
        }
    }
}
