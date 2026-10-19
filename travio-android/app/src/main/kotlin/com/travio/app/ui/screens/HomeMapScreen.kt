package com.travio.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travio.app.data.Trip
import com.travio.app.ui.ForecastDay
import com.travio.app.ui.TripViewModel
import com.travio.app.ui.components.TripMapView
import com.travio.app.ui.theme.Lime
import com.travio.app.ui.theme.RouteColor
import com.travio.app.util.navigateWithGoogleMaps

@Composable
fun HomeMapScreen(viewModel: TripViewModel) {
    val trip by viewModel.trip.collectAsState()
    val dayIndex by viewModel.selectedDayIndex.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val forecast by viewModel.weatherForecast.collectAsState()
    val currentStopIndex by viewModel.currentStopIndex.collectAsState()
    val tileSource by viewModel.tileSource.collectAsState()
    val day = trip.days.getOrNull(dayIndex) ?: return
    val context = LocalContext.current

    var sheetExpanded by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        TripMapView(
            items = day.items,
            routePoints = routePoints,
            currentStopIndex = currentStopIndex,
            tileSource = tileSource,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                InfoChip {
                    Text("TRAVIO · DZIEŃ ${dayIndex + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(day.title, style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    InfoChip(onClick = { showWeatherDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (weather.loading) "…" else "${weather.tempC ?: "--"}°C",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    InfoChip {
                        Text(
                            "${viewModel.totalSpent().toInt()} € / ${trip.budgetPlanned.toInt()} €",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(trip.days.size) { i ->
                    DayPill(
                        label = "Dz. ${i + 1}",
                        active = i == dayIndex,
                        onClick = { viewModel.selectDay(i) }
                    )
                }
            }
        }

        BottomSheetPreview(
            trip = trip,
            dayIndex = dayIndex,
            currentStopIndex = currentStopIndex,
            expanded = sheetExpanded,
            onToggleExpanded = { sheetExpanded = !sheetExpanded },
            onNavigateNext = {
                val target = viewModel.nextStopTarget()
                if (target != null) {
                    navigateWithGoogleMaps(context, target.lat, target.lng)
                    viewModel.advanceToNextStop()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showWeatherDialog) {
        WeatherDialog(current = weather, forecast = forecast, onDismiss = { showWeatherDialog = false })
    }
}

@Composable
private fun WeatherDialog(current: com.travio.app.ui.WeatherState, forecast: List<ForecastDay>, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Pogoda", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Zamknij") }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Dziś: ${current.tempC ?: "--"}°C · ${current.label}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(14.dp))
                if (forecast.isEmpty()) {
                    Text("Prognoza niedostępna.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    forecast.forEach { f ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(f.date, style = MaterialTheme.typography.bodyMedium)
                            Text(f.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${f.tempMax ?: "--"}° / ${f.tempMin ?: "--"}°", fontWeight = FontWeight.Bold)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        onClick = onClick ?: {},
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), content = content)
    }
}

@Composable
private fun DayPill(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (active) Lime else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        contentColor = if (active) androidx.compose.ui.graphics.Color(0xFF10140F) else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BottomSheetPreview(
    trip: Trip,
    dayIndex: Int,
    currentStopIndex: Int,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val day = trip.days.getOrNull(dayIndex) ?: return
    val dragState = rememberDraggableState { delta ->
        // Przeciągnięcie w górę (ujemna delta) rozwija listę, w dół — zwija.
        if (delta < -6f && !expanded) onToggleExpanded()
        if (delta > 6f && expanded) onToggleExpanded()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .draggable(state = dragState, orientation = Orientation.Vertical),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 22.dp)
                .animateContentSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dziś · ${day.items.size} przystanki", style = MaterialTheme.typography.titleMedium)
                Surface(onClick = onToggleExpanded, color = androidx.compose.ui.graphics.Color.Transparent) {
                    Text(
                        if (expanded) "Zwiń" else "Pełen plan",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            val visibleItems = if (expanded) day.items else day.items.take(3)
            if (expanded) {
                LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                    items(visibleItems) { item ->
                        PlanRow(item = item, highlighted = day.items.indexOf(item) == currentStopIndex)
                    }
                }
            } else {
                visibleItems.forEachIndexed { i, item ->
                    PlanRow(item = item, highlighted = i == currentStopIndex)
                }
            }

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onNavigateNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouteColor,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nawiguj do następnego punktu", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlanRow(item: com.travio.app.data.TripItem, highlighted: Boolean) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            item.time,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(46.dp)
        )
        Text(
            item.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}
