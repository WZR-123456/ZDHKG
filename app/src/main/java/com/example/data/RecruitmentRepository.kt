package com.example.data

import kotlinx.coroutines.flow.Flow

class RecruitmentRepository(private val dao: RecruitmentDao) {
    val allRecords: Flow<List<RecruitmentRecord>> = dao.getAllRecords()

    suspend fun insert(record: RecruitmentRecord) {
        // Enforce the 50 record capacity limit
        val currentCount = dao.getCount()
        if (currentCount >= 50) {
            val toDeleteCount = (currentCount - 50) + 1
            if (toDeleteCount > 0) {
                val oldest = dao.getOldestRecords(toDeleteCount)
                for (old in oldest) {
                    dao.deleteRecord(old)
                }
            }
        }
        dao.insertRecord(record)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteRecordById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAllRecords()
    }
}
