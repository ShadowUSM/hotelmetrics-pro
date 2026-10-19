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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Zdjęcia wgrane do assets bywają pełnej rozdzielczości aparatu (np. 3840x2991px).
// Zdekodowane wprost jako ARGB_8888 to ~44 MB na jeden bitmap — kilka takich naraz
// (lista w Planie, przewijanie) realnie rzuca OutOfMemoryError. W podglądzie karty
// potrzeba góra ~1000px w dłuższym boku, więc dekodujemy od razu w mniejszej próbce.
private const val MAX_DECODED_DIMENSION = 1000

private fun calculateInSampleSize(width: Int, height: Int): Int {
    var sample = 1
    val largest = maxOf(width, height)
    while (largest / sample > MAX_DECODED_DIMENSION) {
        sample *= 2
    }
    return sample
}

private fun decodeSampledFromAssets(context: android.content.Context, photoKey: String): Bitmap? {
    return try {
        val bytes = context.assets.open("photos/$photoKey.jpg").use { it.readBytes() }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    } catch (_: Exception) {
        null
    }
}

/**
 * Zdjęcia atrakcji są pobrane raz i wgrane na stałe do assets/photos/<photoKey>.jpg
 * (użycie prywatne, bez celu komercyjnego). Gdy dany klucz nie ma pliku (np. przez
 * limity pobierania przy budowie), pokazujemy ikonę kategorii na kolorowym tle.
 */
@Composable
fun AttractionPhoto(photoKey: String, category: ItemCategory, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, photoKey) {
        value = if (photoKey.isBlank()) null else withContext(Dispatchers.IO) {
            decodeSampledFromAssets(context, photoKey)
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
