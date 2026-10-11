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
    fun testWorkSessionEarningsCalculationWithRoundingUpToHundreds() {
        val startMillis = 1000000000L
        // 8 hours later (8 * 3600 * 1000 = 28,800,000 ms)
        val endMillis = startMillis + (8 * 3600 * 1000L)
        val hourlyRate = 250.0

        val session = WorkSession(
            id = 1,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            breakMinutes = 30, // 30 min break -> 7.5 hours net
            hourlyRate = hourlyRate
        )

        assertEquals(7.5, session.netHours, 0.001)
        // Raw is 7.5 * 250.0 = 1875.0
        // Rounded up to the nearest hundred: 1900.0
        assertEquals(1875.0, session.rawEarnings, 0.001)
        assertEquals(1900.0, session.totalEarnings, 0.001)
        assertEquals("7s 30dk", session.formattedDuration)
    }

    @Test
    fun testRoundingRulesToNextHundred() {
        // Test various amounts rounded up to the nearest hundred
        fun roundUp(raw: Double) = if (raw <= 0.0) 0.0 else kotlin.math.ceil(raw / 100.0) * 100.0

        assertEquals(1300.0, roundUp(1210.0), 0.001)
        assertEquals(1300.0, roundUp(1205.0), 0.001)
        assertEquals(1300.0, roundUp(1201.0), 0.001)
        assertEquals(1200.0, roundUp(1200.0), 0.001)
        assertEquals(100.0, roundUp(50.0), 0.001)
        assertEquals(100.0, roundUp(10.0), 0.001)
        assertEquals(0.0, roundUp(0.0), 0.001)
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
