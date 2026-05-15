package com.startscope.coldstart

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.startscope.coldstart.service.StartCollectorService
import com.startscope.coldstart.ui.AppRoot
import com.startscope.coldstart.ui.theme.StartScopeTheme
import com.startscope.coldstart.util.UsageAccessHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        val app = application as StartScopeApplication
        setContent {
            StartScopeTheme {
                AppRoot(app.container)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as StartScopeApplication
        lifecycleScope.launch {
            val granted = UsageAccessHelper.isGranted(this@MainActivity)
            val enabled = app.container.userPreferences.collectionEnabled.first()
            if (granted && enabled) {
                StartCollectorService.start(this@MainActivity)
            } else {
                StartCollectorService.stop(this@MainActivity)
            }
        }
    }
}
