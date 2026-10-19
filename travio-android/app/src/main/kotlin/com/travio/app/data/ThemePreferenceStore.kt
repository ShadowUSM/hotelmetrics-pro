package com.travio.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.travio.app.ui.components.MapTileSource
import com.travio.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "travio_prefs")
private val THEME_KEY = stringPreferencesKey("theme_mode")
private val TILE_SOURCE_KEY = stringPreferencesKey("tile_source")

class ThemePreferenceStore(private val context: Context) {
    val themeMode = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_KEY] = mode.name }
    }

    val tileSource = context.dataStore.data.map { prefs -> MapTileSource.fromKey(prefs[TILE_SOURCE_KEY]) }

    suspend fun setTileSource(source: MapTileSource) {
        context.dataStore.edit { it[TILE_SOURCE_KEY] = source.name }
    }
}
