package com.travio.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.travio.app.data.ItemCategory

fun iconFor(category: ItemCategory): ImageVector = when (category) {
    ItemCategory.LANDMARK -> Icons.Filled.Place
    ItemCategory.FOOD -> Icons.Filled.Restaurant
    ItemCategory.TRANSPORT -> Icons.Filled.DirectionsWalk
    ItemCategory.HOTEL -> Icons.Filled.Hotel
    ItemCategory.MUSEUM -> Icons.Filled.Museum
    ItemCategory.VIEWPOINT -> Icons.Filled.Landscape
    ItemCategory.ENTERTAINMENT -> Icons.Filled.Celebration
    ItemCategory.OTHER -> Icons.Filled.Place
}
