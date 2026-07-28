package com.dhanuk.quickscanpro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_qrs")
data class GeneratedQR(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    @ColumnInfo(name = "qr_type")
    val type: String,
    @ColumnInfo(name = "display_label")
    val displayLabel: String = "",
    @ColumnInfo(name = "foreground_color")
    val foregroundColor: Long = 0xFF000000,
    @ColumnInfo(name = "background_color")
    val backgroundColor: Long = 0xFFFFFFFF,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
