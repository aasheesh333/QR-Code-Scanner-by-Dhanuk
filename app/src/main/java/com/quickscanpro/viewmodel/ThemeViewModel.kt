package com.quickscanpro.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import android.content.Context
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = getApplication<Application>().dataStore
    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")

    val isDarkTheme = dataStore.data
        .map { preferences ->
            preferences[isDarkThemeKey] ?: false
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings[isDarkThemeKey] = isDark
            }
        }
    }
}
