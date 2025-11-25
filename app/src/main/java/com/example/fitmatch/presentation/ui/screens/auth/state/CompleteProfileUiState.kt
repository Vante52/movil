package com.example.fitmatch.presentation.ui.screens.auth.state

data class CompleteProfileUiState(
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val city: String = "Bogotá, Colombia",
    val phone: String = "",
    val selectedGender: String = "",
    val isGenderDropdownExpanded: Boolean = false,
    val selectedRole: String = "Cliente",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false
){
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