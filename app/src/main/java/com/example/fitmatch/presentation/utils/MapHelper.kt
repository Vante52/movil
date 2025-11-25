package com.example.fitmatch.presentation.utils

import android.graphics.Color
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Utilidades para manipular mapas OSMDroid
 */
object MapHelper {

    private const val TAG = "MapHelper"

    /**
     * Agrega un marcador al mapa
     */
    fun addMarker(
        mapView: MapView,
        geoPoint: GeoPoint,
        title: String,
        snippet: String? = null
    ): Marker {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = title
        marker.snippet = snippet
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate()
        Log.d(TAG, "Marcador agregado: $title")
        return marker
    }

    /**
     * Actualiza posición de un marcador existente
     */
    fun updateMarker(marker: Marker, geoPoint: GeoPoint, mapView: MapView) {
        marker.position = geoPoint
        mapView.invalidate()
    }

    /**
     * Agrega polyline AZUL (recorrido del repartidor)
     */
    fun addPolyline(mapView: MapView, points: List<GeoPoint>): Polyline {
        val polyline = Polyline()
        polyline.setPoints(points)
        polyline.color = Color.BLUE
        polyline.width = 10f
        mapView.overlays.add(polyline)
        mapView.invalidate()
        Log.d(TAG, "Polyline azul agregada")
        return polyline
    }

    /**
     * Agrega polyline ROJA (ruta directa al destino)
     */
    fun addRoutePolyline(mapView: MapView, points: List<GeoPoint>): Polyline {
        val polyline = Polyline()
        polyline.setPoints(points)
        polyline.color = Color.RED
        polyline.width = 8f
        polyline.outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
        mapView.overlays.add(polyline)
        mapView.invalidate()
        Log.d(TAG, "Ruta roja agregada")
        return polyline
    }

    /**
     * Actualiza puntos de una polyline existente
     */
    fun updatePolyline(polyline: Polyline, points: List<GeoPoint>, mapView: MapView) {
        polyline.setPoints(points)
        mapView.invalidate()
    }

    fun removePolyline(mapView: MapView, polyline: Polyline) {
        mapView.overlays.remove(polyline)
        mapView.invalidate()
    }

    /**
     * Elimina todos los marcadores del mapa
     */
    fun clearMarkers(mapView: MapView) {
        mapView.overlays.removeAll { it is Marker }
        mapView.invalidate()
    }

    /**
     * Centra el mapa en una ubicación con zoom
     */
    fun centerMapOnLocation(mapView: MapView, geoPoint: GeoPoint, zoom: Double = 15.0) {
        mapView.controller.animateTo(geoPoint)
        mapView.controller.setZoom(zoom)
    }

    /**
     * Ajusta el zoom para mostrar todos los puntos de una ruta
     */
    fun fitBoundsToRoute(mapView: MapView, points: List<GeoPoint>) {
        if (points.isEmpty()) return

        val latitudes = points.map { it.latitude }
        val longitudes = points.map { it.longitude }

        val minLat = latitudes.minOrNull() ?: return
        val maxLat = latitudes.maxOrNull() ?: return
        val minLon = longitudes.minOrNull() ?: return
        val maxLon = longitudes.maxOrNull() ?: return

        val centerLat = (minLat + maxLat) / 2
        val centerLon = (minLon + maxLon) / 2

        mapView.controller.setCenter(GeoPoint(centerLat, centerLon))

        // Calcular zoom según distancia
        val latDiff = maxLat - minLat
        val lonDiff = maxLon - minLon
        val maxDiff = maxOf(latDiff, lonDiff)

        val zoom = when {
            maxDiff > 0.5 -> 10.0
            maxDiff > 0.1 -> 12.0
            maxDiff > 0.05 -> 14.0
            else -> 16.0
        }

        mapView.controller.setZoom(zoom)
        Log.d(TAG, "Zoom ajustado: $zoom")
    }
}