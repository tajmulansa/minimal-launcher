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

/** Second page: the widgets the user chose, launcher widgets first-class and other apps' widgets alongside. */
@Composable
fun WidgetsScreen(state: LauncherState, nav: Nav, host: WidgetHost, page: Int) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Widgets", trailing = {
            IconButtonLarge(Ic.Sliders, "Settings", { nav.go(Route.Settings) })
            IconButtonLarge(Ic.Plus, "Add widget", { nav.go(Route.AddWidget) })
        })
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.widgets.isEmpty()) {
                AppText("No widgets yet. Tap + to add one.", color = AppColors.muted, modifier = Modifier.padding(vertical = 24.dp))
            }
            state.widgets.forEach { id ->
                when {
                    id.startsWith("ext:") -> ExternalWidget(id.removePrefix("ext:").toIntOrNull(), state, host)
                    else -> when (LauncherWidget.byId(id)) {
                        LauncherWidget.Frog -> TodoWidget(state, nav)
                        LauncherWidget.Water -> WaterWidget(state)
                        LauncherWidget.Budget -> BudgetWidget(state)
                        LauncherWidget.Agenda -> AgendaWidget()
                        LauncherWidget.Habits -> HabitsWidget(state)
                        LauncherWidget.Note -> NoteWidget(state)
                        LauncherWidget.Dump -> DumpWidget(state, nav)
                        LauncherWidget.Quick -> QuickActionsWidget(state, nav)
                        null -> Unit
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Dots(page)
        Spacer(Modifier.height(20.dp))
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

/** A grid of square one-tap buttons, four across. Every tile has the same layout: icon above, one-word label below. */
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

    CeramicCard {
        Caption("Quick actions")
        val tiles = buildList<@Composable (Modifier) -> Unit> {
            add { m -> QuickTile("Lock In", Ic.Lock, state.lockIn, { state.updateLockIn(!state.lockIn) }, m) }
            add { m -> QuickTile("Break", Ic.Pause, false, { confirmUrge = true }, m, danger = true) }
            add { m -> QuickTile("Grayscale", Ic.Contrast, false, { openGrayscaleSettings(context) }, m) }
            if (hasTorch(context)) add { m -> QuickTile("Light", Ic.Bolt, torch, { setTorch(context, !torch) }, m) }
            add { m -> QuickTile("Silent", Ic.Moon, dnd, {
                if (!hasDndAccess(context)) openDndSettings(context)
                else { dnd = !dnd; setDnd(context, dnd) }
            }, m) }
            add { m -> QuickTile("Focus", Ic.Target, false, { nav.go(Route.Focus) }, m) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { tile -> tile(Modifier.weight(1f).aspectRatio(1f)) }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (state.lockIn) {
            AppText("Lock In is on. Only your Home apps show. Type an app's full name in All apps to find any other.", size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
        }
    }
}

/** One square tile. Filled when switched on; the red one (danger) is always filled red. */
@Composable
private fun QuickTile(label: String, icon: Ic, on: Boolean, onClick: () -> Unit, modifier: Modifier, danger: Boolean = false) {
    val bg = when { danger -> AppColors.gate; on -> AppColors.primary; else -> AppColors.phone }
    val fg = when { danger -> AppColors.onGate; on -> AppColors.onPrimary; else -> AppColors.text }
    Box(
        modifier
            .background(bg, RoundedCornerShape(16.dp))
            .semantics { contentDescription = if (danger) "$label, lock the phone for $URGE_LOCK_MINUTES minutes" else "$label, " + if (on) "on" else "off" }
            .clickableRole(onClick, if (danger) Role.Button else Role.Switch),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppIcon(icon, fg, size = 24.dp)
            AppText(label, size = 11.sp, weight = FontWeight.SemiBold, color = fg, maxLines = 1, align = TextAlign.Center)
        }
    }
}

// ------------------------------------------------------------------ to-do list

/** A normal to-do list. One task is the frog for the day: it is bold, sits first, and the rest unlock once it is checked. */
@Composable
private fun TodoWidget(state: LauncherState, nav: Nav) {
    var adding by remember { mutableStateOf(state.frog.isBlank() && state.tasks.isEmpty()) }
    var draft by remember { mutableStateOf("") }
    var editingFrog by remember { mutableStateOf(false) }
    var frogDraft by remember { mutableStateOf("") }
    val locked = state.tasksLocked
    fun submit() {
        val text = draft.trim()
        if (text.isNotEmpty()) { if (state.frog.isBlank()) state.updateFrog(text) else state.addTask(text) }
        draft = ""
        adding = false
    }
    CeramicCard {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Caption("Today")
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.frog.isNotBlank() && !state.frogDone) TextLink("Focus ${state.frogMinutes} min", { nav.go(Route.Focus) })
                Box(Modifier.size(44.dp).clickableRole({ adding = !adding }), contentAlignment = Alignment.Center) {
                    AppIcon(if (adding) Ic.Close else Ic.Plus, AppColors.text, size = 22.dp, description = if (adding) "Close" else "Add a task")
                }
            }
        }

        // The frog: bold, first.
        if (state.frog.isNotBlank()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(44.dp).clickableRole({ state.setFrogDone(!state.frogDone) }, Role.Checkbox), contentAlignment = Alignment.Center) {
                    CheckCircle(state.frogDone, size = 30.dp)
                }
                if (editingFrog) {
                    Box(Modifier.weight(1f)) {
                        AppField(frogDraft, { frogDraft = it }, "Your frog", onDone = {
                            if (frogDraft.isNotBlank()) state.updateFrog(frogDraft.trim())
                            editingFrog = false
                        })
                    }
                } else {
                    AppText(
                        state.frog, Modifier.weight(1f).clickableRole({ frogDraft = state.frog; editingFrog = true }),
                        size = 18.sp, weight = FontWeight.Bold, lineHeight = 24.sp,
                        color = if (state.frogDone) AppColors.muted else AppColors.text, strike = state.frogDone,
                    )
                }
            }
            if (state.tasks.isNotEmpty()) Rule()
        }

        // The other tasks.
        state.tasks.forEach { t ->
            Row(
                Modifier.fillMaxWidth().alpha(if (locked && !t.done) 0.45f else 1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(44.dp).clickableRole({ state.toggleTask(t.id) }, Role.Checkbox), contentAlignment = Alignment.Center) {
                    CheckCircle(t.done, size = 26.dp)
                }
                AppText(t.text, Modifier.weight(1f), size = 16.sp, color = if (t.done) AppColors.muted else AppColors.text, strike = t.done)
                if (!state.frogDone && !t.done) TextLink("Make frog", { state.makeFrog(t.id) })
                Box(Modifier.size(44.dp).clickableRole({ state.removeTask(t.id) }), contentAlignment = Alignment.Center) {
                    AppIcon(Ic.Close, AppColors.muted, size = 16.dp, description = "Remove task")
                }
            }
        }

        if (adding) {
            AppField(draft, { draft = it }, if (state.frog.isBlank()) "Your first task becomes today's frog" else "Add a task", onDone = { submit() })
        } else if (state.frog.isBlank() && state.tasks.isEmpty()) {
            AppText("Nothing here yet. Tap + to add a task.", size = 14.sp, color = AppColors.muted)
        }
        if (locked && state.tasks.isNotEmpty()) {
            AppText("The bold task is your frog. Check it first, then the rest unlock.", size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
        }
    }
}

// ------------------------------------------------------------------ water

@Composable
private fun WaterWidget(state: LauncherState) {
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

// ------------------------------------------------------------------ distraction budget

@Composable
private fun BudgetWidget(state: LauncherState) {
    val context = LocalContext.current
    val limit = state.dailyLimitMin * 60_000L
    val used = state.gatedUsedMs
    val fraction = if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 0f
    CeramicCard {
        WidgetHeader("Distraction budget")
        Box(Modifier.fillMaxWidth().height(12.dp).background(AppColors.phone, RoundedCornerShape(6.dp))) {
            Box(Modifier.fillMaxWidth(fraction).height(12.dp).background(if (fraction >= 1f) AppColors.gate else AppColors.text, RoundedCornerShape(6.dp)))
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
private fun NoteWidget(state: LauncherState) {
    CeramicCard {
        WidgetHeader("Quick note")
        AppField(state.note, { state.updateNote(it) }, "Jot one thing…", singleLine = false)
    }
}

// ------------------------------------------------------------------ brain dump

@Composable
private fun DumpWidget(state: LauncherState, nav: Nav) {
    var text by remember { mutableStateOf("") }
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
