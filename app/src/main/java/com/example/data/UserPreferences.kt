package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mesai_takip_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_WORKING = "key_is_working"
        private const val KEY_ACTIVE_START_TIME = "key_active_start_time"
        private const val KEY_ACTIVE_NOTE = "key_active_note"
        private const val KEY_HOURLY_RATE = "key_hourly_rate"
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_TARGET_HOURS = "key_target_hours"
        private const val KEY_PAID_WEEKS = "key_paid_weeks"
    }

    var isWorking: Boolean
        get() = prefs.getBoolean(KEY_IS_WORKING, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_WORKING, value).apply()

    var activeStartTimeMillis: Long
        get() = prefs.getLong(KEY_ACTIVE_START_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_START_TIME, value).apply()

    var activeNote: String
        get() = prefs.getString(KEY_ACTIVE_NOTE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACTIVE_NOTE, value).apply()

    var hourlyRate: Double
        get() = prefs.getFloat(KEY_HOURLY_RATE, 250.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_HOURLY_RATE, value.toFloat()).apply()

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY, "₺") ?: "₺"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var weeklyTargetHours: Double
        get() = prefs.getFloat(KEY_TARGET_HOURS, 40.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_TARGET_HOURS, value.toFloat()).apply()

    fun isWeekPaid(weekKey: String): Boolean {
        val paidSet = prefs.getStringSet(KEY_PAID_WEEKS, emptySet()) ?: emptySet()
        return paidSet.contains(weekKey)
    }

    fun setWeekPaid(weekKey: String, isPaid: Boolean) {
        val currentSet = prefs.getStringSet(KEY_PAID_WEEKS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (isPaid) {
            currentSet.add(weekKey)
        } else {
            currentSet.remove(weekKey)
        }
        prefs.edit().putStringSet(KEY_PAID_WEEKS, currentSet).apply()
    }

    /**
     * Emits whenever preferences change so UI can react without polling.
     */
    val preferencesChanges: Flow<Unit> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(Unit)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(Unit)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
