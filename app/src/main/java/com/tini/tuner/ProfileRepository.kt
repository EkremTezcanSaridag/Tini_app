package com.tini.tuner

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "tuning_profiles")

class ProfileRepository(private val context: Context) {
    private val profilesKey = stringPreferencesKey("profiles_json")
    private val json = Json { ignoreUnknownKeys = true }

    val profilesFlow: Flow<List<TuningProfile>> = context.profileDataStore.data.map { prefs ->
        val raw = prefs[profilesKey] ?: "[]"
        try {
            json.decodeFromString<List<TuningProfile>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveProfile(profile: TuningProfile) {
        context.profileDataStore.edit { prefs ->
            val list = loadList(prefs[profilesKey])
            val updated = listOf(profile) + list.filter { it.id != profile.id }
            prefs[profilesKey] = json.encodeToString(updated.take(20))
        }
    }

    suspend fun deleteProfile(id: String) {
        context.profileDataStore.edit { prefs ->
            val list = loadList(prefs[profilesKey]).filter { it.id != id }
            prefs[profilesKey] = json.encodeToString(list)
        }
    }

    fun exportToJsonFile(profile: TuningProfile): File {
        val dir = File(context.cacheDir, "profiles").apply { mkdirs() }
        val file = File(dir, "tini_profile_${profile.name.replace(" ", "_")}.json")
        file.writeText(json.encodeToString(profile))
        return file
    }

    fun createFromState(name: String, uiState: TunerUiState, themeMode: ThemeMode): TuningProfile {
        return TuningProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            instrumentName = uiState.currentInstrument.displayName,
            tuningName = uiState.currentTuning.displayName,
            a4Reference = uiState.a4Reference,
            themeMode = themeMode.name,
            createdAtMs = System.currentTimeMillis()
        )
    }

    private fun loadList(raw: String?): List<TuningProfile> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
