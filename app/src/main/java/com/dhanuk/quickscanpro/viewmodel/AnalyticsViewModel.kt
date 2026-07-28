package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit

data class AnalyticsStats(
    val totalScans: Int,
    val scansThisWeek: Int,
    val scansToday: Int,
    val topTypes: List<Pair<String, Int>>,
    val totalGeneratedQRs: Int
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val scanDao = db.scanResultDao()
    private val qrDao = db.generatedQRDao()

    val stats = combine(
        scanDao.getTotalCount(),
        scanDao.getCountSince(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)),
        scanDao.getCountSince(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)),
        scanDao.getCountByType(),
        qrDao.getTotalCount()
    ) { total, week, today, types, generated ->
        AnalyticsStats(
            totalScans = total,
            scansThisWeek = week,
            scansToday = today,
            topTypes = types.take(5).map { it.scan_type to it.count },
            totalGeneratedQRs = generated
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        AnalyticsStats(0, 0, 0, emptyList(), 0)
    )
}
