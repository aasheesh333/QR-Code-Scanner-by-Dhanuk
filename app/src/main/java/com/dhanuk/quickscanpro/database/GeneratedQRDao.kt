package com.dhanuk.quickscanpro.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedQRDao {

    @Insert
    suspend fun insert(generatedQR: GeneratedQR)

    @Query("SELECT * FROM generated_qrs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<GeneratedQR>>

    @Query("SELECT * FROM generated_qrs WHERE is_favorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<GeneratedQR>>

    @Query("SELECT * FROM generated_qrs WHERE qr_type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<GeneratedQR>>

    @Query("SELECT COUNT(*) FROM generated_qrs")
    fun getTotalCount(): Flow<Int>

    @Update
    suspend fun update(generatedQR: GeneratedQR)

    @Query("UPDATE generated_qrs SET is_favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Int, favorite: Boolean)

    @Delete
    suspend fun delete(generatedQR: GeneratedQR)

    @Query("DELETE FROM generated_qrs WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM generated_qrs")
    suspend fun deleteAll()
}
