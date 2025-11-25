package com.example.fitmatch.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.realtimedb.FirebaseRealtimeDatabaseRepository
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import com.example.fitmatch.model.order.CartItem
import com.example.fitmatch.model.product.Product
import com.example.fitmatch.presentation.ui.screens.cliente.state.CartItemState
import com.example.fitmatch.presentation.ui.screens.cliente.state.CartUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel(
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // Estados de la pantalla
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCartItems()
    }

    // -----------------------------
    // Eventos (updates)
    // -----------------------------

    // Sumar item
    fun onIncreaseQuantity(itemId: String) {
        updateRemoteQuantity(itemId) { it + 1 }
    }

    // Restar item
    fun onDecreaseQuantity(itemId: String) {
        updateRemoteQuantity(itemId) { quantity ->
            if (quantity > 1) quantity - 1 else quantity
        }
    }

    // Eliminar item del carrito
    fun onRemoveItem(itemId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            realtimeRepo.removeCartItem(userId, itemId)
        }
    }

    // Campo del cupón
    fun onCouponCodeChanged(code: String) {
        _uiState.update { it.copy(couponCode = code) }
    }

    // Aplicar cupón de descuento
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

            val subtotal = _uiState.value.subtotal

            // Ejemplo simple: cupón FIT10 = 10% de descuento
            val discount = if (currentCoupon.equals("FIT10", ignoreCase = true)) {
                (subtotal * 0.1).toInt()
            } else {
                0
            }

            if (discount == 0) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Cupón inválido o sin descuento"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    appliedDiscount = discount,
                    errorMessage = null
                )
            }
        }
    }

    // Checkout (botón "TRAMITAR PEDIDO")
    fun onCheckout(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update {
                it.copy(errorMessage = "Inicia sesión para completar tu compra")
            }
            return
        }

        if (_uiState.value.items.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Tu carrito está vacío")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessingCheckout = true,
                    errorMessage = null
                )
            }

            try {
                // TODO: aquí va la llamada real para crear la orden en Realtime DB
                delay(1000)

                _uiState.update {
                    it.copy(
                        isProcessingCheckout = false,
                        checkoutSuccess = true
                        // Podrías limpiar el carrito en UI si quieres
                        // items = emptyList()
                    )
                }

                // Avisar a la UI (navegar a pantalla de éxito, etc.)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingCheckout = false,
                        errorMessage = e.message ?: "Ocurrió un error al procesar el pedido"
                    )
                }
            }
        }
    }

    // Limpiar mensaje de error
    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // -----------------------------
    // Observación del carrito
    // -----------------------------

    private fun observeCartItems() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            realtimeRepo.observeCart(userId).collect { items ->
                _uiState.update { state ->
                    state.copy(
                        items = items.map { it.toUiState() },
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private fun updateRemoteQuantity(itemId: String, update: (Int) -> Int) {
        val currentItem = _uiState.value.items.firstOrNull { it.id == itemId } ?: return
        val newQuantity = update(currentItem.quantity)
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            realtimeRepo.updateCartItemQuantity(userId, itemId, newQuantity)
        }
    }

    fun addToCart(
        product: Product,
        vendorName: String,
        size: String,
        color: String,
        quantity: Int = 1
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update {
                it.copy(errorMessage = "Inicia sesión para agregar productos al carrito")
            }
            return
        }

        val cartItem = CartItem(
            userId = userId,
            productId = product.id,
            vendorId = product.vendorId,
            vendorName = vendorName,
            productTitle = product.title,
            productImageUrl = product.imageUrls.firstOrNull().orEmpty(),
            price = product.price,
            quantity = quantity,
            size = size,
            color = color
        )

        viewModelScope.launch {
            realtimeRepo.addOrUpdateCartItem(userId, cartItem)
        }
    }

    private fun CartItem.toUiState() = CartItemState(
        id = id,
        title = productTitle.ifBlank { "Producto" },
        shop = vendorName.ifBlank { "@vendedor" },
        price = price,
        size = size,
        color = color,
        quantity = quantity
    )
}
