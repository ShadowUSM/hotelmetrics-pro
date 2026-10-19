package com.travio.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.travio.app.data.TripItem
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun TripMapView(
    items: List<TripItem>,
    routePoints: List<GeoPoint>,
    routeColor: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            if (routePoints.size >= 2) {
                val line = Polyline(mapView).apply {
                    setPoints(routePoints)
                    outlinePaint.color = routeColor
                    outlinePaint.strokeWidth = 9f
                }
                mapView.overlays.add(line)
            }

            items.forEachIndexed { index, item ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(item.lat, item.lng)
                    title = "${index + 1}. ${item.name}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }

            if (items.isNotEmpty()) {
                mapView.controller.setCenter(GeoPoint(items.first().lat, items.first().lng))
            }
            mapView.invalidate()
        }
    )
}
