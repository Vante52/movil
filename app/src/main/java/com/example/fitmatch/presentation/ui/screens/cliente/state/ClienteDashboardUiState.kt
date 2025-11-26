package com.example.fitmatch.presentation.ui.screens.cliente.state

import java.io.Serializable

//estado INMUTABLE del dashboard
data class ClienteDashboardUiState(
    val productDeck: List<ProductCardState> = emptyList(),
    val actionHistory: List<SwipeActionHistory> = emptyList(),
    val currentProduct: ProductCardState? = null,
    val isTiltEnabled: Boolean = false,
    val tiltSensitivity: Float = 3.0f, // Umbral de inclinación (m/s²)
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showTiltInstructions: Boolean = false,
    val isTemperatureFilterEnabled: Boolean = false
) {
    val hasProducts: Boolean
        get() = productDeck.isNotEmpty()

    val canUndo: Boolean
        get() = actionHistory.isNotEmpty()
}

//estado de las cards
data class ProductCardState(
    val id: String,
    val title: String,
    val brand: String,
    val price: Int,
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val category: String = "",
    val size: String = "",
    val color: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val isSaved: Boolean = false,
    val storeUrl: String = ""
): Serializable

//historial de los swipes
data class SwipeActionHistory(
    val product: ProductCardState,
    val action: SwipeAction
)

//tipo de accion de swipe
enum class SwipeAction {
    LIKE,      // Me gusta (derecha)
    PASS,      // No me gusta (izquierda)
    SAVE,      // Guardar
    OPEN_STORE // Abrir tienda
}