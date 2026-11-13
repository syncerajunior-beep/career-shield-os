package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. ตารางเก็บข้อมูลเช็กลิสต์หน้างานใน SQLite ของ Android (Room DB ของแท้)
@Entity(tableName = "career_tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "SAFETY", "SLA", "FINANCE", "CONTRACT"
    val timeSlot: String = "08:30",
    val isCompleted: Boolean = false,
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
