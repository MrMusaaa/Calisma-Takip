package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.WorkHomeScreen
import com.example.ui.WorkViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.widget.NotificationHelper

class MainActivity : ComponentActivity() {

    private val viewModel: WorkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel and display lock screen notification controller immediately
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.updateNotification(this)

        setContent {
            MyApplicationTheme {
                // Request notification permission on Android 13+ if not already granted
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        if (granted) {
                            NotificationHelper.updateNotification(this@MainActivity)
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val isGranted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!isGranted) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            NotificationHelper.updateNotification(this@MainActivity)
                        }
                    } else {
                        NotificationHelper.updateNotification(this@MainActivity)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    WorkHomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
