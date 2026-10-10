package com.example.productivitylauncher.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.example.productivitylauncher.ui.pip.PipChip
import com.example.productivitylauncher.ui.pip.PipDock
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
    var pipChip by remember { mutableStateOf<String?>(null) }
    // Pip tucks against the screen edge when nobody touches it. pipTouch restarts the idle timer.
    var pipDocked by remember { mutableStateOf(false) }
    var pipTouch by remember { mutableIntStateOf(0) }
    // Pip always knows the time: this ticks every minute so greetings and reminders stay current.
    val minute by produceState(System.currentTimeMillis() / 60_000L) {
        while (true) { delay(60_000L - System.currentTimeMillis() % 60_000L); value = System.currentTimeMillis() / 60_000L }
    }

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
                    // Reading these makes Pip choose again when the minute, the resume or the frog changes.
                    @Suppress("UNUSED_VARIABLE") val clockTick = minute + state.resumeTick
                    val message = pipMessage(state.pipFacts(), pipVariant)

                    // The moment the frog is eaten, Pip says well done in a small speech chip.
                    var wasDone by remember { mutableStateOf(state.frogDone) }
                    LaunchedEffect(state.frogDone) {
                        val justEaten = state.frogDone && !wasDone
                        wasDone = state.frogDone
                        if (justEaten) {
                            pipChip = pipMessage(state.pipFacts(), 0).text
                            delay(9_000L)
                            pipChip = null
                        }
                    }
                    val chip = pipChip

                    // A chip needs Pip out beside it. Otherwise, after a quiet moment, Pip tucks itself away.
                    val chipShowing = chip != null
                    LaunchedEffect(chipShowing) { if (chipShowing) pipDocked = false }
                    LaunchedEffect(pipDocked, pipOpen, chipShowing, pipTouch) {
                        if (!pipDocked && !pipOpen && !chipShowing) {
                            delay(PipDock.IdleMillis)
                            pipDocked = true
                        }
                    }

                    if (chip != null && !pipOpen && !pipDocked) {
                        PipChip(chip, onClick = { pipChip = null; pipOpen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 96.dp, bottom = 116.dp).widthIn(max = 240.dp))
                    }
                    PipBubble(
                        message,
                        docked = pipDocked,
                        onClick = {
                            pipTouch++
                            // First touch brings a tucked-away Pip out; the next one opens its message.
                            if (pipDocked) pipDocked = false else { pipVariant++; pipChip = null; pipOpen = true }
                        },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 110.dp),
                    )
                    if (pipOpen) {
                        // Keep the text Pip started with, so it does not change while it is typing.
                        val cardMessage = remember(pipOpen) { message }
                        PipCard(
                            cardMessage,
                            timeLabel = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
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
