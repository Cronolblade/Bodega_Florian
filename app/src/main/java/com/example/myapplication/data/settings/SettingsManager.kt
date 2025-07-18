package com.example.myapplication.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {
    private val dataStore = context.dataStore
    companion object {
        val THEME_KEY = stringPreferencesKey("theme_option")
    }
    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
    val theme: Flow<Theme> = dataStore.data.map { preferences ->
        Theme.valueOf(preferences[THEME_KEY] ?: Theme.SYSTEM.name)
    }
}

enum class Theme {
    LIGHT, DARK, SYSTEM
}