package com.example.fitmatch.presentation.ui.screens.auth.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.presentation.ui.screens.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val emailOrPhone: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isLoginEnabled: Boolean get() =
        emailOrPhone.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginEvent {
    data object Success : LoginEvent
    data class Message(val text: String) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private companion object {
        const val K_EMAIL = "login_email"
        const val K_PASS  = "login_pass"
        const val K_SHOW  = "login_show"
    }

    private val _uiState = MutableStateFlow(
        LoginUiState(
            emailOrPhone = savedState[K_EMAIL] ?: "",
            password     = savedState[K_PASS]  ?: "",
            showPassword = savedState[K_SHOW]  ?: false
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Canal para eventos de "una sola vez" (navegación, snackbars)
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onEmailChange(value: String) {
        update { it.copy(emailOrPhone = value, error = null) }
        savedState[K_EMAIL] = value
    }

    fun onPasswordChange(value: String) {
        update { it.copy(password = value, error = null) }
        savedState[K_PASS] = value
    }

    fun onTogglePassword() {
        val newShow = !_uiState.value.showPassword
        update { it.copy(showPassword = newShow) }
        savedState[K_SHOW] = newShow
    }

    fun onSubmit() {
        val s = _uiState.value
        if (!s.isLoginEnabled) return
        viewModelScope.launch {
            update { it.copy(isLoading = true, error = null) }
            val result = runCatching { repo.login(s.emailOrPhone, s.password) }.getOrElse {
                Result.failure(it)
            }
            result.fold(
                onSuccess = {
                    _events.send(LoginEvent.Success)
                    update { it.copy(isLoading = false) }
                },
                onFailure = { e ->
                    update { it.copy(isLoading = false, error = e.message ?: "Error desconocido") }
                    _events.send(LoginEvent.Message("No se pudo iniciar sesión"))
                }
            )
        }
    }

    private inline fun update(x: (LoginUiState) -> LoginUiState) {
        _uiState.update(x)
    }
}
