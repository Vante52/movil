package com.example.fitmatch.presentation.ui.screens.common.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.data.auth.AuthRepository
import com.example.fitmatch.data.auth.FirebaseAuthRepository
import com.example.fitmatch.presentation.ui.screens.common.state.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.osmdroid.util.GeoPoint
import com.example.fitmatch.presentation.utils.RouteSimulator
import kotlinx.coroutines.Job

class DeliveryPickupViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryPickupUiState())
    val uiState: StateFlow<DeliveryPickupUiState> = _uiState.asStateFlow()

    private val _events = Channel<DeliveryEvent>()
    val events = _events.receiveAsFlow()
    private var simulationJob: Job? = null
    private var currentRoutePoints: List<GeoPoint> = emptyList()

    init {
        val orderId = savedStateHandle.get<String>("orderId") ?: "MIX-24816"
        loadOrderDetailsWithRealLocations(orderId)
        startRealtimeTracking()
    }

    /**
    Carga orden con ubicaciones reales del vendedor y cliente
     */
    private fun loadOrderDetailsWithRealLocations(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // TODO: Obtener orden real de Firestore/RealtimeDB
                // Por ahora usamos mock con IDs reales
                val vendorId = "VENDOR_USER_ID" // ⬅️ Obtener del pedido
                val clientId = authRepository.currentUser()?.uid ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                // Obtener ubicación del vendedor desde Firestore
                val vendorResult = authRepository.getUserProfile(vendorId)
                val vendorLocation: GeoPoint? = vendorResult.getOrNull()?.let { vendor ->
                    if (vendor.latitude != null && vendor.longitude != null) {
                        GeoPoint(vendor.latitude, vendor.longitude)
                    } else null
                }

                // Obtener ubicación del cliente desde Firestore
                val clientResult = authRepository.getUserProfile(clientId)
                val clientLocation: GeoPoint? = clientResult.getOrNull()?.let { client ->
                    if (client.latitude != null && client.longitude != null) {
                        GeoPoint(client.latitude, client.longitude)
                    } else null
                }

                // Validar que ambas ubicaciones existan
                if (vendorLocation == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El vendedor no tiene ubicación registrada"
                        )
                    }
                    return@launch
                }

                if (clientLocation == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Tu ubicación no está disponible. Ve a Perfil → Actualizar ubicación"
                        )
                    }
                    return@launch
                }

                // Crear orden mock
                val mockOrder = OrderDeliveryInfo(
                    orderId = orderId,
                    orderNumber = "#MIX-24816",
                    customerName = "Tú", // Nombre del cliente actual
                    customerInitials = "TÚ",
                    customerNote = "Entregar en recepción del edificio",
                    createdDaysAgo = 1
                )

                // Crear pasos del viaje con ubicaciones reales
                val mockSteps = listOf(
                    TripStep(
                        stepNumber = 1,
                        title = "Recogida en tienda",
                        address = vendorResult.getOrNull()?.address ?: "Ubicación del vendedor",
                        timeWindow = "2:00 – 3:00 p. m.",
                        isActive = true,
                        latitude = vendorLocation.latitude,
                        longitude = vendorLocation.longitude
                    ),
                    TripStep(
                        stepNumber = 2,
                        title = "Entrega al cliente",
                        address = clientResult.getOrNull()?.address ?: "Tu ubicación",
                        timeWindow = "3:30 – 4:00 p. m.",
                        isActive = false,
                        latitude = clientLocation.latitude,
                        longitude = clientLocation.longitude
                    )
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        order = mockOrder,
                        tripSteps = mockSteps,
                        estimatedTime = "12 min"
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar ubicaciones: ${e.message}"
                    )
                }
            }
        }
    }

    fun onMarkStepComplete() {
        viewModelScope.launch {
            val currentStep = _uiState.value.currentStep ?: return@launch

            _uiState.update { it.copy(isMarkingComplete = true, errorMessage = null) }
            delay(1000)

            // Detener simulación actual
            simulationJob?.cancel()

            _uiState.update { currentState ->
                val updatedSteps = currentState.tripSteps.mapIndexed { index, step ->
                    if (index == currentState.currentStepIndex) {
                        step.copy(isCompleted = true, isActive = false)
                    } else step
                }

                val nextStepIndex = currentState.currentStepIndex + 1
                val hasNextStep = nextStepIndex < currentState.tripSteps.size

                val finalSteps = if (hasNextStep) {
                    updatedSteps.mapIndexed { index, step ->
                        if (index == nextStepIndex) step.copy(isActive = true)
                        else step
                    }
                } else updatedSteps

                currentState.copy(
                    tripSteps = finalSteps,
                    currentStepIndex = if (hasNextStep) nextStepIndex else currentState.currentStepIndex,
                    isMarkingComplete = false,
                    successMessage = if (hasNextStep)
                        "✓ Recogida completada"
                    else
                        "✓ Entrega completada"
                )
            }

            // Emitir evento para mover el marcador
            _events.send(DeliveryEvent.StepCompleted(_uiState.value.currentStepIndex))

            if (_uiState.value.currentStepIndex >= _uiState.value.tripSteps.size) {
                _events.send(DeliveryEvent.OrderCompleted)
            } else {
                // Iniciar nueva simulación hacia el siguiente destino
                startMovementSimulation()
            }

            delay(2000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    fun onCall() {
        viewModelScope.launch {
            val phoneNumber = if (_uiState.value.isPickupStep) {
                "+57 300 111 2222" // Tienda
            } else {
                "+57 300 123 4567" // Cliente
            }
            _events.send(DeliveryEvent.MakeCall(phoneNumber))
        }
    }

    fun onChat() {
        viewModelScope.launch {
            val chatId = _uiState.value.order?.orderId ?: return@launch
            _events.send(DeliveryEvent.OpenChat(chatId))
        }
    }

    fun onNavigate() {
        viewModelScope.launch {
            val step = _uiState.value.currentStep ?: return@launch

            if (step.latitude != null && step.longitude != null) {
                _events.send(
                    DeliveryEvent.NavigateToLocation(
                        lat = step.latitude,
                        lng = step.longitude,
                        address = step.address
                    )
                )
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Ubicación no disponible")
                }
            }
        }
    }

    fun onLocationUpdate(lat: Double, lng: Double) {
        calculateETA(lat, lng)
    }

    fun onDismissMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // ========== LÓGICA PRIVADA ==========

    private fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500)

            val mockOrder = OrderDeliveryInfo(
                orderId = orderId,
                orderNumber = "#MIX-24816",
                customerName = "María González",
                customerInitials = "MG",
                customerNote = "Entregar en recepción del edificio",
                createdDaysAgo = 1
            )

            // COORDENADAS REALES EN BOGOTÁ
            val mockSteps = listOf(
                TripStep(
                    stepNumber = 1,
                    title = "Recogida en Tortini",
                    address = "CC Centro Mayor, local 201",
                    timeWindow = "2:00 – 3:00 p. m.",
                    isActive = true,
                    latitude = 4.6097,  // Centro Mayor, Bogotá
                    longitude = -74.0817
                ),
                TripStep(
                    stepNumber = 2,
                    title = "Entrega al cliente",
                    address = "Cra 15 #93-47, Apto 302, Chapinero",
                    timeWindow = "3:30 – 4:00 p. m.",
                    isActive = false,
                    latitude = 4.6751,  // Chapinero, Bogotá
                    longitude = -74.0570
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    order = mockOrder,
                    tripSteps = mockSteps,
                    estimatedTime = "12 min"
                )
            }
        }
    }

    private fun startRealtimeTracking() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                updateEstimatedTime()
            }
        }
    }

    private fun updateEstimatedTime() {
        val currentEta = _uiState.value.estimatedTime.split(" ")[0].toIntOrNull() ?: 0
        if (currentEta > 0) {
            _uiState.update {
                it.copy(estimatedTime = "${currentEta - 1} min")
            }
        }
    }

    private fun calculateETA(currentLat: Double, currentLng: Double) {
        val destination = _uiState.value.currentStep ?: return
        val destLat = destination.latitude ?: return
        val destLng = destination.longitude ?: return

        // Cálculo simple de distancia (en producción usar API de rutas)
        val R = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(destLat - currentLat)
        val dLon = Math.toRadians(destLng - currentLng)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(destLat)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c // distancia en km

        // Estimar tiempo (velocidad promedio 30 km/h en ciudad)
        val estimatedMinutes = ((distance / 30.0) * 60).toInt().coerceAtLeast(1)

        _uiState.update {
            it.copy(estimatedTime = "$estimatedMinutes min")
        }
    }
    fun startMovementSimulation() {
        if (currentRoutePoints.isEmpty()) return

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            try {
                RouteSimulator.simulateMovement(
                    route = currentRoutePoints,
                    speedKmh = 30.0, // 30 km/h velocidad urbana
                    updateIntervalMs = 2000L // Actualizar cada 2 segundos
                ) { currentPosition, remainingKm ->
                    // Actualizar posición del repartidor
                    _events.trySend(DeliveryEvent.UpdateDriverPosition(currentPosition))

                    // Actualizar ETA basado en distancia restante
                    val etaMinutes = ((remainingKm / 30.0) * 60).toInt().coerceAtLeast(1)
                    _uiState.update { it.copy(estimatedTime = "$etaMinutes min") }
                }
            } catch (e: Exception) {
                // Simulación cancelada o error
            }
        }
    }

    /**
     * Guarda la ruta y inicia la simulación cuando se recibe
     */
    fun onRouteReceived(route: List<GeoPoint>) {
        currentRoutePoints = route
        startMovementSimulation()
    }
}

sealed class DeliveryEvent {
    data class MakeCall(val phoneNumber: String) : DeliveryEvent()
    data class OpenChat(val chatId: String) : DeliveryEvent()

    data class UpdateRoute(val from: GeoPoint, val to: GeoPoint) : DeliveryEvent()
    data class UpdateDriverPosition(val position: GeoPoint) : DeliveryEvent()
    data class StepCompleted(val stepIndex: Int) : DeliveryEvent()
    data class NavigateToLocation(
        val lat: Double,
        val lng: Double,
        val address: String
    ) : DeliveryEvent()
    object OrderCompleted : DeliveryEvent()
}