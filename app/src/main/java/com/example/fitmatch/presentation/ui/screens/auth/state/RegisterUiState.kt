package com.example.fitmatch.presentation.ui.screens.auth.state

//Estado inmutable de la pantalla de Registro.
 // Contiene todos los campos del formulario y estados de UI.

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val city: String = "Bogotá, Colombia",
    val selectedGender: String = "",
    val selectedRole: String = "Comprador", // Default
    val showPassword: Boolean = false,
    val isGenderDropdownExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterEnabled: Boolean = false
) {
    companion object {
        val GENDER_OPTIONS = listOf("Masculino", "Femenino", "Otro", "Prefiero no decir")
        val ROLE_OPTIONS = listOf("Comprador", "Vendedor")

        /**
         * Validación del formulario.
         * Campos obligatorios: email, password, fullName, birthDate, role.
         */
        fun isValidForm(
            email: String,
            password: String,
            fullName: String,
            birthDate: String,
            role: String
        ): Boolean {
            return email.isNotBlank() &&
                    password.length >= 8 &&
                    fullName.isNotBlank() &&
                    birthDate.isNotBlank() &&
                    role.isNotBlank()
        }
    }
}