package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Represents a completed work shift/session.
 *
 * Battery-friendly approach: Start and end timestamps are recorded upon user interaction,
 * without running background loops or holding wake locks.
 */
@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val breakMinutes: Int = 0,
    val hourlyRate: Double = 0.0,
    val note: String = "",
    val dateEpochDay: Long = Instant.ofEpochMilli(startTimeMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toEpochDay()
) {
    /**
     * Net duration in milliseconds (after subtracting unpaid break minutes).
     */
    val netDurationMillis: Long
        get() {
            val total = (endTimeMillis - startTimeMillis).coerceAtLeast(0L)
            val breakMillis = (breakMinutes * 60 * 1000L).coerceAtLeast(0L)
            return (total - breakMillis).coerceAtLeast(0L)
        }

    /**
     * Net hours as Double (e.g. 7.5 hours).
     */
    val netHours: Double
        get() = netDurationMillis / (1000.0 * 60 * 60)

    /**
     * Earned amount for this shift = netHours * hourlyRate.
     */
    val totalEarnings: Double
        get() = netHours * hourlyRate

    /**
     * Formatted string such as "7 sa 30 dk".
     */
    val formattedDuration: String
        get() {
            val totalMinutes = netDurationMillis / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) {
                "${hours}s ${minutes}dk"
            } else {
                "${minutes}dk"
            }
        }
}
