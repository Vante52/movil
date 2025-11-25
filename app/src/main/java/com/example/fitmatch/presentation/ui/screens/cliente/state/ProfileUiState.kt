package com.example.fitmatch.presentation.ui.screens.cliente.state

//estado INMUTABLE del perfil
data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val shippingAddresses: List<ShippingAddress> = emptyList(),
    val preferredSizes: Set<String> = emptySet(),
    val selectedLanguage: String = "Español",
    val isSavingChanges: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

//info del usuario
data class UserProfile(
    val name: String = "Mariana Rodriguez",
    val age: Int = 26,
    val email: String = "mariana.rodriguez@email.com",
    val phone: String = "+57 300 123 4567",
    val location: String = "Medellín, Colombia",
    val role: String = "Comprador",
    val avatarEmoji: String = "👩‍🦰"
)

//métodou pago
data class PaymentMethod(
    val id: String,
    val lastFourDigits: String,
    val type: String = "Tarjeta"
) {
    val maskedNumber: String
        get() = "•••• •••• •••• $lastFourDigits"
}

//dirección envio
data class ShippingAddress(
    val id: String,
    val label: String,
    val address: String,
    val isPrimary: Boolean = false
)