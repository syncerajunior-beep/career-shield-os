package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteWalkDefectDao {

    @Query("SELECT * FROM site_walk_defects ORDER BY capturedAt DESC, id DESC")
    fun getAllDefects(): Flow<List<SiteWalkDefectEntity>>

    @Query("SELECT COUNT(*) FROM site_walk_defects WHERE status = 'OPEN'")
    fun observeOpenDefectCount(): Flow<Int>

    @Query("SELECT COALESCE(MAX(pinNumber), 0) FROM site_walk_defects")
    suspend fun getMaxPinNumber(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefect(defect: SiteWalkDefectEntity): Long

    @Query("UPDATE site_walk_defects SET status = :newStatus WHERE id = :defectId")
    suspend fun updateDefectStatus(defectId: Long, newStatus: String)

    @Delete
    suspend fun deleteDefect(defect: SiteWalkDefectEntity)
}
