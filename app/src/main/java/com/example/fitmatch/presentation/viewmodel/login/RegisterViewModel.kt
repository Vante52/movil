package com.example.fitmatch.presentation.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.example.fitmatch.data.auth.FirebaseAuthRepository
import com.example.fitmatch.model.user.User
import com.example.fitmatch.presentation.ui.screens.auth.state.RegisterUiState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Registro.
 * Maneja toda la lógica de negocio, validación del formulario y registro con Firebase.
 */
class RegisterViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // ========== ESTADO ==========
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // ========== REGEX PARA VALIDACIÓN ==========
    companion object {
        // RFC 5322 compliant email regex (simplificado pero robusto)
        private val EMAIL_REGEX = Regex(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        )
    }

    // ========== EVENTOS DESDE LA UI ==========

    fun onEmailChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = value,
                errorMessage = null,
                isRegisterEnabled = RegisterUiState.isValidForm(
                    email = value,
                    password = currentState.password,
                    fullName = currentState.fullName,
                    birthDate = currentState.birthDate,
                    role = currentState.selectedRole
                )
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = value,
                errorMessage = null,
                isRegisterEnabled = RegisterUiState.isValidForm(
                    email = currentState.email,
                    password = value,
                    fullName = currentState.fullName,
                    birthDate = currentState.birthDate,
                    role = currentState.selectedRole
                )
            )
        }
    }

    fun onFullNameChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                fullName = value,
                errorMessage = null,
                isRegisterEnabled = RegisterUiState.isValidForm(
                    email = currentState.email,
                    password = currentState.password,
                    fullName = value,
                    birthDate = currentState.birthDate,
                    role = currentState.selectedRole
                )
            )
        }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                phone = value,
                errorMessage = null,
                isRegisterEnabled = RegisterUiState.isValidForm(
                    email = currentState.email,
                    password = currentState.password,
                    fullName = currentState.fullName,
                    birthDate = currentState.birthDate,
                    role = currentState.selectedRole
                )
            )
        }
    }

    fun onBirthDateChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                birthDate = value,
                errorMessage = null,
                isRegisterEnabled = RegisterUiState.isValidForm(
                    email = currentState.email,
                    password = currentState.password,
                    fullName = currentState.fullName,
                    birthDate = value,
                    role = currentState.selectedRole
                )
            )
        }
    }

    fun onCityChanged(value: String) {
        _uiState.update { it.copy(city = value) }
    }

    fun onGenderSelected(gender: String) {
        _uiState.update {
            it.copy(
                selectedGender = gender,
                isGenderDropdownExpanded = false
            )
        }
    }

    fun onGenderDropdownToggle() {
        _uiState.update {
            it.copy(isGenderDropdownExpanded = !it.isGenderDropdownExpanded)
        }
    }

    fun onRoleSelected(role: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedRole = role,
                isRegisterEnabled = RegisterUiState.isValidForm(
                    email = currentState.email,
                    password = currentState.password,
                    fullName = currentState.fullName,
                    birthDate = currentState.birthDate,
                    role = role
                )
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    /**
     * Evento: el usuario presionó "Registrarse".
     * Valida el formulario, registra en Firebase Auth y crea el perfil en Firestore.
     *
     * @param onSuccess Callback que recibe el rol para navegar a la pantalla correspondiente
     */
    fun onRegisterClick(onSuccess: (role: String) -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // ========== VALIDACIÓN DE FORMULARIO ==========
            val validationError = validateForm(currentState)
            if (validationError != null) {
                _uiState.update {
                    it.copy(errorMessage = validationError)
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // ========== 1. REGISTRAR EN FIREBASE AUTH ==========
                val firebaseUser = authRepository.register(
                    email = currentState.email.trim(),
                    password = currentState.password
                )

                if (firebaseUser == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al crear la cuenta. Intenta nuevamente."
                        )
                    }
                    return@launch
                }

                // ========== 2. CREAR PERFIL EN FIRESTORE ==========
                val user = User(
                    id = firebaseUser.uid,
                    email = currentState.email.trim(),
                    fullName = currentState.fullName.trim(),
                    birthDate = currentState.birthDate.trim(),
                    city = currentState.city.trim(),
                    gender = currentState.selectedGender,
                    role = currentState.selectedRole,
                    phone = currentState.phone,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    profileCompleted = true
                )

                val profileResult = authRepository.createUserProfile(user)

                profileResult.onSuccess {
                    // REGISTRO EXITOSO
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(currentState.selectedRole)
                }.onFailure { exception ->
                    // ⚠️ Error al crear perfil (pero la cuenta de Auth ya existe)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Cuenta creada pero error al guardar perfil: ${exception.message}"
                        )
                    }
                }

            } catch (e: FirebaseAuthException) {
                // ========== MANEJO DE ERRORES DE FIREBASE AUTH ==========
                val errorMessage = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" ->
                        "Este email ya está registrado. Intenta iniciar sesión."
                    "ERROR_WEAK_PASSWORD" ->
                        "La contraseña es muy débil. Usa al menos 8 caracteres con letras y números."
                    "ERROR_INVALID_EMAIL" ->
                        "El formato del email no es válido."
                    "ERROR_NETWORK_REQUEST_FAILED" ->
                        "Sin conexión a Internet. Verifica tu red."
                    else ->
                        "Error al registrar: ${e.message}"
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

    // ========== VALIDACIÓN DEL FORMULARIO ==========

    /**
     * Valida el formulario completo antes de enviar a Firebase.
     * @return String con mensaje de error o null si es válido
     */
    private fun validateForm(state: RegisterUiState): String?{
        // Email vacío
        if (state.email.isBlank()) {
            return "El email es obligatorio"
        }

        // Email con formato inválido
        if (!EMAIL_REGEX.matches(state.email.trim())) {
            return "Ingresa un email válido (ejemplo: usuario@dominio.com)"
        }

        // Contraseña vacía
        if (state.password.isBlank()) {
            return "La contraseña es obligatoria"
        }

        // Contraseña muy corta
        if (state.password.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres"
        }

        // Contraseña débil (opcional pero recomendado)
        if (!isPasswordStrong(state.password)) {
            return "La contraseña debe contener letras y números"
        }

        // Nombre vacío
        if (state.fullName.isBlank()) {
            return "El nombre completo es obligatorio"
        }

        // Nombre muy corto
        if (state.fullName.trim().length < 3) {
            return "Ingresa tu nombre completo"
        }

        // Fecha de nacimiento vacía
        if (state.birthDate.isBlank()) {
            return "La fecha de nacimiento es obligatoria"
        }

        // Validar formato de fecha (básico)
        if (!isValidDateFormat(state.birthDate)) {
            return "Formato de fecha inválido. Usa: dd/mm/aaaa"
        }

        // Rol no seleccionado
        if (state.selectedRole.isBlank()) {
            return "Selecciona cómo quieres usar la app"
        }

        // Ciudad vacía
        if (state.city.isBlank()) {
            return "La ciudad es obligatoria"
        }

        if (state.phone.isBlank()) {
            return "El numero de telefono es obligatorio"
        }

        if (!isValidPhone(state.phone)) {
            return "El numero de telefono debe tener 10 dígitos"
        }

        if (state.selectedGender.isBlank()) {
            return "Selecciona tu genero"
        }

        return null // Formulario válido
    }

    /**
     * Verifica que la contraseña tenga letras y números.
     */
    private fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    /**
     * Valida formato básico de fecha: dd/mm/aaaa o dd-mm-aaaa
     */
    private fun isValidDateFormat(date: String): Boolean {
        // Acepta dd/mm/aaaa o dd-mm-aaaa
        val dateRegex = Regex("^\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}$")
        return dateRegex.matches(date.trim())
    }

    /**
     * Verifica que un número de teléfono tenga 10 dígitos.
     * @param phone Número de teléfono a verificar.
     * @return true si el número tiene 10 dígitos, false en caso contrario.
     */
    private fun isValidPhone(phone: String): Boolean {
        // Acepta números con 10 dígitos
        val phoneRegex = Regex("^\\d{10}$")
        return phoneRegex.matches(phone.trim())
    }
}