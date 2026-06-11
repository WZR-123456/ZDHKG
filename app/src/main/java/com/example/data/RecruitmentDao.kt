package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecruitmentDao {
    @Query("SELECT * FROM recruitment_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<RecruitmentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecruitmentRecord): Long

    @Delete
    suspend fun deleteRecord(record: RecruitmentRecord)

    @Query("DELETE FROM recruitment_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM recruitment_records")
    suspend fun deleteAllRecords()

    @Query("SELECT COUNT(*) FROM recruitment_records")
    suspend fun getCount(): Int

    @Query("SELECT * FROM recruitment_records ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getOldestRecords(limit: Int): List<RecruitmentRecord>
}
