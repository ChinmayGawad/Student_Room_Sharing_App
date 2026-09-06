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

    // Wizard Form State (persists across rotations and page changes)
    var title: String = ""
    var location: String = ""
    var roomType: String = "Private Room"
    var description: String = ""

    val selectedImageUris = mutableListOf<Uri>()

    var rent: String = ""
    var deposit: String = ""
    val selectedAmenities = mutableListOf<String>()

    fun setStep1Data(title: String, location: String, roomType: String, description: String) {
        this.title = title
        this.location = location
        this.roomType = roomType
        this.description = description
    }

    fun setImageUris(uris: List<Uri>) {
        selectedImageUris.clear()
        selectedImageUris.addAll(uris)
    }

    fun setStep3Data(rent: String, deposit: String, amenities: List<String>) {
        this.rent = rent
        this.deposit = deposit
        selectedAmenities.clear()
        selectedAmenities.addAll(amenities)
    }

    fun publishCurrentRoom(contentResolver: android.content.ContentResolver) {
        publishRoom(
            title = title,
            location = location,
            roomType = roomType,
            rent = rent,
            deposit = deposit,
            imageUris = selectedImageUris,
            contentResolver = contentResolver,
            amenities = selectedAmenities,
            description = description
        )
    }

    fun publishRoom(
        title: String,
        location: String,
        roomType: String,
        rent: String,
        deposit: String,
        imageUris: List<Uri>,
        contentResolver: android.content.ContentResolver,
        amenities: List<String> = emptyList(),
        description: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = AddRoomUiState(isLoading = true)

            val uploadedImages = imageUris.mapNotNull { uri ->
                repository.uploadImageToFreeStorage(uri, contentResolver)
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
                description = if (description.isNotEmpty()) "Type: $roomType\n\n$description" else "Type: $roomType",
                price = rent,
                deposit = deposit,
                imageUrls = ArrayList(uploadedImages),
                imageResourceId = 0,
                amenities = ArrayList(amenities),
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
