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

    @Query("SELECT * FROM scan_results WHERE is_vault = 0 AND is_hidden = 0 ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanResult>>

    /** Vault shows both new vaulted rows and legacy rows hidden before hide-to-vault sync. */
    @Query("SELECT * FROM scan_results WHERE is_vault = 1 OR is_hidden = 1 ORDER BY timestamp DESC")
    fun getVaultScans(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE is_favorite = 1 AND is_vault = 0 AND is_hidden = 0 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE scan_type = :type AND is_vault = 0 AND is_hidden = 0 ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE content LIKE '%' || :query || '%' AND is_vault = 0 AND is_hidden = 0 ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ScanResult>>

    @Query("SELECT COUNT(*) FROM scan_results WHERE is_vault = 0 AND is_hidden = 0")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT scan_type, COUNT(*) as count FROM scan_results WHERE is_vault = 0 AND is_hidden = 0 GROUP BY scan_type ORDER BY count DESC")
    fun getCountByType(): Flow<List<TypeCount>>

    @Query("SELECT COUNT(*) FROM scan_results WHERE timestamp >= :since AND is_vault = 0 AND is_hidden = 0")
    fun getCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_results WHERE is_vault = 1 OR is_hidden = 1")
    fun getVaultCount(): Flow<Int>

    @Query("SELECT auto_category, COUNT(*) as count FROM scan_results WHERE is_vault = 0 AND auto_category != '' AND is_hidden = 0 GROUP BY auto_category ORDER BY count DESC")
    fun getCountByAutoCategory(): Flow<List<CategoryCount>>

    @Query("SELECT * FROM scan_results WHERE reminder_time IS NOT NULL AND reminder_time > 0 ORDER BY reminder_time ASC")
    fun getReminders(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_results WHERE reminder_time IS NOT NULL AND reminder_time > :now ORDER BY reminder_time ASC")
    suspend fun getPendingReminders(now: Long): List<ScanResult>

    @Update
    suspend fun update(scanResult: ScanResult)

    @Query("UPDATE scan_results SET is_favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Int, favorite: Boolean)

    @Query("UPDATE scan_results SET note = :note WHERE id = :id")
    suspend fun setNote(id: Int, note: String)

    @Query("UPDATE scan_results SET collection_id = :collectionId WHERE id = :id")
    suspend fun setCollection(id: Int, collectionId: Int?)

    /** Vaulting always hides from normal history; unvaulting restores it back. */
    @Query("UPDATE scan_results SET is_vault = :vault, is_hidden = :vault WHERE id = :id")
    suspend fun setVault(id: Int, vault: Boolean)

    @Query("UPDATE scan_results SET reminder_time = :time WHERE id = :id")
    suspend fun setReminder(id: Int, time: Long?)

    @Query("UPDATE scan_results SET auto_category = :category WHERE id = :id")
    suspend fun setAutoCategory(id: Int, category: String)

    @Query("UPDATE scan_results SET translated_text = :text WHERE id = :id")
    suspend fun setTranslatedText(id: Int, text: String)

    @Query("SELECT * FROM scan_results WHERE collection_id = :collectionId AND is_vault = 0 AND is_hidden = 0 ORDER BY timestamp DESC")
    fun getByCollection(collectionId: Int): Flow<List<ScanResult>>

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM scan_results WHERE is_vault = 0 AND is_hidden = 0")
    suspend fun deleteAll()

    @Query("DELETE FROM scan_results")
    suspend fun deleteEverything()

    @Query("DELETE FROM scan_results WHERE is_vault = 1 OR is_hidden = 1")
    suspend fun deleteAllVault()

    // ─── Batch (grouped bulk scan) ───

    /** Full batch; History renders visible members while Vault renders protected members. */
    @Query("SELECT * FROM scan_results WHERE batch_id = :batchId ORDER BY timestamp ASC")
    fun getBatch(batchId: String): Flow<List<ScanResult>>

    /** Eye/hide action means "hide into Vault"; unhide restores to normal history. */
    @Query("UPDATE scan_results SET is_hidden = :hidden, is_vault = :hidden WHERE id = :id")
    suspend fun setHidden(id: Int, hidden: Boolean)

    @Query("UPDATE scan_results SET is_hidden = :hidden, is_vault = :hidden WHERE batch_id = :batchId")
    suspend fun setBatchHidden(batchId: String, hidden: Boolean)

    @Query("DELETE FROM scan_results WHERE batch_id = :batchId")
    suspend fun deleteBatch(batchId: String)
}

data class TypeCount(
    val scan_type: String,
    val count: Int
)

data class CategoryCount(
    val auto_category: String,
    val count: Int
)
