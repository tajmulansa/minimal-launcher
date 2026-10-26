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
import com.example.productivitylauncher.ui.theme.AppColors
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import androidx.compose.foundation.layout.BoxScope

import com.example.productivitylauncher.data.DAY_MS
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.runtime.key
import com.example.productivitylauncher.data.Task
import com.example.productivitylauncher.ui.theme.Sans
import com.example.productivitylauncher.ui.theme.enterStagger
import androidx.compose.foundation.layout.widthIn
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.ui.home.Hair
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

/** The to-do list exactly like the prototype: check circle, text that gets struck through, hairline per row. Long-press a task to remove it. */
@Composable
internal fun TodoList(state: LauncherState) {
    var draft by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        state.allTasks.forEachIndexed { i, t ->
            key(t.id) {
                TodoRow(
                    t, Modifier.enterStagger(i, rise = 14.dp),
                    onToggle = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); state.toggleAny(t.id) },
                    onRemove = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); state.removeAny(t.id) },
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppText("+", size = 16.sp, mono = true, color = AppColors.muted)
            BasicTextField(
                value = draft, onValueChange = { draft = it }, singleLine = true,
                textStyle = TextStyle(color = AppColors.text, fontSize = 17.sp, fontFamily = Sans, letterSpacing = (-0.17).sp),
                cursorBrush = SolidColor(AppColors.text),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { state.addAny(draft); draft = "" }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (draft.isEmpty()) AppText("Add a task", size = 17.sp, color = AppColors.muted, display = false)
                        inner()
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoRow(t: Task, modifier: Modifier, onToggle: () -> Unit, onRemove: () -> Unit) {
    val strike by animateFloatAsState(if (t.done) 1f else 0f, Motion.state(450), label = "strike")
    val alpha by animateFloatAsState(if (t.done) 0.4f else 1f, Motion.state(400), label = "tkAlpha")
    val line = AppColors.text
    Row(
        modifier.fillMaxWidth().hairlineBelow().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.semantics { contentDescription = "Toggle task" }.clickableRole(onToggle, Role.Checkbox)) { CheckCircle(t.done, size = 30.dp) }
        Box(
            Modifier
                .weight(1f)
                .combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggle, onLongClick = onRemove)
                .graphicsLayer { this.alpha = alpha }
                .drawWithContent {
                    drawContent()
                    if (strike > 0f) {
                        val y = size.height * 0.55f
                        drawLine(line, Offset(0f, y), Offset(size.width * strike, y), strokeWidth = 1.5.dp.toPx())
                    }
                },
        ) {
            AppText(t.text, size = 17.sp, letterSpacing = (-0.17).sp, display = false)
        }
    }
}


// ------------------------------------------------------------------ the Today page

/** The Today page, as in the prototype: Focus ring, quick actions, week dots, screen time and tasks. */
@Composable
fun TodayScreen(state: LauncherState, nav: Nav, host: WidgetHost, page: Int, onOpenHome: () -> Unit) {
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

    val total = state.allTasks.size
    val done = state.allTasks.count { it.done }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 150.dp)) {
            Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Caption("Study")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (total > 0) Caption("$done/$total done")
                    IconButtonLarge(Ic.Plus, "Add widget", { nav.go(Route.AddWidget) })
                    IconButtonLarge(Ic.Sliders, "Settings", { nav.go(Route.Settings) })
                }
            }
            AppText("Focus", Modifier.padding(top = 12.dp).enterStagger(0), size = 44.sp, weight = FontWeight.Medium, letterSpacing = (-2).sp)

            val left = (state.focusEnd - now).coerceAtLeast(0)
            val totalMs = if (active) state.focusTotalMs else state.frogMinutes * 60_000L
            val progress = if (active && totalMs > 0) (left.toFloat() / totalMs).coerceIn(0f, 1f) else 1f
            Box(Modifier.padding(top = 40.dp).fillMaxWidth().enterStagger(1), contentAlignment = Alignment.Center) {
                Ring(progress, 236.dp, 2.dp, AppColors.focus, AppColors.line) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppText(if (active) formatClock(left) else "%02d:00".format(state.frogMinutes), size = 60.sp, weight = FontWeight.Light, letterSpacing = (-3).sp, tnum = true)
                        AppText((state.nextTask?.text ?: if (total == 0) "Add a task below" else "Rest").uppercase(), Modifier.padding(horizontal = 12.dp), size = 11.sp, weight = FontWeight.Medium, color = AppColors.muted, letterSpacing = 1.sp, mono = true, maxLines = 1, align = TextAlign.Center)
                    }
                }
            }
            Row(Modifier.padding(top = 44.dp).fillMaxWidth().enterStagger(2), horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                if (!active) StepCircle("−5") { state.updateFrogMinutes(state.frogMinutes - 5) }
                val canStart = active || state.nextTask != null
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
            WeekDots(state)

            Hair(1, Modifier.padding(vertical = 22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Caption("Screen time")
                Caption(state.screenTimeMs?.let { formatMillis(it) } ?: "")
            }
            ScreenBars(state, context)

            Hair(2, Modifier.padding(vertical = 22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Caption("Tasks")
                if (total > 0) Caption("$done of $total")
            }
            TodoList(state)

            if (state.hasWidget("habits")) {
                Hair(3, Modifier.padding(vertical = 22.dp))
                HabitStack(state)
            }

            val ext = state.widgets.filter { it.startsWith("ext:") }
            ext.forEach { id ->
                Hair(3, Modifier.padding(vertical = 22.dp))
                ExternalWidget(id.removePrefix("ext:").toIntOrNull(), state, host)
            }
            Spacer(Modifier.height(24.dp))
        }
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
    var streak = 0
    while (streak < 400 && dayKey(now - streak * DAY_MS) in state.eatenDays) streak++
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Caption("$streak day\nstreak", Modifier.align(Alignment.CenterVertically))
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

/** One thin bar for each of today's most used apps, like the prototype. */
@Composable
private fun ScreenBars(state: LauncherState, context: android.content.Context) {
    if (!state.usageAccess) {
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText("Allow usage access to see your screen time.", size = 15.sp, color = AppColors.muted, display = false)
            TextLink("Allow usage access", { openUsageAccessSettings(context) }, Modifier.align(Alignment.Start))
        }
        return
    }
    val top = state.appMsToday.entries
        .sortedByDescending { it.value }
        .mapNotNull { e -> state.appByPackage(e.key)?.let { it.label to e.value } }
        .filter { it.second >= 60_000L }
        .take(5)
    if (top.isEmpty()) {
        AppText("Nothing used yet today.", Modifier.padding(top = 14.dp), size = 15.sp, color = AppColors.muted, display = false)
        return
    }
    val max = top.maxOf { it.second }.coerceAtLeast(1L)
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        top.forEachIndexed { i, (name, ms) ->
            var shown by remember { mutableStateOf(!Motion.enabled) }
            LaunchedEffect(Unit) { delay(500L + 90L * i); shown = true }
            val p by animateFloatAsState(if (shown) (ms.toFloat() / max).coerceIn(0f, 1f) else 0f, Motion.slow(1200), label = "bar")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppText(name, Modifier.width(86.dp), size = 15.sp, maxLines = 1, display = false)
                Box(Modifier.weight(1f).height(3.dp).background(AppColors.line, RoundedCornerShape(2.dp))) {
                    Box(Modifier.fillMaxWidth(p).height(3.dp).background(AppColors.text, RoundedCornerShape(2.dp)))
                }
                AppText(formatMillis(ms), Modifier.width(44.dp), size = 11.sp, mono = true, color = AppColors.muted, align = TextAlign.End)
            }
        }
    }
}

/** A widget from another app, shown flat on the Today page. Resize it or remove it from here. */
@Composable
internal fun ExternalWidget(appWidgetId: Int?, state: LauncherState, host: WidgetHost) {
    val context = LocalContext.current
    if (appWidgetId == null) return
    val info = remember(appWidgetId) { host.manager.getAppWidgetInfo(appWidgetId) }
    if (info == null) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Caption("Widget unavailable")
                Caption("Remove", Modifier.clickableRole({ host.delete(appWidgetId); state.removeWidgetId("ext:$appWidgetId") }), color = AppColors.gate)
            }
            AppText("This widget's app is no longer installed.", size = 15.sp, color = AppColors.muted, display = false)
        }
        return
    }
    val density = context.resources.displayMetrics.density
    val defaultHeight = (info.minHeight / density).toInt().coerceIn(80, 400)
    val heightDp = state.widgetHeight("ext:$appWidgetId", defaultHeight)
    val widthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp - 80
    var resizing by remember { mutableStateOf(false) }
    val label = remember(appWidgetId) { info.loadLabel(context.packageManager) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption(label)
            Caption(if (resizing) "Done" else "Resize", Modifier.clickableRole({ resizing = !resizing }).padding(vertical = 4.dp), color = AppColors.text)
        }
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
            HeightSlider(heightDp) { state.updateWidgetHeight("ext:$appWidgetId", it) }
            Caption("Remove widget", Modifier.clickableRole({ host.delete(appWidgetId); state.removeWidgetId("ext:$appWidgetId") }).padding(vertical = 8.dp), color = AppColors.gate)
        }
    }
}

/** Flat height control: a hairline track with a round thumb. Drag or tap anywhere on it. Value is shown in mono. */
@Composable
private fun HeightSlider(heightDp: Int, onCommit: (Int) -> Unit) {
    val minH = 80f; val maxH = 420f
    var live by remember(heightDp) { mutableStateOf(heightDp.toFloat()) }
    var widthPx by remember { mutableStateOf(1f) }
    val thumb = 26.dp
    fun toValue(x: Float) = (minH + (x / widthPx).coerceIn(0f, 1f) * (maxH - minH))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption("Height")
            Caption("${live.toInt()}dp", color = AppColors.text)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(Unit) {
                    detectTapGestures { live = toValue(it.x); onCommit(live.toInt()) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { onCommit(live.toInt()) },
                        onHorizontalDrag = { c, _ -> live = toValue(c.position.x) },
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.line))
            val frac = ((live - minH) / (maxH - minH)).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth(frac).height(1.5.dp).background(AppColors.text))
            Box(
                Modifier
                    .graphicsLayer { translationX = (widthPx - thumb.toPx()) * frac }
                    .size(thumb)
                    .background(AppColors.text, CircleShape),
            )
        }
    }
}

// ------------------------------------------------------------------ habit stack

/** "After I <anchor>, I <action>": a chain of small habits. Check each one off once a day; the streak counts days in a row. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HabitStack(state: LauncherState) {
    val haptic = LocalHapticFeedback.current
    val today = dayKey()
    var adding by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }
    val done = state.habits.count { today in it.doneDays }
    Column(Modifier.fillMaxWidth().animateContentSize(Motion.lift())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption("Habit stack")
            if (state.habits.isNotEmpty()) Caption("$done of ${state.habits.size}")
        }
        if (state.habits.isEmpty() && !adding) {
            AppText("Stack small habits on things you already do. After I wake up, I drink water.", Modifier.padding(top = 10.dp), size = 15.sp, color = AppColors.muted, display = false)
        }
        Column(Modifier.padding(top = 8.dp)) {
            state.habits.forEachIndexed { i, h ->
                key(h.id) {
                    val on = today in h.doneDays
                    var streak = 0
                    val now = System.currentTimeMillis()
                    val startBack = if (on) 0 else 1
                    while (streak < 400 && dayKey(now - (streak + startBack) * DAY_MS) in h.doneDays) streak++
                    val last = i == state.habits.lastIndex
                    val line = AppColors.line
                    val alpha by animateFloatAsState(if (on) 0.4f else 1f, Motion.state(400), label = "habitAlpha")
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .enterStagger(i, rise = 14.dp)
                            .drawBehind {
                                if (!last) drawLine(line, Offset(15.dp.toPx(), 44.dp.toPx()), Offset(15.dp.toPx(), size.height + 14.dp.toPx()), 1.5.dp.toPx())
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(Modifier.semantics { contentDescription = "Toggle habit" }.clickableRole({ haptic.performHapticFeedback(HapticFeedbackType.LongPress); state.toggleHabit(h.id) }, Role.Checkbox)) { CheckCircle(on, size = 30.dp) }
                        Column(
                            Modifier.weight(1f).graphicsLayer { this.alpha = alpha }.combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { state.toggleHabit(h.id) }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); state.removeHabit(h.id) }),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Caption("After " + h.anchor)
                            AppText(h.action, size = 17.sp, letterSpacing = (-0.17).sp, display = false)
                        }
                        if (streak > 0) AppText("${streak}d", Modifier.padding(top = 4.dp), size = 11.sp, mono = true, color = AppColors.muted)
                    }
                }
            }
        }
        if (adding) {
            AppField(anchor, { anchor = it }, "After I… (wake up)")
            AppField(action, { action = it }, "I will… (drink water)", onDone = {
                state.addHabit(anchor, action); anchor = ""; action = ""; adding = false
            })
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TextLink("Add habit", { state.addHabit(anchor, action); anchor = ""; action = ""; adding = false })
                TextLink("Cancel", { adding = false })
            }
        } else {
            Row(Modifier.fillMaxWidth().clickableRole({ adding = true }).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppText("+", size = 16.sp, mono = true, color = AppColors.muted)
                AppText("Add a habit", size = 17.sp, color = AppColors.muted, display = false)
            }
        }
    }
}
