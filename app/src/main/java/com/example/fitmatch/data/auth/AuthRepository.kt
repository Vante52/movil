package com.example.fitmatch.data.auth

import com.example.fitmatch.model.user.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    suspend fun signIn(email: String, password: String): FirebaseUser?
    suspend fun register(email: String, password: String): FirebaseUser?

    //OAuth
    suspend fun signInWithGoogle(idToken: String): FirebaseUser?

    suspend fun signInWithFacebook(accessToken: String): FirebaseUser?

    // Devuelve los métodos de sign-in registrados para un email (p. ej. ["password","google.com"])
    suspend fun fetchSignInMethodsForEmail(email: String): List<String>

    // Linkear la credencial pendiente (por ejemplo, credential de Google/Facebook)
    // con una cuenta que se autentica con email+password.
    suspend fun linkPendingCredentialWithEmail(email: String, password: String, pending: AuthCredential): FirebaseUser?

    suspend fun createUserProfile(user: User): Result<Unit>

    suspend fun getUserProfile(userId: String): Result<User?>

    fun signOut()
    fun currentUser(): FirebaseUser?
}