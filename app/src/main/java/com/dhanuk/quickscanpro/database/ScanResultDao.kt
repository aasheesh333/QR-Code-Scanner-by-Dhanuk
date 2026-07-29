package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(scanResult: ScanResult): Long

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE is_favorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE scan_type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ScanResult>>

    @Query("SELECT COUNT(*) FROM scan_results")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT scan_type, COUNT(*) as count FROM scan_results GROUP BY scan_type ORDER BY count DESC")
    fun getCountByType(): Flow<List<TypeCount>>

    @Query("SELECT COUNT(*) FROM scan_results WHERE timestamp >= :since")
    fun getCountSince(since: Long): Flow<Int>

    @Update
    suspend fun update(scanResult: ScanResult)

    @Query("UPDATE scan_results SET is_favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Int, favorite: Boolean)

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()
}

data class TypeCount(
    val scan_type: String,
    val count: Int
)
