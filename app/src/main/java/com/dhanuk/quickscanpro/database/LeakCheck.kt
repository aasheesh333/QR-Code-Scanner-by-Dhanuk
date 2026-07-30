package com.dhanuk.quickscanpro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leak_checks")
data class LeakCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "domain") val domain: String,
    @ColumnInfo(name = "leaked") val leaked: Boolean,
    @ColumnInfo(name = "breach_count") val breachCount: Int = 0,
    @ColumnInfo(name = "first_seen") val firstSeen: Long = 0L,
    @ColumnInfo(name = "checked_at") val checkedAt: Long = System.currentTimeMillis()
)
