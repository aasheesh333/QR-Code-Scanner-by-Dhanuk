package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = getApplication<Application>().appDataStore

    private val vibrateKey = booleanPreferencesKey("vibrate_enabled")
    private val soundKey = booleanPreferencesKey("sound_enabled")
    private val incognitoKey = booleanPreferencesKey("incognito_mode")
    private val autoCopyKey = booleanPreferencesKey("auto_copy_on_scan")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val vibrateEnabled = dataStore.data
        .map { it[vibrateKey] ?: true }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val soundEnabled = dataStore.data
        .map { it[soundKey] ?: true }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val incognitoMode = dataStore.data
        .map { it[incognitoKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val autoCopyOnScan = dataStore.data
        .map { it[autoCopyKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val onboardingCompleted = dataStore.data
        .map { it[onboardingCompletedKey] }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun setVibrate(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[vibrateKey] = enabled } }
    }

    fun setSound(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[soundKey] = enabled } }
    }

    fun setIncognito(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[incognitoKey] = enabled } }
    }

    fun setAutoCopy(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[autoCopyKey] = enabled } }
    }

    fun completeOnboarding() {
        viewModelScope.launch { dataStore.edit { it[onboardingCompletedKey] = true } }
    }
}
