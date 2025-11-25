package com.example.fitmatch.presentation.ui.screens.cliente.state

//estado INMUTABLE del carrito de compras
data class CartUiState(
    val items: List<CartItemState> = emptyList(),
    val couponCode: String = "",
    val appliedDiscount: Int = 0,
    val shippingCost: Int = 9_900,
    val isLoading: Boolean = false,
    val isProcessingCheckout: Boolean = false,
    val errorMessage: String? = null,
    val checkoutSuccess: Boolean = false
) {
    //calculos de la cuenta
    val subtotal: Int
        get() = items.sumOf { it.price * it.quantity }

    val total: Int
        get() = subtotal + shippingCost - appliedDiscount

    val isEmpty: Boolean
        get() = items.isEmpty()

    val itemCount: Int
        get() = items.sumOf { it.quantity }
}

//estado de un item individual en el carrito
data class CartItemState(
    val id: String,
    val title: String,
    val shop: String,
    val price: Int,
    val size: String,
    val color: String,
    val quantity: Int = 1
)