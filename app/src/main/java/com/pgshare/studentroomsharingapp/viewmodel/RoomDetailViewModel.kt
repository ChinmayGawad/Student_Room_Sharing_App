package com.pgshare.studentroomsharingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoomDetailUiState(
    val ownerName: String = "Unknown User",
    val isLoading: Boolean = true,
    val error: String? = null
)

class RoomDetailViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomDetailUiState())
    val uiState: StateFlow<RoomDetailUiState> = _uiState.asStateFlow()

    fun loadOwnerName(userId: String?) {
        userId?.let { uid ->
            viewModelScope.launch {
                val name = repository.getUserName(uid)
                _uiState.value = _uiState.value.copy(
                    ownerName = name ?: "Unknown User",
                    isLoading = false
                )
            }
        } ?: run {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomDetailViewModel() as T
        }
    }
}
