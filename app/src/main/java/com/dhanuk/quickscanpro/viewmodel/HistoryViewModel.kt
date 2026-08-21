package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import com.dhanuk.quickscanpro.database.CategoryCount
import com.dhanuk.quickscanpro.database.ScanResult
import com.dhanuk.quickscanpro.util.AutoOrganizer
import com.dhanuk.quickscanpro.util.BarcodeTypeDetector
import com.dhanuk.quickscanpro.util.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "HistoryViewModel"

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val scanResultDao = db.scanResultDao()
    private val collectionDao = db.scanCollectionDao()
    private val leakDao = db.leakCheckDao()
    private val calendarDao = db.calendarEventDao()
    private val templateDao = db.qrTemplateDao()

    val leakChecks = leakDao.all()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val calendarEvents = calendarDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentTemplates = templateDao.recent()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveLeakCheck(domain: String, leaked: Boolean, breachCount: Int, firstSeen: Long) {
        viewModelScope.launch {
            leakDao.upsert(
                com.dhanuk.quickscanpro.database.LeakCheck(
                    domain = domain, leaked = leaked,
                    breachCount = breachCount, firstSeen = firstSeen
                )
            )
        }
    }

    fun saveCalendarEvent(event: com.dhanuk.quickscanpro.database.CalendarEvent) {
        viewModelScope.launch { calendarDao.insert(event) }
    }

    fun saveTemplate(template: com.dhanuk.quickscanpro.database.QRTemplate) {
        viewModelScope.launch { templateDao.insert(template) }
    }

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

    private val autoFavsQuery: Flow<Triple<String, Boolean, String?>> = combine(
        searchQuery, showFavoritesOnly, selectedAutoCategory
    ) { q, fav, auto -> Triple<String, Boolean, String?>(q, fav, auto) }

    private val typeCollection: Flow<Pair<String?, Int?>> = combine(
        selectedType, selectedCollectionId
    ) { t, c -> Pair(t, c) }

    val filteredHistory: StateFlow<List<ScanResult>> = scanResultDao.getAll().combine(
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

    fun delete(id: Int, context: Context? = null) {
        viewModelScope.launch {
            scanResultDao.delete(id)
            context?.let { ReminderScheduler.cancel(it, id) }
        }
    }

    fun restore(scanResult: ScanResult) {
        viewModelScope.launch {
            scanResultDao.insert(scanResult)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            scanResultDao.deleteAll()
        }
    }

    // ─── Batch (grouped bulk scan) ───

    /** Saves a bulk-scan session as one history batch (single grouped row). */
    fun saveBatch(items: List<ScanResult>) {
        if (items.isEmpty()) return
        val batchId = "batch-${System.currentTimeMillis()}"
        viewModelScope.launch {
            try {
                items.forEach { raw ->
                    val detectedType = BarcodeTypeDetector.detectType(raw.content)
                    val autoCat = AutoOrganizer.categorize(detectedType, raw.content)
                    scanResultDao.insert(
                        raw.copy(type = detectedType, autoCategory = autoCat, batchId = batchId)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save batch", e)
            }
        }
    }

    fun batchItems(batchId: String): Flow<List<ScanResult>> = scanResultDao.getBatch(batchId)

    fun setHidden(scanResult: ScanResult, hidden: Boolean) {
        viewModelScope.launch { scanResultDao.setHidden(scanResult.id, hidden) }
    }

    fun setBatchHidden(batchId: String, hidden: Boolean) {
        viewModelScope.launch { scanResultDao.setBatchHidden(batchId, hidden) }
    }

    fun deleteBatch(batchId: String) {
        viewModelScope.launch { scanResultDao.deleteBatch(batchId) }
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
    private var lastSavedTime: Long = 0L

    /** Saves a scan directly as a vaulted item (used from the result screen). */
    fun saveAsVaulted(content: String) {
        val detectedType = BarcodeTypeDetector.detectType(content)
        val autoCat = AutoOrganizer.categorize(detectedType, content)
        val enriched = ScanResult(content = content, type = detectedType, autoCategory = autoCat, isVault = true)

        lastSavedContent = content
        lastSavedTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                scanResultDao.insert(enriched)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert vaulted scan result", e)
            }
        }
    }

    fun addScanResult(scanResult: ScanResult) {
        val detectedType = BarcodeTypeDetector.detectType(scanResult.content)
        val autoCat = AutoOrganizer.categorize(detectedType, scanResult.content)
        val enriched = scanResult.copy(type = detectedType, autoCategory = autoCat)

        val now = System.currentTimeMillis()
        val isRapidDup = enriched.content == lastSavedContent &&
            (now - lastSavedTime) < 2500L
        if (isRapidDup) return

        val latest = history.value.firstOrNull()
        val isLatestDup = latest != null && latest.content == enriched.content &&
            (now - latest.timestamp) < 2500L
        if (isLatestDup) return

        lastSavedContent = enriched.content
        lastSavedTime = now

        viewModelScope.launch {
            try {
                scanResultDao.insert(enriched)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert scan result", e)
            }
        }
    }
}
