package com.travio.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.gson.Gson

// Jeden piksel bitmapy budynku to maks. 512x512 (assets/buildings3d/<light|dark>/<key>.png),
// dekodowany od razu w jeszcze mniejszej próbce — tyle samo ryzyko OOM co w przypadku
// zdjęć w Planie (AssetPhoto.kt), więc ta sama ostrożność: inSampleSize + wątek IO.
private const val MAX_DECODED_DIMENSION = 360

private data class BuildingManifestTheme(val top: Double)
private typealias BuildingManifest = Map<String, Map<String, BuildingManifestTheme>>

private var cachedManifest: BuildingManifest? = null

private fun loadManifest(context: Context): BuildingManifest {
    cachedManifest?.let { return it }
    val json = context.assets.open("buildings3d_manifest.json").bufferedReader().use { it.readText() }
    val type = object : com.google.gson.reflect.TypeToken<BuildingManifest>() {}.type
    val parsed: BuildingManifest = Gson().fromJson(json, type) ?: emptyMap()
    cachedManifest = parsed
    return parsed
}

/** Czy dla danego klucza atrakcji istnieje gotowa ilustracja 3D (w obu motywach). */
fun hasBuildingIllustration(context: Context, photoKey: String?): Boolean {
    if (photoKey.isNullOrBlank()) return false
    return loadManifest(context).containsKey(photoKey)
}

private fun calculateInSampleSize(width: Int, height: Int): Int {
    var sample = 1
    val largest = maxOf(width, height)
    while (largest / sample > MAX_DECODED_DIMENSION) sample *= 2
    return sample
}

// Budynków jest tylko 19 na motyw, więc trzymanie zdekodowanych bitmap w pamięci (zamiast
// odczytu + dekodowania z assets przy każdej aktualizacji mapy) jest tanie i eliminuje
// powtarzalny narzut na głównym wątku, na którym `update` AndroidView się wykonuje.
private val decodedBuildingCache = mutableMapOf<String, Bitmap?>()

private fun decodeSampledCached(context: Context, path: String): Bitmap? {
    decodedBuildingCache[path]?.let { return it }
    if (decodedBuildingCache.containsKey(path)) return null // wcześniej nie udało się zdekodować
    val bitmap = try {
        val bytes = context.assets.open(path).use { it.readBytes() }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    } catch (_: Exception) {
        null
    }
    decodedBuildingCache[path] = bitmap
    return bitmap
}

/** [bitmap] gotowy do ustawienia jako ikona markera; [anchorV] to pionowy punkt zakotwiczenia
 * (0=góra, 1=dół ikony) który ma pokrywać się z pozycją GPS — środek dla samego krążka,
 * ok. 88% wysokości (podstawa budynku) gdy domalowana jest ilustracja 3D. */
data class MapMarkerIcon(val bitmap: Bitmap, val anchorV: Float)

/**
 * Buduje jedną gotową bitmapę do użycia jako ikona markera na mapie: ilustrowany budynek
 * (jeśli dostępny dla [photoKey] i włączony w ustawieniach) z numerowaną pinezką domalowaną
 * tuż nad jego najwyższym punktem — albo, gdy budynku brak, sam mały numerowany krążek jak
 * w widoku 2D. Jeden marker na atrakcję, bez osobnego przesuwania dwóch elementów.
 */
fun buildMapMarkerIcon(
    context: Context,
    photoKey: String?,
    isDarkTheme: Boolean,
    number: Int,
    current: Boolean,
    visited: Boolean
): MapMarkerIcon {
    val manifestEntry = if (photoKey.isNullOrBlank()) null else loadManifest(context)[photoKey]
    val themeEntry = manifestEntry?.get(if (isDarkTheme) "dark" else "light")

    if (themeEntry == null) {
        return MapMarkerIcon(numberedMarkerBitmap(number, current, visited), anchorV = 0.5f)
    }

    val theme = if (isDarkTheme) "dark" else "light"
    val building = decodeSampledCached(context, "buildings3d/$theme/$photoKey.png")
        ?: return MapMarkerIcon(numberedMarkerBitmap(number, current, visited), anchorV = 0.5f)

    val result = building.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)

    val pinRadius = result.width * 0.09f
    val pinCenterY = (result.height * themeEntry.top.toFloat() - pinRadius * 0.6f)
        .coerceAtLeast(pinRadius + 4f)
    val pinCenterX = result.width / 2f

    val fillColor = when {
        current -> Color.parseColor("#FF5252")
        visited -> Color.parseColor("#9AA39C")
        else -> Color.parseColor("#0E7A6E")
    }
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
    canvas.drawCircle(pinCenterX, pinCenterY, pinRadius + 3.5f, ring)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor; style = Paint.Style.FILL }
    canvas.drawCircle(pinCenterX, pinCenterY, pinRadius, fill)
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = pinRadius * 1.1f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textY = pinCenterY - (text.descent() + text.ascent()) / 2f
    canvas.drawText(number.toString(), pinCenterX, textY, text)

    return MapMarkerIcon(result, anchorV = 0.88f)
}
