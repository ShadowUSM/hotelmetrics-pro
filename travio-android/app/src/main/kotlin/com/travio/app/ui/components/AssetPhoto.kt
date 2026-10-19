package com.travio.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.travio.app.data.ItemCategory

/**
 * Zdjęcia atrakcji są pobrane raz i wgrane na stałe do assets/photos/<photoKey>.jpg
 * (użycie prywatne, bez celu komercyjnego). Gdy dany klucz nie ma pliku (np. przez
 * limity pobierania przy budowie), pokazujemy ikonę kategorii na kolorowym tle.
 */
@Composable
fun AttractionPhoto(photoKey: String, category: ItemCategory, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, photoKey) {
        value = if (photoKey.isBlank()) null else try {
            context.assets.open("photos/$photoKey.jpg").use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = loaded.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                iconFor(category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
            )
        }
    }
}
