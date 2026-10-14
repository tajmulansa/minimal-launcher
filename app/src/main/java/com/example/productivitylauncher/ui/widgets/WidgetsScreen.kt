package com.example.productivitylauncher.ui.widgets

import android.Manifest
import android.appwidget.AppWidgetHostView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.LauncherWidget
import com.example.productivitylauncher.data.URGE_LOCK_MINUTES
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.isDndOn
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openGrayscaleSettings
import com.example.productivitylauncher.data.hasTorch
import com.example.productivitylauncher.data.setTorch
import com.example.productivitylauncher.data.observeTorch
import com.example.productivitylauncher.data.WidgetHost
import com.example.productivitylauncher.data.clockLabel
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.hasCalendarPermission
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.data.todayEvents
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.IconButtonLarge
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.home.Dots
import com.example.productivitylauncher.ui.theme.AppColors

/** Second page: the widgets the user chose. Small widgets sit two to a row. Edit mode lets you move or remove them. */
@Composable
fun WidgetsScreen(state: LauncherState, nav: Nav, host: WidgetHost, page: Int) {
    var editing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        PageHeader("Widgets", trailing = {
            IconButtonLarge(if (editing) Ic.Check else Ic.Edit, if (editing) "Done editing" else "Edit widgets", { editing = !editing })
            IconButtonLarge(Ic.Plus, "Add widget", { nav.go(Route.AddWidget) })
            IconButtonLarge(Ic.Gear, "Settings", { nav.go(Route.Settings) })
        })
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.widgets.isEmpty()) {
                AppText("No widgets yet. Tap + to add one.", color = AppColors.muted, modifier = Modifier.padding(vertical = 24.dp))
            }
            val ids = state.widgets
            fun isHalf(id: String) = LauncherWidget.byId(id)?.half == true
            var i = 0
            while (i < ids.size) {
                val id = ids[i]
                val next = ids.getOrNull(i + 1)
                if (isHalf(id) && next != null && isHalf(next)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { WidgetSlot(id, true, editing, state, nav, host) }
                        Box(Modifier.weight(1f)) { WidgetSlot(next, true, editing, state, nav, host) }
                    }
                    i += 2
                } else {
                    WidgetSlot(id, false, editing, state, nav, host)
                    i += 1
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Dots(page)
        Spacer(Modifier.height(20.dp))
    }
}

/** One widget, with move and remove buttons above it while editing. */
@Composable
private fun WidgetSlot(id: String, compact: Boolean, editing: Boolean, state: LauncherState, nav: Nav, host: WidgetHost) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (editing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    EditButton(Ic.Up, "Move up") { state.moveWidget(id, -1) }
                    EditButton(Ic.Down, "Move down") { state.moveWidget(id, 1) }
                }
                EditButton(Ic.Close, "Remove widget") {
                    if (id.startsWith("ext:")) id.removePrefix("ext:").toIntOrNull()?.let { host.delete(it) }
                    state.removeWidgetId(id)
                }
            }
        }
        if (id.startsWith("ext:")) ExternalWidget(id.removePrefix("ext:").toIntOrNull(), state, host)
        else when (LauncherWidget.byId(id)) {
            LauncherWidget.Frog -> TodoWidget(state, nav)
            LauncherWidget.Water -> WaterWidget(state, compact)
            LauncherWidget.Budget -> BudgetWidget(state, compact)
            LauncherWidget.Agenda -> AgendaWidget()
            LauncherWidget.Habits -> HabitsWidget(state)
            LauncherWidget.Note -> NoteWidget(state, compact)
            LauncherWidget.Dump -> DumpWidget(state, nav, compact)
            LauncherWidget.Quick -> QuickActionsWidget(state, nav)
            null -> Unit
        }
    }
}

@Composable
private fun EditButton(ic: Ic, description: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clickableRole(onClick), contentAlignment = Alignment.Center) {
        AppIcon(ic, AppColors.muted, size = 20.dp, description = description)
    }
}

@Composable
private fun WidgetHeader(label: String, accent: Boolean = false, link: String? = null, onLink: () -> Unit = {}) {
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
private fun QuickActionsWidget(state: LauncherState, nav: Nav) {
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

    CeramicCard(padding = PaddingValues(16.dp)) {
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
private fun QuickTile(label: String, icon: Ic, on: Boolean, onClick: () -> Unit, modifier: Modifier, danger: Boolean = false) {
    val bg = if (on) AppColors.primary else AppColors.phone
    val fg = when { on -> AppColors.onPrimary; danger -> AppColors.gate; else -> AppColors.text }
    Column(
        modifier
            .semantics { contentDescription = if (danger) "$label, lock the phone for $URGE_LOCK_MINUTES minutes" else "$label, " + if (on) "on" else "off" }
            .clickableRole(onClick, if (danger) Role.Button else Role.Switch),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(bg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            AppIcon(icon, fg, size = 22.dp)
        }
        AppText(label, size = 11.sp, weight = FontWeight.Medium, color = AppColors.muted, maxLines = 1, align = TextAlign.Center)
    }
}

// ------------------------------------------------------------------ to-do list

/**
 * A plain to-do list. One task is the most important of the day: it is bold and first, and the rest unlock once it is checked.
 * Tap a task to show its actions.
 */
@Composable
private fun TodoWidget(state: LauncherState, nav: Nav) {
    var adding by remember { mutableStateOf(state.frog.isBlank() && state.tasks.isEmpty()) }
    var draft by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var editingMain by remember { mutableStateOf(false) }
    var mainDraft by remember { mutableStateOf("") }
    val locked = state.tasksLocked
    fun submit() {
        val text = draft.trim()
        if (text.isNotEmpty()) { if (state.frog.isBlank()) state.updateFrog(text) else state.addTask(text) }
        draft = ""
        adding = false
    }
    CeramicCard(padding = PaddingValues(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            AppText("Today", size = 15.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.frog.isNotBlank() && !state.frogDone) {
                    Box(Modifier.heightIn(min = 44.dp).clickableRole({ nav.go(Route.Focus) }).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                        AppText("Focus", size = 14.sp, weight = FontWeight.Medium, color = AppColors.muted)
                    }
                }
                Box(Modifier.size(44.dp).clickableRole({ adding = !adding }), contentAlignment = Alignment.Center) {
                    AppIcon(if (adding) Ic.Close else Ic.Plus, AppColors.text, size = 20.dp, description = if (adding) "Close" else "Add a task")
                }
            }
        }

        // The most important task: bold, first.
        if (state.frog.isNotBlank()) {
            TaskRow(
                done = state.frogDone, text = state.frog, important = true, dimmed = false,
                editing = editingMain, editDraft = mainDraft, onEditDraft = { mainDraft = it },
                onEditDone = { if (mainDraft.isNotBlank()) state.updateFrog(mainDraft.trim()); editingMain = false },
                onCheck = { state.setFrogDone(!state.frogDone) },
                onSelect = { selected = if (selected == "main") null else "main" },
                actions = if (selected == "main") listOf(
                    Ic.Edit to { mainDraft = state.frog; editingMain = true; selected = null },
                    Ic.Close to { state.updateFrog(""); selected = null },
                ) else emptyList(),
            )
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
            Box(Modifier.padding(end = 10.dp, top = 4.dp)) {
                AppField(draft, { draft = it }, if (state.frog.isBlank()) "Your most important task today" else "Add a task", onDone = { submit() })
            }
        } else if (state.frog.isBlank() && state.tasks.isEmpty()) {
            AppText("Nothing here yet. Tap + to add a task.", size = 14.sp, color = AppColors.muted, modifier = Modifier.padding(bottom = 6.dp))
        }
        if (locked && state.tasks.isNotEmpty()) {
            AppText("Do the bold task first. The rest unlock after.", size = 12.sp, color = AppColors.muted, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

/** One task line: round check, text, and (when tapped) its action icons. */
@Composable
private fun TaskRow(
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
    Row(
        Modifier.fillMaxWidth().alpha(if (dimmed) 0.45f else 1f),
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
                weight = if (important) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 22.sp,
                color = if (done) AppColors.muted else AppColors.text, strike = done,
            )
        }
        actions.forEach { (ic, run) ->
            Box(Modifier.size(44.dp).clickableRole(run), contentAlignment = Alignment.Center) {
                AppIcon(ic, AppColors.muted, size = 18.dp, description = if (ic == Ic.Close) "Remove" else if (ic == Ic.Star) "Make most important" else "Edit")
            }
        }
    }
}

// ------------------------------------------------------------------ water

@Composable
private fun WaterWidget(state: LauncherState, compact: Boolean) {
    if (compact) {
        CeramicCard(padding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth()) {
            Caption("Water")
            Row(verticalAlignment = Alignment.Bottom) {
                AppText("${state.water}", size = 28.sp, weight = FontWeight.Medium, letterSpacing = (-1).sp)
                AppText(" / ${state.waterGoal}", size = 14.sp, color = AppColors.muted, modifier = Modifier.padding(bottom = 4.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallRound(Ic.Minus, "One less glass") { state.updateWater(state.water - 1) }
                SmallRound(Ic.Plus, "One more glass") { state.updateWater(state.water + 1) }
            }
        }
        return
    }
    CeramicCard {
        WidgetHeader("Water")
        Row(verticalAlignment = Alignment.Bottom) {
            AppText("${state.water}", size = 34.sp, weight = FontWeight.Medium, letterSpacing = (-1).sp)
            AppText(" / ${state.waterGoal} glasses", size = 16.sp, color = AppColors.muted, modifier = Modifier.padding(bottom = 5.dp))
        }
        val total = state.waterGoal
        val rows = (0 until total).chunked(8)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { i ->
                        val on = i < state.water
                        Box(
                            Modifier.weight(1f).height(34.dp).background(if (on) AppColors.focus.copy(alpha = 0.2f) else AppColors.phone, RoundedCornerShape(10.dp))
                                .clickableRole({ state.updateWater(if (on && i == state.water - 1) i else i + 1) }),
                            contentAlignment = Alignment.Center,
                        ) { AppIcon(Ic.Drop, if (on) AppColors.focus else AppColors.dot, size = 16.dp) }
                    }
                    repeat(8 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Stepper("${state.water}", { state.updateWater(state.water - 1) }, { state.updateWater(state.water + 1) }, valueWidth = 40.dp)
            TextLink("Goal: ${state.waterGoal}", { state.updateWaterGoal(if (state.waterGoal >= 12) 4 else state.waterGoal + 1) })
        }
    }
}

@Composable
private fun SmallRound(ic: Ic, description: String, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).background(AppColors.phone, RoundedCornerShape(12.dp)).clickableRole(onClick), contentAlignment = Alignment.Center) {
        AppIcon(ic, AppColors.text, size = 18.dp, description = description)
    }
}

// ------------------------------------------------------------------ distraction budget

@Composable
private fun BudgetWidget(state: LauncherState, compact: Boolean) {
    val context = LocalContext.current
    val limit = state.dailyLimitMin * 60_000L
    val used = state.gatedUsedMs
    val fraction = if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 0f
    val barColor = if (fraction >= 1f) AppColors.gate else AppColors.text
    if (compact) {
        CeramicCard(padding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth()) {
            Caption("Distraction")
            AppText(formatMillis((limit - used).coerceAtLeast(0)), size = 24.sp, weight = FontWeight.Medium, letterSpacing = (-0.5).sp)
            AppText("left today", size = 12.sp, color = AppColors.muted)
            Box(Modifier.fillMaxWidth().height(6.dp).background(AppColors.phone, RoundedCornerShape(3.dp))) {
                Box(Modifier.fillMaxWidth(fraction).height(6.dp).background(barColor, RoundedCornerShape(3.dp)))
            }
        }
        return
    }
    CeramicCard {
        WidgetHeader("Distraction budget")
        Box(Modifier.fillMaxWidth().height(10.dp).background(AppColors.phone, RoundedCornerShape(5.dp))) {
            Box(Modifier.fillMaxWidth(fraction).height(10.dp).background(barColor, RoundedCornerShape(5.dp)))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AppText("Used ${formatMillis(used)}", size = 13.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
            AppText("Left ${formatMillis((limit - used).coerceAtLeast(0))}", size = 13.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
        }
        if (!state.usageAccess) {
            AppText("Estimated from the times you chose at the gate. Allow usage access for exact numbers.", size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
            CButton("Allow usage access", { openUsageAccessSettings(context) }, kind = BtnKind.Soft, small = true)
        }
    }
}

// ------------------------------------------------------------------ agenda

@Composable
private fun AgendaWidget() {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasCalendarPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val events = remember(granted) { if (granted) todayEvents(context) else emptyList() }
    CeramicCard {
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
                            Modifier.weight(1f).background(if (active) AppColors.primary else AppColors.phone, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            AppText(e.title, size = 15.sp, weight = FontWeight.Medium, color = if (active) AppColors.onPrimary else AppColors.text, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ habits

@Composable
private fun HabitsWidget(state: LauncherState) {
    var adding by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }
    CeramicCard {
        WidgetHeader("Habit stack", link = if (adding) null else "Add", onLink = { adding = true })
        if (state.habits.isEmpty() && !adding) {
            AppText("Link a new habit to one you already have. For example: after I brush my teeth, I read one page.", size = 14.sp, color = AppColors.muted, lineHeight = 20.sp)
        }
        val today = dayKey()
        state.habits.forEach { h ->
            val done = today in h.doneDays
            Row(
                Modifier.fillMaxWidth().background(AppColors.phone, RoundedCornerShape(14.dp)).clickableRole({ state.toggleHabit(h.id) }).padding(horizontal = 14.dp, vertical = 12.dp),
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

// ------------------------------------------------------------------ note

@Composable
private fun NoteWidget(state: LauncherState, compact: Boolean) {
    CeramicCard(padding = if (compact) PaddingValues(16.dp) else PaddingValues(22.dp), modifier = Modifier.fillMaxWidth()) {
        if (compact) Caption("Note") else WidgetHeader("Quick note")
        AppField(state.note, { state.updateNote(it) }, "Jot one thing…", singleLine = false)
    }
}

// ------------------------------------------------------------------ brain dump

@Composable
private fun DumpWidget(state: LauncherState, nav: Nav, compact: Boolean) {
    var text by remember { mutableStateOf("") }
    if (compact) {
        CeramicCard(padding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth(), onClick = { nav.go(Route.BrainDump) }) {
            Caption("Brain dump")
            AppText(if (state.dumps.isEmpty()) "Empty" else "${state.dumps.size} saved", size = 24.sp, weight = FontWeight.Medium, letterSpacing = (-0.5).sp)
            AppText("Tap to open", size = 12.sp, color = AppColors.muted)
        }
        return
    }
    CeramicCard {
        WidgetHeader("Brain dump", link = if (state.dumps.isEmpty()) "Open" else "${state.dumps.size} saved", onLink = { nav.go(Route.BrainDump) })
        AppField(text, { text = it }, "Empty your head here…", onDone = { state.addDump(text); text = "" })
    }
}

// ------------------------------------------------------------------ other apps' widgets

@Composable
private fun ExternalWidget(appWidgetId: Int?, state: LauncherState, host: WidgetHost) {
    val context = LocalContext.current
    if (appWidgetId == null) return
    val info = remember(appWidgetId) { host.manager.getAppWidgetInfo(appWidgetId) }
    if (info == null) {
        // The app that provided this widget was removed.
        CeramicCard {
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
    CeramicCard(padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
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
