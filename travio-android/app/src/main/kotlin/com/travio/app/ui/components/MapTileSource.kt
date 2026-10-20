package com.travio.app.ui.components

import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

enum class MapTileSource(val label: String) {
    OSM_STANDARD("OpenStreetMap"),
    ESRI_STREET("Esri (ulice)"),
    ESRI_SATELLITE("Esri (satelita)"),
    ESRI_DARK_GRAY("Esri (ciemna)");

    companion object {
        fun fromKey(key: String?): MapTileSource = entries.find { it.name == key } ?: OSM_STANDARD
    }
}

/** Esri ArcGIS REST tile services adresują kafelki jako z/y/x (odwrotnie niż standard XYZ),
 * więc trzeba nadpisać budowanie URL-a zamiast polegać na domyślnym z/x/y z XYTileSource. */
private class EsriTileSource(name: String, baseUrl: String, ext: String) : XYTileSource(
    name, 0, 19, 256, ext, arrayOf(baseUrl)
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${getBaseUrl()}$zoom/$y/$x$mImageFilenameEnding"
    }
}

/**
 * Wszystkie te źródła są darmowe i nie wymagają klucza API — tak samo jak w wersji HTML
 * aplikacji (żadnych kont, żadnych limitów rozliczeniowych).
 */
fun tileSourceFor(option: MapTileSource): ITileSource = when (option) {
    MapTileSource.OSM_STANDARD -> TileSourceFactory.MAPNIK
    MapTileSource.ESRI_STREET -> EsriTileSource(
        "EsriWorldStreet",
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/",
        ".png"
    )
    MapTileSource.ESRI_SATELLITE -> EsriTileSource(
        "EsriWorldImagery",
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/",
        ".jpg"
    )
    // CartoDB (Voyager/Dark Matter) zaczął wymagać klucza API — darmowe kafelki zwracają
    // teraz placeholder "API KEY REQUIRED" zamiast mapy. Esri Dark Gray Canvas to prawdziwy,
    // ciemny motyw mapy, nadal bez klucza i konta.
    MapTileSource.ESRI_DARK_GRAY -> EsriTileSource(
        "EsriDarkGray",
        "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/",
        ".png"
    )
}
