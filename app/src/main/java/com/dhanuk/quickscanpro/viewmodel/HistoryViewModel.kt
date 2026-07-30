package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import com.dhanuk.quickscanpro.database.CategoryCount
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.util.AutoOrganizer
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val scanResultDao = db.scanResultDao()
    private val collectionDao = db.scanCollectionDao()

    val collections = collectionDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val searchQuery = MutableStateFlow("")
    private val selectedType = MutableStateFlow<String?>(null)
    private val showFavoritesOnly = MutableStateFlow(false)
    private val selectedCollectionId = MutableStateFlow<Int?>(null)
    private val selectedAutoCategory = MutableStateFlow<String?>(null)

    val history = scanResultDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val vaultScans = scanResultDao.getVaultScans()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val autoCategoryCounts = scanResultDao.getCountByAutoCategory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val autoFavsQuery: Flow<Triple<String, Boolean, String>> = combine(
        searchQuery, showFavoritesOnly, selectedAutoCategory
    ) { q, fav, auto -> Triple(q, fav, auto) }

    private val typeCollection: Flow<Pair<String?, Int?>> = combine(
        selectedType, selectedCollectionId
    ) { t, c -> Pair(t, c) }

    val filteredHistory: Flow<List<ScanResult>> = scanResultDao.getAll().combine(
        autoFavsQuery.combine(typeCollection) { a, b -> Pair(a, b) }
    ) { all, (autoFavs, typeColl) ->
        val (q, favsOnly, autoCat) = autoFavs
        val (type, collectionId) = typeColl
        var filtered = all

        if (autoCat != null) {
            filtered = filtered.filter { it.autoCategory == autoCat }
        }
        if (collectionId != null) {
            filtered = filtered.filter { it.collectionId == collectionId }
        }
        if (favsOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }
        if (q.isNotBlank()) {
            filtered = filtered.filter {
                it.content.contains(q, ignoreCase = true) ||
                        it.note.contains(q, ignoreCase = true)
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

    fun setSelectedCollection(id: Int?) {
        selectedCollectionId.value = id
    }

    fun setSelectedAutoCategory(category: String?) {
        selectedAutoCategory.value = category
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

    fun deleteAllVault() {
        viewModelScope.launch { scanResultDao.deleteAllVault() }
    }

    fun setNote(id: Int, note: String) {
        viewModelScope.launch { scanResultDao.setNote(id, note) }
    }

    fun setCollection(id: Int, collectionId: Int?) {
        viewModelScope.launch { scanResultDao.setCollection(id, collectionId) }
    }

    fun setVault(scanResult: ScanResult, vault: Boolean, context: Context) {
        viewModelScope.launch {
            scanResultDao.setVault(scanResult.id, vault)
            if (!vault) ReminderScheduler.cancel(context, scanResult.id)
        }
    }

    fun setReminder(scanResult: ScanResult, time: Long?, context: Context) {
        viewModelScope.launch {
            scanResultDao.setReminder(scanResult.id, time)
            if (time != null && time > System.currentTimeMillis()) {
                ReminderScheduler.schedule(
                    context, scanResult.id, scanResult.content, time
                )
            } else {
                ReminderScheduler.cancel(context, scanResult.id)
            }
        }
    }

    fun setTranslatedText(id: Int, text: String) {
        viewModelScope.launch { scanResultDao.setTranslatedText(id, text) }
    }

    fun addCollection(name: String, color: Long, emoji: String = "") {
        viewModelScope.launch {
            collectionDao.insert(
                com.dhanuk.quickscanpro.database.ScanCollection(
                    name = name, color = color, emoji = emoji
                )
            )
        }
    }

    fun deleteCollection(id: Int) {
        viewModelScope.launch { collectionDao.delete(id) }
    }

    private var lastSavedContent: String? = null

    fun addScanResult(scanResult: ScanResult) {
        val detectedType = BarcodeTypeDetector.detectType(scanResult.content)
        val autoCat = AutoOrganizer.categorize(detectedType, scanResult.content)
        val enriched = scanResult.copy(type = detectedType, autoCategory = autoCat)

        // In-memory guard against rapid duplicate inserts (e.g. camera firing
        // multiple times before the Room flow emits the new row).
        if (enriched.content == lastSavedContent) return
        val latest = history.value.firstOrNull()
        if (latest != null && latest.content == enriched.content) {
            lastSavedContent = enriched.content
            return
        }
        lastSavedContent = enriched.content
        viewModelScope.launch {
            scanResultDao.insert(enriched)
        }
    }
}
