package com.example.todolistapp.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val COMPLETED_TASK_COLOR_ENABLED =
            booleanPreferencesKey("completed_task_color_enabled")
    }

    val completedTaskColorEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[COMPLETED_TASK_COLOR_ENABLED] ?: true
        }

    suspend fun saveCompletedTaskColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[COMPLETED_TASK_COLOR_ENABLED] = enabled
        }
    }
}
