package com.travio.app.network

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val current: CurrentWeather?
)

data class CurrentWeather(
    val temperature_2m: Double?,
    val weather_code: Int?
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun current(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("current") current: String = "temperature_2m,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

/** Mapuje kod pogody Open-Meteo (WMO) na krótki polski opis. */
fun weatherCodeLabel(code: Int?): String = when (code) {
    null -> "pogoda"
    0 -> "bezchmurnie"
    1, 2 -> "lekko pochmurno"
    3 -> "pochmurno"
    45, 48 -> "mgła"
    in 51..57 -> "mżawka"
    in 61..67 -> "deszcz"
    in 71..77 -> "śnieg"
    in 80..82 -> "przelotny deszcz"
    in 95..99 -> "burza"
    else -> "pogoda"
}
