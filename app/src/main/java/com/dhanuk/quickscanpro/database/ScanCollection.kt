package com.dhanuk.quickscanpro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** User-defined folder for organizing scans — a feature most
 *  scanner apps don't offer. */
@Entity(tableName = "scan_collections")
data class ScanCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "color")
    val color: Long = 0xFF700B97,
    @ColumnInfo(name = "emoji", defaultValue = "")
    val emoji: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
