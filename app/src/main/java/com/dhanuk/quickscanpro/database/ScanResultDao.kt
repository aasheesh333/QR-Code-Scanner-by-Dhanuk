package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Insert
    suspend fun insert(scanResult: ScanResult)

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanResult>>

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()
}
