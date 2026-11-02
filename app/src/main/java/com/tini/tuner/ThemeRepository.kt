package com.tini.tuner

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

class ThemeRepository(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    
    val themeModeFlow: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        val modeString = preferences[themeModeKey] ?: ThemeMode.LIGHT.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }
}
