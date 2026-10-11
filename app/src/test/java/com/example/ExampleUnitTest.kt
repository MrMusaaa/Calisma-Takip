package com.example

import com.example.data.WeekHelper
import com.example.data.WorkSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testHourRoundingRules() {
        // User requirements:
        // "eğer toplam saat 12.10 mesela 12 yap ama saat 12.15 se 12.30 yap eger 12.3 ve ustuyse direkt bı üste yuvarla"
        // 12 saat 10 dakika (730 dakika) -> 12.0 saat
        assertEquals(12.0, WeekHelper.roundWorkHours(12 * 60 + 10L), 0.001)
        assertEquals("12s", WeekHelper.formatRoundedHours(12.0))

        // 12 saat 05 dakika -> 12.0 saat
        assertEquals(12.0, WeekHelper.roundWorkHours(12 * 60 + 5L), 0.001)

        // 12 saat 14 dakika -> 12.0 saat
        assertEquals(12.0, WeekHelper.roundWorkHours(12 * 60 + 14L), 0.001)

        // 12 saat 15 dakika (735 dakika) -> 12.5 saat (12 saat 30 dk)
        assertEquals(12.5, WeekHelper.roundWorkHours(12 * 60 + 15L), 0.001)
        assertEquals("12s 30dk", WeekHelper.formatRoundedHours(12.5))

        // 12 saat 20 dakika -> 12.5 saat
        assertEquals(12.5, WeekHelper.roundWorkHours(12 * 60 + 20L), 0.001)

        // 12 saat 29 dakika -> 12.5 saat
        assertEquals(12.5, WeekHelper.roundWorkHours(12 * 60 + 29L), 0.001)

        // 12 saat 30 dakika ("12.3 ve üstü") -> 13.0 saat
        assertEquals(13.0, WeekHelper.roundWorkHours(12 * 60 + 30L), 0.001)
        assertEquals("13s", WeekHelper.formatRoundedHours(13.0))

        // 12 saat 45 dakika -> 13.0 saat
        assertEquals(13.0, WeekHelper.roundWorkHours(12 * 60 + 45L), 0.001)
    }

    @Test
    fun testWorkSessionWithHourAndMoneyRounding() {
        val startMillis = 1000000000L
        // 8 hours and 10 minutes -> rounds to 8.0 hours
        val endMillis = startMillis + (8 * 3600 * 1000L) + (10 * 60 * 1000L)
        val hourlyRate = 250.0

        val session = WorkSession(
            id = 1,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            breakMinutes = 0,
            hourlyRate = hourlyRate
        )

        assertEquals(8.0, session.netHours, 0.001)
        assertEquals(2000.0, session.rawEarnings, 0.001)
        assertEquals(2000.0, session.totalEarnings, 0.001) // 2000 stays 2000!
        assertEquals("8s", session.formattedDuration)
    }

    @Test
    fun testRoundingRulesToNextHundred() {
        // User requirements:
        // "eğer 2000 de kalsın ama 2010 sa mesela 2100 yap yada 2120 falansa 2200 yap"
        assertEquals(2000.0, WeekHelper.roundUpToHundred(2000.0), 0.001)
        assertEquals(2000.0, WeekHelper.roundUpToHundred(2000.000000000002), 0.001)
        assertEquals(2100.0, WeekHelper.roundUpToHundred(2010.0), 0.001)
        assertEquals(2200.0, WeekHelper.roundUpToHundred(2120.0), 0.001)
        assertEquals(2100.0, WeekHelper.roundUpToHundred(2100.0), 0.001)
        assertEquals(2200.0, WeekHelper.roundUpToHundred(2200.0), 0.001)
        assertEquals(2100.0, WeekHelper.roundUpToHundred(2005.0), 0.001)
        assertEquals(1300.0, WeekHelper.roundUpToHundred(1210.0), 0.001)
        assertEquals(100.0, WeekHelper.roundUpToHundred(50.0), 0.001)
        assertEquals(0.0, WeekHelper.roundUpToHundred(0.0), 0.001)
    }

    @Test
    fun testWeekPeriodStartsOnMondayAndEndsOnSunday() {
        val testDate = LocalDate.of(2026, 10, 10) // Saturday
        val weekPeriod = WeekHelper.getWeekPeriodForDate(testDate)

        assertEquals(DayOfWeek.MONDAY, weekPeriod.monday.dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, weekPeriod.sunday.dayOfWeek)
        assertEquals(DayOfWeek.TUESDAY, weekPeriod.paydayTuesday.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 5), weekPeriod.monday)
        assertEquals(LocalDate.of(2026, 10, 11), weekPeriod.sunday)
    }

    @Test
    fun testTuesdayPaydayCalculation() {
        val tuesday = LocalDate.of(2026, 10, 13) // A Tuesday
        val paydayStatus = WeekHelper.getPaydayStatus(tuesday)

        assertTrue(paydayStatus.isTodayPayday)
        assertEquals(0, paydayStatus.daysRemaining)

        val monday = LocalDate.of(2026, 10, 12) // Monday (1 day before Tuesday)
        val mondayPaydayStatus = WeekHelper.getPaydayStatus(monday)
        assertFalse(mondayPaydayStatus.isTodayPayday)
        assertEquals(1, mondayPaydayStatus.daysRemaining)
    }
}
