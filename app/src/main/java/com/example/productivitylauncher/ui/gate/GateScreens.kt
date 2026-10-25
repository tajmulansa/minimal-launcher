package com.example.productivitylauncher.ui.gate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.data.phraseMatches
import com.example.productivitylauncher.data.phraseProgress
import com.example.productivitylauncher.service.SessionOverlayService
import com.example.productivitylauncher.ui.LockReason
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Display
import com.example.productivitylauncher.ui.theme.LocalPalette
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.Mono
import kotlinx.coroutines.delay

/**
 * The gate: three calm steps before a gated app opens, laid out like the Still prototype.
 * 1. Breathe (and wait a little longer each time you open the app today).
 * 2. Type a random phrase, so it can't be done on autopilot.
 * 3. Choose how long you will spend. A floating timer then counts it down.
 * The close button and "Not now" are always there.
 */
@Composable
fun GateScreen(state: LauncherState, app: AppEntry, nav: Nav) {
    var step by remember { mutableIntStateOf(1) }

    fun cancel() {
        state.recordBackOut()
        nav.back()
    }

    Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                for (i in 1..3) Tick(on = step >= i, Modifier.weight(1f))
            }
            Box(
                Modifier.size(40.dp).border(1.dp, AppColors.line, CircleShape).semantics { contentDescription = "Close" }.clickableRole({ cancel() }),
                contentAlignment = Alignment.Center,
            ) { AppIcon(Ic.Close, AppColors.text, size = 16.dp) }
        }
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 36.dp),
            transitionSpec = {
                (fadeIn(Motion.enter(500)) + slideInHorizontally(Motion.morph(700)) { 40 }) togetherWith
                    (fadeOut(Motion.exit(260)) + slideOutHorizontally(Motion.morph(500)) { -40 })
            },
            label = "gateStep",
        ) { s ->
            Column(Modifier.fillMaxSize()) {
                when (s) {
                    1 -> BreatheStep(state, app, onContinue = { step = 2 }, onCancel = { cancel() })
                    2 -> PhraseStep(app, onContinue = { step = 3 }, onCancel = { cancel() })
                    else -> TimeStep(state, app, nav, onCancel = { cancel() })
                }
            }
        }
    }
}

/** One of the three progress ticks: a hairline that fills from the left. */
@Composable
private fun Tick(on: Boolean, modifier: Modifier) {
    val p by animateFloatAsState(if (on) 1f else 0f, Motion.morph(700), label = "tick")
    Box(modifier.height(2.dp).background(AppColors.line)) {
        Box(Modifier.fillMaxSize().graphicsLayer { scaleX = p; transformOrigin = TransformOrigin(0f, 0.5f) }.background(AppColors.text))
    }
}

/** The big rounded call to action. Fades down when it can't be used yet. */
@Composable
private fun Cta(text: String, enabled: Boolean, onClick: () -> Unit) {
    val a by animateFloatAsState(if (enabled) 1f else 0.22f, Motion.state(300), label = "cta")
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer { alpha = a }
            .background(AppColors.text, RadiusPill)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) { AppText(text, size = 16.sp, weight = FontWeight.Medium, color = AppColors.phone, display = false) }
}

@Composable
private fun Ghost(text: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clickableRole(onClick).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        AppText(text, size = 15.sp, color = AppColors.muted, display = false)
    }
}

@Composable
private fun ColumnScope.BreatheStep(state: LauncherState, app: AppEntry, onContinue: () -> Unit, onCancel: () -> Unit) {
    val total = state.breaths
    val extraWait = remember { extraWaitSeconds(state.opensToday(app.packageName), state.growingWait) }
    val scale = remember { Animatable(0.55f) }
    var breathNo by remember { mutableIntStateOf(1) }
    var phase by remember { mutableIntStateOf(0) } // 0 in, 1 out, 2 finished
    var waitLeft by remember { mutableIntStateOf(extraWait) }

    LaunchedEffect(Unit) {
        for (i in 1..total) {
            breathNo = i
            phase = 0
            scale.animateTo(1f, orbSpec())
            phase = 1
            scale.animateTo(0.55f, orbSpec())
        }
        phase = 2
        while (waitLeft > 0) {
            delay(1000)
            waitLeft -= 1
        }
        // Like the prototype, the breath leads straight on to the next step.
        delay(500)
        onContinue()
    }
    // The soft disc trails the ring a little, as in the prototype.
    val disc = remember { Animatable(0.55f) }
    LaunchedEffect(Unit) { snapshotFlow { scale.targetValue }.collect { t -> delay(120); disc.animateTo(t, orbSpec()) } }

    AppText((app.label + " \u00B7 pause first").uppercase(), Modifier.fillMaxWidth(), size = 11.sp, weight = FontWeight.Medium, color = AppColors.muted, letterSpacing = 1.sp, mono = true, maxLines = 1)
    AppText("One slow breath\nbefore you decide.", Modifier.padding(top = 10.dp), size = 34.sp, weight = FontWeight.Medium)
    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.size(250.dp).scale(disc.value).background(AppColors.text.copy(alpha = 0.07f), CircleShape))
        Box(Modifier.size(250.dp).scale(scale.value).border(1.5.dp, AppColors.text, CircleShape))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppText(
                when (phase) { 0 -> "Breathe in"; 1 -> "Breathe out"; else -> "Good" },
                size = 26.sp, weight = FontWeight.Normal,
            )
        }
    }
    if (extraWait > 0) {
        AppText(
            "Opened ${state.opensToday(app.packageName)} time${if (state.opensToday(app.packageName) == 1) "" else "s"} today, so the wait is a little longer.",
            size = 14.sp, color = AppColors.gate, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
    }
    Ghost("Not now", onCancel)
}

@Composable
private fun ColumnScope.PhraseStep(app: AppEntry, onContinue: () -> Unit, onCancel: () -> Unit) {
    val target = remember { "I choose to open ${app.label}" }
    var typed by remember { mutableStateOf("") }
    val ok = phraseMatches(typed, target)
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { delay(500); focus.requestFocus() }
    val progress = phraseProgress(typed, target)
    val ink = AppColors.text
    val muted = AppColors.muted.copy(alpha = 0.45f)
    val warn = AppColors.gate

    Caption("Type to continue")
    // The phrase is drawn big; each letter turns dark as you get it right, and warm if it is wrong.
    val text = buildAnnotatedString {
        target.forEachIndexed { i, c ->
            val style = when {
                i < typed.length && i < progress -> SpanStyle(color = ink)
                i < typed.length -> SpanStyle(color = warn, textDecoration = TextDecoration.Underline)
                else -> SpanStyle(color = muted)
            }
            withStyle(style) { append(c) }
        }
    }
    Box(Modifier.padding(top = 30.dp).fillMaxWidth().pointerInput(Unit) { detectTapGestures { focus.requestFocus() } }) {
        androidx.compose.material3.Text(
            text = text, fontSize = 38.sp, fontFamily = Display, fontWeight = FontWeight.Medium, letterSpacing = (-1.5).sp, lineHeight = 44.sp,
        )
        // The real input is invisible; the phrase above is what you read.
        BasicTextField(
            value = typed,
            onValueChange = { new -> if (new.length - typed.length <= 3) typed = new }, // no pasting; a keyboard may add a word and a space at once
            modifier = Modifier.size(1.dp).focusRequester(focus),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None, keyboardType = androidx.compose.ui.text.input.KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { if (ok) onContinue() }),
            singleLine = true,
        )
    }
    AppText("Typing it makes you decide, not tap.", Modifier.padding(top = 18.dp), size = 15.sp, color = AppColors.muted, display = false)
    Spacer(Modifier.weight(1f))
    Cta("Continue", ok, onContinue)
    Ghost("Not now", onCancel)
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

    Caption("How long")
    AppText("How long do you\nneed?", Modifier.padding(top = 10.dp), size = 34.sp, weight = FontWeight.Medium)
    AppText("The app locks again when time is up.", Modifier.padding(top = 10.dp), size = 15.sp, color = AppColors.muted, display = false)
    if (remainingMin < 5) {
        AppText("You're almost out of gated time for today. That's a good place to stop.", Modifier.padding(top = 32.dp), size = 18.sp, color = AppColors.gate)
    } else {
        // Four big numbers; the chosen one lifts and a line slides under it.
        val cols = presets.size.coerceAtLeast(1)
        val idx = if (custom) -1 else presets.indexOf(selected).coerceAtLeast(0)
        Box(Modifier.padding(top = 36.dp).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                presets.forEach { m ->
                    val on = !custom && selected == m
                    val lift by animateDpAsState((if (on) -4 else 0).dp, Motion.morph(500), label = "lift")
                    val tint by androidx.compose.animation.animateColorAsState(if (on) AppColors.text else AppColors.muted, Motion.state(300), label = "tint")
                    Column(
                        Modifier.weight(1f).offset(y = lift).clickableRole({ custom = false; selected = m }).padding(top = 16.dp, bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AppText("$m", size = 56.sp, weight = FontWeight.Light, color = tint, letterSpacing = (-3).sp)
                        Caption("min", Modifier.padding(top = 8.dp), color = tint)
                    }
                }
            }
            if (idx >= 0) {
                val frac by animateFloatAsState(idx.toFloat() / cols, Motion.morph(600), label = "under")
                Box(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth(1f / cols).height(2.dp)
                        .graphicsLayer { translationX = frac * 0f }
                        .offset(x = androidx.compose.ui.platform.LocalConfiguration.current.let { ((it.screenWidthDp - 56) * frac).dp })
                        .background(AppColors.text),
                )
            }
        }
        Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextLink(if (custom) "Custom" else "Custom time", { custom = true })
            if (custom) Stepper("$customMin min", { customMin = (customMin - 5).coerceAtLeast(1) }, { customMin = (customMin + 5).coerceAtMost(remainingMin) }, valueWidth = 90.dp)
        }
    }
    AppText(
        (if (opens == 0) "First open today" else "Opened $opens time${if (opens == 1) "" else "s"} today") + "  ·  ${formatMillis(remainingMin * 60_000L)} of gated time left",
        Modifier.padding(top = 18.dp), size = 13.sp, color = AppColors.gate, display = false,
    )
    if (!canDrawOverlays(context)) TextLink("Allow the floating timer", { openOverlaySettings(context) })
    Spacer(Modifier.weight(1f))
    Cta(if (minutes > 0) "Open ${app.label} for $minutes min" else "Open", minutes > 0 && remainingMin >= 5) {
        state.recordOpen(app.packageName)
        state.startSession(app.packageName, minutes)
        if (canDrawOverlays(context)) SessionOverlayService.start(context, app.label, state.sessionEnd, dark)
        nav.go(Route.Main)
        launchApp(context, app)
    }
    Ghost("Not now", onCancel)
}

/** A plain full-screen message in the Still style: small label, big title, quiet paragraph, one button. */
@Composable
private fun Message(label: String, labelColor: Color, title: String, body: String, mono: String, primary: String, onPrimary: () -> Unit, secondary: String? = null, onSecondary: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 56.dp, bottom = 24.dp)) {
        Caption(label, color = labelColor)
        AppText(title, Modifier.padding(top = 10.dp), size = 40.sp, weight = FontWeight.Medium)
        AppText(body, Modifier.padding(top = 16.dp), size = 16.sp, color = AppColors.muted, lineHeight = 24.sp, display = false)
        AppText(mono, Modifier.padding(top = 24.dp), size = 14.sp, color = AppColors.muted, mono = true)
        Spacer(Modifier.weight(1f))
        Cta(primary, true, onPrimary)
        if (secondary != null) Ghost(secondary, onSecondary)
    }
}

/** Shown instead of the gate when the daily limit is used up or a focus session is running. */
@Composable
fun LockedScreen(state: LauncherState, reason: LockReason, app: AppEntry?, nav: Nav) {
    Message(
        label = "Locked",
        labelColor = AppColors.gate,
        title = if (reason == LockReason.DailyLimit) "Daily limit\nreached" else "Focus time",
        body = if (reason == LockReason.DailyLimit)
            "You've used your ${formatMillis(state.dailyLimitMin * 60_000L)} in gated apps today${if (app != null) ", so ${app.label} is closed until tomorrow" else ""}. Rest well, it opens again tomorrow."
        else
            "Gated apps are closed while your focus session runs${if (app != null) ", so ${app.label} can wait" else ""}. ${formatClock(state.focusEnd - System.currentTimeMillis())} left.",
        mono = if (reason == LockReason.DailyLimit) "[^_^] proud of you for sticking to it." else "[-_-] shh, you've got this.",
        primary = "Back to Home", onPrimary = { nav.go(Route.Main) },
    )
}

/** "Time's up" after a gated session ends. */
@Composable
fun TimesUpScreen(state: LauncherState, app: AppEntry?, nav: Nav) {
    Message(
        label = "Done",
        labelColor = AppColors.muted,
        title = "Time's up",
        body = "Your time in ${app?.label ?: "that app"} is over. Stopping on time is a real win.",
        mono = "[^o^] nicely done.",
        primary = "Back to Home", onPrimary = { nav.go(Route.Main) },
        secondary = if (app != null && !state.limitReached) "I need a bit more" else null,
        onSecondary = { if (app != null) nav.go(Route.Gate(app.packageName)) },
    )
}

private fun <T> orbSpec(): androidx.compose.animation.core.FiniteAnimationSpec<T> =
    if (Motion.enabled) androidx.compose.animation.core.tween(3000, easing = androidx.compose.animation.core.CubicBezierEasing(0.45f, 0f, 0.3f, 1f))
    else androidx.compose.animation.core.snap()
