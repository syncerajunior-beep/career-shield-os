package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ตารางเก็บสคริปต์ SOS เอาตัวรอดและหนังสือแจ้งเตือนใน SQLite ของ Android
@Entity(tableName = "sos_scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "CONTRACT", "EOT_LD", "SLA", "SAFETY"
    val riskLevel: String = "CRITICAL",
    val timestamp: Long = System.currentTimeMillis()
)
