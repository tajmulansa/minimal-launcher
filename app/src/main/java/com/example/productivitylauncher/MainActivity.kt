package com.example.productivitylauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.productivitylauncher.ui.LauncherApp
import com.example.productivitylauncher.ui.theme.LauncherTheme

/**
 * The single activity of the app. It is registered as a HOME activity in the manifest,
 * so Android can offer it as the default launcher.
 */
class MainActivity : ComponentActivity() {

    /** Incremented every time the user presses Home while we are already open. */
    private var homeSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The UI is dark-only, so keep the system bar icons light regardless of the system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            LauncherTheme {
                LauncherApp(homeSignal = homeSignal)
            }
        }
    }

    // launchMode="singleTask": pressing Home again arrives here instead of creating a new activity.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        homeSignal++
    }
}
