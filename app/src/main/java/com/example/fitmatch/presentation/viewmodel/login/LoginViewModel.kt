package com.example.fitmatch.presentation.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.example.fitmatch.data.auth.FirebaseAuthRepository
import com.example.fitmatch.presentation.ui.screens.auth.state.LoginUiState
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login.
 * Maneja la autenticación con Firebase y el estado de la UI.
 */
class LoginViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // ========== ESTADO ==========
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ========== EVENTOS DESDE LA UI ==========

    /**
     * El usuario modifica el campo de email o teléfono
     */
    fun onEmailOrPhoneChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = value,
                errorMessage = null,
                isLoginEnabled = LoginUiState.isValidForm(value, currentState.password)
            )
        }
    }

    /**
     * El usuario modifica el campo de contraseña
     */
    fun onPasswordChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = value,
                errorMessage = null,
                isLoginEnabled = LoginUiState.isValidForm(currentState.email, value)
            )
        }
    }

    /**
     * El usuario cambia la visibilidad de la contraseña
     */
    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    /**
     * Evento: el usuario presionó el botón "Continuar" (Login).
     * Realiza la autenticación con Firebase.
     */
    fun onLoginClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Validación básica antes de llamar a Firebase
            if (currentState.email.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Por favor ingresa tu email o teléfono")
                }
                return@launch
            }

            if (currentState.password.length < 8) {
                _uiState.update {
                    it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // ========== AUTENTICACIÓN CON FIREBASE ==========
                val firebaseUser = authRepository.signIn(
                    email = currentState.email.trim(),
                    password = currentState.password
                )

                if (firebaseUser != null) {
                    // ✅ Login exitoso
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                } else {
                    // ⚠️ Firebase devolvió null (no debería pasar normalmente)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al iniciar sesión. Intenta nuevamente."
                        )
                    }
                }

            } catch (e: FirebaseAuthException) {
                // ========== MANEJO DE ERRORES DE FIREBASE ==========
                val errorMessage = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" ->
                        "El formato del email no es válido"

                    "ERROR_WRONG_PASSWORD" ->
                        "Contraseña incorrecta. Intenta de nuevo."

                    "ERROR_USER_NOT_FOUND" ->
                        "No existe una cuenta con este email. ¿Quieres crear una?"

                    "ERROR_USER_DISABLED" ->
                        "Esta cuenta ha sido deshabilitada. Contacta a soporte."

                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Demasiados intentos fallidos. Intenta más tarde."

                    "ERROR_NETWORK_REQUEST_FAILED" ->
                        "Sin conexión a Internet. Verifica tu red."

                    "ERROR_INVALID_CREDENTIAL" ->
                        "Credenciales inválidas. Verifica tu email y contraseña."

                    else ->
                        "Error al iniciar sesión: ${e.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }

            } catch (e: Exception) {
                // ========== ERRORES GENERALES ==========
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
     * Evento: el usuario presionó "Olvidé mi contraseña"
     */
    fun onForgotPasswordClick() {
        // TODO: Implementar recuperación de contraseña con Firebase
        // authRepository.sendPasswordResetEmail(email)
        _uiState.update { it.copy(errorMessage = null) }
    }
}