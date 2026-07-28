package com.dhanuk.quickscanpro.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScanResult::class, GeneratedQR::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scanResultDao(): ScanResultDao
    abstract fun generatedQRDao(): GeneratedQRDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add type and is_favorite columns to scan_results
                db.execSQL("ALTER TABLE scan_results ADD COLUMN scan_type TEXT NOT NULL DEFAULT 'unknown'")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")

                // Create generated_qrs table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS generated_qrs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        content TEXT NOT NULL,
                        qr_type TEXT NOT NULL,
                        display_label TEXT NOT NULL DEFAULT '',
                        foreground_color INTEGER NOT NULL DEFAULT 4278190080,
                        background_color INTEGER NOT NULL DEFAULT 4294967295,
                        is_favorite INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scan_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
