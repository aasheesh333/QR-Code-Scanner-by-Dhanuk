package com.dhanuk.quickscanpro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    @ColumnInfo(name = "scan_type")
    val type: String = "unknown",
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
