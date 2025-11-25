package com.example.fitmatch.presentation.utils

import android.util.Log
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import kotlin.math.abs

/**
 * Simulador de movimiento del repartidor a lo largo de una ruta
 * Interpola entre puntos de la ruta para crear movimiento suave
 */
object RouteSimulator {

    private const val TAG = "RouteSimulator"

    /**
     * Simula el movimiento del repartidor a lo largo de una ruta
     * @param route Lista de GeoPoints que conforman la ruta completa
     * @param speedKmh Velocidad del repartidor en km/h (default: 30 km/h)
     * @param onPositionUpdate Callback que se ejecuta en cada actualización de posición
     */
    suspend fun simulateMovement(
        route: List<GeoPoint>,
        speedKmh: Double = 30.0,
        updateIntervalMs: Long = 1000L, // Actualizar cada 1 segundo
        onPositionUpdate: (GeoPoint, distanceToDestination: Double) -> Unit
    ) {
        if (route.size < 2) {
            Log.w(TAG, "Ruta muy corta para simular")
            return
        }

        Log.d(TAG, "Iniciando simulación con ${route.size} puntos")

        // Calcular distancia total de la ruta
        val totalDistance = calculateTotalDistance(route)
        Log.d(TAG, "Distancia total de la ruta: ${"%.2f".format(totalDistance)} km")

        // Convertir velocidad a metros por segundo
        val speedMps = (speedKmh * 1000.0) / 3600.0

        // Distancia que recorre en cada intervalo
        val distancePerInterval = speedMps * (updateIntervalMs / 1000.0)

        var currentSegment = 0
        var progressInSegment = 0.0

        while (currentSegment < route.size - 1) {
            val start = route[currentSegment]
            val end = route[currentSegment + 1]

            // Calcular distancia del segmento actual
            val segmentDistance = calculateDistance(start, end)

            // Calcular cuántos pasos necesitamos para este segmento
            val steps = (segmentDistance / distancePerInterval).toInt().coerceAtLeast(1)

            for (step in 0..steps) {
                progressInSegment = step.toDouble() / steps.toDouble()

                // Interpolar posición entre start y end
                val currentPosition = interpolate(start, end, progressInSegment)

                // Calcular distancia restante al destino
                val remainingDistance = calculateRemainingDistance(
                    route,
                    currentSegment,
                    progressInSegment
                )

                // Enviar actualización
                onPositionUpdate(currentPosition, remainingDistance)

                // Esperar antes de la siguiente actualización
                delay(updateIntervalMs)
            }

            // Pasar al siguiente segmento
            currentSegment++
        }

        // Posición final
        onPositionUpdate(route.last(), 0.0)
        Log.d(TAG, "Simulación completada")
    }

    /**
     * Interpola entre dos puntos geográficos
     */
    private fun interpolate(start: GeoPoint, end: GeoPoint, fraction: Double): GeoPoint {
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lon = start.longitude + (end.longitude - start.longitude) * fraction
        return GeoPoint(lat, lon)
    }

    /**
     * Calcula la distancia entre dos puntos en metros usando fórmula de Haversine
     */
    private fun calculateDistance(start: GeoPoint, end: GeoPoint): Double {
        val R = 6371000.0 // Radio de la Tierra en metros
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(start.latitude)) *
                Math.cos(Math.toRadians(end.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    /**
     * Calcula la distancia total de una ruta
     */
    private fun calculateTotalDistance(route: List<GeoPoint>): Double {
        var total = 0.0
        for (i in 0 until route.size - 1) {
            total += calculateDistance(route[i], route[i + 1])
        }
        return total / 1000.0 // Convertir a kilómetros
    }

    /**
     * Calcula la distancia restante desde la posición actual hasta el final de la ruta
     */
    private fun calculateRemainingDistance(
        route: List<GeoPoint>,
        currentSegment: Int,
        progressInSegment: Double
    ): Double {
        var remaining = 0.0

        // Distancia restante en el segmento actual
        val currentStart = route[currentSegment]
        val currentEnd = route[currentSegment + 1]
        val segmentDistance = calculateDistance(currentStart, currentEnd)
        remaining += segmentDistance * (1.0 - progressInSegment)

        // Distancia de los segmentos restantes
        for (i in currentSegment + 1 until route.size - 1) {
            remaining += calculateDistance(route[i], route[i + 1])
        }

        return remaining / 1000.0 // Convertir a kilómetros
    }
}