package com.travio.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travio.app.data.ItemCategory
import com.travio.app.data.Trip
import com.travio.app.data.TripItem
import com.travio.app.ui.TripViewModel
import com.travio.app.ui.components.AttractionPhoto
import com.travio.app.ui.components.iconFor
import com.travio.app.ui.theme.Lime
import com.travio.app.util.openGoogleMapsDirections
import com.travio.app.util.openGoogleMapsRoute

@Composable
fun PlanScreen(viewModel: TripViewModel) {
    val trip by viewModel.trip.collectAsState()
    val dayIndex by viewModel.selectedDayIndex.collectAsState()
    val expandedItemId by viewModel.expandedItemId.collectAsState()
    val pagerState = rememberPagerState(initialPage = dayIndex, pageCount = { trip.days.size })

    // Przeciągnięcie palcem w prawo/lewo zmienia dzień; a wybór dnia z chipów przewija pager.
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != dayIndex) viewModel.selectDay(pagerState.currentPage)
    }
    androidx.compose.runtime.LaunchedEffect(dayIndex) {
        if (pagerState.currentPage != dayIndex) pagerState.animateScrollToPage(dayIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)) {
            Text("PLAN DNIA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Dzień ${dayIndex + 1} · ${trip.days.getOrNull(dayIndex)?.title.orEmpty()}", style = MaterialTheme.typography.headlineSmall)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            items(trip.days.size) { i ->
                Surface(
                    onClick = { viewModel.selectDay(i) },
                    color = if (i == dayIndex) Lime else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (i == dayIndex) androidx.compose.ui.graphics.Color(0xFF10140F) else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        "Dz. ${i + 1}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val day = trip.days.getOrNull(page) ?: return@HorizontalPager
            DayPlanList(
                trip = trip,
                dayIndex = page,
                expandedItemId = expandedItemId,
                onToggleExpand = { viewModel.toggleExpanded(it) },
                onMoveToDay = { itemId, targetDay -> viewModel.moveItemToDay(itemId, page, targetDay) }
            )
        }
    }
}

@Composable
private fun DayPlanList(
    trip: Trip,
    dayIndex: Int,
    expandedItemId: String?,
    onToggleExpand: (String) -> Unit,
    onMoveToDay: (String, Int) -> Unit
) {
    val day = trip.days.getOrNull(dayIndex) ?: return
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(day.items, key = { it.id }) { item ->
            PlanItemCard(
                item = item,
                expanded = expandedItemId == item.id,
                dayCount = trip.days.size,
                currentDayIndex = dayIndex,
                onToggleExpand = { onToggleExpand(item.id) },
                onMoveToDay = { target -> onMoveToDay(item.id, target) }
            )
        }

        val lastItem = day.items.lastOrNull()
        if (lastItem != null) {
            item {
                EndOfDayHotelCard(
                    lastItem = lastItem,
                    hotel = trip.hotel,
                    onNavigate = {
                        openGoogleMapsRoute(context, lastItem.lat, lastItem.lng, trip.hotel.lat, trip.hotel.lng)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlanItemCard(
    item: TripItem,
    expanded: Boolean,
    dayCount: Int,
    currentDayIndex: Int,
    onToggleExpand: () -> Unit,
    onMoveToDay: (Int) -> Unit
) {
    val context = LocalContext.current
    var moveMenuOpen by remember { mutableStateOf(false) }
    val rotation by androidx.compose.animation.core.animateFloatAsState(if (expanded) 180f else 0f, label = "expand-rotate")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconFor(ItemCategory.fromKey(item.category)), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${item.time} · ${item.dur} MIN",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        item.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                    if (item.cost > 0) {
                        Spacer(Modifier.height(7.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AttachMoney, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${item.cost.toInt()} € · ${item.costLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Zwiń" else "Rozwiń",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                AttractionPhoto(
                    photoKey = item.photoKey,
                    category = ItemCategory.fromKey(item.category),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
                if (item.practicalInfo.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.practicalInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { openGoogleMapsDirections(context, item.lat, item.lng, item.name) }) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mapy Google")
                    }
                    Box {
                        OutlinedButton(onClick = { moveMenuOpen = true }) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Przenieś")
                        }
                        DropdownMenu(expanded = moveMenuOpen, onDismissRequest = { moveMenuOpen = false }) {
                            for (d in 0 until dayCount) {
                                if (d == currentDayIndex) continue
                                DropdownMenuItem(
                                    text = { Text("Dzień ${d + 1}") },
                                    onClick = { moveMenuOpen = false; onMoveToDay(d) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndOfDayHotelCard(lastItem: TripItem, hotel: com.travio.app.data.Hotel, onNavigate: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Powrót do hotelu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Z \"${lastItem.name}\" do ${hotel.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onNavigate) {
                Icon(Icons.Filled.Map, contentDescription = "Trasa do hotelu", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
