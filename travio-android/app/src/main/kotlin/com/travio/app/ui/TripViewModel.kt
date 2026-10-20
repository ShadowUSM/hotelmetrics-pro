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
import com.travio.app.ui.components.MapTileSource
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

data class ForecastDay(val date: String, val label: String, val tempMax: Int?, val tempMin: Int?)

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

    private val _weatherForecast = MutableStateFlow<List<ForecastDay>>(emptyList())
    val weatherForecast: StateFlow<List<ForecastDay>> = _weatherForecast.asStateFlow()

    private val _expenses = MutableStateFlow(ExpenseStore.all())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Numer "aktualnego" przystanku na dany dzień — punkty na mapie są numerowane
    // w kolejności zwiedzania, a ten indeks wskazuje, dokąd prowadzi "Nawiguj dalej".
    private val _currentStopIndex = MutableStateFlow(0)
    val currentStopIndex: StateFlow<Int> = _currentStopIndex.asStateFlow()

    // Id rozwiniętego elementu na ekranie Planu (tylko jeden na raz).
    private val _expandedItemId = MutableStateFlow<String?>(null)
    val expandedItemId: StateFlow<String?> = _expandedItemId.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = themeStore.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM
    )

    val tileSource: StateFlow<MapTileSource> = themeStore.tileSource.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), MapTileSource.OSM_STANDARD
    )

    val show3DBuildings: StateFlow<Boolean> = themeStore.show3DBuildings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    init {
        loadRoute(_selectedDayIndex.value)
        loadWeather()
    }

    fun selectDay(index: Int) {
        _selectedDayIndex.value = index
        _currentStopIndex.value = 0
        _expandedItemId.value = null
        loadRoute(index)
    }

    fun toggleExpanded(itemId: String) {
        _expandedItemId.value = if (_expandedItemId.value == itemId) null else itemId
    }

    /** Punkt, do którego prowadzi "Nawiguj do następnego punktu" — po wywołaniu nawigacji
     * znacznik na mapie przesuwa się na kolejny przystanek dnia. */
    fun nextStopTarget(): com.travio.app.data.TripItem? {
        val day = _trip.value.days.getOrNull(_selectedDayIndex.value) ?: return null
        return day.items.getOrNull(_currentStopIndex.value)
    }

    fun advanceToNextStop() {
        val day = _trip.value.days.getOrNull(_selectedDayIndex.value) ?: return
        if (_currentStopIndex.value < day.items.size - 1) {
            _currentStopIndex.value += 1
        }
    }

    fun moveItemToDay(itemId: String, fromDayIndex: Int, toDayIndex: Int) {
        val trip = _trip.value
        if (fromDayIndex == toDayIndex) return
        val fromDay = trip.days.getOrNull(fromDayIndex) ?: return
        val toDay = trip.days.getOrNull(toDayIndex) ?: return
        val item = fromDay.items.find { it.id == itemId } ?: return

        val newDays = trip.days.mapIndexed { index, day ->
            when (index) {
                fromDayIndex -> day.copy(items = day.items.filterNot { it.id == itemId })
                toDayIndex -> day.copy(items = (day.items + item).sortedBy { it.time })
                else -> day
            }
        }
        _trip.value = trip.copy(days = newDays)
        if (_selectedDayIndex.value == fromDayIndex || _selectedDayIndex.value == toDayIndex) {
            loadRoute(_selectedDayIndex.value)
        }
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
                val daily = resp.daily
                if (daily?.time != null) {
                    _weatherForecast.value = daily.time.indices.map { i ->
                        ForecastDay(
                            date = daily.time[i],
                            label = weatherCodeLabel(daily.weather_code?.getOrNull(i)),
                            tempMax = daily.temperature_2m_max?.getOrNull(i)?.toInt(),
                            tempMin = daily.temperature_2m_min?.getOrNull(i)?.toInt()
                        )
                    }
                }
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

    fun setTileSource(source: MapTileSource) {
        viewModelScope.launch { themeStore.setTileSource(source) }
    }

    fun setShow3DBuildings(enabled: Boolean) {
        viewModelScope.launch { themeStore.setShow3DBuildings(enabled) }
    }
}
