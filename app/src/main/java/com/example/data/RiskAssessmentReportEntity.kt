package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity storing construction risk assessment reports in local SQLite.
 *
 * Captures hazard details, assessment findings, corrective mitigation plans,
 * optional evidence photo URI, and inference origin ("ON_DEVICE", "IN_CLOUD", or "MANUAL").
 */
@Entity(
    tableName = "risk_assessment_reports",
    indices = [
        Index(value = ["reportType"]),
        Index(value = ["severityLevel"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class RiskAssessmentReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val reportCode: String,
    val projectName: String = "Career Shield Site",
    val siteZone: String,
    val reportType: String, // "SITE_SAFETY", "CONTRACT_LD", "MATERIAL_AUDIT", "PHOTO_INSPECTION"
    val severityLevel: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val hazardSummary: String,
    val assessmentFindings: String,
    val mitigationPlan: String,
    val inspectorName: String = "Site Risk Manager",
    val photoUri: String? = null,
    val inferenceOrigin: String = "MANUAL", // "ON_DEVICE", "IN_CLOUD", "MANUAL"
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "MITIGATED", "STOP_WORK"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
