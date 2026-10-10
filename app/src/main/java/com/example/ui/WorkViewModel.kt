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
import com.example.widget.WorkTrackerWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WorkUiState(
    val isWorking: Boolean = false,
    val activeStartTimeMillis: Long = 0L,
    val activeElapsedSeconds: Long = 0L,
    val hourlyRate: Double = 250.0,
    val currencySymbol: String = "₺",
    val selectedWeekOffset: Int = 0,
    val currentWeekPeriod: WeekPeriod = WeekHelper.getWeekPeriod(0),
    val paydayStatus: PaydayStatus = WeekHelper.getPaydayStatus(),
    val isWeekPaid: Boolean = false,
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

    private val _activeElapsedSeconds = MutableStateFlow(0L)

    // Flow of all sessions from Room
    private val allSessionsFlow = repository.allSessions

    // Combine database, active status, preferences, and selected week
    val uiState: StateFlow<WorkUiState> = combine(
        allSessionsFlow,
        _selectedWeekOffset,
        _activeElapsedSeconds,
        repository.preferences.preferencesChanges
    ) { allSessions, offset, elapsed, _ ->
        val prefs = repository.preferences
        val isWorking = prefs.isWorking
        val startTime = prefs.activeStartTimeMillis
        val rate = prefs.hourlyRate
        val currency = prefs.currencySymbol

        val weekPeriod = WeekHelper.getWeekPeriod(offset)
        val paydayStatus = WeekHelper.getPaydayStatus()
        val isPaid = prefs.isWeekPaid(weekPeriod.weekKey)

        // Filter sessions within this week's Monday 00:00 to Sunday 23:59:59 (by epochDay)
        val weekSessions = allSessions.filter {
            it.dateEpochDay >= weekPeriod.startEpochDay && it.dateEpochDay <= weekPeriod.endEpochDay
        }

        val totalWeekHours = weekSessions.sumOf { it.netHours }
        val totalWeekEarnings = weekSessions.sumOf { it.totalEarnings }

        val today = LocalDate.now()
        val daySummaries = (0..6).map { dayIndex ->
            val date = weekPeriod.monday.plusDays(dayIndex.toLong())
            val dayOfWeek = date.dayOfWeek
            val daySessions = weekSessions.filter { it.dateEpochDay == date.toEpochDay() }
            val dayHours = daySessions.sumOf { it.netHours }
            val dayEarnings = daySessions.sumOf { it.totalEarnings }

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
            activeElapsedSeconds = elapsed,
            hourlyRate = rate,
            currencySymbol = currency,
            selectedWeekOffset = offset,
            currentWeekPeriod = weekPeriod,
            paydayStatus = paydayStatus,
            isWeekPaid = isPaid,
            weekSessions = weekSessions,
            totalWeekHours = totalWeekHours,
            totalWeekEarnings = totalWeekEarnings,
            dayBreakdown = daySummaries,
            allSessions = allSessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkUiState()
    )

    init {
        // Ticker for UI when active (runs only while Composable observes ViewModel)
        viewModelScope.launch {
            while (isActive) {
                val prefs = repository.preferences
                if (prefs.isWorking && prefs.activeStartTimeMillis > 0) {
                    val diff = (System.currentTimeMillis() - prefs.activeStartTimeMillis) / 1000L
                    _activeElapsedSeconds.value = diff.coerceAtLeast(0L)
                } else {
                    _activeElapsedSeconds.value = 0L
                }
                delay(1000L)
            }
        }
    }

    fun startWork(note: String = "") {
        repository.startWork(note)
        NotificationHelper.updateNotification(context)
        WorkTrackerWidgetProvider.updateAllWidgets(context)
        com.example.widget.WorkTileService.requestTileUpdate(context)
    }

    fun stopWork() {
        viewModelScope.launch {
            repository.stopWork()
            NotificationHelper.updateNotification(context)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
            com.example.widget.WorkTileService.requestTileUpdate(context)
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
        }
    }

    fun toggleWeekPaid() {
        val currentPeriod = uiState.value.currentWeekPeriod
        val currentPaid = repository.preferences.isWeekPaid(currentPeriod.weekKey)
        repository.preferences.setWeekPaid(currentPeriod.weekKey, !currentPaid)
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
            val zoneId = ZoneId.systemDefault()
            val startDt = date.atTime(startHour, startMinute)
            var endDt = date.atTime(endHour, endMinute)
            // If end time is earlier than start time, assumes overnight shift
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
        }
    }

    fun deleteSession(session: WorkSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
        }
    }

    fun updateSession(session: WorkSession) {
        viewModelScope.launch {
            repository.updateSession(session)
            WorkTrackerWidgetProvider.updateAllWidgets(context)
        }
    }
}
