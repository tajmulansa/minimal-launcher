package com.example.productivitylauncher.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import kotlinx.coroutines.launch
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.GatedTag
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Intro
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.enterStagger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/** Home: a rolling clock, your next task, a few plain app names and the dock. Pull the top down for Quick actions. */
@Composable
fun HomeScreen(
    state: LauncherState,
    nav: Nav,
    onOpenApp: (AppEntry) -> Unit,
    page: Int,
    onOpenWidgets: () -> Unit,
    onOpenQuick: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val dateLine = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now)
    val cal = Calendar.getInstance().apply { time = now }
    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
    val use24 = DateFormat.is24HourFormat(context)
    val shownHour = com.example.productivitylauncher.data.clockHour(hour24, use24)
    val minute = cal.get(Calendar.MINUTE)
    val minuteOfDay = hour24 * 60 + cal.get(Calendar.MINUTE)
    val greeting = com.example.productivitylauncher.data.homeGreeting(minuteOfDay, state.wakeMin, state.bedMin)

    Column(Modifier.fillMaxSize().verticalSwipes(onUp = onOpenDrawer, onDown = onOpenQuick)) {
        // Top row. Tap the date to open Quick actions. Swipe up anywhere to open All apps, swipe down from the top for Quick actions.
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, top = 12.dp).enterStagger(0, once = true),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppText(dateLine.uppercase(), Modifier.padding(vertical = 10.dp), size = 13.sp, weight = FontWeight.Medium, color = AppColors.muted, letterSpacing = 1.2.sp, mono = true)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .border(1.dp, AppColors.line, CircleShape)
                            .semantics { contentDescription = "Quick actions" }
                            .clickableRole(onOpenQuick),
                        contentAlignment = Alignment.Center,
                    ) { AppIcon(Ic.Sliders, AppColors.text, size = 18.dp) }
                }
            }

            // The clock.
            Column(Modifier.padding(start = 28.dp, end = 28.dp, top = 22.dp).enterStagger(1, once = true)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hs = shownHour.toString().padStart(2, '0')
                    Odometer(hs[0].digitToInt(), 0, 112.sp)
                    Odometer(hs[1].digitToInt(), 90, 112.sp)
                    Colon(112.sp)
                    Odometer(minute / 10, 180, 112.sp)
                    Odometer(minute % 10, 270, 112.sp)
                }
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                    AppText(if (state.userName.isBlank()) greeting else greeting.removeSuffix(".") + ", ", size = 22.sp, weight = FontWeight.Normal, color = AppColors.muted)
                    if (state.userName.isNotBlank()) {
                        val underline by animateFloatAsState(1f, if (Motion.enabled) tween<Float>(1100, delayMillis = if (Intro.played) 0 else 900, easing = Motion.Soft) else snap(), label = "nameLine")
                        val ink = AppColors.text
                        AppText(
                            state.userName, Modifier.drawBehind { drawLine(ink, androidx.compose.ui.geometry.Offset(0f, size.height * 0.93f), androidx.compose.ui.geometry.Offset(size.width * underline, size.height * 0.93f), 1.5.dp.toPx()) },
                            size = 22.sp, weight = FontWeight.Medium, color = AppColors.text, maxLines = 1,
                        )
                        AppText(".", size = 22.sp, color = AppColors.muted)
                    }
                }
            }
        }

        Hair(0, Modifier.padding(horizontal = 28.dp, vertical = 20.dp))

        Column(
            Modifier.weight(1f).fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 150.dp),
        ) {
            // Next task: tap to complete it, like the prototype.
            val next = state.nextTask
            var checking by remember { mutableStateOf(false) }
            val nextText = com.example.productivitylauncher.data.upNextText(next?.text, state.allTasks.isNotEmpty())
            Row(
                Modifier
                    .fillMaxWidth()
                    .enterStagger(2, once = true)
                    .clickableRole({
                        if (next == null) { if (state.allTasks.isEmpty()) onOpenWidgets() }
                        else if (!checking) {
                            checking = true
                            scope.launch { delay(520); state.toggleAny(next.id); checking = false }
                        }
                    }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CheckCircle(checking, size = 30.dp)
                Column(Modifier.weight(1f)) {
                    Caption("Up next")
                    androidx.compose.animation.Crossfade(
                        targetState = nextText,
                        animationSpec = Motion.enter(300),
                        label = "nextTask",
                    ) { txt ->
                        AppText(txt, Modifier.padding(top = 2.dp), size = 19.sp, weight = FontWeight.Medium, letterSpacing = (-0.4).sp, maxLines = 1, color = if (next == null) AppColors.muted else AppColors.text)
                    }
                }
            }

            Hair(1, Modifier.padding(vertical = 20.dp))

            val shown = state.homeApps.filter { !state.isGated(it) }.mapNotNull { state.appByPackage(it) }
            if (state.lockIn) Caption("Locked in \u00B7 only your home apps", Modifier.padding(bottom = 6.dp))
            shown.forEachIndexed { i, app ->
                Box(Modifier.enterStagger(3 + i, once = true)) {
                    AppRow(app, gated = state.isGated(app.packageName), meta = state.appMsToday[app.packageName]?.takeIf { it >= 60_000L }?.let { formatMillis(it) } ?: "", onClick = { onOpenApp(app) })
                }
            }
            LaunchedEffect(Unit) { delay(900); Intro.played = true }
            if (shown.size < 6 && !state.lockIn) {
                Row(
                    Modifier.fillMaxWidth().clickableRole({ nav.go(Route.HomePicker) }).padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppIcon(Ic.Plus, AppColors.muted, size = 18.dp)
                    AppText(if (shown.isEmpty()) "Choose your six apps" else "Add an app", size = 18.sp, weight = FontWeight.Medium, color = AppColors.muted)
                }
            }
        }

    }
}

/** One digit that rolls up a strip of 0..9 to its value. With animations off it simply changes. */
@Composable
private fun Odometer(digit: Int, delayMs: Int, size: TextUnit) {
    val density = LocalDensity.current
    val cell = with(density) { size.toDp() }
    var target by remember { mutableStateOf(if (Intro.played) digit else 0) }
    LaunchedEffect(digit) { target = digit }
    val y by animateFloatAsState(
        target.toFloat(),
        if (Motion.enabled) tween<Float>(900, delayMillis = if (Intro.played) 0 else delayMs, easing = Motion.Soft) else snap(),
        label = "digit",
    )
    Box(
        Modifier
            .clipToBounds()
            .layout { m, c ->
                val cellPx = cell.roundToPx()
                val p = m.measure(c.copy(minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity))
                layout(p.width, cellPx) { p.place(0, 0) }
            },
    ) {
        Column(
            Modifier.graphicsLayer { translationY = -y * cell.toPx() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (d in 0..9) {
                Box(Modifier.height(cell), contentAlignment = Alignment.Center) {
                    AppText(d.toString(), size = size, weight = FontWeight.Light, letterSpacing = (-0.06).em, lineHeight = size, tnum = true)
                }
            }
        }
    }
}

@Composable
private fun Colon(size: TextUnit) {
    val alpha = if (Motion.enabled) {
        val t = rememberInfiniteTransition(label = "colon")
        val a by t.animateFloat(
            1f, 0.25f,
            infiniteRepeatable(keyframes { durationMillis = 2000; 1f at 0; 1f at 900; 0.25f at 1000; 0.25f at 1900 }, RepeatMode.Restart),
            label = "colonAlpha",
        )
        a
    } else 1f
    Box(Modifier.width(with(LocalDensity.current) { size.toDp() } * 0.3f).graphicsLayer { this.alpha = alpha }, contentAlignment = Alignment.Center) {
        AppText(":", size = size, weight = FontWeight.Light, color = AppColors.muted)
    }
}

/** A hairline that draws itself in from the left. */
@Composable
fun Hair(index: Int, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(Intro.played || !Motion.enabled) }
    LaunchedEffect(Unit) { shown = true }
    val p by animateFloatAsState(
        if (shown) 1f else 0f,
        if (Motion.enabled) tween<Float>(1000, delayMillis = if (Intro.played) 0 else 200 + index * 120, easing = Motion.Soft) else snap(),
        label = "hair",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .graphicsLayer { scaleX = p; transformOrigin = TransformOrigin(0f, 0.5f) }
            .background(AppColors.line),
    )
}

@Composable
private fun AppRow(app: AppEntry, gated: Boolean, meta: String, onClick: () -> Unit) {
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val shift by animateFloatAsState(if (pressed) 8f else 0f, Motion.press(), label = "shift")
    Row(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shift.dp.toPx(); scaleX = if (pressed) 0.985f else 1f; scaleY = if (pressed) 0.985f else 1f }
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppText(
            app.label, Modifier.weight(1f), size = 28.sp, weight = FontWeight.Medium, letterSpacing = (-1.1).sp, lineHeight = 32.sp,
            color = if (gated) AppColors.gate else AppColors.text, maxLines = 1,
        )
        if (gated) GatedTag()
        if (meta.isNotEmpty()) AppText(meta, size = 11.sp, color = AppColors.muted, mono = true)
    }
}

/** One vertical swipe detector that never steals touches from children or the pager: up opens All apps, down from the top opens Quick actions. */
fun Modifier.verticalSwipes(onUp: () -> Unit, onDown: () -> Unit): Modifier = pointerInput(Unit) {
    val limit = 70.dp.toPx()
    val side = 50.dp.toPx()
    val topZone = 320.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var last = down.position
        while (true) {
            val e = awaitPointerEvent(PointerEventPass.Initial)
            val c = e.changes.firstOrNull { it.id == down.id } ?: break
            last = c.position
            if (!c.pressed) break
        }
        val dy = last.y - down.position.y
        val dx = last.x - down.position.x
        if (kotlin.math.abs(dx) < side) {
            if (dy < -limit) onUp() else if (dy > limit && down.position.y < topZone) onDown()
        }
    }
}

/**
 * The bar under both pages, as in the prototype: All apps pill and three round buttons, then the Home / Today tabs
 * with a line that follows the pager while you swipe.
 */
@Composable
fun BottomBar(pager: PagerState, onApps: () -> Unit, onPhone: () -> Unit, onMessages: () -> Unit, onFocus: () -> Unit, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.38f to AppColors.phone, 1f to AppColors.phone))
            .padding(start = 22.dp, end = 22.dp, bottom = 14.dp, top = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(
                Modifier
                    .height(48.dp)
                    .background(AppColors.text, RadiusPill)
                    .clickableRole(onApps)
                    .padding(start = 16.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppIcon(Ic.Chevron, AppColors.phone, size = 18.dp, modifier = Modifier.graphicsLayer { rotationZ = -90f })
                AppText("All apps", size = 15.sp, weight = FontWeight.Medium, color = AppColors.phone, display = false)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DockButton(Ic.Phone, "Phone", onPhone)
                DockButton(Ic.Message, "Messages", onMessages)
                DockButton(Ic.Timer, "Focus", onFocus)
            }
        }
        Tabs(pager, onSelect)
    }
}

@Composable
private fun Tabs(pager: PagerState, onSelect: (Int) -> Unit) {
    val labels = listOf("Home", "Study")
    val widths = remember { androidx.compose.runtime.mutableStateListOf(0f, 0f) }
    val offsets = remember { androidx.compose.runtime.mutableStateListOf(0f, 0f) }
    val p = (pager.currentPage + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
    val idx = Math.round(p)
    Box(Modifier.fillMaxWidth().padding(bottom = 4.dp), contentAlignment = Alignment.TopCenter) {
        Box {
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                labels.forEachIndexed { i, l ->
                    val tint by animateColorAsState(if (i == idx) AppColors.text else AppColors.muted, Motion.state(160), label = "tabTint")
                    Caption(
                        l,
                        Modifier
                            .onGloballyPositioned { c -> widths[i] = c.size.width.toFloat(); offsets[i] = c.positionInParent().x }
                            .clickableRole({ onSelect(i) })
                            .padding(vertical = 8.dp),
                        color = tint,
                    )
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .graphicsLayer { translationX = offsets[0] + (offsets[1] - offsets[0]) * p }
                    .width(with(LocalDensity.current) { (widths[0] + (widths[1] - widths[0]) * p).toDp() })
                    .height(1.5.dp)
                    .background(AppColors.text),
            )
        }
    }
}

@Composable
private fun DockButton(ic: Ic, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .border(1.dp, AppColors.line, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, AppColors.text, size = 20.dp)
    }
}
