package com.example.productivitylauncher.ui.gate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.data.extraWaitSeconds
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.generatePhrase
import com.example.productivitylauncher.data.launchApp
import com.example.productivitylauncher.data.letterOf
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.data.phraseMatches
import com.example.productivitylauncher.data.phraseProgress
import com.example.productivitylauncher.service.SessionOverlayService
import com.example.productivitylauncher.ui.LockReason
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppBadge
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.LocalPalette
import kotlinx.coroutines.delay

/**
 * The gate: three calm steps before a gated app opens.
 * 1. Breathe (and wait a little longer each time you open the app today).
 * 2. Type a random phrase, so it can't be done on autopilot.
 * 3. Choose how long you will spend. A floating timer then counts it down.
 * Cancel is always the big, easy button.
 */
@Composable
fun GateScreen(state: LauncherState, app: AppEntry, nav: Nav) {
    var step by remember { mutableIntStateOf(1) }

    fun cancel() {
        state.recordBackOut()
        nav.back()
    }

    Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppBadge(letterOf(app.label), gated = true, size = 64.dp)
            AppText(app.label, size = 20.sp, weight = FontWeight.SemiBold, color = AppColors.gate)
            Caption(
                when (step) { 1 -> "Step 1 of 3 · Breathe"; 2 -> "Step 2 of 3 · Type"; else -> "Step 3 of 3 · Choose time" },
            )
        }
        when (step) {
            1 -> BreatheStep(state, app, onContinue = { step = 2 }, onCancel = { cancel() })
            2 -> PhraseStep(state, onContinue = { step = 3 }, onCancel = { cancel() })
            else -> TimeStep(state, app, nav, onCancel = { cancel() })
        }
    }
}

@Composable
private fun ColumnScope.BreatheStep(state: LauncherState, app: AppEntry, onContinue: () -> Unit, onCancel: () -> Unit) {
    val total = state.breaths
    val extraWait = remember { extraWaitSeconds(state.opensToday(app.packageName), state.growingWait) }
    val scale = remember { Animatable(0.8f) }
    var breathNo by remember { mutableIntStateOf(1) }
    var phase by remember { mutableIntStateOf(0) } // 0 in, 1 out, 2 finished
    var waitLeft by remember { mutableIntStateOf(extraWait) }

    LaunchedEffect(Unit) {
        for (i in 1..total) {
            breathNo = i
            phase = 0
            scale.animateTo(1.2f, tween(4000, easing = FastOutSlowInEasing))
            phase = 1
            scale.animateTo(0.8f, tween(4000, easing = FastOutSlowInEasing))
        }
        phase = 2
        while (waitLeft > 0) {
            delay(1000)
            waitLeft -= 1
        }
    }

    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(180.dp)
                    .scale(scale.value)
                    .shadow(12.dp, CircleShape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                    .background(AppColors.card, CircleShape)
                    .border(8.dp, if (phase == 0) AppColors.focus else AppColors.focus.copy(alpha = 0.35f), CircleShape),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppText(
                    when (phase) { 0 -> "Breathe in…"; 1 -> "Breathe out…"; else -> "Well done" },
                    size = 20.sp, weight = FontWeight.Medium,
                )
                if (phase < 2) AppText("Breath $breathNo of $total", size = 13.sp, color = AppColors.muted, modifier = Modifier.padding(top = 4.dp))
            }
        }
        AppText(
            "Take $total slow breaths. Then decide if you really want to open this.",
            size = 16.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 24.sp, modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp),
        )
        if (extraWait > 0) {
            AppText(
                "Opened ${state.opensToday(app.packageName)} time${if (state.opensToday(app.packageName) == 1) "" else "s"} today, so the wait is a little longer.",
                size = 14.sp, weight = FontWeight.SemiBold, color = AppColors.gate, align = TextAlign.Center, modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CButton("Cancel, go back", onCancel, Modifier.fillMaxWidth(), BtnKind.Primary)
        val ready = phase == 2 && waitLeft == 0
        TextLink(
            when {
                phase < 2 -> "Continue (after $total breaths)"
                waitLeft > 0 -> "Continue in ${waitLeft}s"
                else -> "Continue"
            },
            onContinue, enabled = ready,
        )
    }
}

@Composable
private fun ColumnScope.PhraseStep(state: LauncherState, onContinue: () -> Unit, onCancel: () -> Unit) {
    val target = remember { generatePhrase(state.phraseWords) }
    var typed by remember { mutableStateOf("") }
    val ok = phraseMatches(typed, target)

    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AppText("Type this to continue", size = 26.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp, align = TextAlign.Center)
        AppText("Typing it slows you down, just enough to choose on purpose.", size = 15.sp, color = AppColors.muted, align = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp))
        Box(
            Modifier.padding(top = 24.dp).fillMaxWidth().shadow(6.dp, RadiusMd, ambientColor = Color(0x14000000), spotColor = Color(0x14000000)).background(AppColors.card, RadiusMd).padding(22.dp),
            contentAlignment = Alignment.Center,
        ) { AppText(target, size = 19.sp, letterSpacing = 1.sp, align = TextAlign.Center, lineHeight = 27.sp, mono = true) }
        Spacer(Modifier.height(16.dp))
        AppField(
            value = typed,
            onChange = { new ->
                // Pasting is blocked: only one character may be added at a time.
                if (new.length - typed.length <= 1) typed = new
            },
            placeholder = "type here",
            mono = true, center = true,
            fill = if (ok) AppColors.focus.copy(alpha = 0.15f) else AppColors.phone,
        )
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            AppText("${phraseProgress(typed, target)} / ${target.length}", size = 12.sp, weight = FontWeight.Bold, color = AppColors.muted)
            AppText(if (ok) "Matched" else "Match it exactly", size = 12.sp, weight = FontWeight.Bold, color = if (ok) AppColors.focus else AppColors.muted)
        }
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CButton("Cancel, go back", onCancel, Modifier.fillMaxWidth(), BtnKind.Primary)
        TextLink("Continue", onContinue, enabled = ok)
    }
}

@Composable
private fun ColumnScope.TimeStep(state: LauncherState, app: AppEntry, nav: Nav, onCancel: () -> Unit) {
    val context = LocalContext.current
    val dark = LocalPalette.current.dark
    val remainingMin = ((state.dailyLimitMin * 60_000L - state.gatedUsedMs).coerceAtLeast(0) / 60_000L).toInt()
    val presets = listOf(5, 10, 15, 30).filter { it <= remainingMin }
    var selected by remember { mutableIntStateOf(presets.firstOrNull() ?: 0) }
    var custom by remember { mutableStateOf(false) }
    var customMin by remember { mutableIntStateOf(minOf(20, remainingMin).coerceAtLeast(1)) }
    val minutes = if (custom) customMin else selected
    val opens = state.opensToday(app.packageName)

    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AppText("How long?", size = 28.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp)
        AppText("Pick a time. A small timer will stay on screen so you know when it's up.", size = 15.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp))
        if (remainingMin < 5) {
            AppText("You're almost out of gated time for today. That's a good place to stop.", size = 16.sp, weight = FontWeight.Medium, color = AppColors.gate, align = TextAlign.Center, modifier = Modifier.padding(top = 24.dp))
        } else {
            Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                presets.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { m ->
                            TimeChoice("$m min", !custom && selected == m, Modifier.weight(1f)) { custom = false; selected = m }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                TimeChoice("Custom", custom, Modifier.fillMaxWidth()) { custom = true }
                if (custom) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Stepper("$customMin min", { customMin = (customMin - 5).coerceAtLeast(1) }, { customMin = (customMin + 5).coerceAtMost(remainingMin) }, valueWidth = 90.dp)
                    }
                }
            }
        }
        AppText(
            if (opens == 0) "First open today" else "Opened $opens time${if (opens == 1) "" else "s"} today",
            size = 15.sp, weight = FontWeight.SemiBold, color = AppColors.gate, modifier = Modifier.padding(top = 18.dp),
        )
        AppText("${formatMillis(remainingMin * 60_000L)} of gated time left today", size = 13.sp, color = AppColors.muted, modifier = Modifier.padding(top = 4.dp))
        if (!canDrawOverlays(context)) {
            TextLink("Allow the floating timer", { openOverlaySettings(context) })
        }
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CButton("Cancel, go back", onCancel, Modifier.fillMaxWidth(), BtnKind.Primary)
        TextLink(
            if (minutes > 0) "Open ${app.label} for $minutes min" else "Open",
            {
                state.recordOpen(app.packageName)
                state.startSession(app.packageName, minutes)
                if (canDrawOverlays(context)) {
                    SessionOverlayService.start(context, app.label, state.sessionEnd, dark)
                }
                nav.go(Route.Main)
                launchApp(context, app)
            },
            enabled = minutes > 0 && remainingMin >= 5,
        )
    }
}

@Composable
private fun TimeChoice(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .shadow(if (selected) 6.dp else 3.dp, RadiusMd, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .background(AppColors.card, RadiusMd)
            .border(2.dp, if (selected) AppColors.text else Color.Transparent, RadiusMd)
            .clickableRole(onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(text, size = 17.sp, weight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

/** Shown instead of the gate when the daily limit is used up or a focus session is running. */
@Composable
fun LockedScreen(state: LauncherState, reason: LockReason, app: AppEntry?, nav: Nav) {
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(96.dp).background(AppColors.gateSoft, CircleShape), contentAlignment = Alignment.Center) {
                AppIcon(Ic.Lock, AppColors.gate, size = 40.dp)
            }
            AppText(
                if (reason == LockReason.DailyLimit) "Daily limit reached" else "Focus time",
                size = 28.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp, modifier = Modifier.padding(top = 24.dp),
            )
            AppText(
                if (reason == LockReason.DailyLimit)
                    "You've used your ${formatMillis(state.dailyLimitMin * 60_000L)} in gated apps today${if (app != null) ", so ${app.label} is closed until tomorrow" else ""}. Rest well, it opens again tomorrow."
                else
                    "Gated apps are closed while your focus session runs${if (app != null) ", so ${app.label} can wait" else ""}. ${formatClock(state.focusEnd - System.currentTimeMillis())} left.",
                size = 16.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 24.sp, modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
            )
            AppText(
                if (reason == LockReason.DailyLimit) "[^_^] proud of you for sticking to it." else "[-_-] shh, you've got this.",
                size = 14.sp, color = AppColors.muted, mono = true, align = TextAlign.Center, modifier = Modifier.padding(top = 18.dp),
            )
        }
        CButton("Back to Home", { nav.go(Route.Main) }, Modifier.fillMaxWidth(), BtnKind.Primary)
    }
}

/** "Time's up" after a gated session ends. */
@Composable
fun TimesUpScreen(state: LauncherState, app: AppEntry?, nav: Nav) {
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(96.dp).background(AppColors.focus.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                AppIcon(Ic.Check, AppColors.focus, size = 40.dp)
            }
            AppText("Time's up", size = 32.sp, weight = FontWeight.SemiBold, letterSpacing = (-1).sp, modifier = Modifier.padding(top = 24.dp))
            AppText(
                "Your time in ${app?.label ?: "that app"} is over. Stopping on time is a real win.",
                size = 16.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 24.sp, modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
            )
            AppText("[^o^] nicely done.", size = 14.sp, color = AppColors.muted, mono = true, modifier = Modifier.padding(top = 18.dp))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CButton("Back to Home", { nav.go(Route.Main) }, Modifier.fillMaxWidth(), BtnKind.Primary)
            if (app != null && !state.limitReached) TextLink("I need a bit more", { nav.go(Route.Gate(app.packageName)) })
        }
    }
}
