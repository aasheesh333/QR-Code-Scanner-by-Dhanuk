package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent): Long

    @Query("SELECT * FROM calendar_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CalendarEvent>>

    @Query("UPDATE calendar_events SET imported = 1 WHERE id = :id")
    suspend fun markImported(id: Int)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun delete(id: Int)
}
