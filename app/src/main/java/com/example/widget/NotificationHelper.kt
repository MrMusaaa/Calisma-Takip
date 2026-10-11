package com.example.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.UserPreferences
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationHelper {
    const val CHANNEL_ID = "work_lockscreen_duo_channel_v3"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Kilit Ekranı Mesai Kontrolü"
            val descriptionText = "Redmi ve Android kilit ekranında canlı Duolingo tarzı Başlat/Bitir paneli"
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
     * Updates or shows the persistent Duolingo-style lock screen controller.
     * When idle: Vibrant "MESAİYE HAZIR MISIN?" card with bright 3D green "[▶ MESAİYİ BAŞLAT]" button.
     * When active: Vibrant "MESAİDESİN! İYİ ÇALIŞMALAR" card with bright 3D red "[⏹ MESAİYİ BİTİR]" button.
     * Zero background CPU / zero active loop!
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

        val customViews = RemoteViews(context.packageName, R.layout.notification_duolingo_style)
        customViews.setOnClickPendingIntent(R.id.duo_notif_root, openAppPendingIntent)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_fg_1791615200348)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setSilent(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customViews)
            .setCustomBigContentView(customViews)

        if (isWorking && startTime > 0) {
            val timeStr = try {
                val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())
                dt.format(DateTimeFormatter.ofPattern("HH:mm, d MMMM"))
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

            // Duolingo vibrant active style
            customViews.setTextViewText(R.id.duo_notif_tag, "🔥 MESAİDESİN")
            customViews.setTextViewText(R.id.duo_notif_badge, "● ÇALIŞIYOR")
            customViews.setTextViewText(R.id.duo_notif_title, "Harika gidiyorsun! Mesain kaydediliyor 💪")
            customViews.setTextViewText(R.id.duo_notif_subtitle, "⏰ Başlangıç: $timeStr • Pil Tüketimi: %0")
            customViews.setTextViewText(R.id.duo_notif_btn, "⏹ MESAİYİ BİTİR VE KAYDET")
            customViews.setInt(R.id.duo_notif_btn, "setBackgroundResource", R.drawable.duo_btn_stop)
            customViews.setOnClickPendingIntent(R.id.duo_notif_btn, stopPendingIntent)

            // System notification fallback texts
            builder
                .setContentTitle("🔥 Mesai Devam Ediyor ($timeStr)")
                .setContentText("Bitirmek için dokunun • Pil Harcamaz")
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

            // Duolingo vibrant idle style
            customViews.setTextViewText(R.id.duo_notif_tag, "🦉 MESAİ TAKİP")
            customViews.setTextViewText(R.id.duo_notif_badge, "BOŞTA")
            customViews.setTextViewText(R.id.duo_notif_title, "Yeni vardiyaya hazır mısın? 🚀")
            customViews.setTextViewText(R.id.duo_notif_subtitle, "Saatlik: ${prefs.hourlyRate.toInt()} ${prefs.currencySymbol} • Salı: Maaş Günü")
            customViews.setTextViewText(R.id.duo_notif_btn, "▶ MESAİYİ BAŞLAT")
            customViews.setInt(R.id.duo_notif_btn, "setBackgroundResource", R.drawable.duo_btn_start)
            customViews.setOnClickPendingIntent(R.id.duo_notif_btn, startPendingIntent)

            // System notification fallback texts
            builder
                .setContentTitle("🦉 Mesai Takip — Hazır")
                .setContentText("Başlamak için dokunun • Sıfır Pil Tüketimi")
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
