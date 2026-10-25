package com.example.productivitylauncher.ui.today

import android.Manifest
import android.appwidget.AppWidgetHostView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import com.example.productivitylauncher.ui.components.Rolling
import com.example.productivitylauncher.ui.theme.Motion
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.LauncherWidget
import com.example.productivitylauncher.data.URGE_LOCK_MINUTES
import com.example.productivitylauncher.data.WidgetHost
import com.example.productivitylauncher.data.clockLabel
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.daysUntil
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.hasCalendarPermission
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.hasTorch
import com.example.productivitylauncher.data.isDndOn
import com.example.productivitylauncher.data.observeTorch
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openGrayscaleSettings
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.data.setTorch
import com.example.productivitylauncher.data.todayEvents
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.hairlineBelow
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.IconButtonLarge
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.RadiusLg
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.home.Dots
import com.example.productivitylauncher.ui.theme.AppColors
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import androidx.compose.foundation.layout.BoxScope

@Composable
import com.example.productivitylauncher.data.DAY_MS
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.ui.home.Hair
import com.example.productivitylauncher.ui.home.QuickRow
import com.example.productivitylauncher.ui.components.TextLink
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

// ------------------------------------------------------------------ building blocks

/** A flat block of a widget: no box, no fill. Sections are separated by hairlines on the page. */
@Composable
internal fun Section(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier.fillMaxWidth()
    Column((if (onClick != null) base.clickableRole(onClick) else base).padding(padding), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

/** A small widget: also flat and full width. */
@Composable
/** A ring that fills as [progress] goes from 0 to 1. */
@Composable
internal fun Ring(progress: Float, diameter: Dp, stroke: Dp, color: Color, track: Color, content: @Composable () -> Unit = {}) {
    val shown by animateFloatAsState(progress.coerceIn(0f, 1f), Motion.fill(), label = "ring")
    Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(diameter)) {
            val w = stroke.toPx()
            val arc = Size(this.size.width - w, this.size.height - w)
            drawArc(track, 0f, 360f, false, topLeft = Offset(w / 2, w / 2), size = arc, style = Stroke(w))
            val p = shown
            if (p > 0f) drawArc(color, -90f, 360f * p, false, topLeft = Offset(w / 2, w / 2), size = arc, style = Stroke(w, cap = StrokeCap.Round))
        }
        content()
    }
}

// ------------------------------------------------------------------ to-do list

/**
 * A plain to-do list. One task is the most important of the day: it sits first, bold, in a soft pill,
 * and the rest unlock once it is checked. Tap a task to show its actions.
 */
@Composable
internal fun TodoWidget(state: LauncherState) {
    var adding by remember { mutableStateOf(state.frog.isBlank() && state.tasks.isEmpty()) }
    var draft by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var editingMain by remember { mutableStateOf(false) }
    var mainDraft by remember { mutableStateOf("") }
    val locked = state.tasksLocked
    val haptic = LocalHapticFeedback.current
    val pillAlpha by animateFloatAsState(if (state.frogDone) 0.24f else 0.10f, Motion.state(200), label = "pill")
    val hasMain = state.frog.isNotBlank()
    val total = state.tasks.size + if (hasMain) 1 else 0
    val done = state.tasks.count { it.done } + if (hasMain && state.frogDone) 1 else 0
    fun submit() {
        val text = draft.trim()
        if (text.isNotEmpty()) { if (state.frog.isBlank()) state.updateFrog(text) else state.addTask(text) }
        draft = ""
        adding = false
    }
    Section(modifier = Modifier.animateContentSize(Motion.lift()), padding = PaddingValues(0.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Caption("Tasks")
            if (total > 0) Rolling("$done of $total") { AppText(it, size = 13.sp, weight = FontWeight.SemiBold, color = AppColors.muted) }
        }

        if (hasMain) {
            Box(Modifier.fillMaxWidth()) {
                TaskRow(
                    done = state.frogDone, text = state.frog, important = true, dimmed = false,
                    editing = editingMain, editDraft = mainDraft, onEditDraft = { mainDraft = it },
                    onEditDone = { if (mainDraft.isNotBlank()) state.updateFrog(mainDraft.trim()); editingMain = false },
                    onCheck = { if (!state.frogDone) haptic.performHapticFeedback(HapticFeedbackType.LongPress); state.setFrogDone(!state.frogDone) },
                    onSelect = { selected = if (selected == "main") null else "main" },
                    actions = if (selected == "main") listOf(
                        Ic.Edit to { mainDraft = state.frog; editingMain = true; selected = null },
                        Ic.Close to { state.updateFrog(""); selected = null },
                    ) else emptyList(),
                )
            }
        }

        state.tasks.forEach { t ->
            TaskRow(
                done = t.done, text = t.text, important = false, dimmed = locked && !t.done,
                onCheck = { state.toggleTask(t.id) },
                onSelect = { selected = if (selected == t.id) null else t.id },
                actions = if (selected == t.id) buildList {
                    if (!state.frogDone && !t.done) add(Ic.Star to { state.makeFrog(t.id); selected = null })
                    add(Ic.Close to { state.removeTask(t.id); selected = null })
                } else emptyList(),
            )
        }

        if (adding) {
            if (!hasMain && state.tasks.isEmpty()) {
                AppText("What is the one thing that matters most today?", size = 15.sp, color = AppColors.muted, lineHeight = 21.sp, modifier = Modifier)
            }
            Box(Modifier.padding(bottom = 6.dp)) {
                AppField(draft, { draft = it }, if (!hasMain) "e.g. Finish chemistry chapter 4" else "Add a task", onDone = { submit() })
            }
        } else {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).clickableRole({ adding = true }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppIcon(Ic.Plus, AppColors.muted, size = 18.dp)
                AppText("Add a task", size = 15.sp, color = AppColors.muted)
            }
        }
        if (locked && state.tasks.isNotEmpty()) {
            AppText("Do the bold task first. The rest unlock after.", size = 12.sp, color = AppColors.muted, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        }
    }
}

/** One task line: round check, text, and (when tapped) its action icons. */
@Composable
internal fun TaskRow(
    done: Boolean,
    text: String,
    important: Boolean,
    dimmed: Boolean,
    onCheck: () -> Unit,
    onSelect: () -> Unit,
    actions: List<Pair<Ic, () -> Unit>>,
    editing: Boolean = false,
    editDraft: String = "",
    onEditDraft: (String) -> Unit = {},
    onEditDone: () -> Unit = {},
) {
    val taskColor by animateColorAsState(if (done) AppColors.muted else AppColors.text, Motion.state(220), label = "taskColor")
    val rowAlpha by animateFloatAsState(if (dimmed) 0.45f else 1f, Motion.state(220), label = "rowAlpha")
    Row(
        Modifier.fillMaxWidth().alpha(rowAlpha).hairlineBelow(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clickableRole(onCheck, Role.Checkbox), contentAlignment = Alignment.CenterStart) {
            CheckCircle(done, size = if (important) 24.dp else 22.dp)
        }
        if (editing) {
            Box(Modifier.weight(1f)) { AppField(editDraft, onEditDraft, "Your most important task", onDone = onEditDone) }
        } else {
            AppText(
                text, Modifier.weight(1f).clickableRole(onSelect).padding(vertical = 10.dp),
                size = if (important) 17.sp else 16.sp,
                weight = if (important) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 22.sp,
                color = taskColor, strike = done,
            )
        }
        actions.forEach { (ic, run) ->
            Box(Modifier.size(44.dp).clickableRole(run), contentAlignment = Alignment.Center) {
                AppIcon(ic, AppColors.muted, size = 18.dp, description = if (ic == Ic.Close) "Remove" else if (ic == Ic.Star) "Make most important" else "Edit")
            }
        }
    }
}


// ------------------------------------------------------------------ the Today page

/** The Today page, as in the prototype: Focus ring, quick actions, week dots, screen time and tasks. */
@Composable
fun TodayScreen(state: LauncherState, nav: Nav, page: Int, onOpenHome: () -> Unit) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val active = state.focusActive
    LaunchedEffect(state.focusEnd) {
        while (state.focusEnd > System.currentTimeMillis()) { now = System.currentTimeMillis(); delay(500) }
        now = System.currentTimeMillis()
        if (state.focusEnd > 0L) {
            state.stopFocus()
            if (state.focusDnd) setDnd(context, false)
        }
    }
    fun start() {
        if (state.focusDnd && hasDndAccess(context)) setDnd(context, true)
        state.startFocus(state.frogMinutes)
    }
    fun stop() {
        state.stopFocus()
        if (state.focusDnd) setDnd(context, false)
    }

    val total = state.tasks.size + if (state.frog.isNotBlank()) 1 else 0
    val done = state.tasks.count { it.done } + if (state.frog.isNotBlank() && state.frogDone) 1 else 0

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 12.dp)) {
            Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Caption("Today")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (total > 0) Caption("$done of $total done")
                    IconButtonLarge(Ic.Sliders, "Settings", { nav.go(Route.Settings) })
                }
            }
            AppText("Focus", Modifier.padding(top = 12.dp).enterStagger(0), size = 44.sp, weight = FontWeight.Medium, letterSpacing = (-2).sp)

            val left = (state.focusEnd - now).coerceAtLeast(0)
            val totalMs = if (active) state.focusTotalMs else state.frogMinutes * 60_000L
            val progress = if (active && totalMs > 0) (left.toFloat() / totalMs).coerceIn(0f, 1f) else 1f
            Box(Modifier.padding(top = 26.dp).fillMaxWidth().enterStagger(1), contentAlignment = Alignment.Center) {
                Ring(progress, 236.dp, 2.dp, AppColors.focus, AppColors.line) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppText(if (active) formatClock(left) else "%02d:00".format(state.frogMinutes), size = 60.sp, weight = FontWeight.Light, letterSpacing = (-3).sp)
                        Caption(if (state.frog.isBlank()) "Add a task below" else state.frog, Modifier.padding(horizontal = 36.dp), )
                    }
                }
            }
            Row(Modifier.padding(top = 22.dp).fillMaxWidth().enterStagger(2), horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                if (!active) StepCircle("−5") { state.updateFrogMinutes(state.frogMinutes - 5) }
                val canStart = active || state.frog.isNotBlank()
                Box(
                    Modifier
                        .height(56.dp)
                        .widthIn(min = 132.dp)
                        .alpha(if (canStart) 1f else 0.3f)
                        .background(AppColors.text, RadiusPill)
                        .then(if (canStart) Modifier.clickableRole({ if (active) stop() else start() }) else Modifier),
                    contentAlignment = Alignment.Center,
                ) { AppText(if (active) "End" else "Start", size = 16.sp, weight = FontWeight.Medium, color = AppColors.phone, display = false) }
                if (!active) StepCircle("+5") { state.updateFrogMinutes(state.frogMinutes + 5) }
            }

            Hair(0, Modifier.padding(vertical = 22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Caption("Quick actions"); Caption("Pull down on Home") }
            Box(Modifier.padding(top = 14.dp)) { QuickRow(state) }

            Hair(1, Modifier.padding(vertical = 22.dp))
            WeekDots(state)

            Hair(1, Modifier.padding(vertical = 22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Caption("Screen time")
                Caption(state.screenTimeMs?.let { formatMillis(it) } ?: "")
            }
            ScreenBars(state, context)

            Hair(2, Modifier.padding(vertical = 22.dp))
            TodoWidget(state)
        }
        Dots(page, onSelect = { if (it == 0) onOpenHome() })
    }
}

@Composable
private fun StepCircle(text: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).border(1.dp, AppColors.line, CircleShape).clickableRole(onClick), contentAlignment = Alignment.Center) {
        AppText(text, size = 12.sp, mono = true)
    }
}

/** Seven small circles, one per day: filled when that day's most important task was done. */
@Composable
private fun WeekDots(state: LauncherState) {
    val now = System.currentTimeMillis()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        for (i in 6 downTo 0) {
            val t = now - i * DAY_MS
            val on = dayKey(t) in state.eatenDays
            var shown by remember { mutableStateOf(!Motion.enabled) }
            LaunchedEffect(Unit) { delay(300L + 70L * (6 - i)); shown = true }
            val a by animateFloatAsState(if (shown) 1f else 0f, Motion.enter(450), label = "wd")
            Column(Modifier.graphicsLayer { scaleX = 0.4f + 0.6f * a; scaleY = 0.4f + 0.6f * a; alpha = a }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(10.dp).background(if (on) AppColors.text else Color.Transparent, CircleShape).border(1.5.dp, if (on) AppColors.text else AppColors.muted, CircleShape))
                AppText(java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(java.util.Date(t)).take(1).uppercase(), size = 10.sp, mono = true, color = AppColors.muted)
            }
        }
    }
}

/** One thin bar per day of the last week. */
@Composable
private fun ScreenBars(state: LauncherState, context: android.content.Context) {
    val week = state.weekMs
    if (week.size != 7) {
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText("Allow usage access to see your screen time.", size = 15.sp, color = AppColors.muted, display = false)
            TextLink("Allow usage access", { openUsageAccessSettings(context) }, Modifier.align(Alignment.Start))
        }
        return
    }
    val now = System.currentTimeMillis()
    val max = maxOf(state.screenGoalMin * 60_000L, week.max()).coerceAtLeast(1L)
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        week.forEachIndexed { i, ms ->
            var shown by remember { mutableStateOf(!Motion.enabled) }
            LaunchedEffect(Unit) { delay(500L + 90L * i); shown = true }
            val p by animateFloatAsState(if (shown) (ms.toFloat() / max).coerceIn(0f, 1f) else 0f, Motion.slow(1200), label = "bar")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppText(java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(java.util.Date(now - (6 - i) * DAY_MS)), Modifier.width(56.dp), size = 15.sp, display = false, color = if (i == 6) AppColors.text else AppColors.muted)
                Box(Modifier.weight(1f).height(3.dp).background(AppColors.line, RoundedCornerShape(2.dp))) {
                    Box(Modifier.fillMaxWidth(p).height(3.dp).background(AppColors.text, RoundedCornerShape(2.dp)))
                }
                AppText(formatMillis(ms), Modifier.width(52.dp), size = 11.sp, mono = true, color = AppColors.muted, align = TextAlign.End)
            }
        }
    }
}
