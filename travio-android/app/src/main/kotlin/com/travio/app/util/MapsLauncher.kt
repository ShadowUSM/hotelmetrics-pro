package com.travio.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Otwiera Mapy Google z wyznaczoną trasą pieszą do punktu (lub w przeglądarce, jeśli
 * aplikacja Map nie jest zainstalowana — ten sam link działa w obu przypadkach). */
fun openGoogleMapsDirections(context: Context, lat: Double, lng: Double, label: String = "") {
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking" +
            if (label.isNotBlank()) "&destination_place_id=&query=${Uri.encode(label)}" else ""
    )
    launch(context, uri)
}

/** Próbuje od razu uruchomić nawigację turn-by-turn (tryb pieszy) w aplikacji Mapy Google;
 * jeśli aplikacja nie jest zainstalowana, spada do zwykłego linku z wyznaczoną trasą. */
fun navigateWithGoogleMaps(context: Context, lat: Double, lng: Double) {
    val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
    val intent = Intent(Intent.ACTION_VIEW, navUri).apply { setPackage("com.google.android.apps.maps") }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        openGoogleMapsDirections(context, lat, lng)
    }
}

/** Trasa pomiędzy dwoma konkretnymi punktami (np. z ostatniej atrakcji dnia do hotelu),
 * niezależna od aktualnej lokalizacji użytkownika. */
fun openGoogleMapsRoute(context: Context, originLat: Double, originLng: Double, destLat: Double, destLng: Double) {
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&origin=$originLat,$originLng&destination=$destLat,$destLng&travelmode=walking"
    )
    launch(context, uri)
}

private fun launch(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Brak przeglądarki/aplikacji zdolnej obsłużyć link -- nic więcej nie da się zrobić.
    }
}
