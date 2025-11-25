package com.example.fitmatch.presentation.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.example.fitmatch.data.auth.FirebaseAuthRepository
import com.example.fitmatch.model.user.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WelcomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Data class para el resultado del login
data class LoginResult(
    val isNewUser: Boolean,
    val userId: String
)

class WelcomeViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    /**
     * Login con Google
     * Callback recibe LoginResult
     */
    fun onGoogleSignIn(
        idToken: String,
        onSuccess: (LoginResult) -> Unit // ✅ Un solo parámetro tipo LoginResult
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val firebaseUser = authRepository.signInWithGoogle(idToken)

                if (firebaseUser != null) {
                    val profileResult = authRepository.getUserProfile(firebaseUser.uid)

                    profileResult.onSuccess { existingUser ->
                        if (existingUser == null) {
                            // Usuario nuevo
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                fullName = firebaseUser.displayName ?: "",
                                birthDate = "",
                                city = "",
                                gender = "",
                                role = "Cliente",
                                phone = null,
                                profileCompleted = false,
                                createdAt = Timestamp.now(),
                                updatedAt = Timestamp.now()
                            )

                            authRepository.createUserProfile(newUser).onSuccess {
                                _uiState.update { it.copy(isLoading = false) }
                                onSuccess(LoginResult(isNewUser = true, userId = firebaseUser.uid))
                            }.onFailure { e ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Error: ${e.message}"
                                    )
                                }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false) }

                            val isNewUser = !existingUser.profileCompleted
                            onSuccess(LoginResult(isNewUser = isNewUser, userId = existingUser.id))
                        }
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error: ${e.message}"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al iniciar sesión"
                        )
                    }
                }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    /**
     * Login con Facebook
     */
    fun onFacebookSignIn(
        accessToken: String,
        onSuccess: (LoginResult) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val firebaseUser = authRepository.signInWithFacebook(accessToken)

                if (firebaseUser != null) {
                    val profileResult = authRepository.getUserProfile(firebaseUser.uid)

                    profileResult.onSuccess { existingUser ->
                        if (existingUser == null) {
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                fullName = firebaseUser.displayName ?: "",
                                birthDate = "",
                                city = "",
                                gender = "",
                                role = "Cliente",
                                phone = null,
                                profileCompleted = false,
                                createdAt = Timestamp.now(),
                                updatedAt = Timestamp.now()
                            )

                            authRepository.createUserProfile(newUser).onSuccess {
                                _uiState.update { it.copy(isLoading = false) }
                                onSuccess(LoginResult(isNewUser = true, userId = firebaseUser.uid))
                            }.onFailure { e ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Error: ${e.message}"
                                    )
                                }
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false) }

                            val isNewUser = !existingUser.profileCompleted
                            onSuccess(LoginResult(isNewUser = isNewUser, userId = existingUser.id))
                        }
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error: ${e.message}"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al iniciar sesión"
                        )
                    }
                }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    private fun handleAuthError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("account-exists-with-different-credential") == true ->
                "Esta cuenta ya existe con otro método de inicio de sesión"
            e.message?.contains("network") == true ->
                "Error de conexión. Verifica tu internet"
            else -> "Error: ${e.message}"
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = errorMessage
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}