package com.travio.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.travio.app.data.TripItem
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private fun numberedMarkerBitmap(number: Int, current: Boolean, visited: Boolean): Bitmap {
    val sizePx = 84
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = sizePx / 2f
    val radius = sizePx / 2f - 6f

    val fillColor = when {
        current -> Color.parseColor("#FF5252")
        visited -> Color.parseColor("#9AA39C")
        else -> Color.parseColor("#0E7A6E")
    }

    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
    canvas.drawCircle(center, center, radius + 4f, ring)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor; style = Paint.Style.FILL }
    canvas.drawCircle(center, center, radius, fill)

    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = sizePx * 0.42f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textY = center - (text.descent() + text.ascent()) / 2f
    canvas.drawText(number.toString(), center, textY, text)
    return bitmap
}

@Composable
fun TripMapView(
    items: List<TripItem>,
    routePoints: List<GeoPoint>,
    currentStopIndex: Int,
    tileSource: MapTileSource,
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
                setTileSource(tileSourceFor(tileSource))
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            mapView.setTileSource(tileSourceFor(tileSource))
            mapView.overlays.clear()

            if (routePoints.size >= 2) {
                // "Obwódka" pod kolorową linią — trasa jest widoczna niezależnie od motywu
                // (limonka na czarnym tle zlewała się z resztą interfejsu).
                val casing = Polyline(mapView).apply {
                    setPoints(routePoints)
                    outlinePaint.color = Color.WHITE
                    outlinePaint.strokeWidth = 13f
                }
                val line = Polyline(mapView).apply {
                    setPoints(routePoints)
                    outlinePaint.color = Color.parseColor("#FF5252")
                    outlinePaint.strokeWidth = 8f
                }
                mapView.overlays.add(casing)
                mapView.overlays.add(line)
            }

            items.forEachIndexed { index, item ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(item.lat, item.lng)
                    title = "${index + 1}. ${item.name}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = BitmapDrawable(
                        mapView.resources,
                        numberedMarkerBitmap(
                            number = index + 1,
                            current = index == currentStopIndex,
                            visited = index < currentStopIndex
                        )
                    )
                }
                mapView.overlays.add(marker)
            }

            val focus = items.getOrNull(currentStopIndex) ?: items.firstOrNull()
            if (focus != null) {
                mapView.controller.setCenter(GeoPoint(focus.lat, focus.lng))
            }
            mapView.invalidate()
        }
    )
}
