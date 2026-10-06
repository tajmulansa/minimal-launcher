package com.example.productivitylauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.productivitylauncher.ui.apps.AppListScreen
import com.example.productivitylauncher.ui.home.HomeScreen
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.tools.ToolsScreen
import kotlinx.coroutines.launch

/** The screens of the launcher. Keep this tiny until a real navigation need appears. */
enum class Screen { Home, Apps }

@Composable
fun LauncherApp(homeSignal: Int) {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Pressing the Home button always returns to the home screen.
    LaunchedEffect(homeSignal) {
        screen = Screen.Home
        pagerState.scrollToPage(1)
    }

    // A launcher never closes: Back goes to Home, and does nothing on Home.
    BackHandler {
        if (screen == Screen.Apps) {
            screen = Screen.Home
        } else if (pagerState.currentPage != 1) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .systemBarsPadding(),
    ) {
        when (screen) {
            Screen.Home -> {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> ToolsScreen()
                        1 -> HomeScreen(onOpenApps = { screen = Screen.Apps })
                    }
                }
            }
            Screen.Apps -> AppListScreen(onClose = { screen = Screen.Home })
        }
    }
}
