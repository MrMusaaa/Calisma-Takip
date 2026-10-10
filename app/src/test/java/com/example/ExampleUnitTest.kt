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
    fun testWorkSessionEarningsCalculation() {
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
        assertEquals(7.5 * 250.0, session.totalEarnings, 0.001) // 1875.0
        assertEquals("7s 30dk", session.formattedDuration)
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
