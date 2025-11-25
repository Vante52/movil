package com.example.fitmatch.presentation.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TemperatureSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val ambientTempSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    private val _temperature = MutableStateFlow<Float?>(null)
    val temperature = _temperature.asStateFlow()

    private val prefs = context.getSharedPreferences("temperature_prefs", Context.MODE_PRIVATE)

    init {
        // Leer el último valor guardado al iniciar
        val lastTemp = prefs.getFloat("last_temperature", Float.NaN)
        if (!lastTemp.isNaN()) {
            _temperature.value = lastTemp
        }
    }

    fun startListening() {
        ambientTempSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.firstOrNull()?.let {
            _temperature.value = it
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}