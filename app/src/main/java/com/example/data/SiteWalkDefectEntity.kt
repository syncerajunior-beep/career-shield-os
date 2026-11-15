package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity for Daily Site Walk defects (Home & Construction Inspection).
 * Stores immutable real-time capture timestamp, architectural PDF plan coordinates,
 * measured dimensions (e.g. door/window opening vs spec), evidence photo URI,
 * and Hybrid AI findings ("ON_DEVICE", "IN_CLOUD", or "MANUAL").
 */
@Entity(
    tableName = "site_walk_defects",
    indices = [
        Index(value = ["tradeCategory"]),
        Index(value = ["severityLevel"]),
        Index(value = ["status"]),
        Index(value = ["capturedAt"])
    ]
)
data class SiteWalkDefectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pinNumber: Int = 1,
    val floorZone: String,
    val tradeCategory: String, // "OPENINGS", "MASONRY", "STRUCTURAL", "MEP_SAFETY"
    val openingCode: String = "", // e.g. "W1", "D2", "C3"
    val measuredInput: String = "", // e.g. "117.5 x 199 cm"
    val inspectorNote: String,
    val aiAnalysis: String = "",
    val severityLevel: String = "MAJOR", // "CRITICAL", "MAJOR", "MINOR"
    val responsibleParty: String = "CONTRACTOR", // "MASONRY", "ALUMINUM", "STRUCTURAL", "MEP", "CONTRACTOR"
    val photoUri: String? = null,
    val pdfPlanUri: String? = null,
    val pdfPageIndex: Int = 0,
    val pinXRatio: Float? = null,
    val pinYRatio: Float? = null,
    val inferenceOrigin: String = "MANUAL", // "ON_DEVICE", "IN_CLOUD", "MANUAL"
    val status: String = "OPEN", // "OPEN", "FIXED"
    val capturedAt: Long = System.currentTimeMillis()
)
