package com.example.widget

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.data.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Quick Settings Tile allowing instant 1-tap start and stop directly from the lock screen
 * shade without opening the application.
 */
class WorkTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val repository = WorkRepository.getInstance(applicationContext)

        if (repository.preferences.isWorking) {
            // STOP WORK
            CoroutineScope(Dispatchers.IO).launch {
                repository.stopWork()
                NotificationHelper.cancelActiveWorkNotification(applicationContext)
                WorkTrackerWidgetProvider.updateAllWidgets(applicationContext)
                launch(Dispatchers.Main) {
                    updateTileState()
                }
            }
        } else {
            // START WORK
            repository.startWork()
            val startTime = repository.preferences.activeStartTimeMillis
            NotificationHelper.showActiveWorkNotification(applicationContext, startTime)
            WorkTrackerWidgetProvider.updateAllWidgets(applicationContext)
            updateTileState()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val repository = WorkRepository.getInstance(applicationContext)
        val isWorking = repository.preferences.isWorking

        if (isWorking) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Mesai: Çalışılıyor"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Bitirmek için dokunun"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Mesai Başlat"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Başlamak için dokunun"
            }
        }
        tile.updateTile()
    }

    companion object {
        fun requestTileUpdate(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, WorkTileService::class.java)
                )
            } catch (e: Exception) {
                // Ignore if not supported
            }
        }
    }
}
