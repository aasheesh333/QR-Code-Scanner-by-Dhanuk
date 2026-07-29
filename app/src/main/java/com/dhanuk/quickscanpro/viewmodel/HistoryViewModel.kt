package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scanResultDao = AppDatabase.getDatabase(application).scanResultDao()

    private val searchQuery = MutableStateFlow("")
    private val selectedType = MutableStateFlow<String?>(null)
    private val showFavoritesOnly = MutableStateFlow(false)

    val history = scanResultDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredHistory = combine(
        scanResultDao.getAll(),
        searchQuery.asStateFlow(),
        selectedType.asStateFlow(),
        showFavoritesOnly.asStateFlow()
    ) { all, query, type, favsOnly ->
        var filtered = all

        if (favsOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.content.contains(query, ignoreCase = true)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedType(type: String?) {
        selectedType.value = type
    }

    fun setShowFavoritesOnly(show: Boolean) {
        showFavoritesOnly.value = show
    }

    fun toggleFavorite(scanResult: ScanResult) {
        viewModelScope.launch {
            scanResultDao.setFavorite(scanResult.id, !scanResult.isFavorite)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            scanResultDao.delete(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            scanResultDao.deleteAll()
        }
    }

    private var lastSavedContent: String? = null

    fun addScanResult(scanResult: ScanResult) {
        val detectedType = BarcodeTypeDetector.detectType(scanResult.content)
        val withType = scanResult.copy(type = detectedType)

        // In-memory guard against rapid duplicate inserts (e.g. camera firing
        // multiple times before the Room flow emits the new row).
        if (withType.content == lastSavedContent) return
        val latest = history.value.firstOrNull()
        if (latest != null && latest.content == withType.content) {
            lastSavedContent = withType.content
            return
        }
        lastSavedContent = withType.content
        viewModelScope.launch {
            scanResultDao.insert(withType)
        }
    }
}
