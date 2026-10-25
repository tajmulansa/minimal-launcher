package com.example.productivitylauncher.ui.widgets

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
internal fun WidgetHeader(label: String, accent: Boolean = false, link: String? = null, onLink: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Caption(label, color = if (accent) AppColors.focus else AppColors.muted)
        if (link != null) {
            Row(Modifier.clickableRole(onLink).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                AppText(link, size = 13.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
                AppIcon(Ic.Chevron, AppColors.muted, size = 14.dp)
            }
        }
    }
}

// ------------------------------------------------------------------ quick actions

/** Plain square buttons, four across, each with a one-word label under it. Filled when switched on. */
@Composable
internal fun QuickActionsWidget(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    var confirmUrge by remember { mutableStateOf(false) }
    if (confirmUrge) {
        AlertDialog(
            onDismissRequest = { confirmUrge = false },
            containerColor = AppColors.card,
            title = { AppText("Lock the phone for $URGE_LOCK_MINUTES minutes?", size = 20.sp, weight = FontWeight.SemiBold) },
            text = { AppText("The launcher will show only a timer, with Call and Message. It can't be stopped early.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp) },
            confirmButton = { TextButton(onClick = { confirmUrge = false; state.startUrgeLock() }) { AppText("Lock", weight = FontWeight.SemiBold, color = AppColors.gate) } },
            dismissButton = { TextButton(onClick = { confirmUrge = false }) { AppText("Cancel", weight = FontWeight.SemiBold) } },
        )
    }

    var torch by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val stop = observeTorch(context) { torch = it }
        onDispose { stop() }
    }
    var dnd by remember { mutableStateOf(isDndOn(context)) }

    Section {
        val tiles = buildList<@Composable (Modifier) -> Unit> {
            add { m -> QuickTile("Lock In", Ic.Lock, state.lockIn, { state.updateLockIn(!state.lockIn) }, m) }
            add { m -> QuickTile("Break", Ic.Pause, false, { confirmUrge = true }, m, danger = true) }
            add { m -> QuickTile("Gray", Ic.Contrast, false, { openGrayscaleSettings(context) }, m) }
            if (hasTorch(context)) add { m -> QuickTile("Light", Ic.Bolt, torch, { setTorch(context, !torch) }, m) }
            add { m -> QuickTile("Silent", Ic.Moon, dnd, {
                if (!hasDndAccess(context)) openDndSettings(context)
                else { dnd = !dnd; setDnd(context, dnd) }
            }, m) }
            add { m -> QuickTile("Focus", Ic.Target, false, { nav.go(Route.Focus) }, m) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            tiles.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { tile -> tile(Modifier.weight(1f)) }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** A square icon button with its label underneath. The red one only tints its icon. */
@Composable
internal fun QuickTile(label: String, icon: Ic, on: Boolean, onClick: () -> Unit, modifier: Modifier, danger: Boolean = false) {
    val bg by animateColorAsState(if (on) AppColors.primary else Color.Transparent, Motion.state(), label = "tileBg")
    val fg by animateColorAsState(when { on -> AppColors.onPrimary; danger -> AppColors.gate; else -> AppColors.text }, Motion.state(), label = "tileFg")
    Column(
        modifier
            .semantics { contentDescription = if (danger) "$label, lock the phone for $URGE_LOCK_MINUTES minutes" else "$label, " + if (on) "on" else "off" }
            .clickableRole(onClick, if (danger) Role.Button else Role.Switch),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(bg, CircleShape).border(1.dp, if (on) Color.Transparent else AppColors.line, CircleShape), contentAlignment = Alignment.Center) {
            AppIcon(icon, fg, size = 22.dp)
        }
        AppText(label.uppercase(), size = 10.sp, weight = FontWeight.Medium, color = AppColors.muted, maxLines = 1, align = TextAlign.Center, letterSpacing = 0.8.sp, mono = true)
    }
}


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
internal fun Tile(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    val base = modifier.fillMaxWidth()
    Box(if (onClick != null) base.clickableRole(onClick) else base, content = content)
}

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

@Composable
private fun ThinBar(progress: Float, color: Color) {
    val shown by animateFloatAsState(progress.coerceIn(0f, 1f), Motion.fill(), label = "bar")
    Box(Modifier.fillMaxWidth().height(3.dp).background(AppColors.line, RoundedCornerShape(2.dp))) {
        Box(Modifier.fillMaxWidth(shown).height(3.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun RoundAction(ic: Ic, description: String, filled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).background(if (filled) AppColors.primary else Color.Transparent, CircleShape).border(1.dp, if (filled) Color.Transparent else AppColors.line, CircleShape).clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) { AppIcon(ic, if (filled) AppColors.onPrimary else AppColors.text, size = 18.dp, description = description) }
}

// ------------------------------------------------------------------ focus

@Composable
internal fun FocusTile(state: LauncherState, nav: Nav) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.focusEnd) {
        while (state.focusEnd > System.currentTimeMillis()) { now = System.currentTimeMillis(); delay(1000) }
        now = System.currentTimeMillis()
    }
    val active = state.focusEnd > now
    val left = (state.focusEnd - now).coerceAtLeast(0)
    val progress = if (active && state.focusTotalMs > 0) left.toFloat() / state.focusTotalMs else 1f
    val ring = AppColors.focus
    val track = AppColors.line
    Tile(onClick = { nav.go(Route.Focus) }) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Caption("Focus")
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Ring(progress, 76.dp, 2.dp, ring, track) {
                    if (active) AppText(formatClock(left), size = 15.sp, weight = FontWeight.SemiBold)
                    else AppIcon(Ic.Play, AppColors.text, size = 22.dp)
                }
            }
            AppText(
                if (active) "Running" else if (state.frog.isBlank()) "Add a task first" else "${state.frogMinutes} min",
                size = 12.sp, color = AppColors.muted, maxLines = 1,
            )
        }
    }
}

// ------------------------------------------------------------------ water

@Composable
internal fun WaterTile(state: LauncherState) {
    Tile {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Caption("Water")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    AppText("${state.water}", size = 40.sp, weight = FontWeight.Medium, letterSpacing = (-1.5).sp)
                    AppText(
                        " / ${state.waterGoal}", Modifier.padding(bottom = 7.dp).clickableRole({ state.updateWaterGoal(if (state.waterGoal >= 12) 4 else state.waterGoal + 1) }),
                        size = 16.sp, color = AppColors.muted,
                    )
                }
                ThinBar(if (state.waterGoal > 0) state.water.toFloat() / state.waterGoal else 0f, AppColors.focus)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RoundAction(Ic.Minus, "One less glass", false) { state.updateWater(state.water - 1) }
                RoundAction(Ic.Plus, "One more glass", true) { state.updateWater(state.water + 1) }
            }
        }
    }
}

// ------------------------------------------------------------------ gated apps time

@Composable
internal fun BudgetTile(state: LauncherState) {
    val context = LocalContext.current
    val limit = state.dailyLimitMin * 60_000L
    val used = state.gatedUsedMs
    val left = (limit - used).coerceAtLeast(0)
    val over = limit > 0 && used >= limit
    Tile(onClick = if (!state.usageAccess) ({ openUsageAccessSettings(context) }) else null) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Caption("Gated apps")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppText(formatMillis(left), size = 34.sp, weight = FontWeight.Medium, letterSpacing = (-1).sp, color = if (over) AppColors.gate else AppColors.text, maxLines = 1)
                ThinBar(if (limit > 0) used.toFloat() / limit else 0f, if (over) AppColors.gate else AppColors.text)
            }
            AppText(
                if (over) "Limit reached" else if (!state.usageAccess) "Tap for exact time" else "left today",
                size = 12.sp, color = if (over) AppColors.gate else AppColors.muted, maxLines = 1,
            )
        }
    }
}

// ------------------------------------------------------------------ exam countdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamTile(state: LauncherState) {
    var open by remember { mutableStateOf(false) }
    val has = state.examDay > 0
    val days = if (has) daysUntil(state.examDay) else 0L
    Tile(onClick = { open = true }) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Caption("Exam")
            if (!has) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Box(Modifier.size(44.dp).border(1.dp, AppColors.line, CircleShape), contentAlignment = Alignment.Center) {
                        AppIcon(Ic.Plus, AppColors.text, size = 18.dp)
                    }
                }
                AppText("Add your exam", size = 12.sp, color = AppColors.muted)
            } else {
                AppText(
                    if (days < 0) "Done" else "$days", size = 40.sp, weight = FontWeight.Medium, letterSpacing = (-1.5).sp,
                    color = if (days in 0..3) AppColors.gate else AppColors.text, maxLines = 1,
                )
                AppText(
                    (if (days < 0) "" else if (days == 1L) "day · " else "days · ") + state.examName.ifBlank { "Exam" },
                    size = 12.sp, color = AppColors.muted, maxLines = 1,
                )
            }
        }
    }
    if (open) {
        var name by remember { mutableStateOf(state.examName) }
        var showPicker by remember { mutableStateOf(false) }
        val picker = rememberDatePickerState(initialSelectedDateMillis = if (has) state.examDay * 86_400_000L else null)
        val dateText = picker.selectedDateMillis?.let {
            java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date(it))
        } ?: "Pick a date"
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = AppColors.card,
            title = { AppText("Exam countdown", size = 20.sp, weight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppField(name, { name = it }, "Name, e.g. Physics final")
                    CButton(dateText, { showPicker = true }, Modifier.fillMaxWidth(), BtnKind.Soft, small = true, leading = Ic.Calendar)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.updateExam(name.trim(), (picker.selectedDateMillis ?: 0L) / 86_400_000L)
                        open = false
                    },
                    enabled = picker.selectedDateMillis != null,
                ) { AppText("Save", weight = FontWeight.SemiBold) }
            },
            dismissButton = {
                Row {
                    if (has) TextButton(onClick = { state.updateExam("", 0L); open = false }) { AppText("Remove", weight = FontWeight.SemiBold, color = AppColors.gate) }
                    TextButton(onClick = { open = false }) { AppText("Cancel", weight = FontWeight.SemiBold) }
                }
            },
        )
        if (showPicker) {
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = { TextButton(onClick = { showPicker = false }) { AppText("OK", weight = FontWeight.SemiBold) } },
            ) { DatePicker(state = picker) }
        }
    }
}

// ------------------------------------------------------------------ brain dump

@Composable
internal fun DumpTile(state: LauncherState, nav: Nav) {
    Tile(onClick = { nav.go(Route.BrainDump) }) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Caption("Brain dump")
            AppText(if (state.dumps.isEmpty()) "0" else "${state.dumps.size}", size = 40.sp, weight = FontWeight.Medium, letterSpacing = (-1.5).sp)
            AppText(if (state.dumps.isEmpty()) "Tap to empty your head" else "saved · tap to open", size = 12.sp, color = AppColors.muted, maxLines = 1)
        }
    }
}

// ------------------------------------------------------------------ note

@Composable
internal fun NoteWidget(state: LauncherState) {
    Section {
        Caption("Quick note")
        AppField(state.note, { state.updateNote(it) }, "Jot one thing…", singleLine = false)
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
            Caption("Today")
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

// ------------------------------------------------------------------ agenda

@Composable
internal fun AgendaWidget() {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasCalendarPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val events = remember(granted) { if (granted) todayEvents(context) else emptyList() }
    Section {
        WidgetHeader("Agenda")
        when {
            !granted -> {
                AppText("Show today's events from your calendar. Read only, nothing leaves your phone.", size = 14.sp, color = AppColors.muted, lineHeight = 20.sp)
                CButton("Allow calendar", { launcher.launch(Manifest.permission.READ_CALENDAR) }, kind = BtnKind.Soft, small = true)
            }
            events.isEmpty() -> AppText("Nothing on your calendar today.", color = AppColors.muted)
            else -> {
                val now = System.currentTimeMillis()
                events.forEach { e ->
                    val active = now in e.begin until e.end
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppText(if (e.allDay) "All day" else clockLabel(e.begin), Modifier.width(52.dp), size = 13.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
                        Box(
                            Modifier.weight(1f).hairlineBelow().padding(vertical = 12.dp),
                        ) {
                            AppText(e.title, size = 16.sp, weight = if (active) FontWeight.SemiBold else FontWeight.Normal, color = if (active) AppColors.text else AppColors.muted, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ habits

@Composable
internal fun HabitsWidget(state: LauncherState) {
    var adding by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }
    Section {
        WidgetHeader("Habit stack", link = if (adding) null else "Add", onLink = { adding = true })
        if (state.habits.isEmpty() && !adding) {
            AppText("Link a new habit to one you already have. For example: after I brush my teeth, I read one page.", size = 14.sp, color = AppColors.muted, lineHeight = 20.sp)
        }
        val today = dayKey()
        state.habits.forEach { h ->
            val done = today in h.doneDays
            Row(
                Modifier.fillMaxWidth().hairlineBelow().clickableRole({ state.toggleHabit(h.id) }).padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CheckCircle(done, size = 26.dp)
                Column(Modifier.weight(1f)) {
                    AppText("I ${h.action}", size = 15.sp, weight = FontWeight.Medium, color = if (done) AppColors.muted else AppColors.text, strike = done)
                    AppText("After ${h.anchor}", size = 12.sp, color = AppColors.muted)
                }
                Box(Modifier.size(44.dp).clickableRole({ state.removeHabit(h.id) }), contentAlignment = Alignment.Center) {
                    AppIcon(Ic.Close, AppColors.muted, size = 16.dp, description = "Remove habit")
                }
            }
        }
        if (adding) {
            AppField(anchor, { anchor = it }, "After I… (e.g. brush my teeth)")
            AppField(action, { action = it }, "I will… (e.g. read one page)", onDone = {
                state.addHabit(anchor, action); anchor = ""; action = ""; adding = false
            })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CButton("Add habit", { state.addHabit(anchor, action); anchor = ""; action = ""; adding = false }, Modifier.weight(1f), BtnKind.Accent, small = true, enabled = anchor.isNotBlank() && action.isNotBlank())
                CButton("Cancel", { adding = false }, Modifier.weight(1f), BtnKind.Soft, small = true)
            }
        }
    }
}

// ------------------------------------------------------------------ other apps' widgets

@Composable
internal fun ExternalWidget(appWidgetId: Int?, state: LauncherState, host: WidgetHost) {
    val context = LocalContext.current
    if (appWidgetId == null) return
    val info = remember(appWidgetId) { host.manager.getAppWidgetInfo(appWidgetId) }
    if (info == null) {
        // The app that provided this widget was removed.
        Section {
            WidgetHeader("Widget unavailable", link = "Remove", onLink = { host.delete(appWidgetId); state.removeWidgetId("ext:$appWidgetId") })
            AppText("This widget's app is no longer installed.", color = AppColors.muted)
        }
        return
    }
    val density = context.resources.displayMetrics.density
    val defaultHeight = (info.minHeight / density).toInt().coerceIn(80, 400)
    val heightDp = state.widgetHeight("ext:$appWidgetId", defaultHeight)
    val widthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp - 80
    var resizing by remember { mutableStateOf(false) }
    val label = remember(appWidgetId) { info.loadLabel(context.packageManager) }
    Section(padding = PaddingValues(0.dp)) {
        WidgetHeader(label, link = if (resizing) "Done" else "Resize", onLink = { resizing = !resizing })
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(heightDp.dp),
            factory = { ctx ->
                host.host.createView(ctx, appWidgetId, info).also { view: AppWidgetHostView ->
                    view.setAppWidget(appWidgetId, info)
                }
            },
            update = { view -> view.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp) },
        )
        if (resizing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Stepper("${heightDp}dp", { state.updateWidgetHeight("ext:$appWidgetId", heightDp - 40) }, { state.updateWidgetHeight("ext:$appWidgetId", heightDp + 40) }, valueWidth = 64.dp)
                CButton("Remove", { host.delete(appWidgetId); state.removeWidgetId("ext:$appWidgetId") }, kind = BtnKind.Soft, small = true, leading = Ic.Trash)
            }
        }
    }
}
