package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for construction risk assessment reports and relational inspection checklists.
 */
@Dao
interface InspectionDao {

    // --- Construction Risk Assessment Reports ---

    @Query("SELECT * FROM risk_assessment_reports ORDER BY createdAt DESC, id DESC")
    fun getAllRiskReports(): Flow<List<RiskAssessmentReportEntity>>

    @Query(
        """
        SELECT * FROM risk_assessment_reports
        WHERE (:reportType = 'ALL' OR UPPER(reportType) = UPPER(:reportType))
          AND (:severityLevel = 'ALL' OR UPPER(severityLevel) = UPPER(:severityLevel))
          AND (:status = 'ALL' OR UPPER(status) = UPPER(:status))
          AND (
              :searchQuery = ''
              OR siteZone LIKE '%' || :searchQuery || '%'
              OR hazardSummary LIKE '%' || :searchQuery || '%'
              OR assessmentFindings LIKE '%' || :searchQuery || '%'
              OR reportCode LIKE '%' || :searchQuery || '%'
          )
        ORDER BY createdAt DESC, id DESC
        """
    )
    fun getFilteredRiskReports(
        reportType: String,
        severityLevel: String,
        status: String,
        searchQuery: String
    ): Flow<List<RiskAssessmentReportEntity>>

    @Query("SELECT * FROM risk_assessment_reports WHERE id = :reportId")
    fun getRiskReportById(reportId: Long): Flow<RiskAssessmentReportEntity?>

    @Transaction
    @Query("SELECT * FROM risk_assessment_reports ORDER BY createdAt DESC, id DESC")
    fun getReportsWithChecklists(): Flow<List<ReportWithChecklists>>

    @Query("SELECT COUNT(*) FROM risk_assessment_reports")
    suspend fun getRiskReportCount(): Int

    @Query(
        "SELECT COUNT(*) FROM risk_assessment_reports WHERE status IN ('OPEN', 'STOP_WORK') AND severityLevel = 'CRITICAL'"
    )
    fun observeOpenCriticalReportCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskReport(report: RiskAssessmentReportEntity): Long

    @Update
    suspend fun updateRiskReport(report: RiskAssessmentReportEntity)

    @Delete
    suspend fun deleteRiskReport(report: RiskAssessmentReportEntity)

    @Query(
        "UPDATE risk_assessment_reports SET status = :newStatus, updatedAt = :updatedAt WHERE id = :reportId"
    )
    suspend fun updateRiskReportStatus(
        reportId: Long,
        newStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    // --- Inspection Checklists & Items ---

    @Transaction
    @Query("SELECT * FROM inspection_checklists ORDER BY inspectedAt DESC, id DESC")
    fun getAllChecklistsWithItems(): Flow<List<ChecklistWithItems>>

    @Transaction
    @Query("SELECT * FROM inspection_checklists WHERE id = :checklistId")
    fun getChecklistWithItemsById(checklistId: Long): Flow<ChecklistWithItems?>

    @Transaction
    @Query("SELECT * FROM inspection_checklists WHERE tradeCategory = :tradeCategory ORDER BY inspectedAt DESC, id DESC")
    fun getChecklistsWithItemsByTrade(tradeCategory: String): Flow<List<ChecklistWithItems>>

    @Query("SELECT * FROM inspection_checklist_items WHERE checklistId = :checklistId ORDER BY sequenceOrder ASC, id ASC")
    suspend fun getItemsForChecklistOnce(checklistId: Long): List<InspectionChecklistItemEntity>

    @Query("SELECT COUNT(*) FROM inspection_checklists")
    suspend fun getChecklistCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: InspectionChecklistEntity): Long

    @Update
    suspend fun updateChecklist(checklist: InspectionChecklistEntity)

    @Delete
    suspend fun deleteChecklist(checklist: InspectionChecklistEntity)

    @Query("UPDATE inspection_checklists SET overallStatus = :overallStatus, inspectedAt = :inspectedAt WHERE id = :checklistId")
    suspend fun updateChecklistOverallStatus(
        checklistId: Long,
        overallStatus: String,
        inspectedAt: Long = System.currentTimeMillis()
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<InspectionChecklistItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: InspectionChecklistItemEntity): Long

    @Update
    suspend fun updateChecklistItem(item: InspectionChecklistItemEntity)

    @Delete
    suspend fun deleteChecklistItem(item: InspectionChecklistItemEntity)

    @Query(
        "UPDATE inspection_checklist_items SET resultStatus = :resultStatus, remarks = :remarks, checkedAt = :checkedAt WHERE id = :itemId"
    )
    suspend fun updateChecklistItemResult(
        itemId: Long,
        resultStatus: String,
        remarks: String,
        checkedAt: Long = System.currentTimeMillis()
    )

    @Transaction
    suspend fun insertChecklistWithItems(
        checklist: InspectionChecklistEntity,
        items: List<InspectionChecklistItemEntity>
    ): Long {
        val generatedChecklistId = insertChecklist(checklist)
        val normalizedItems = items.mapIndexed { index, item ->
            item.copy(
                checklistId = generatedChecklistId,
                sequenceOrder = if (item.sequenceOrder <= 0) index + 1 else item.sequenceOrder
            )
        }
        if (normalizedItems.isNotEmpty()) {
            insertChecklistItems(normalizedItems)
        }
        return generatedChecklistId
    }
}
