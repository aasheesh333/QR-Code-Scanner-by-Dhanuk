package com.dhanuk.quickscanpro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_templates_history")
data class QRTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "template_key") val templateKey: String,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "qr_type") val qrType: String,
    val timestamp: Long = System.currentTimeMillis()
)
