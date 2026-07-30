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
    @ColumnInfo(name = "note", defaultValue = "")
    val note: String = "",
    @ColumnInfo(name = "collection_id")
    val collectionId: Int? = null,
    @ColumnInfo(name = "is_vault", defaultValue = "0")
    val isVault: Boolean = false,
    @ColumnInfo(name = "reminder_time")
    val reminderTime: Long? = null,
    @ColumnInfo(name = "auto_category", defaultValue = "")
    val autoCategory: String = "",
    @ColumnInfo(name = "translated_text", defaultValue = "")
    val translatedText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
