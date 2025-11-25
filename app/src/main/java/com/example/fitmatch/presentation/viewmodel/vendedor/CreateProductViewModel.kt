package com.example.fitmatch.presentation.viewmodel.vendedor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.cloudinary.CloudinaryRepository
import com.example.fitmatch.data.realtimedb.FirebaseRealtimeDatabaseRepository
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import com.example.fitmatch.model.product.Product
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateProductViewModel(
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProductUiState())
    val uiState: StateFlow<CreateProductUiState> = _uiState.asStateFlow()

    fun publishProduct(
        title: String,
        description: String,
        price: Int,
        sizes: List<String>,
        colors: List<String>,
        tags: List<String>,
        mediaUris: List<Uri>
    ) {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Inicia sesión como vendedor para publicar") }
            return
        }

        if (mediaUris.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Agrega al menos una imagen") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, errorMessage = null, isSuccess = false) }
            try {
                val uploadedUrls = mediaUris.map { uri ->
                    cloudinaryRepository.uploadImage(uri)
                }

                val now = System.currentTimeMillis()
                val product = Product(
                    vendorId = userId,
                    title = title,
                    description = description,
                    price = price,
                    sizes = sizes,
                    color = colors.firstOrNull() ?: "",
                    tags = tags,
                    imageUrls = uploadedUrls,
                    stock = 1,
                    createdAt = now,
                    updatedAt = now
                )

                realtimeRepo.saveProduct(product)
                _uiState.update { CreateProductUiState(isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        errorMessage = e.message ?: "Ocurrió un error al publicar"
                    )
                }
            }
        }
    }

    fun onMessageConsumed() {
        _uiState.update { it.copy(errorMessage = null, isSuccess = false, isPublishing = false) }
    }
}


data class CreateProductUiState(
    val isPublishing: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
