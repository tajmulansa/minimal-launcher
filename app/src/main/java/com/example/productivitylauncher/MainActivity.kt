package com.example.productivitylauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.WidgetHost
import com.example.productivitylauncher.ui.LauncherApp
import com.example.productivitylauncher.ui.theme.LauncherTheme
import com.example.productivitylauncher.ui.theme.resolvePalette

/**
 * The single activity of the app. It is registered as a HOME activity in the manifest,
 * so Android can offer it as the default launcher.
 */
class MainActivity : ComponentActivity() {

    private lateinit var state: LauncherState
    private lateinit var widgetHost: WidgetHost

    /** Incremented every time the user presses Home while we are already open. */
    private var homeSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = LauncherState(this)
        widgetHost = WidgetHost(this)
        setContent {
            val palette = resolvePalette(state.look.theme, state.look.accent)
            val dark = palette.dark
            // Keep the system bar icons readable on both themes, including a manual theme choice.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                    navigationBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                )
                onDispose { }
            }
            LauncherTheme(palette) {
                LauncherApp(state = state, widgetHost = widgetHost, homeSignal = homeSignal)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        widgetHost.start()
    }

    override fun onResume() {
        super.onResume()
        state.onResume()
    }

    override fun onStop() {
        widgetHost.stop()
        super.onStop()
    }

    // launchMode="singleTask": pressing Home again arrives here instead of creating a new activity.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        homeSignal++
    }
}
