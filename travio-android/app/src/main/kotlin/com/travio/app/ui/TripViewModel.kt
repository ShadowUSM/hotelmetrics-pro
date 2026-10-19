package com.travio.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travio.app.data.Expense
import com.travio.app.data.ExpenseStore
import com.travio.app.data.ThemePreferenceStore
import com.travio.app.data.Trip
import com.travio.app.data.TripRepository
import com.travio.app.network.NetworkModule
import com.travio.app.network.weatherCodeLabel
import com.travio.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.UUID

data class WeatherState(val tempC: Int? = null, val label: String = "pogoda", val loading: Boolean = true)

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TripRepository(application)
    private val themeStore = ThemePreferenceStore(application)

    private val _trip = MutableStateFlow(repository.loadTrip())
    val trip: StateFlow<Trip> = _trip.asStateFlow()

    private val _selectedDayIndex = MutableStateFlow(repository.currentDayIndex(_trip.value))
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    private val _weather = MutableStateFlow(WeatherState())
    val weather: StateFlow<WeatherState> = _weather.asStateFlow()

    private val _expenses = MutableStateFlow(ExpenseStore.all())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = themeStore.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM
    )

    init {
        loadRoute(_selectedDayIndex.value)
        loadWeather()
    }

    fun selectDay(index: Int) {
        _selectedDayIndex.value = index
        loadRoute(index)
    }

    private fun loadRoute(dayIndex: Int) {
        viewModelScope.launch {
            _routePoints.value = emptyList()
            val day = _trip.value.days.getOrNull(dayIndex) ?: return@launch
            if (day.items.size < 2) return@launch
            try {
                val coords = day.items.joinToString(";") { "${it.lng},${it.lat}" }
                val resp = NetworkModule.routeApi.walkingRoute(coords)
                val geo = resp.routes?.firstOrNull()?.geometry?.coordinates
                if (geo != null) {
                    _routePoints.value = geo.map { GeoPoint(it[1], it[0]) }
                }
            } catch (_: Exception) {
                // Brak internetu/limit OSRM -> mapa pokaże same znaczniki, bez trasy.
            }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            try {
                val hotel = _trip.value.hotel
                val resp = NetworkModule.weatherApi.current(hotel.lat, hotel.lng)
                _weather.value = WeatherState(
                    tempC = resp.current?.temperature_2m?.toInt(),
                    label = weatherCodeLabel(resp.current?.weather_code),
                    loading = false
                )
            } catch (_: Exception) {
                _weather.value = WeatherState(loading = false, label = "brak danych")
            }
        }
    }

    fun addExpense(label: String, category: String, amount: Double) {
        ExpenseStore.add(Expense(UUID.randomUUID().toString(), label, category, amount, _trip.value.days.getOrNull(_selectedDayIndex.value)?.date ?: ""))
        _expenses.value = ExpenseStore.all()
    }

    fun totalSpent(): Double = ExpenseStore.totalSpent()
    fun totalsByCategory(): Map<String, Double> = ExpenseStore.totalsByCategory()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeStore.setThemeMode(mode) }
    }
}
