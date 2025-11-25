package com.example.fitmatch.presentation.viewmodel.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitmatch.presentation.sensors.TemperatureSensorManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TemperatureViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = TemperatureSensorManager(application)

    val temperature: StateFlow<Float?> = sensorManager.temperature
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)



    val clothingSuggestion: StateFlow<String> = temperature.map { temp ->
        when {
            temp == null -> "Sin datos"
            temp < 15 -> "Ropa de invierno 🧥"
            temp in 15.0..25.0 -> "Ropa ligera 👕"
            else -> "Ropa de verano 🩳"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Sin datos")

    fun start() = sensorManager.startListening()
    fun stop() = sensorManager.stopListening()
}