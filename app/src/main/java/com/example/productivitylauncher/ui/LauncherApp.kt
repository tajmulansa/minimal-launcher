package com.example.productivitylauncher.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.PipAction
import com.example.productivitylauncher.data.WidgetHost
import com.example.productivitylauncher.data.launchApp
import com.example.productivitylauncher.data.pipMessage
import com.example.productivitylauncher.ui.apps.AppListScreen
import com.example.productivitylauncher.ui.apps.GatedPickerScreen
import com.example.productivitylauncher.ui.apps.HomePickerScreen
import com.example.productivitylauncher.ui.focus.EveningScreen
import com.example.productivitylauncher.ui.focus.FocusScreen
import com.example.productivitylauncher.ui.focus.WeekScreen
import com.example.productivitylauncher.ui.gate.GateScreen
import com.example.productivitylauncher.ui.gate.LockedScreen
import com.example.productivitylauncher.ui.gate.TimesUpScreen
import com.example.productivitylauncher.ui.home.HomeScreen
import com.example.productivitylauncher.ui.onboarding.OnboardingScreen
import com.example.productivitylauncher.ui.pip.PipBubble
import com.example.productivitylauncher.ui.pip.PipCard
import com.example.productivitylauncher.ui.settings.SettingsScreen
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.widgets.AddWidgetScreen
import com.example.productivitylauncher.ui.widgets.BrainDumpScreen
import com.example.productivitylauncher.ui.widgets.WidgetsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherApp(state: LauncherState, widgetHost: WidgetHost, homeSignal: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pager = rememberPagerState(pageCount = { 2 })

    var route by remember { mutableStateOf<Route>(if (state.onboarded) Route.Main else Route.Onboarding) }
    val stack = remember { ArrayList<Route>() }
    var pipOpen by remember { mutableStateOf(false) }
    var pipVariant by remember { mutableIntStateOf(0) }

    val nav = remember {
        Nav(
            go = { r ->
                if (r == Route.Main) stack.clear() else stack.add(route)
                route = r
                pipOpen = false
            },
            back = {
                route = if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) else Route.Main
            },
        )
    }

    // Pressing Home always returns to the first page.
    LaunchedEffect(homeSignal) {
        if (homeSignal > 0) {
            stack.clear()
            if (route != Route.Onboarding) route = Route.Main
            pipOpen = false
            pager.scrollToPage(0)
        }
    }

    // Runs each time the launcher comes back to the foreground.
    LaunchedEffect(state.resumeTick) {
        state.rollover()
        state.applyPending()
        state.syncSession()
        withContext(Dispatchers.IO) {
            state.refreshApps(context.packageManager)
            state.refreshUsage()
        }
        if (route != Route.Onboarding && state.consumeTimesUp()) {
            val pkg = state.sessionPkg
            stack.clear()
            route = Route.TimesUp(pkg)
        }
    }

    // Keep screen time fresh while the launcher is open.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            withContext(Dispatchers.IO) { state.refreshUsage() }
        }
    }

    // A launcher never closes: Back goes toward Home, and does nothing on Home.
    BackHandler {
        when {
            route == Route.Onboarding -> Unit
            route != Route.Main -> nav.back()
            pipOpen -> pipOpen = false
            pager.currentPage != 0 -> scope.launch { pager.animateScrollToPage(0) }
        }
    }

    fun openApp(app: AppEntry) {
        val pkg = app.packageName
        when {
            !state.isGated(pkg) -> launchApp(context, app)
            state.sessionActive && state.sessionPkg == pkg -> launchApp(context, app)
            state.focusActive && state.focusLockGated -> nav.go(Route.Locked(LockReason.Focus, pkg))
            state.limitReached -> nav.go(Route.Locked(LockReason.DailyLimit, pkg))
            else -> nav.go(Route.Gate(pkg))
        }
    }

    Box(Modifier.fillMaxSize().background(AppColors.phone).systemBarsPadding()) {
        when (val r = route) {
            Route.Main -> {
                HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                    if (page == 0) HomeScreen(state, nav, onOpenApp = { openApp(it) }, page = 0)
                    else WidgetsScreen(state, nav, widgetHost, page = 1)
                }
                if (state.pipOn) {
                    val message = pipMessage(state.pipFacts(), pipVariant)
                    PipBubble(message, onClick = { pipVariant++; pipOpen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 28.dp, bottom = 110.dp))
                    if (pipOpen) {
                        PipCard(
                            message,
                            onDismiss = { pipOpen = false },
                            onAction = { action ->
                                pipOpen = false
                                when (action) {
                                    PipAction.Focus -> nav.go(Route.Focus)
                                    PipAction.Evening -> nav.go(Route.Evening)
                                    PipAction.BrainDump -> nav.go(Route.BrainDump)
                                    PipAction.SetFrog -> scope.launch { pager.animateScrollToPage(1) }
                                    PipAction.Water -> state.updateWater(state.water + 1)
                                    PipAction.None -> Unit
                                }
                            },
                        )
                    }
                }
            }
            Route.Apps -> AppListScreen(state, nav, onOpenApp = { openApp(it) })
            is Route.Gate -> {
                val app = state.appByPackage(r.pkg)
                if (app == null) nav.back() else GateScreen(state, app, nav)
            }
            Route.BrainDump -> BrainDumpScreen(state, nav)
            Route.AddWidget -> AddWidgetScreen(state, nav, widgetHost)
            Route.Focus -> FocusScreen(state, nav)
            Route.Evening -> EveningScreen(state, nav)
            Route.Week -> WeekScreen(state, nav)
            Route.Settings -> SettingsScreen(state, nav)
            Route.GatedPicker -> GatedPickerScreen(state, nav)
            Route.HomePicker -> HomePickerScreen(state, nav)
            is Route.Locked -> LockedScreen(state, r.reason, state.appByPackage(r.pkg), nav)
            is Route.TimesUp -> TimesUpScreen(state, state.appByPackage(r.pkg), nav)
            Route.Onboarding -> OnboardingScreen(state, nav)
        }
    }
}
