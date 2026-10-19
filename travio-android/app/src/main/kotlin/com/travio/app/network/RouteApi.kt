package com.travio.app.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class OsrmResponse(
    val routes: List<OsrmRoute>?
)

data class OsrmRoute(
    val geometry: OsrmGeometry?,
    val distance: Double?,
    val duration: Double?
)

data class OsrmGeometry(
    val coordinates: List<List<Double>>?
)

interface RouteApi {
    /** Trasa piesza łącząca punkty, OSRM demo server (publiczny, bez klucza). */
    @GET("route/v1/foot/{coords}")
    suspend fun walkingRoute(
        @Path("coords") coords: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): OsrmResponse
}
