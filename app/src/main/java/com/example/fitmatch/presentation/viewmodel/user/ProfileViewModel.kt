package com.example.fitmatch.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.example.fitmatch.data.auth.FirebaseAuthRepository
import com.example.fitmatch.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Carga el perfil del usuario actual desde Firebase
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUser = authRepository.currentUser()

                if (currentUser == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No hay usuario autenticado"
                        )
                    }
                    return@launch
                }

                // Obtener el perfil del usuario desde Firestore
                val result = authRepository.getUserProfile(currentUser.uid)

                result.onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar perfil: ${e.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                authRepository.signOut()

                _uiState.update {
                    it.copy(
                        user = null,
                        isLoading = false,
                        isLoggedOut = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cerrar sesión: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}