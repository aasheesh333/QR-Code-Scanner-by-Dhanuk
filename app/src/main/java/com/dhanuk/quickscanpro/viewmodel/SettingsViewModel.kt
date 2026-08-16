package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private val biometricLockKey = booleanPreferencesKey("biometric_lock_enabled")
    private val vaultLockModeKey = stringPreferencesKey("vault_lock_mode")
    private val vaultPinKey = stringPreferencesKey("vault_pin")
    private val scanHistoryKey = booleanPreferencesKey("scan_history_enabled")
    private val defaultActionKey = stringPreferencesKey("default_scan_action")
    private val themePrimaryIdxKey = intPreferencesKey("theme_primary_index")
    private val themeSecondaryIdxKey = intPreferencesKey("theme_secondary_index")
    private val themeAccentIdxKey = intPreferencesKey("theme_accent_index")

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
        .map { it[onboardingCompletedKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val biometricLock = dataStore.data
        .map { it[biometricLockKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    /** Vault lock mode: "none", "pin" or "biometric". Migrates the old boolean flag. */
    val vaultLockMode = dataStore.data
        .map { it[vaultLockModeKey] ?: if (it[biometricLockKey] == true) "biometric" else "none" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "none")

    val vaultPin = dataStore.data
        .map { it[vaultPinKey] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val scanHistory = dataStore.data
        .map { it[scanHistoryKey] ?: true }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val defaultAction = dataStore.data
        .map { it[defaultActionKey] ?: "show_result" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "show_result")

    val themePrimaryIndex = dataStore.data
        .map { it[themePrimaryIdxKey] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val themeSecondaryIndex = dataStore.data
        .map { it[themeSecondaryIdxKey] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val themeAccentIndex = dataStore.data
        .map { it[themeAccentIdxKey] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

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

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[biometricLockKey] = enabled } }
    }

    fun setVaultLockMode(mode: String) {
        viewModelScope.launch { dataStore.edit { it[vaultLockModeKey] = mode } }
    }

    fun setVaultPin(pin: String) {
        viewModelScope.launch {
            dataStore.edit {
                it[vaultPinKey] = pin
                if (pin.isNotBlank()) it[vaultLockModeKey] = "pin"
            }
        }
    }

    fun setScanHistory(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[scanHistoryKey] = enabled } }
    }

    fun setDefaultAction(action: String) {
        viewModelScope.launch { dataStore.edit { it[defaultActionKey] = action } }
    }

    fun setThemePrimaryIndex(index: Int) {
        viewModelScope.launch { dataStore.edit { it[themePrimaryIdxKey] = index } }
    }

    fun setThemeSecondaryIndex(index: Int) {
        viewModelScope.launch { dataStore.edit { it[themeSecondaryIdxKey] = index } }
    }

    fun setThemeAccentIndex(index: Int) {
        viewModelScope.launch { dataStore.edit { it[themeAccentIdxKey] = index } }
    }
}
