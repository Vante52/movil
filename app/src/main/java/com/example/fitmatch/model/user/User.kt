package com.example.fitmatch.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de Usuario para Firebase Firestore.
 */
data class User(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("email")
    val email: String = "",

    @PropertyName("fullName")
    val fullName: String = "",

    @PropertyName("birthDate")
    val birthDate: String = "",

    @PropertyName("city")
    val city: String = "",

    @PropertyName("gender")
    val gender: String = "",

    @PropertyName("role")
    val role: String = "", // "Cliente" | "Vendedor"

    @PropertyName("phone")
    val phone: String? = null,

    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @PropertyName("profileCompleted")
    val profileCompleted: Boolean = false
) {
    // Constructor sin argumentos requerido por Firestore
    // (ya proporcionado por los valores por defecto)

    /**
     * Verifica si el perfil del usuario está completo.
     */
    fun isProfileComplete(): Boolean {
        return fullName.isNotBlank() &&
                birthDate.isNotBlank() &&
                city.isNotBlank() &&
                role.isNotBlank() &&
                profileCompleted
    }

    /**
     * Crea una copia del usuario marcando el perfil como completado.
     */
    fun markAsCompleted(): User {
        return this.copy(
            profileCompleted = true,
            updatedAt = Timestamp.now()
        )
    }
}