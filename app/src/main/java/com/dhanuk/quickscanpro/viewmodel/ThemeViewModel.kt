package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = getApplication<Application>().appDataStore

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")

    val themeMode = dataStore.data
        .map { preferences ->
            val stored = preferences[themeModeKey]
            try {
                if (stored != null) ThemeMode.valueOf(stored) else ThemeMode.LIGHT
            } catch (e: Exception) {
                ThemeMode.LIGHT
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.LIGHT)

    val isDarkTheme = dataStore.data
        .map { preferences ->
            val mode = try {
                val stored = preferences[themeModeKey]
                if (stored != null) ThemeMode.valueOf(stored) else null
            } catch (e: Exception) { null }
            when (mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.AMOLED -> true
                else -> preferences[isDarkThemeKey] ?: false
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings[themeModeKey] = mode.name
            }
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings[isDarkThemeKey] = isDark
            }
        }
    }
}
