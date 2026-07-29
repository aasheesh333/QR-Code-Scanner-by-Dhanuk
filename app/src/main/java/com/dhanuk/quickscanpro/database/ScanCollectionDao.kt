package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: ScanCollection): Long

    @Query("SELECT * FROM scan_collections ORDER BY timestamp ASC")
    fun getAll(): Flow<List<ScanCollection>>

    @Query("DELETE FROM scan_collections WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT COUNT(*) FROM scan_collections")
    fun getCount(): Flow<Int>
}
