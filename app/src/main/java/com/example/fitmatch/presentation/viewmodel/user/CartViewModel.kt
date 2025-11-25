package com.example.fitmatch.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.presentation.ui.screens.cliente.state.CartItemState
import com.example.fitmatch.presentation.ui.screens.cliente.state.CartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CartViewModel : ViewModel() {

    //estados de la pantalla
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // Cargar items del carrito (mock inicial o desde repositorio)
        loadCartItems()
    }

    //Eventos (updates)

    //sumar item
    fun onIncreaseQuantity(itemId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.id == itemId) item.copy(quantity = item.quantity + 1)
                    else item
                }
            )
        }
    }

    //quitar item
    fun onDecreaseQuantity(itemId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.map { item ->
                    if (item.id == itemId && item.quantity > 1) {
                        item.copy(quantity = item.quantity - 1)
                    } else item
                }
            )
        }
    }

    //eliminar cosas del carro
    fun onRemoveItem(itemId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items.filter { it.id != itemId },
                errorMessage = null
            )
        }
    }

    //el campo del cupon
    fun onCouponCodeChanged(code: String) {
        _uiState.update { it.copy(couponCode = code) }
    }

    //cupones de descuento jaja
    fun onApplyCoupon() {
        viewModelScope.launch {
            val currentCoupon = _uiState.value.couponCode.trim()

            if (currentCoupon.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Ingresa un código de cupón válido")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // TODO: Llamada real al repositorio para validar cupón
            delay(800)

            // Simulación: cupones válidos
            val discount = when (currentCoupon.uppercase()) {
                "WELCOME20" -> 20_000
                "SAVE10" -> 10_000
                "FLASH50" -> 50_000
                else -> 0
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    appliedDiscount = discount,
                    errorMessage = if (discount == 0)
                        "Cupón no válido o expirado"
                    else null
                )
            }
        }
    }

    //procesar checkout ir a pantalla de pago
    fun onCheckout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.isEmpty) {
                _uiState.update {
                    it.copy(errorMessage = "El carrito está vacío")
                }
                return@launch
            }

            _uiState.update {
                it.copy(isProcessingCheckout = true, errorMessage = null)
            }

            // TODO: Llamada al repositorio para crear orden
            delay(1000)

            _uiState.update {
                it.copy(
                    isProcessingCheckout = false,
                    checkoutSuccess = true
                )
            }

            onSuccess()
        }
    }

    //limpiar mensaje error
    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

//TODO: cargar los items desde el repository al carrito
    private fun loadCartItems() {
        _uiState.update {
            it.copy(
                items = listOf(
                    CartItemState(
                        id = "1",
                        title = "Blazer Premium en Lino",
                        shop = "@ateliernova",
                        price = 189_900,
                        size = "M",
                        color = "Negro",
                        quantity = 1
                    ),
                    CartItemState(
                        id = "2",
                        title = "Pantalón Wide Leg",
                        shop = "@lunaurban",
                        price = 129_900,
                        size = "S",
                        color = "Beige",
                        quantity = 2
                    )
                )
            )
        }
    }
}