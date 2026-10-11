package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DayWorkSummary
import com.example.data.PaydayStatus
import com.example.data.WeekHelper
import com.example.data.WeekPeriod
import com.example.data.WorkRepository
import com.example.data.WorkSession
import com.example.widget.NotificationHelper
import com.example.widget.WorkTileService
import com.example.widget.WorkTrackerWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class WorkUiState(
    val isWorking: Boolean = false,
    val activeStartTimeMillis: Long = 0L,
    val hourlyRate: Double = 250.0,
    val currencySymbol: String = "₺",
    val selectedWeekOffset: Int = 0,
    val currentWeekPeriod: WeekPeriod = WeekHelper.getWeekPeriod(0),
    val previousWeekPeriod: WeekPeriod = WeekHelper.getWeekPeriod(-1),
    val paydayStatus: PaydayStatus = WeekHelper.getPaydayStatus(),
    val isSelectedWeekPaid: Boolean = false,
    val isPayoutWeekPaid: Boolean = false,

    // Upcoming Tuesday Payout: the previous completed Monday-Sunday week to be paid this Tuesday
    val payoutWeekPeriod: WeekPeriod = WeekHelper.getWeekPeriod(-1),
    val payoutWeekEarnings: Double = 0.0,
    val payoutWeekHours: Double = 0.0,

    // Current ongoing week's accumulating earnings (to be paid next Tuesday)
    val ongoingWeekEarnings: Double = 0.0,
    val ongoingWeekHours: Double = 0.0,

    // Selected week view (for chart and historical view)
    val weekSessions: List<WorkSession> = emptyList(),
    val totalWeekHours: Double = 0.0,
    val totalWeekEarnings: Double = 0.0,
    val dayBreakdown: List<DayWorkSummary> = emptyList(),
    val allSessions: List<WorkSession> = emptyList()
)

class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WorkRepository.getInstance(application)
    private val context = application.applicationContext

    private val _selectedWeekOffset = MutableStateFlow(0)
    val selectedWeekOffset: StateFlow<Int> = _selectedWeekOffset.asStateFlow()

    // Flow of all sessions from Room
    private val allSessionsFlow = repository.allSessions

    // Combine database, preferences, and selected week (Zero background CPU / No timer loops)
    val uiState: StateFlow<WorkUiState> = combine(
        allSessionsFlow,
        _selectedWeekOffset,
        repository.preferences.preferencesChanges
    ) { allSessions, offset, _ ->
        val prefs = repository.preferences
        val isWorking = prefs.isWorking
        val startTime = prefs.activeStartTimeMillis
        val rate = prefs.hourlyRate
        val currency = prefs.currencySymbol

        val today = LocalDate.now()
        val currentWeek = WeekHelper.getWeekPeriodForDate(today)
        val previousCompletedWeek = WeekHelper.getWeekPeriodForDate(currentWeek.monday.minusDays(1))

        // Selected week period for the chart/history view
        val selectedWeekPeriod = WeekHelper.getWeekPeriod(offset)
        val paydayStatus = WeekHelper.getPaydayStatus()

        // 1. Payout Week: The completed previous week (Pazartesi - Pazar) to be paid on upcoming Tuesday
        val payoutWeekSessions = allSessions.filter {
            it.dateEpochDay >= previousCompletedWeek.startEpochDay && it.dateEpochDay <= previousCompletedWeek.endEpochDay
        }
        val payoutHours = payoutWeekSessions.sumOf { it.netHours }
        val payoutEarnings = WeekHelper.roundUpToHundred(payoutWeekSessions.sumOf { it.rawEarnings })
        val isPayoutPaid = prefs.isWeekPaid(previousCompletedWeek.weekKey)

        // 2. Ongoing Current Week (Pazartesi - Pazar): Accumulating for the following Tuesday
        val ongoingWeekSessions = allSessions.filter {
            it.dateEpochDay >= currentWeek.startEpochDay && it.dateEpochDay <= currentWeek.endEpochDay
        }
        val ongoingHours = ongoingWeekSessions.sumOf { it.netHours }
        val ongoingEarnings = WeekHelper.roundUpToHundred(ongoingWeekSessions.sumOf { it.rawEarnings })

        // 3. Selected week details (can be any past/current week)
        val selectedWeekSessions = allSessions.filter {
            it.dateEpochDay >= selectedWeekPeriod.startEpochDay && it.dateEpochDay <= selectedWeekPeriod.endEpochDay
        }
        val totalSelectedWeekHours = selectedWeekSessions.sumOf { it.netHours }
        val totalSelectedWeekEarnings = WeekHelper.roundUpToHundred(selectedWeekSessions.sumOf { it.rawEarnings })
        val isSelectedWeekPaid = prefs.isWeekPaid(selectedWeekPeriod.weekKey)

        val daySummaries = (0..6).map { dayIndex ->
            val date = selectedWeekPeriod.monday.plusDays(dayIndex.toLong())
            val dayOfWeek = date.dayOfWeek
            val daySessions = selectedWeekSessions.filter { it.dateEpochDay == date.toEpochDay() }
            val dayHours = daySessions.sumOf { it.netHours }
            val daySum = daySessions.sumOf { it.rawEarnings }
            val dayEarnings = WeekHelper.roundUpToHundred(daySum)

            val shortName = when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Pzt"
                DayOfWeek.TUESDAY -> "Sal"
                DayOfWeek.WEDNESDAY -> "Çar"
                DayOfWeek.THURSDAY -> "Per"
                DayOfWeek.FRIDAY -> "Cum"
                DayOfWeek.SATURDAY -> "Cmt"
                DayOfWeek.SUNDAY -> "Paz"
            }
            val fullName = when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Pazartesi"
                DayOfWeek.TUESDAY -> "Salı"
                DayOfWeek.WEDNESDAY -> "Çarşamba"
                DayOfWeek.THURSDAY -> "Perşembe"
                DayOfWeek.FRIDAY -> "Cuma"
                DayOfWeek.SATURDAY -> "Cumartesi"
                DayOfWeek.SUNDAY -> "Pazar"
            }

            DayWorkSummary(
                dayOfWeek = dayOfWeek,
                dayName = shortName,
                fullDayName = fullName,
                date = date,
                dateEpochDay = date.toEpochDay(),
                totalHours = dayHours,
                totalEarnings = dayEarnings,
                isToday = (date == today),
                isPayday = (dayOfWeek == DayOfWeek.TUESDAY),
                sessions = daySessions
            )
        }

        WorkUiState(
            isWorking = isWorking,
            activeStartTimeMillis = startTime,
            hourlyRate = rate,
            currencySymbol = currency,
            selectedWeekOffset = offset,
            currentWeekPeriod = selectedWeekPeriod,
            previousWeekPeriod = previousCompletedWeek,
            paydayStatus = paydayStatus,
            isSelectedWeekPaid = isSelectedWeekPaid,
            isPayoutWeekPaid = isPayoutPaid,
            payoutWeekPeriod = previousCompletedWeek,
            payoutWeekEarnings = payoutEarnings,
            payoutWeekHours = payoutHours,
            ongoingWeekEarnings = ongoingEarnings,
            ongoingWeekHours = ongoingHours,
            weekSessions = selectedWeekSessions,
            totalWeekHours = totalSelectedWeekHours,
            totalWeekEarnings = totalSelectedWeekEarnings,
            dayBreakdown = daySummaries,
            allSessions = allSessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkUiState()
    )

    fun startWork(note: String = "") {
        repository.startWork(note)
        NotificationHelper.updateNotification(context)
        WorkTrackerWidgetProvider.updateAllWidgets(context)
        WorkTileService.requestTileUpdate(context)
    }

    fun stopWork() {
        viewModelScope.launch {
            repository.stopWork()
            NotificationHelper.updateNotification(context)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
            WorkTileService.requestTileUpdate(context)
        }
    }

    fun setWeekOffset(offset: Int) {
        _selectedWeekOffset.value = offset
    }

    fun previousWeek() {
        _selectedWeekOffset.update { it - 1 }
    }

    fun nextWeek() {
        _selectedWeekOffset.update { it + 1 }
    }

    fun resetToCurrentWeek() {
        _selectedWeekOffset.value = 0
    }

    fun updateHourlyRate(newRate: Double) {
        if (newRate >= 0) {
            repository.preferences.hourlyRate = newRate
            WorkTrackerWidgetProvider.updateAllWidgets(context)
            NotificationHelper.updateNotification(context)
        }
    }

    fun togglePayoutWeekPaid() {
        val payoutPeriod = uiState.value.payoutWeekPeriod
        val currentPaid = repository.preferences.isWeekPaid(payoutPeriod.weekKey)
        repository.preferences.setWeekPaid(payoutPeriod.weekKey, !currentPaid)
    }

    fun toggleSelectedWeekPaid() {
        val selectedPeriod = uiState.value.currentWeekPeriod
        val currentPaid = repository.preferences.isWeekPaid(selectedPeriod.weekKey)
        repository.preferences.setWeekPaid(selectedPeriod.weekKey, !currentPaid)
    }

    fun addManualSession(
        date: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        breakMinutes: Int,
        hourlyRate: Double,
        note: String
    ) {
        viewModelScope.launch {
            val zoneId = java.time.ZoneId.systemDefault()
            val startDt = date.atTime(startHour, startMinute)
            var endDt = date.atTime(endHour, endMinute)
            if (endDt.isBefore(startDt)) {
                endDt = endDt.plusDays(1)
            }

            val startMillis = startDt.atZone(zoneId).toInstant().toEpochMilli()
            val endMillis = endDt.atZone(zoneId).toInstant().toEpochMilli()

            val session = WorkSession(
                startTimeMillis = startMillis,
                endTimeMillis = endMillis,
                breakMinutes = breakMinutes,
                hourlyRate = hourlyRate,
                note = note,
                dateEpochDay = date.toEpochDay()
            )
            repository.insertManualSession(session)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
            NotificationHelper.updateNotification(context)
        }
    }

    fun deleteSession(session: WorkSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
            NotificationHelper.updateNotification(context)
        }
    }

    fun updateSession(session: WorkSession) {
        viewModelScope.launch {
            repository.updateSession(session)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
            NotificationHelper.updateNotification(context)
        }
    }
}
