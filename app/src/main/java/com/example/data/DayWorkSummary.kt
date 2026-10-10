package com.example.data

import java.time.DayOfWeek
import java.time.LocalDate

data class DayWorkSummary(
    val dayOfWeek: DayOfWeek,
    val dayName: String,
    val fullDayName: String,
    val date: LocalDate,
    val dateEpochDay: Long,
    val totalHours: Double,
    val totalEarnings: Double,
    val isToday: Boolean,
    val isPayday: Boolean,
    val sessions: List<WorkSession>
)
