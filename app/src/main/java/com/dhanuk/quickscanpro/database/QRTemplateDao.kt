package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QRTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: QRTemplate): Long

    @Query("SELECT * FROM qr_templates_history ORDER BY timestamp DESC LIMIT 20")
    fun recent(): Flow<List<QRTemplate>>

    @Query("DELETE FROM qr_templates_history WHERE id = :id")
    suspend fun delete(id: Int)
}
