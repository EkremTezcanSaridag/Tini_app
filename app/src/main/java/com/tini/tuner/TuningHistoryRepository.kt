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
import kotlinx.serialization.decodeFromString

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "tuning_history")

class TuningHistoryRepository(private val context: Context) {
    private val historyKey = stringPreferencesKey("history_entries")
    private val json = Json { ignoreUnknownKeys = true }
    
    val historyFlow: Flow<List<TuningHistoryEntry>> = context.historyDataStore.data.map { preferences ->
        val jsonString = preferences[historyKey] ?: "[]"
        try {
            json.decodeFromString<List<TuningHistoryEntry>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun addEntry(entry: TuningHistoryEntry) {
        context.historyDataStore.edit { preferences ->
            val currentJsonString = preferences[historyKey] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<TuningHistoryEntry>>(currentJsonString)
            } catch (e: Exception) {
                emptyList()
            }
            
            // Keep only last 100 entries
            val newList = (listOf(entry) + currentList).take(100)
            preferences[historyKey] = json.encodeToString(newList)
        }
    }
    
    suspend fun clearHistory() {
        context.historyDataStore.edit { preferences ->
            preferences[historyKey] = "[]"
        }
    }
}
