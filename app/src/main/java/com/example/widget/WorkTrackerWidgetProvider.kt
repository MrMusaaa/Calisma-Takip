package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.UserPreferences
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WorkTrackerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = UserPreferences(context)
            val views = RemoteViews(context.packageName, R.layout.work_tracker_widget)

            // Open app when clicking root
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            val isWorking = prefs.isWorking
            val startTime = prefs.activeStartTimeMillis

            if (isWorking && startTime > 0) {
                // ACTIVE STATE
                views.setTextViewText(R.id.widget_status_badge, "● ÇALIŞIYOR")
                views.setInt(R.id.widget_status_badge, "setBackgroundColor", android.graphics.Color.parseColor("#065F46"))
                views.setTextColor(R.id.widget_status_badge, android.graphics.Color.parseColor("#34D399"))

                val timeStr = try {
                    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())
                    dt.format(DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) {
                    ""
                }
                views.setTextViewText(R.id.widget_time_text, "Başlama: $timeStr")
                views.setTextViewText(R.id.widget_subtitle, "Pil dostu kayıt • Salı Maaş Günü")

                // Setup chronometer
                val elapsedSinceStart = System.currentTimeMillis() - startTime
                val chronometerBase = SystemClock.elapsedRealtime() - elapsedSinceStart
                views.setChronometer(R.id.widget_chronometer, chronometerBase, null, true)
                views.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)

                // Setup Stop button
                views.setTextViewText(R.id.widget_btn_action, "⏹ MESAİYİ BİTİR")
                views.setInt(R.id.widget_btn_action, "setBackgroundResource", R.drawable.widget_btn_stop_bg)

                val stopIntent = Intent(context, WorkActionReceiver::class.java).apply {
                    action = WorkActionReceiver.ACTION_STOP_WORK
                }
                val stopPendingIntent = PendingIntent.getBroadcast(
                    context,
                    101,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_action, stopPendingIntent)
            } else {
                // IDLE STATE
                views.setTextViewText(R.id.widget_status_badge, "BOŞTA")
                views.setInt(R.id.widget_status_badge, "setBackgroundColor", android.graphics.Color.parseColor("#334155"))
                views.setTextColor(R.id.widget_status_badge, android.graphics.Color.parseColor("#94A3B8"))

                views.setViewVisibility(R.id.widget_chronometer, View.GONE)
                views.setTextViewText(R.id.widget_time_text, "Mesaiye Başla")
                views.setTextViewText(R.id.widget_subtitle, "Saatlik: ${prefs.hourlyRate.toInt()} ${prefs.currencySymbol} • Salı Maaş")

                // Setup Start button
                views.setTextViewText(R.id.widget_btn_action, "▶ BAŞLAT")
                views.setInt(R.id.widget_btn_action, "setBackgroundResource", R.drawable.widget_btn_start_bg)

                val startIntent = Intent(context, WorkActionReceiver::class.java).apply {
                    action = WorkActionReceiver.ACTION_START_WORK
                }
                val startPendingIntent = PendingIntent.getBroadcast(
                    context,
                    102,
                    startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_action, startPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WorkTrackerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }
}
