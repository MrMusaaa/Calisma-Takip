package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class WorkRepository(
    private val workSessionDao: WorkSessionDao,
    val preferences: UserPreferences
) {
    val allSessions: Flow<List<WorkSession>> = workSessionDao.getAllSessions()

    fun getSessionsForWeek(startEpochDay: Long, endEpochDay: Long): Flow<List<WorkSession>> {
        return workSessionDao.getSessionsInEpochRange(startEpochDay, endEpochDay)
    }

    suspend fun getSessionsForWeekOnce(startEpochDay: Long, endEpochDay: Long): List<WorkSession> {
        return workSessionDao.getSessionsInEpochRangeOnce(startEpochDay, endEpochDay)
    }

    fun startWork(note: String = "") {
        preferences.isWorking = true
        preferences.activeStartTimeMillis = System.currentTimeMillis()
        preferences.activeNote = note
    }

    suspend fun stopWork(): WorkSession? {
        if (!preferences.isWorking) return null

        val startTime = preferences.activeStartTimeMillis
        val endTime = System.currentTimeMillis()
        val note = preferences.activeNote
        val rate = preferences.hourlyRate

        // Reset active state
        preferences.isWorking = false
        preferences.activeStartTimeMillis = 0L
        preferences.activeNote = ""

        if (endTime <= startTime) return null

        val session = WorkSession(
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            breakMinutes = 0,
            hourlyRate = rate,
            note = note
        )
        val insertedId = workSessionDao.insertSession(session)
        return session.copy(id = insertedId)
    }

    suspend fun insertManualSession(session: WorkSession): Long {
        return workSessionDao.insertSession(session)
    }

    suspend fun updateSession(session: WorkSession) {
        workSessionDao.updateSession(session)
    }

    suspend fun deleteSession(session: WorkSession) {
        workSessionDao.deleteSession(session)
    }

    suspend fun deleteSessionById(id: Long) {
        workSessionDao.deleteSessionById(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: WorkRepository? = null

        fun getInstance(context: Context): WorkRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val prefs = UserPreferences(context)
                val instance = WorkRepository(db.workSessionDao(), prefs)
                INSTANCE = instance
                instance
            }
        }
    }
}
