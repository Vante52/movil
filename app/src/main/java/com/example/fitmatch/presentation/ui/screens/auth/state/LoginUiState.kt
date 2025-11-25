package com.example.fitmatch.presentation.ui.screens.auth.state
//propiedades necesarias para renderizar el login
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginEnabled: Boolean = false
) {
    companion object {
        //Validación centralizada: el botón se habilita si hay datos válidos.
        fun isValidForm(email: String, password: String): Boolean {
            return email.isNotBlank() && password.length >= 8
        }
    }
}