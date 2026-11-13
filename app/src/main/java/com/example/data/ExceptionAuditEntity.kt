package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Audit-Logged Exception Entity stored locally in Room DB.
 *
 * Records human approval or hold decisions (and transparency justification notes) when a site
 * requisition or lot price diverges from the Certified Baseline.
 * These records provide an accountability trail without mutating the Certified Baseline.
 */
@Entity(tableName = "exception_audit_logs")
data class ExceptionAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lotOrItemTitle: String,
    val areaOrScope: String,
    val baselineVersion: String,
    val aiFlagSummary: String,
    val decisionStatus: String, // "APPROVED_WITH_EXCEPTION" | "HOLD_FOR_INVESTIGATION"
    val justificationNote: String,
    val inferenceOrigin: String, // "ON_DEVICE" | "IN_CLOUD"
    val timestamp: Long = System.currentTimeMillis()
)
