package com.travio.app.data

data class TripItem(
    val id: String,
    val time: String,
    val dur: Int,
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val desc: String,
    val cost: Double,
    val costLabel: String,
    // Nullable mimo że w Kotlinie te pola mają domyślną wartość "" — Gson konstruuje
    // obiekty przez Unsafe (z pominięciem konstruktora), więc gdy klucza brakuje w JSON,
    // zostaje tu surowy null, nie "". Odczyt przez nie-nullowalny String rzucał NPE przy
    // każdym rozwinięciu punktu bez tych pól (czyli prawie każdego punktu w planie).
    val practicalInfo: String? = null,
    val photoKey: String? = null
)

data class TripDay(
    val id: String,
    val date: String,
    val title: String,
    val items: List<TripItem>
)

data class Hotel(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

data class Trip(
    val title: String,
    val city: String,
    val country: String,
    val dateStart: String,
    val dateEnd: String,
    val currency: String,
    val budgetPlanned: Double,
    val hotel: Hotel,
    val cityFacts: List<String>,
    val days: List<TripDay>
)

data class Expense(
    val id: String,
    val label: String,
    val category: String,
    val amount: Double,
    val date: String
)

enum class ItemCategory(val label: String) {
    LANDMARK("Zabytki"),
    FOOD("Restauracje"),
    TRANSPORT("Transport"),
    HOTEL("Hotel"),
    MUSEUM("Muzeum"),
    VIEWPOINT("Punkt widokowy"),
    ENTERTAINMENT("Rozrywka"),
    OTHER("Inne");

    companion object {
        fun fromKey(key: String): ItemCategory = when (key) {
            "landmark" -> LANDMARK
            "food" -> FOOD
            "transport" -> TRANSPORT
            "hotel" -> HOTEL
            "museum" -> MUSEUM
            "viewpoint" -> VIEWPOINT
            "entertainment" -> ENTERTAINMENT
            else -> OTHER
        }
    }
}
