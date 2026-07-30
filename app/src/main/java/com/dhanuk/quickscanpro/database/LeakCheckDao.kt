package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeakCheckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(check: LeakCheck): Long

    @Query("SELECT * FROM leak_checks WHERE domain = :domain LIMIT 1")
    suspend fun get(domain: String): LeakCheck?

    @Query("SELECT * FROM leak_checks ORDER BY checked_at DESC")
    fun all(): Flow<List<LeakCheck>>

    @Query("DELETE FROM leak_checks WHERE id = :id")
    suspend fun delete(id: Int)
}
