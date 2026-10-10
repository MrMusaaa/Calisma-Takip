package com.example.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.UserPreferences
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {
    const val CHANNEL_ID = "work_lockscreen_control_channel_v2"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Kilit Ekranı Mesai Kontrolü"
            val descriptionText = "Redmi ve Android kilit ekranında doğrudan Başlat / Bitir butonu"
            // IMPORTANCE_HIGH ensures Xiaomi MIUI / HyperOS displays it directly on the lockscreen
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableVibration(false)
                setSound(null, null)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Updates or shows the persistent lock screen controller notification.
     * When idle: Shows "Hazır" with "[▶ MESAİYİ BAŞLAT]".
     * When active: Shows "Çalışılıyor" with live timer and "[⏹ MESAİYİ BİTİR]".
     */
    fun updateNotification(context: Context) {
        createNotificationChannel(context)
        val prefs = UserPreferences(context)
        val isWorking = prefs.isWorking
        val startTime = prefs.activeStartTimeMillis

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_fg_1791615200348)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setSilent(true)

        if (isWorking && startTime > 0) {
            val timeStr = try {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())
                dt.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                ""
            }

            val stopIntent = Intent(context, WorkActionReceiver::class.java).apply {
                action = WorkActionReceiver.ACTION_STOP_WORK
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder
                .setContentTitle("⏱ Mesai Devam Ediyor (${timeStr})")
                .setContentText("Kilit ekranından bitirmek için aşağıdaki butona dokunun")
                .setSubText("Salı: Maaş Günü")
                .setWhen(startTime)
                .setUsesChronometer(true)
                .addAction(
                    android.R.drawable.ic_media_pause,
                    "⏹ MESAİYİ BİTİR",
                    stopPendingIntent
                )
        } else {
            val startIntent = Intent(context, WorkActionReceiver::class.java).apply {
                action = WorkActionReceiver.ACTION_START_WORK
            }
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder
                .setContentTitle("⏱ Mesai Takip — Hazır")
                .setContentText("Saatlik: ${prefs.hourlyRate.toInt()} ${prefs.currencySymbol} • Başlamak için dokunun")
                .setSubText("Kilit Ekranı Kontrolü")
                .addAction(
                    android.R.drawable.ic_media_play,
                    "▶ MESAİYİ BAŞLAT",
                    startPendingIntent
                )
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
