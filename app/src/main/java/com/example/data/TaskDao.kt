package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 2. คำสั่งจัดการฐานข้อมูล (DAO) สำหรับทั้ง TaskEntity และ ScriptEntity
@Dao
interface TaskDao {
    @Query("SELECT * FROM career_tasks ORDER BY timeSlot ASC, id ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM career_tasks
        WHERE (:category = 'ALL' OR UPPER(category) = UPPER(:category))
          AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY timeSlot ASC, id ASC
        """
    )
    fun getFilteredTasks(category: String, searchQuery: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM career_tasks")
    suspend fun getTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE career_tasks SET isCompleted = 0")
    suspend fun resetAllTasks()

    // คำสั่งสำหรับ SOS Scripts
    @Query("SELECT * FROM sos_scripts ORDER BY id ASC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT COUNT(*) FROM sos_scripts")
    suspend fun getScriptCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllScripts(scripts: List<ScriptEntity>)

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Delete
    suspend fun deleteScript(script: ScriptEntity)

    // คำสั่งสำหรับ Exception Audit Log (หลักฐานอนุมัติข้อยกเว้น แยกจาก Certified Baseline)
    @Query("SELECT * FROM exception_audit_logs ORDER BY timestamp DESC, id DESC")
    fun getAllExceptionAudits(): Flow<List<ExceptionAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExceptionAudit(audit: ExceptionAuditEntity)

    @Delete
    suspend fun deleteExceptionAudit(audit: ExceptionAuditEntity)
}
