package com.pgshare.studentroomsharingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pgshare.studentroomsharingapp.Adapter.InboxUserInfo
import com.pgshare.studentroomsharingapp.model.RecentChat
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InboxUiState(
    val inboxList: List<RecentChat> = emptyList(),
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true
)

class InboxViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    private val _userProfiles = MutableStateFlow<Map<String, InboxUserInfo>>(emptyMap())
    val userProfiles: StateFlow<Map<String, InboxUserInfo>> = _userProfiles.asStateFlow()

    init {
        observeInbox()
    }

    private fun observeInbox() {
        val userId = repository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = InboxUiState(isLoading = false, isLoggedIn = false)
            return
        }
        viewModelScope.launch {
            repository.observeInbox(userId).collect { items ->
                _uiState.value = InboxUiState(
                    inboxList = items,
                    isLoading = false,
                    isLoggedIn = true
                )
                fetchUserProfiles(items)
            }
        }
    }

    private suspend fun fetchUserProfiles(items: List<RecentChat>) {
        val targetIds = items.map { it.targetUserId }.distinct().filter { it.isNotBlank() }
        if (targetIds.isEmpty()) return

        val profiles = coroutineScope {
            targetIds.map { uid ->
                async {
                    val profile = repository.getUserProfile(uid)
                    val displayName = profile?.username?.takeIf { it.isNotBlank() }
                        ?: profile?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                        ?: if (uid.contains("owner", ignoreCase = true)) "Property Owner" else "Room Owner"
                    uid to InboxUserInfo(displayName, profile?.profileImageUrl)
                }
            }.associate { it.await() }
        }
        _userProfiles.value = profiles
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InboxViewModel() as T
        }
    }
}
