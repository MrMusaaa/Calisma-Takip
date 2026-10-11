package com.example.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class WeekPeriod(
    val monday: LocalDate,
    val sunday: LocalDate,
    val weekKey: String, // e.g. "2026-W41"
    val startEpochDay: Long,
    val endEpochDay: Long,
    val label: String, // e.g. "6 Eki - 12 Eki 2026"
    val paydayTuesday: LocalDate // The Tuesday following this Sunday (or relevant payday)
)

object WeekHelper {
    private val TURKISH_LOCALE = Locale("tr", "TR")
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", TURKISH_LOCALE)
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", TURKISH_LOCALE)
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", TURKISH_LOCALE)
    val DAY_NAME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", TURKISH_LOCALE)

    /**
     * Gets the Monday-Sunday week period for a given offset from today (0 = current week, -1 = last week).
     */
    fun getWeekPeriod(weekOffset: Int = 0): WeekPeriod {
        val today = LocalDate.now()
        val targetDate = today.plusWeeks(weekOffset.toLong())
        return getWeekPeriodForDate(targetDate)
    }

    /**
     * Gets the Monday-Sunday week period containing the given date.
     */
    fun getWeekPeriodForDate(date: LocalDate): WeekPeriod {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        // The payday for this Monday-Sunday week is the Tuesday immediately following Sunday
        val paydayTuesday = sunday.plusDays(2) // Sunday + 2 days = Tuesday

        val weekKey = "${monday.year}-W${monday.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
        val label = "${monday.format(SHORT_DATE_FORMATTER)} - ${sunday.format(SHORT_DATE_FORMATTER)} ${sunday.year}"

        return WeekPeriod(
            monday = monday,
            sunday = sunday,
            weekKey = weekKey,
            startEpochDay = monday.toEpochDay(),
            endEpochDay = sunday.toEpochDay(),
            label = label,
            paydayTuesday = paydayTuesday
        )
    }

    /**
     * Information about the upcoming or current Payday (Tuesday).
     */
    fun getPaydayStatus(today: LocalDate = LocalDate.now()): PaydayStatus {
        val currentDayOfWeek = today.dayOfWeek
        val daysUntilTuesday = when (currentDayOfWeek) {
            DayOfWeek.TUESDAY -> 0
            DayOfWeek.WEDNESDAY -> 6
            DayOfWeek.THURSDAY -> 5
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 3
            DayOfWeek.SUNDAY -> 2
            DayOfWeek.MONDAY -> 1
        }
        val nextPayday = today.plusDays(daysUntilTuesday.toLong())
        val formattedDate = nextPayday.format(DateTimeFormatter.ofPattern("d MMMM EEEE", TURKISH_LOCALE))

        val countdownText = when (daysUntilTuesday) {
            0 -> "🎉 Bugün Salı: MAAŞ GÜNÜ!"
            1 -> "⚡ Yarın Maaş Günü!"
            else -> "⏳ Maaş gününe $daysUntilTuesday gün kaldı"
        }

        return PaydayStatus(
            isTodayPayday = (daysUntilTuesday == 0),
            daysRemaining = daysUntilTuesday,
            paydayDate = nextPayday,
            formattedPaydayDate = formattedDate,
            countdownText = countdownText
        )
    }

    fun formatMillisTime(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
        return dt.format(TIME_FORMATTER)
    }

    fun formatMillisDate(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
        return dt.format(DATE_FORMATTER)
    }

    fun formatEpochDayDate(epochDay: Long): String {
        val ld = LocalDate.ofEpochDay(epochDay)
        return ld.format(DATE_FORMATTER)
    }

    /**
     * Küsüratı onlar basamağı ne olursa olsun yüzler basamağına yukarı doğru yuvarlar.
     * Tam yüzlükler (örn: 2000, 2100, 2200) kesinlikle korunur, artırılmaz.
     *
     * Örnekler:
     * - 2000 -> 2000 (aynı kalır)
     * - 2010 -> 2100
     * - 2120 -> 2200
     * - 2001 -> 2100
     * - 50   -> 100
     */
    fun roundUpToHundred(amount: Double): Double {
        if (amount <= 0.0) return 0.0
        val roundedToCents = kotlin.math.round(amount * 100.0) / 100.0
        val remainder = roundedToCents % 100.0
        return if (remainder < 0.01 || remainder > 99.99) {
            kotlin.math.round(roundedToCents / 100.0) * 100.0
        } else {
            kotlin.math.ceil(roundedToCents / 100.0) * 100.0
        }
    }

    /**
     * Kullanıcı kuralına göre çalışma süresini saat olarak yuvarlar:
     * - Dakika < 15 ise (örn: 12.10) -> Aşağı yuvarla (12.0 saat)
     * - Dakika 15..29 arası ise (örn: 12.15) -> 12:30 yap (12.5 saat)
     * - Dakika >= 30 ise (örn: 12.30 ve üstü) -> Direkt bir üste yuvarla (13.0 saat)
     */
    fun roundWorkHours(totalMinutes: Long): Double {
        if (totalMinutes <= 0L) return 0.0
        val fullHours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            minutes < 15 -> fullHours.toDouble()
            minutes < 30 -> fullHours + 0.5
            else -> (fullHours + 1).toDouble()
        }
    }

    fun formatRoundedHours(roundedHours: Double): String {
        val h = roundedHours.toInt()
        val isHalf = (roundedHours - h) >= 0.4
        return when {
            h > 0 && isHalf -> "${h}s 30dk"
            h > 0 -> "${h}s"
            isHalf -> "30dk"
            else -> "0s"
        }
    }
}

data class PaydayStatus(
    val isTodayPayday: Boolean,
    val daysRemaining: Int,
    val paydayDate: LocalDate,
    val formattedPaydayDate: String,
    val countdownText: String
)
