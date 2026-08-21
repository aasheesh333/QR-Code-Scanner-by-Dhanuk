package com.dhanuk.quickscanpro.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ScanResult::class, GeneratedQR::class, ScanCollection::class,
        CalendarEvent::class, QRTemplate::class, LeakCheck::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scanResultDao(): ScanResultDao
    abstract fun generatedQRDao(): GeneratedQRDao
    abstract fun scanCollectionDao(): ScanCollectionDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun qrTemplateDao(): QRTemplateDao
    abstract fun leakCheckDao(): LeakCheckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN scan_type TEXT NOT NULL DEFAULT 'unknown'")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN collection_id INTEGER")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS scan_collections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER NOT NULL DEFAULT 4286644119,
                        emoji TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN is_vault INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN reminder_time INTEGER")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN auto_category TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN translated_text TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS calendar_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        location TEXT NOT NULL DEFAULT '',
                        start_time INTEGER NOT NULL,
                        end_time INTEGER,
                        source_content TEXT NOT NULL DEFAULT '',
                        imported INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS qr_templates_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        template_key TEXT NOT NULL,
                        label TEXT NOT NULL,
                        content TEXT NOT NULL,
                        qr_type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS leak_checks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        domain TEXT NOT NULL,
                        leaked INTEGER NOT NULL DEFAULT 0,
                        breach_count INTEGER NOT NULL DEFAULT 0,
                        first_seen INTEGER NOT NULL DEFAULT 0,
                        checked_at INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN batch_id TEXT")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN is_hidden INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scan_database"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
