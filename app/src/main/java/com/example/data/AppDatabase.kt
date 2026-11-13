package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 3. ตัวเชื่อมฐานข้อมูล Room Database
@Database(
    entities = [TaskEntity::class, ScriptEntity::class, ExceptionAuditEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE career_tasks ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exception_audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lotOrItemTitle TEXT NOT NULL,
                        areaOrScope TEXT NOT NULL,
                        baselineVersion TEXT NOT NULL,
                        aiFlagSummary TEXT NOT NULL,
                        decisionStatus TEXT NOT NULL,
                        justificationNote TEXT NOT NULL,
                        inferenceOrigin TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "career_shield_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
