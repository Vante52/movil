package com.example.fitmatch.presentation.ui.screens.common.state

//estado INMUTABLE
data class DeliveryPickupUiState(
    val order: OrderDeliveryInfo? = null,
    val tripSteps: List<TripStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val estimatedTime: String = "0 min",
    val driverInfo: DriverInfo? = null,
    val isLoading: Boolean = false,
    val isMarkingComplete: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val currentStep: TripStep?
        get() = tripSteps.getOrNull(currentStepIndex)

    val isPickupStep: Boolean
        get() = currentStepIndex == 0

    val isDeliveryStep: Boolean
        get() = currentStepIndex == 1

    val canMarkComplete: Boolean
        get() = currentStep != null && !isMarkingComplete
}

// la orden
data class OrderDeliveryInfo(
    val orderId: String,
    val orderNumber: String,
    val customerName: String,
    val customerInitials: String,
    val customerNote: String?,
    val createdDaysAgo: Int
)

//se supone que el repartidor, pero no vamos a usar, esto solo es para la pantalla
data class DriverInfo(
    val name: String,
    val phone: String,
    val rating: Float,
    val vehicleInfo: String
)

//el viaje
data class TripStep(
    val stepNumber: Int,
    val title: String,
    val address: String,
    val timeWindow: String,
    val isActive: Boolean,
    val isCompleted: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)