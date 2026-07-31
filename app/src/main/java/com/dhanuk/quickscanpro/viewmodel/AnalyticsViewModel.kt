package com.dhanuk.quickscanpro.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhanuk.quickscanpro.database.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val TAG = "AnalyticsViewModel"

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

    private fun weekAgo(now: Long) = now - TimeUnit.DAYS.toMillis(7)

    private fun dayStart(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val stats = combine(
        scanDao.getTotalCount(),
        scanDao.getAll(),
        scanDao.getCountByType(),
        qrDao.getTotalCount()
    ) { total, allScans, types, generated ->
        val now = System.currentTimeMillis()
        val week = allScans.count { it.timestamp >= weekAgo(now) }
        val today = allScans.count { it.timestamp >= dayStart(now) }
        AnalyticsStats(
            totalScans = total,
            scansThisWeek = week,
            scansToday = today,
            topTypes = types.take(5).map { it.scan_type to it.count },
            totalGeneratedQRs = generated
        )
    }.catch { e ->
        Log.e(TAG, "Stats flow error", e)
        emit(AnalyticsStats(0, 0, 0, emptyList(), 0))
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        AnalyticsStats(0, 0, 0, emptyList(), 0)
    )
}
