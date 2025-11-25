package com.example.fitmatch.presentation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import org.osmdroid.util.GeoPoint

/**
 * Helper para gestionar actualizaciones de ubicación en tiempo real
 */
class LocationHandler(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    companion object {
        private const val TAG = "LocationHandler"
        private const val UPDATE_INTERVAL = 5000L // 5 segundos
        private const val FASTEST_INTERVAL = 2000L // 2 segundos
    }

    /**
     * Inicia actualizaciones continuas de ubicación
     * @param onLocationUpdate Callback que se ejecuta cada vez que cambia la ubicación
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocationUpdate: (GeoPoint) -> Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    Log.d(TAG, "Nueva ubicación: ${location.latitude}, ${location.longitude}")
                    onLocationUpdate(geoPoint)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Actualizaciones de ubicación iniciadas")
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando actualizaciones: ${e.message}")
        }
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
                Log.d(TAG, "Actualizaciones de ubicación detenidas")
            } catch (e: Exception) {
                Log.e(TAG, "Error deteniendo actualizaciones: ${e.message}")
            }
        }
    }

    /**
     * Obtiene la última ubicación conocida (no espera por actualización)
     * @param onLocation Callback que recibe la ubicación actual
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocation: (GeoPoint) -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    Log.d(TAG, "Ubicación actual obtenida: ${it.latitude}, ${it.longitude}")
                    onLocation(geoPoint)
                } ?: Log.w(TAG, "No hay última ubicación disponible")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error obteniendo ubicación actual: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
        }
    }
}