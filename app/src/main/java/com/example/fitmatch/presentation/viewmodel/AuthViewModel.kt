package com.example.fitmatch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val uid: String?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()

    /**
     * Credenciales en conflicto: el email ya tiene otros providers registrados.
     * - email: el email afectado
     * - existingProviders: lista de providers existentes para ese email (p. ej. ["password","google.com"])
     * El ViewModel mantiene internamente la pendingCredential para que luego la puedas linkear.
     */
    data class CredentialConflict(val email: String, val existingProviders: List<String>) : AuthUiState()
}

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Guardamos la credencial pendiente temporalmente para poder linkearla más tarde.
    // NO la expongas en un StateFlow público (por seguridad).
    private var pendingCredential: AuthCredential? = null

    // Sign in tradicional
    fun signIn(email: String, password: String) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            val user = repo.signIn(email, password)
            _uiState.value = AuthUiState.Success(user?.uid)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    // Flow para Google: recibe idToken y email (email puede venir del Google account)
    fun signInWithGoogle(idToken: String, email: String?) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            // Si falla por colisión, capturamos la excepción para resolverla abajo.
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            try {
                repo.signInWithGoogle(idToken)
                _uiState.value = AuthUiState.Success(repo.currentUser()?.uid)
            } catch (e: Exception) {
                handleCredentialException(e, credential, email)
            }
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Error Google sign-in")
        }
    }

    // Flow para Facebook: recibe accessToken y (opcional) email
    fun signInWithFacebook(accessToken: String, email: String?) = viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken)
            try {
                repo.signInWithFacebook(accessToken)
                _uiState.value = AuthUiState.Success(repo.currentUser()?.uid)
            } catch (e: Exception) {
                handleCredentialException(e, credential, email)
            }
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Error Facebook sign-in")
        }
    }

    // Manejo centralizado de excepciones de colisión entre credenciales
    private suspend fun handleCredentialException(e: Exception, credential: AuthCredential, email: String?) {
        // Guarda la pending credential para poder linkear luego
        pendingCredential = credential

        // Intentamos deducir el email. Si el caller no nos lo pasó, intentamos extraerlo de la excepción,
        // pero eso no siempre es posible, por eso permite email nullable.
        val emailToCheck = email ?: extractEmailFromException(e)

        if (emailToCheck.isNullOrBlank()) {
            // No tenemos email: avisamos error genérico (UI debe pedir email al usuario)
            _uiState.value = AuthUiState.Error("Credencial en conflicto pero no se pudo determinar el email. Proporciona tu email.")
            return
        }

        // Obtener providers existentes para ese email
        try {
            val providers = repo.fetchSignInMethodsForEmail(emailToCheck)
            _uiState.value = AuthUiState.CredentialConflict(emailToCheck, providers)
        } catch (fetchEx: Exception) {
            _uiState.value = AuthUiState.Error(fetchEx.message ?: "No se pudieron obtener métodos de inicio de sesión")
        }
    }

    // Si el usuario tiene provider "password": pedir password, luego linkear
    fun linkPendingCredentialWithEmail(email: String, password: String) = viewModelScope.launch {
        val pending = pendingCredential
        if (pending == null) {
            _uiState.value = AuthUiState.Error("No hay credencial pendiente para linkear")
            return@launch
        }
        _uiState.value = AuthUiState.Loading
        try {
            repo.linkPendingCredentialWithEmail(email, password, pending)
            // Limpia pendingCredential
            pendingCredential = null
            _uiState.value = AuthUiState.Success(repo.currentUser()?.uid)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Error linkeando credencial")
        }
    }

    // Si UI decide "iniciar con el otro provider" (ej: provider == "google.com"), el UI debe iniciar ese flow.
    // Cuando el usuario complete el sign-in con el provider existente, deberá linkear la pending credential
    // ejecutando linkPendingCredentialWithEmail(...) después de autenticarse con el provider existente.
    //
    // Nota: la UI debe mostrar qué providers existen (proviene del CredentialConflict).
    //
    fun signOut() {
        repo.signOut()
        _uiState.value = AuthUiState.Idle
    }

    // Attempt to extract email from some Firebase exception types (best effort)
    private fun extractEmailFromException(e: Exception): String? {
        return when (e) {
            is FirebaseAuthUserCollisionException -> e.email
            is FirebaseAuthException -> {
                // algunos errores no exponen email; devolvemos null
                null
            }
            else -> null
        }
    }
}
