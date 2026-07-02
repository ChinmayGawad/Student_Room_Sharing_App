package com.pgshare.studentroomsharingapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddRoomUiState(
    val isLoading: Boolean = false
)

sealed class AddRoomEvent {
    data object Success : AddRoomEvent()
    data class Error(val message: String) : AddRoomEvent()
}

class AddRoomViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRoomUiState())
    val uiState: StateFlow<AddRoomUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddRoomEvent>()
    val events: SharedFlow<AddRoomEvent> = _events.asSharedFlow()

    fun publishRoom(
        title: String,
        location: String,
        roomType: String,
        rent: String,
        deposit: String,
        imageUris: List<Uri>,
        contentResolver: android.content.ContentResolver
    ) {
        viewModelScope.launch {
            _uiState.value = AddRoomUiState(isLoading = true)

            val base64Images = withContext(Dispatchers.IO) {
                imageUris.mapNotNull { uri ->
                    repository.compressAndEncodeImage(uri, contentResolver)
                }
            }

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                _uiState.value = AddRoomUiState()
                _events.emit(AddRoomEvent.Error("You must be logged in to publish a room"))
                return@launch
            }

            // Role verification
            val role = repository.getUserRole(currentUserId)
            if (role != "owner") {
                _uiState.value = AddRoomUiState()
                _events.emit(AddRoomEvent.Error("Only property owners can list rooms"))
                return@launch
            }

            val newRoom = Room(
                id = "",
                userId = currentUserId,
                roomName = title,
                location = location,
                description = "Type: $roomType",
                price = rent,
                deposit = deposit,
                imageUrls = ArrayList(base64Images),
                imageResourceId = 0,
                isRoomBooked = false
            )

            repository.publishRoom(newRoom)
                .onSuccess {
                    _uiState.value = AddRoomUiState()
                    _events.emit(AddRoomEvent.Success)
                }
                .onFailure { e ->
                    _uiState.value = AddRoomUiState()
                    _events.emit(AddRoomEvent.Error(e.message ?: "Failed to publish room"))
                }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddRoomViewModel() as T
        }
    }
}
