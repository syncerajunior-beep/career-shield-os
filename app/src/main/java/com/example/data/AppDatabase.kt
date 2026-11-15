package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 3. ตัวเชื่อมฐานข้อมูล Room Database
@Database(
    entities = [
        TaskEntity::class,
        ScriptEntity::class,
        ExceptionAuditEntity::class,
        RiskAssessmentReportEntity::class,
        InspectionChecklistEntity::class,
        InspectionChecklistItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun inspectionDao(): InspectionDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS risk_assessment_reports (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reportCode TEXT NOT NULL,
                        projectName TEXT NOT NULL,
                        siteZone TEXT NOT NULL,
                        reportType TEXT NOT NULL,
                        severityLevel TEXT NOT NULL,
                        hazardSummary TEXT NOT NULL,
                        assessmentFindings TEXT NOT NULL,
                        mitigationPlan TEXT NOT NULL,
                        inspectorName TEXT NOT NULL,
                        photoUri TEXT,
                        inferenceOrigin TEXT NOT NULL,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_reportType ON risk_assessment_reports (reportType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_severityLevel ON risk_assessment_reports (severityLevel)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_status ON risk_assessment_reports (status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_createdAt ON risk_assessment_reports (createdAt)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inspection_checklists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        checklistCode TEXT NOT NULL,
                        title TEXT NOT NULL,
                        tradeCategory TEXT NOT NULL,
                        siteZone TEXT NOT NULL,
                        inspectorName TEXT NOT NULL,
                        linkedReportId INTEGER,
                        overallStatus TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        inspectedAt INTEGER NOT NULL,
                        FOREIGN KEY(linkedReportId) REFERENCES risk_assessment_reports(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklists_linkedReportId ON inspection_checklists (linkedReportId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklists_tradeCategory ON inspection_checklists (tradeCategory)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklists_overallStatus ON inspection_checklists (overallStatus)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inspection_checklist_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        checklistId INTEGER NOT NULL,
                        sequenceOrder INTEGER NOT NULL,
                        itemTitle TEXT NOT NULL,
                        standardReference TEXT NOT NULL,
                        resultStatus TEXT NOT NULL,
                        isCriticalStopWork INTEGER NOT NULL,
                        remarks TEXT NOT NULL,
                        photoUri TEXT,
                        checkedAt INTEGER,
                        FOREIGN KEY(checklistId) REFERENCES inspection_checklists(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklist_items_checklistId ON inspection_checklist_items (checklistId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklist_items_resultStatus ON inspection_checklist_items (resultStatus)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "career_shield_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
