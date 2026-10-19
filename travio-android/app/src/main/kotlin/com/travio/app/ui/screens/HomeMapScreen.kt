package com.travio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travio.app.data.Trip
import com.travio.app.ui.TripViewModel
import com.travio.app.ui.components.TripMapView
import com.travio.app.ui.theme.Lime

@Composable
fun HomeMapScreen(viewModel: TripViewModel) {
    val trip by viewModel.trip.collectAsState()
    val dayIndex by viewModel.selectedDayIndex.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val day = trip.days.getOrNull(dayIndex) ?: return

    val routeColor = MaterialTheme.colorScheme.primary.toArgb()

    Box(modifier = Modifier.fillMaxSize()) {

        TripMapView(
            items = day.items,
            routePoints = routePoints,
            routeColor = routeColor,
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
                    InfoChip {
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
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun InfoChip(content: @Composable ColumnScope.() -> Unit) {
    Surface(
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
private fun BottomSheetPreview(trip: Trip, dayIndex: Int, modifier: Modifier = Modifier) {
    val day = trip.days.getOrNull(dayIndex) ?: return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 22.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dziś · ${day.items.size} przystanki", style = MaterialTheme.typography.titleMedium)
                Text("Pełen plan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            day.items.take(3).forEachIndexed { i, item ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        item.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(46.dp)
                    )
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    contentColor = androidx.compose.ui.graphics.Color(0xFF10140F)
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
