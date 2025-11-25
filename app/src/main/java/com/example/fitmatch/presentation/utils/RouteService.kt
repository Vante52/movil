package com.example.fitmatch.presentation.utils
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL

/**
 * Servicio para obtener rutas reales usando OSRM (Open Source Routing Machine)
 */
object RouteService {

    private const val TAG = "RouteService"
    private const val OSRM_BASE_URL = "https://router.project-osrm.org/route/v1/driving"

    /**
     * Obtiene una ruta real entre dos puntos usando las calles
     * @param start Punto de inicio
     * @param end Punto de destino
     * @return Lista de GeoPoints que forman la ruta por las calles
     */
    suspend fun getRoute(start: GeoPoint, end: GeoPoint): List<GeoPoint> = withContext(Dispatchers.IO) {
        try {
            // Construir URL para OSRM
            val url = "$OSRM_BASE_URL/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson"

            Log.d(TAG, "Solicitando ruta: $url")

            // Hacer petición HTTP
            val response = URL(url).readText()
            val json = JSONObject(response)

            // Extraer coordenadas de la ruta
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                Log.w(TAG, "No se encontraron rutas")
                return@withContext listOf(start, end)
            }

            val route = routes.getJSONObject(0)
            val geometry = route.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            // Convertir coordenadas a GeoPoints
            val routePoints = mutableListOf<GeoPoint>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val lon = coord.getDouble(0)
                val lat = coord.getDouble(1)
                routePoints.add(GeoPoint(lat, lon))
            }

            Log.d(TAG, "Ruta obtenida: ${routePoints.size} puntos")
            routePoints

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ruta: ${e.message}", e)
            // Si falla, devolver línea recta como fallback
            listOf(start, end)
        }
    }

    /**
     * Obtiene información detallada de la ruta (distancia, duración)
     */
    suspend fun getRouteInfo(start: GeoPoint, end: GeoPoint): RouteInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "$OSRM_BASE_URL/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=false"

            val response = URL(url).readText()
            val json = JSONObject(response)

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext null

            val route = routes.getJSONObject(0)
            val distance = route.getDouble("distance") // en metros
            val duration = route.getDouble("duration") // en segundos

            RouteInfo(
                distanceMeters = distance,
                durationSeconds = duration
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo info de ruta: ${e.message}")
            null
        }
    }
}

data class RouteInfo(
    val distanceMeters: Double,
    val durationSeconds: Double
) {
    val distanceKm: Double
        get() = distanceMeters / 1000.0

    val durationMinutes: Int
        get() = (durationSeconds / 60).toInt()
}