package com.example.fitmatch.presentation.viewmodel.user

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.presentation.ui.screens.cliente.state.ClienteDashboardUiState
import com.example.fitmatch.presentation.ui.screens.cliente.state.ProductCardState
import com.example.fitmatch.presentation.ui.screens.cliente.state.SwipeAction
import com.example.fitmatch.data.realtimedb.FirebaseRealtimeDatabaseRepository
import com.example.fitmatch.data.realtimedb.RealtimeDatabaseRepository
import com.example.fitmatch.model.product.Product

import com.example.fitmatch.presentation.ui.screens.cliente.state.SwipeActionHistory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow


class ClienteDashboardViewModel(
    private val context: Context,
    private val realtimeRepo: RealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository()
) : ViewModel(), SensorEventListener {

    private val _uiState = MutableStateFlow(ClienteDashboardUiState())
    val uiState: StateFlow<ClienteDashboardUiState> = _uiState.asStateFlow()

    // eventos
    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    // Servicio del sensor
    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Control de debounce para el sensor
    private var lastTiltActionTime = 0L
    private val tiltDebounceMs = 1000L // 1 segundo entre acciones
    private var loadJob: Job? = null

    init {
        loadProducts()
    }

    //me fusta
    fun onSwipeRight() {
        val product = _uiState.value.currentProduct ?: return
        processSwipeAction(product, SwipeAction.LIKE)
    }

    //pass
    fun onSwipeLeft() {
        val product = _uiState.value.currentProduct ?: return
        processSwipeAction(product, SwipeAction.PASS)
    }

    //guardar en favorites
    fun onToggleSave(productId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                productDeck = currentState.productDeck.map { product ->
                    if (product.id == productId) {
                        product.copy(isSaved = !product.isSaved)
                    } else product
                }
            )
        }
    }

    //deshacer
    fun onUndo() {
        _uiState.update { currentState ->
            val lastAction = currentState.actionHistory.lastOrNull() ?: return@update currentState

            currentState.copy(
                productDeck = currentState.productDeck + lastAction.product,
                actionHistory = currentState.actionHistory.dropLast(1),
                currentProduct = lastAction.product
            )
        }
    }

    //abrir tienda de producto
    fun onOpenStore(product: ProductCardState) {
        viewModelScope.launch {
            _events.send(DashboardEvent.NavigateToStore(product.storeUrl))
        }
    }

    //detalle producto
    fun onViewDetails(product: ProductCardState) {
        viewModelScope.launch {
            _events.send(DashboardEvent.ShowProductDetails(product))
        }
    }

    //activar y desactivar el sensor
    fun onToggleTiltSensor(enable: Boolean) {
        if (enable) {
            startTiltSensor()
        } else {
            stopTiltSensor()
        }

        _uiState.update {
            it.copy(
                isTiltEnabled = enable,
                showTiltInstructions = enable // Mostrar instrucciones al activar
            )
        }

        // Ocultar instrucciones después de 3 segundos
        if (enable) {
            viewModelScope.launch {
                delay(3000)
                _uiState.update { it.copy(showTiltInstructions = false) }
            }
        }
    }

    //sensibilidad sensor
    fun onAdjustTiltSensitivity(sensitivity: Float) {
        _uiState.update { it.copy(tiltSensitivity = sensitivity) }
    }

    //producto visto
    fun onProductSeen(product: ProductCardState) {
        // TODO: Enviar evento de analytics
        viewModelScope.launch {
            // Simulación de tracking
            delay(100)
        }
    }

    //recargar productod
    fun onReloadProducts() {
        loadProducts()
    }

    //mensaje error
    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // el sensorcito con acelerometro

    private fun startTiltSensor() {
        if (accelerometer == null) {
            _uiState.update {
                it.copy(errorMessage = "Tu dispositivo no tiene acelerómetro")
            }
            return
        }

        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI // 60Hz aprox
        )
    }

    private fun stopTiltSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (!_uiState.value.isTiltEnabled) return

        val x = event.values[0] // Inclinación lateral (-10 a 10)
        // Negativo = izquierda, Positivo = derecha

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTiltActionTime < tiltDebounceMs) return

        val threshold = _uiState.value.tiltSensitivity

        when {
            x < -threshold -> {
                // Inclinado a la izquierda → PASS
                lastTiltActionTime = currentTime
                onSwipeLeft()
                viewModelScope.launch {
                    _events.send(DashboardEvent.ShowTiltFeedback("PASS", isLeft = true))
                }
            }

            x > threshold -> {
                // Inclinado a la derecha → LIKE
                lastTiltActionTime = currentTime
                onSwipeRight()
                viewModelScope.launch {
                    _events.send(DashboardEvent.ShowTiltFeedback("LIKE", isLeft = false))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesitamos manejar cambios de precisión
    }

    //swipes lógica

    private fun processSwipeAction(product: ProductCardState, action: SwipeAction) {
        _uiState.update { currentState ->
            val updatedDeck = currentState.productDeck.filter { it.id != product.id }
            val newHistory = currentState.actionHistory + SwipeActionHistory(product, action)

            currentState.copy(
                productDeck = updatedDeck,
                actionHistory = newHistory,
                currentProduct = updatedDeck.lastOrNull()
            )
        }

        // Enviar evento para feedback visual
        viewModelScope.launch {
            when (action) {
                SwipeAction.LIKE -> _events.send(DashboardEvent.ProductLiked(product))
                SwipeAction.PASS -> _events.send(DashboardEvent.ProductPassed(product))
                else -> {}
            }
        }
    }

    private fun loadProducts() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                realtimeRepo.observeAllProducts().collect { products ->
                    val productCards = products
                        .filter { it.isActive && it.stock > 0 }
                        .map { it.toCardState() }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            productDeck = productCards,
                            currentProduct = productCards.lastOrNull()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar productos"
                    )
                }
            }
        }
    }

    private fun Product.toCardState(): ProductCardState = ProductCardState(
        id = id,
        title = title,
        brand = brand,
        price = price,
        imageUrl = imageUrls.firstOrNull().orEmpty(),
        category = category,
        size = sizes.firstOrNull().orEmpty(),
        color = color,
        tags = tags,
        storeUrl = ""
    )


    /* Implementar despues
    //activar sensor temperatura
    fun onToggleTemperatureFilter(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isTemperatureFilterEnabled = enabled)
    }
*/
    // --- SENSOR DE TEMPERATURA ---

    private var temperatureSensor: Sensor? = null
    private val _temperature = MutableStateFlow<Float?>(null)
    val temperature: StateFlow<Float?> = _temperature

    private val temperatureListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            _temperature.value = event.values[0]
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startTemperatureSensor() {
        temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (temperatureSensor != null) {
            sensorManager?.registerListener(temperatureListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            // si el dispositivo no tiene sensor, usar una temperatura simulada
            _temperature.value = 23.0f
        }
    }

    fun stopTemperatureSensor() {
        sensorManager?.unregisterListener(temperatureListener)
    }


    // sensor

    override fun onCleared() {
        super.onCleared()
        stopTiltSensor()
        stopTemperatureSensor()
    }

}

//eventos de swipe y nav
sealed class DashboardEvent {
    data class NavigateToStore(val storeUrl: String) : DashboardEvent()
    data class ShowProductDetails(val product: ProductCardState) : DashboardEvent()
    data class ProductLiked(val product: ProductCardState) : DashboardEvent()
    data class ProductPassed(val product: ProductCardState) : DashboardEvent()
    data class ShowTiltFeedback(val action: String, val isLeft: Boolean) : DashboardEvent()
}