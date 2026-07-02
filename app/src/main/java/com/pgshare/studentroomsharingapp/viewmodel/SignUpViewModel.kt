package com.pgshare.studentroomsharingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SignUpEvent {
    data object Success : SignUpEvent()
    data class Error(val message: String) : SignUpEvent()
}

class SignUpViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SignUpEvent>()
    val events: SharedFlow<SignUpEvent> = _events.asSharedFlow()

    fun signUp(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.value = SignUpUiState(isLoading = true)
            repository.signUp(name, email, password, role)
                .onSuccess {
                    _uiState.value = SignUpUiState()
                    _events.emit(SignUpEvent.Success)
                }
                .onFailure { e ->
                    _uiState.value = SignUpUiState(error = e.message ?: "Registration failed")
                    _events.emit(SignUpEvent.Error(e.message ?: "Registration failed"))
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel() as T
        }
    }
}
