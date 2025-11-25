package com.example.fitmatch.presentation.viewmodel.vendedor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.realtimedb.FirebaseRealtimeDatabaseRepository
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import com.example.fitmatch.model.product.Product
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VendorInventoryViewModel(
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeInventory()
    }

    private fun observeInventory() {
        val vendorId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            realtimeRepo.observeVendorProducts(vendorId).collect { products ->
                _products.value = products
            }
        }
    }

    fun saveProduct(product: Product) {
        val vendorId = auth.currentUser?.uid
        if (vendorId == null) {
            _errorMessage.value = "Inicia sesión como vendedor para guardar productos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productWithVendor = product.copy(vendorId = vendorId)
                realtimeRepo.saveProduct(productWithVendor)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStock(productId: String, newStock: Int) {
        val vendorId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                realtimeRepo.updateProductStock(vendorId, productId, newStock)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}