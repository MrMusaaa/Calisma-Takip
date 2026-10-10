package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {

    @Query("SELECT * FROM work_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay ORDER BY startTimeMillis ASC")
    fun getSessionsInEpochRange(startEpochDay: Long, endEpochDay: Long): Flow<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay ORDER BY startTimeMillis ASC")
    suspend fun getSessionsInEpochRangeOnce(startEpochDay: Long, endEpochDay: Long): List<WorkSession>

    @Query("SELECT * FROM work_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): WorkSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkSession): Long

    @Update
    suspend fun updateSession(session: WorkSession)

    @Delete
    suspend fun deleteSession(session: WorkSession)

    @Query("DELETE FROM work_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}
