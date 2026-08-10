package com.pgshare.studentroomsharingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pgshare.studentroomsharingapp.model.Message
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val receiverName: String = "Unknown",
    val receiverProfileImageUrl: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ChatViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var receiverId: String = ""
    private var senderId: String = ""
    private var chatRoomId: String = ""

    fun initialize(senderId: String, receiverId: String, roomId: String) {
        this.receiverId = receiverId
        this.senderId = senderId
        val pair = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"
        chatRoomId = "${roomId}_$pair"

        loadReceiverName()
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            repository.observeMessages(chatRoomId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages, isLoading = false)
            }
        }
    }

    private fun loadReceiverName() {
        viewModelScope.launch {
            val profile = repository.getUserProfile(receiverId)
            val name = profile?.username ?: profile?.email?.substringBefore("@") ?: "Unknown"
            _uiState.value = _uiState.value.copy(
                receiverName = name,
                receiverProfileImageUrl = profile?.profileImageUrl
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                repository.sendMessage(chatRoomId, senderId, receiverId, chatRoomId.substringBefore("_"), text)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel() as T
        }
    }
}
