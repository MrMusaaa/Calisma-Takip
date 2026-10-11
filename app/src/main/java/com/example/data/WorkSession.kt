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
     * Toplam çalışma süresi (dakika olarak).
     * Mola parametresi kaldırılmıştır.
     */
    val totalMinutes: Long
        get() = (endTimeMillis - startTimeMillis).coerceAtLeast(0L) / (1000 * 60)

    /**
     * Yuvarlanmış çalışma saati:
     * - Dakika < 15 ise -> Tam saate aşağı yuvarla (örn: 12.10 -> 12.0 saat)
     * - Dakika 15..29 ise -> Buçuğa yuvarla (örn: 12.15 -> 12.5 saat)
     * - Dakika >= 30 ise -> Direkt bir üste yuvarla (örn: 12.30+ -> 13.0 saat)
     */
    val netHours: Double
        get() = WeekHelper.roundWorkHours(totalMinutes)

    /**
     * Ham kazanç tutarı = netHours * hourlyRate.
     */
    val rawEarnings: Double
        get() = netHours * hourlyRate

    /**
     * Onlar basamağındaki küsürat ne olursa olsun yüzler basamağına yukarı yuvarlanan kazanç.
     * Tam yüzlükler (2000, 2100 vb.) kesinlikle korunur; 2010 -> 2100, 2120 -> 2200 olur.
     */
    val totalEarnings: Double
        get() = WeekHelper.roundUpToHundred(rawEarnings)

    /**
     * Formatlı süre (örn: "12s", "12s 30dk").
     */
    val formattedDuration: String
        get() = WeekHelper.formatRoundedHours(netHours)
}
