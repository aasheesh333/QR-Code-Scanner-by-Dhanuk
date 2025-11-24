package com.quickscanpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quickscanpro.database.AppDatabase
import com.quickscanpro.database.ScanResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scanResultDao = AppDatabase.getDatabase(application).scanResultDao()

    val history = scanResultDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun addScanResult(scanResult: ScanResult) {
        // history list is ordered by timestamp DESC (newest first)
        val latest = history.value.firstOrNull()
        if (latest == null || latest.content != scanResult.content) {
            viewModelScope.launch {
                scanResultDao.insert(scanResult)
            }
        }
    }
}
