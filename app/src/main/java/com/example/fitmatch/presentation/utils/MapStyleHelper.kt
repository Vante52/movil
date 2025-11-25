package com.example.fitmatch.presentation.utils

import android.util.Log
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView

/**
 * Helper para gestionar los estilos visuales del mapa
 * Soporta modo claro y oscuro
 */
object MapStyleHelper {

    private const val TAG = "MapStyleHelper"

    /**
     * Aplica estilo claro al mapa (Mapnik - estándar de OpenStreetMap)
     */
    fun applyLightStyle(mapView: MapView) {
        Log.d(TAG, "Aplicando estilo CLARO (Mapnik)")
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.invalidate()
    }

    /**
     * Aplica estilo oscuro al mapa (CartoDB Dark Matter)
     */
    fun applyDarkStyle(mapView: MapView) {
        Log.d(TAG, "Aplicando estilo OSCURO (CartoDB Dark Matter)")

        val darkMatter = object : OnlineTileSourceBase(
            "CartoDB Dark Matter",
            0, 20, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/dark_all/",
                "https://b.basemaps.cartocdn.com/dark_all/",
                "https://c.basemaps.cartocdn.com/dark_all/"
            )
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                        MapTileIndex.getX(pMapTileIndex) + "/" +
                        MapTileIndex.getY(pMapTileIndex) + ".png"
            }
        }

        mapView.setTileSource(darkMatter)
        mapView.invalidate()
    }
}