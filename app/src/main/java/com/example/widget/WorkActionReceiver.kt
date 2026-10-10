package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val repository = WorkRepository.getInstance(context)

        when (action) {
            ACTION_START_WORK -> {
                if (!repository.preferences.isWorking) {
                    repository.startWork()
                    NotificationHelper.updateNotification(context)
                    WorkTrackerWidgetProvider.updateAllWidgets(context)
                    WorkTileService.requestTileUpdate(context)
                }
            }

            ACTION_STOP_WORK -> {
                if (repository.preferences.isWorking) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.stopWork()
                        } finally {
                            NotificationHelper.updateNotification(context)
                            WorkTrackerWidgetProvider.updateAllWidgets(context)
                            WorkTileService.requestTileUpdate(context)
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_TOGGLE_WORK -> {
                if (repository.preferences.isWorking) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.stopWork()
                        } finally {
                            NotificationHelper.updateNotification(context)
                            WorkTrackerWidgetProvider.updateAllWidgets(context)
                            WorkTileService.requestTileUpdate(context)
                            pendingResult.finish()
                        }
                    }
                } else {
                    repository.startWork()
                    NotificationHelper.updateNotification(context)
                    WorkTrackerWidgetProvider.updateAllWidgets(context)
                    WorkTileService.requestTileUpdate(context)
                }
            }
        }
    }

    companion object {
        const val ACTION_START_WORK = "com.example.action.START_WORK"
        const val ACTION_STOP_WORK = "com.example.action.STOP_WORK"
        const val ACTION_TOGGLE_WORK = "com.example.action.TOGGLE_WORK"
    }
}
