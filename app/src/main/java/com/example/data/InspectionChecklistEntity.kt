package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Header entity for a construction site inspection checklist.
 * Optionally links to a [RiskAssessmentReportEntity] (SET_NULL on report deletion).
 */
@Entity(
    tableName = "inspection_checklists",
    foreignKeys = [
        ForeignKey(
            entity = RiskAssessmentReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedReportId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["linkedReportId"]),
        Index(value = ["tradeCategory"]),
        Index(value = ["overallStatus"])
    ]
)
data class InspectionChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val checklistCode: String,
    val title: String,
    val tradeCategory: String, // "STRUCTURAL", "SAFETY", "MEP", "FINISHING", "FACADE"
    val siteZone: String,
    val inspectorName: String = "Site Engineer",
    val linkedReportId: Long? = null,
    val overallStatus: String = "PENDING", // "PENDING", "PASSED", "FAILED_HOLD"
    val notes: String = "",
    val inspectedAt: Long = System.currentTimeMillis()
)

/**
 * Individual inspection line-item belonging to an [InspectionChecklistEntity].
 * Automatically deleted when its parent checklist is removed (CASCADE).
 */
@Entity(
    tableName = "inspection_checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = InspectionChecklistEntity::class,
            parentColumns = ["id"],
            childColumns = ["checklistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["checklistId"]),
        Index(value = ["resultStatus"])
    ]
)
data class InspectionChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val checklistId: Long,
    val sequenceOrder: Int,
    val itemTitle: String,
    val standardReference: String = "",
    val resultStatus: String = "PENDING", // "PENDING", "PASS", "FAIL", "NA"
    val isCriticalStopWork: Boolean = false,
    val remarks: String = "",
    val photoUri: String? = null,
    val checkedAt: Long? = null
)

/**
 * 1-to-many Room relation pairing an [InspectionChecklistEntity] with its line items.
 */
data class ChecklistWithItems(
    @Embedded val checklist: InspectionChecklistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "checklistId"
    )
    val items: List<InspectionChecklistItemEntity>
)

/**
 * 1-to-many Room relation pairing a [RiskAssessmentReportEntity] with any linked checklists.
 */
data class ReportWithChecklists(
    @Embedded val report: RiskAssessmentReportEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "linkedReportId"
    )
    val checklists: List<InspectionChecklistEntity>
)
