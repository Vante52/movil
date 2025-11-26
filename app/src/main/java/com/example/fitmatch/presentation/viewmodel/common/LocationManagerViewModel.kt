package com.example.fitmatch.presentation.viewmodel.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.example.fitmatch.data.auth.FirebaseAuthRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import java.util.Locale

/**
 * ViewModel centralizado para gestionar la ubicación del usuario
 */
class LocationManagerViewModel(
    private val context: Context,
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    /**
     * Obtiene la ubicación actual del usuario y la guarda en Firestore
     */
    @SuppressLint("MissingPermission")
    fun updateCurrentLocation() {
        val userId = authRepository.currentUser()?.uid
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Usuario no autenticado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }

            try {
                // Obtener última ubicación conocida
                val location = fusedLocationClient.lastLocation.await()

                if (location == null) {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = "No se pudo obtener la ubicación"
                        )
                    }
                    return@launch
                }

                val latitude = location.latitude
                val longitude = location.longitude

                // Obtener dirección legible (opcional)
                val address = try {
                    val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    addresses?.firstOrNull()?.getAddressLine(0)
                } catch (e: Exception) {
                    null
                }

                // Guardar en Firestore
                val result = authRepository.updateUserLocation(
                    userId = userId,
                    latitude = latitude,
                    longitude = longitude,
                    address = address
                )

                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            currentLocation = GeoPoint(latitude, longitude),
                            address = address,
                            successMessage = "Ubicación actualizada"
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = "Error al guardar ubicación: ${e.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Obtiene ubicación sin guardarla
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoPoint? {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { GeoPoint(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

data class LocationUiState(
    val isUpdating: Boolean = false,
    val currentLocation: GeoPoint? = null,
    val address: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)